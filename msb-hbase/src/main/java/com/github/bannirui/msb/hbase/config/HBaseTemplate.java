package com.github.bannirui.msb.hbase.config;

import com.github.bannirui.msb.ex.FrameworkException;
import com.github.bannirui.msb.hbase.metadata.HBaseEntityMetadata;
import com.github.bannirui.msb.hbase.util.Bytes;
import com.stumbleupon.async.Deferred;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hbase.async.BinaryComparator;
import org.hbase.async.CompareFilter;
import org.hbase.async.Config;
import org.hbase.async.DeleteRequest;
import org.hbase.async.FilterList;
import org.hbase.async.GetRequest;
import org.hbase.async.GetResultOrException;
import org.hbase.async.HBaseClient;
import org.hbase.async.KeyValue;
import org.hbase.async.PutRequest;
import org.hbase.async.QualifierFilter;
import org.hbase.async.ScanFilter;
import org.hbase.async.Scanner;
import org.hbase.async.TimestampsFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

public class HBaseTemplate implements DisposableBean {
    private Logger logger = LoggerFactory.getLogger(HBaseTemplate.class);
    private HbaseAnnotationParse hbaseAnnotationParse;
    private HBaseClient client;

    public HBaseTemplate(Config config, HbaseAnnotationParse hbaseAnnotationParse) {
        this.client = new HBaseClient(config);
        this.hbaseAnnotationParse = hbaseAnnotationParse;
    }

    public HBaseTemplate(String quorumSpec) {
        this.client = new HBaseClient(quorumSpec);
    }

    public HBaseClient getClient() {
        return this.client;
    }

    public <T> HBaseTemplate.HbaseAsyncResponse put(T t) {
        List<Deferred<Object>> deferreds = this.putObjAsync(this.hbaseAnnotationParse.parsePutRequestEntity(t));
        return new HbaseAsyncResponse(deferreds);
    }

    public <T> void putSync(T t) {
        List<Deferred<Object>> putDeferreds = this.putObjAsync(this.hbaseAnnotationParse.parsePutRequestEntity(t));
        (new HbaseAsyncResponse(putDeferreds)).join();
    }

    public <T> HBaseTemplate.HbaseAsyncResponse put(List<T> t) {
        List<Deferred<Object>> deferreds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(t)) {
            for (T entity : t) {
                List<Deferred<Object>> results = this.putObjAsync(this.hbaseAnnotationParse.parsePutRequestEntity(entity));
                if (results != null) {
                    deferreds.addAll(results);
                }
            }
        }
        return new HbaseAsyncResponse(deferreds);
    }

    private List<Deferred<Object>> putObjAsync(HbasePutEntity hbasePutEntity) {
        Map<String, List<HbasePutEntity.PutInfo>> groupedPutInfos = hbasePutEntity.getGroupedPutInfos();
        if (groupedPutInfos != null && !groupedPutInfos.isEmpty()) {
            if (hbasePutEntity.getVersion() == 0L) {
                hbasePutEntity.setVersion(System.currentTimeMillis());
            }
            List<Deferred<Object>> results = new ArrayList<>();
            for (Map.Entry<String, List<HbasePutEntity.PutInfo>> familyGroupInfo : groupedPutInfos.entrySet()) {
                List<HbasePutEntity.PutInfo> putInfos = familyGroupInfo.getValue();
                int size = putInfos.size();
                byte[][] qualifiers = new byte[size][];
                byte[][] values = new byte[size][];
                int i = 0;
                for (HbasePutEntity.PutInfo putInfo : putInfos) {
                    qualifiers[i] = putInfo.getQualifier();
                    values[i] = putInfo.getValue();
                    i++;
                }
                String family = familyGroupInfo.getKey();
                PutRequest putRequest =
                    new PutRequest(hbasePutEntity.getTableName(), hbasePutEntity.getKey(), Bytes.toBytes(family), qualifiers, values,
                        hbasePutEntity.getVersion());
                Deferred<Object> deferred = this.client.put(putRequest);
                results.add(deferred);
            }
            this.client.flush();
            return results;
        } else {
            return null;
        }
    }

    public <T> void delete(String rowKey, Class<T> clazz) {
        DeleteRequest deleteRequest = this.hbaseAnnotationParse.parseDeleteRequestEntity(clazz, Bytes.toBytes(rowKey));
        Deferred<Object> delete = this.client.delete(deleteRequest);
        this.client.flush();
        try {
            delete.join();
        } catch (Exception var6) {
            throw FrameworkException.getInstance(var6, "Hbase删除异常rowKey={0}", rowKey);
        }
    }

    public <T> void delete(List<String> rowKeys, Class<T> clazz) {
        List<Deferred<Object>> deferreds = rowKeys.stream().map((rowKey) -> {
            DeleteRequest deleteRequest = this.hbaseAnnotationParse.parseDeleteRequestEntity(clazz, Bytes.toBytes(rowKey));
            Deferred<Object> deleteDeferred = this.client.delete(deleteRequest);
            this.client.flush();
            return deleteDeferred;
        }).collect(Collectors.toList());

        for (int i = 0, sz = deferreds.size(); i < sz; ++i) {
            try {
                ((Deferred<?>) deferreds.get(i)).join();
            } catch (Exception e) {
                throw FrameworkException.getInstance(e, "Hbase删除异常rowKey={0}", rowKeys.get(i));
            }
        }
    }

    public <T> Map<String, T> get(List<String> rowkeys, Class<T> clazz) {
        if (CollectionUtils.isEmpty(rowkeys)) {
            throw FrameworkException.getInstance("查询Hbase异常rowkeys不能为空", new Object[0]);
        }
        Map<String, T> tMap = new HashMap<>();
        List<Deferred<ArrayList<KeyValue>>> deferreds = new ArrayList<>();
        rowkeys.forEach((rowkey) -> {
            GetRequest getRequest = this.hbaseAnnotationParse.parseGetRequestEntity(clazz, Bytes.toBytes(rowkey));
            Deferred<ArrayList<KeyValue>> arrayListDeferred = this.client.get(getRequest);
            arrayListDeferred.addCallback((keyValues) -> {
                if (keyValues != null) {
                    T t = this.hbaseAnnotationParse.returnGetResponse(clazz, keyValues);
                    tMap.put(rowkey, t);
                }
                return null;
            }).addErrback((o) -> {
                this.logger.error(String.format("Error occurred in get data from hbase with clazz:[%s] rowKey:{%s]", clazz.getName(), rowkey));
                return null;
            });
            deferreds.add(arrayListDeferred);
            this.client.flush();
        });
        this.waitDefererGroup(deferreds);
        return tMap.isEmpty() ? null : tMap;
    }

    public <T> T get(String rowKey, Class<T> clazz) {
        Deferred<ArrayList<KeyValue>> arrayListDeferred =
            this.client.get(this.hbaseAnnotationParse.parseGetRequestEntity(clazz, Bytes.toBytes(rowKey)));
        try {
            this.client.flush();
            ArrayList<KeyValue> join = arrayListDeferred.join();
            return join != null ? this.hbaseAnnotationParse.returnGetResponse(clazz, join) : null;
        } catch (Exception e) {
            throw FrameworkException.getInstance(e, "查询Hbase异常rowKey={0}", rowKey);
        }
    }

    public <T> T get(String rowKey, Class<T> clazz, String family) {
        return this.get((String) rowKey, clazz, family, null);
    }

    public <T> T get(String rowKey, Class<T> clazz, List<String> qualifiers) {
        return this.get((String) rowKey, clazz, (String) null, qualifiers);
    }

    public <T> T get(String rowKey, Class<T> clazz, String family, List<String> qualifiers) {
        GetRequest getRequest = this.buildGetRequest(rowKey, clazz, family, qualifiers);
        Deferred<ArrayList<KeyValue>> arrayListDeferred = this.client.get(getRequest);
        try {
            this.client.flush();
            ArrayList<KeyValue> resp = arrayListDeferred.join();
            return resp != null ? this.hbaseAnnotationParse.returnGetResponse(clazz, resp) : null;
        } catch (Exception e) {
            throw FrameworkException.getInstance(e, "查询Hbase异常rowKey={0}", rowKey);
        }
    }

    public <T> Map<String, T> get(List<String> rowKeys, Class<T> clazz, String family, List<String> qualifiers) {
        List<GetRequest> reqs =
            rowKeys.stream().map((rowKey) -> this.buildGetRequest(rowKey, clazz, family, qualifiers)).collect(Collectors.toList());
        Deferred<List<GetResultOrException>> listDeferred = this.client.get(reqs);
        try {
            this.client.flush();
            List<GetResultOrException> values = listDeferred.join();
            HashMap<String, T> result = new HashMap();
            for (GetResultOrException resultOrException : values) {
                ArrayList<KeyValue> cells = resultOrException.getCells();
                if (CollectionUtils.isNotEmpty(cells)) {
                    T t = this.hbaseAnnotationParse.returnGetResponse(clazz, cells);
                    result.put(new String(cells.get(0).key()), t);
                } else if (resultOrException.getException() != null) {
                    this.logger.error("批量查询出现异常 ", resultOrException.getException());
                }
            }
            return result;
        } catch (Exception e) {
            throw FrameworkException.getInstance(e, "查询Hbase异常rowKey={0}", rowKeys.toString());
        }
    }

    private <T> GetRequest buildGetRequest(String rowKey, Class<T> clazz, String family, List<String> qualifiers) {
        if (StringUtils.isNotBlank(family)) {
            GetRequest getRequest = this.hbaseAnnotationParse.parseGetRequestEntity(clazz, Bytes.toBytes(rowKey));
            getRequest.family(Bytes.toBytes(family));
            byte[][] columns = Bytes.toBytesArray(qualifiers);
            if (columns != null) {
                getRequest.qualifiers(columns);
            }
            return getRequest;
        } else if (CollectionUtils.isNotEmpty(qualifiers)) {
            List<ScanFilter> qualifierFilters =
                qualifiers.stream().map((qualifier) -> new QualifierFilter(CompareFilter.CompareOp.EQUAL, new BinaryComparator(qualifier.getBytes())))
                    .collect(Collectors.toList());
            FilterList qualifierListFilter = new FilterList(qualifierFilters, FilterList.Operator.MUST_PASS_ONE);
            GetRequest getRequest = this.hbaseAnnotationParse.parseGetRequestEntity(clazz, Bytes.toBytes(rowKey));
            getRequest.setFilter(qualifierListFilter);
            return getRequest;
        } else {
            throw FrameworkException.getInstance("qualifiers和family不能全为空");
        }
    }

    private <T> List<T> getRowByVersionResult(List<KeyValue> results, HbaseAnnotationParse hbaseAnnotationParse, Class<T> clazz) {
        if (CollectionUtils.isEmpty(results)) {
            return new ArrayList<>();
        }
        Map<Long, List<KeyValue>> collect =
            results.stream().collect(Collectors.groupingBy(KeyValue::timestamp, LinkedHashMap::new, Collectors.toList()));
        List<T> objs = new ArrayList<>(collect.size());
        try {
            for (List<KeyValue> keyValues : collect.values()) {
                T t = hbaseAnnotationParse.returnGetResponse(clazz, keyValues);
                objs.add(t);
            }
            return objs;
        } catch (Exception e) {
            throw FrameworkException.getInstance(e, "获取rowKey查询结果集异常");
        }
    }

    public <T> List<T> getRowByVersion(String rowkey, Class<T> clazz, Long... version) {
        GetRequest getRequest = this.hbaseAnnotationParse.parseGetRequestEntity(clazz, Bytes.toBytes(rowkey));
        if (version != null && version.length > 0) {
            getRequest.setFilter(new TimestampsFilter(version));
            getRequest.maxVersions(version.length);
        } else {
            getRequest.maxVersions(2147483647);
        }
        Deferred<ArrayList<KeyValue>> resultDeferred = this.client.get(getRequest);
        this.client.flush();
        List<KeyValue> results = null;
        try {
            results = resultDeferred.join();
        } catch (Exception e) {
            throw FrameworkException.getInstance(e, "查询Hbase异常 keys={0}", rowkey);
        }
        return results != null ? this.getRowByVersionResult(results, this.hbaseAnnotationParse, clazz) : null;
    }

    private void waitDefererGroup(List<Deferred<ArrayList<KeyValue>>> deferreds) {
        if (!deferreds.isEmpty()) {
            Deferred<ArrayList<ArrayList<KeyValue>>> d = Deferred.group(deferreds);
            try {
                d.join();
            } catch (Exception e) {
                this.logger.error(e.getMessage(), e);
            }
        }
    }

    public <T> Map<String, List<T>> getAllVersion(List<String> rowkeys, Class<T> clazz) {
        if (CollectionUtils.isEmpty(rowkeys)) {
            throw FrameworkException.getInstance("查询Hbase异常rowkeys不能为空", new Object[0]);
        }
        Map<String, List<T>> map = new HashMap<>();
        List<Deferred<ArrayList<KeyValue>>> deffers = new ArrayList<>();
        for (String rowkey : rowkeys) {
            GetRequest getRequest = this.hbaseAnnotationParse.parseGetRequestEntity(clazz, Bytes.toBytes(rowkey));
            getRequest.maxVersions(2147483647);
            Deferred<ArrayList<KeyValue>> arrayListDeferred = this.client.get(getRequest);
            arrayListDeferred.addCallback((keyValues) -> {
                if (keyValues != null) {
                    List<T> rowByVersionResult = this.getRowByVersionResult(keyValues, this.hbaseAnnotationParse, clazz);
                    map.put(rowkey, rowByVersionResult);
                }
                return null;
            });
            arrayListDeferred.addErrback((o) -> {
                this.logger.error("Error occurred when get data from hbase with clazz:{} rowKey:{} ErrBack:{}", clazz.getName(), rowkey, o);
                return null;
            });
            deffers.add(arrayListDeferred);
            this.client.flush();
        }
        this.waitDefererGroup(deffers);
        return map;
    }

    public <T> Map<String, T> scan(String startKey, String endKey, Class<T> clazz) {
        Map<String, T> result = new HashMap<>();
        byte[] tableName = this.hbaseAnnotationParse.getTableName(clazz);
        Scanner scanner = this.client.newScanner(tableName);
        scanner.setStartKey(startKey);
        scanner.setStopKey(endKey);
        try {
            ArrayList<ArrayList<KeyValue>> rows;
            while ((rows = scanner.nextRows(1).joinUninterruptibly()) != null) {
                for (ArrayList<KeyValue> kvs : rows) {
                    byte[] key = kvs.get(0).key();
                    T t = this.hbaseAnnotationParse.returnGetResponse(clazz, kvs);
                    result.put(new String(key, StandardCharsets.UTF_8), t);
                }
            }
        } catch (Exception e) {
            throw FrameworkException.getInstance(e, "hbase读取异常");
        } finally {
            this.client.flush();
        }
        return result;
    }

    public <T> Map<String, List<T>> scan(String startKey, String endKey, Class<T> clazz, ScanFilter filter) {
        Map<String, List<T>> result = new HashMap<>();
        byte[] tableName = this.hbaseAnnotationParse.getTableName(clazz);
        Scanner scanner = this.client.newScanner(tableName);
        scanner.setStartKey(startKey);
        scanner.setStopKey(endKey);
        scanner.setFilter(filter);
        scanner.setMaxVersions(2147483647);
        try {
            ArrayList<ArrayList<KeyValue>> rows = null;
            while ((rows = scanner.nextRows(1).joinUninterruptibly()) != null) {
                for (ArrayList<KeyValue> kvs : rows) {
                    String key = new String(kvs.get(0).key(), StandardCharsets.UTF_8);
                    List<T> list = this.getRowByVersionResult(kvs, this.hbaseAnnotationParse, clazz);
                    result.put(key, list);
                }
            }
        } catch (Exception e) {
            throw FrameworkException.getInstance(e, "hbase读取异常", new Object[0]);
        } finally {
            this.client.flush();
        }
        return result;
    }

    public void destroy() throws Exception {
        if (Objects.isNull(this.client)) {
            return;
        }
        try {
            this.client.shutdown().join();
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
        }
    }

    public <T> Scanner newScanner(Class<T> clazz) {
        HBaseEntityMetadata entityMetadata = this.hbaseAnnotationParse.getEntityMetadata(clazz);
        return this.client.newScanner(entityMetadata.getTabName());
    }

    public <T> GetRequest buildGetRequest(String rowKey, Class<T> clazz) {
        return this.hbaseAnnotationParse.parseGetRequestEntity(clazz, Bytes.toBytes(rowKey));
    }

    public <T> DeleteRequest buildDeleteRequest(String rowKey, Class<T> clazz) {
        return this.hbaseAnnotationParse.parseDeleteRequestEntity(clazz, Bytes.toBytes(rowKey));
    }

    public <T> PutRequest buildPutRequest(String rowKey, Class<T> clazz, String family, List<String> qualifiers, List<String> values) {
        HBaseEntityMetadata entityMetadata = this.hbaseAnnotationParse.getEntityMetadata(clazz);
        return new PutRequest(entityMetadata.getTabName(), Bytes.toBytes(rowKey), Bytes.toBytes(family), Bytes.toBytesArray(qualifiers),
            Bytes.toBytesArray(values));
    }

    public <T> PutRequest buildPutRequest(String rowKey, Class<T> clazz, String family, List<String> qualifiers, List<String> values,
                                          long timestamp) {
        HBaseEntityMetadata entityMetadata = this.hbaseAnnotationParse.getEntityMetadata(clazz);
        return new PutRequest(entityMetadata.getTabName(), Bytes.toBytes(rowKey), Bytes.toBytes(family), Bytes.toBytesArray(qualifiers),
            Bytes.toBytesArray(values), timestamp);
    }

    public static class HbaseAsyncResponse {
        private List<Deferred<Object>> deferreds;

        public HbaseAsyncResponse(List<Deferred<Object>> deferreds) {
            this.deferreds = deferreds;
        }

        public void join() {
            if (CollectionUtils.isEmpty(this.deferreds)) {
                return;
            }
            for (Deferred<Object> putDeferred : this.deferreds) {
                try {
                    putDeferred.join();
                } catch (Exception e) {
                    throw FrameworkException.getInstance(e, "Hbase Put异常");
                }
            }
        }
    }
}

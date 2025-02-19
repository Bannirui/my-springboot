package com.github.bannirui.msb.hbase.config;

import com.github.bannirui.msb.ex.FrameworkException;
import com.github.bannirui.msb.hbase.annotation.HColumn;
import com.github.bannirui.msb.hbase.annotation.HRowKey;
import com.github.bannirui.msb.hbase.annotation.HTable;
import com.github.bannirui.msb.hbase.annotation.HVersion;
import com.github.bannirui.msb.hbase.codec.HbaseCellDataCodec;
import com.github.bannirui.msb.hbase.codec.HbaseCellDataObjCodec;
import com.github.bannirui.msb.hbase.metadata.HBaseEntityMetadata;
import com.github.bannirui.msb.hbase.metadata.HColumnInfo;
import com.github.bannirui.msb.hbase.util.Bytes;
import com.github.bannirui.msb.hbase.util.TypeUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hbase.async.DeleteRequest;
import org.hbase.async.GetRequest;
import org.hbase.async.KeyValue;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

public class HbaseAnnotationParse {
    private static final Map<Class<? extends HbaseCellDataCodec>, HbaseCellDataCodec> CODEC_INSTANCES_CACHE = new ConcurrentReferenceHashMap<>();
    /**
     * 缓存了java实体对应的hbase表信息
     * <ul>
     *     <li>key java实体类</li>
     *     <li>value hbase表信息</li>
     * </ul>
     */
    private static final Map<Class<?>, HBaseEntityMetadata> CLAZZMETADATA_CACHE = new ConcurrentReferenceHashMap<>(64);

    /**
     * java实体映射的hbase表信息 没有缓存的情况下懒加载进行缓存
     * @param clazz java实体
     * @return java实体映射的hbase信息
     */
    public HBaseEntityMetadata getEntityMetadata(Class<?> clazz) {
        HBaseEntityMetadata cachedMetadata = CLAZZMETADATA_CACHE.get(clazz);
        return cachedMetadata != null ? cachedMetadata : CLAZZMETADATA_CACHE.computeIfAbsent(clazz, (k) -> this.parseEntityMetadata(k, HBaseEntityMetadata.class));
    }

    /**
     * 从java实体中解析出hbase信息
     * @param tableClazz Java实体 是hbase的表
     * @param metadataClass hbase信息
     */
    protected HBaseEntityMetadata parseEntityMetadata(Class<?> tableClazz, Class<? extends HBaseEntityMetadata> metadataClass) {
        // entity用注解标识是hbase实体
        HTable hTable = tableClazz.getAnnotation(HTable.class);
        if (hTable == null) {
            throw FrameworkException.getInstance("Hbase实体注解解析错误 请在类上注解@HTable");
        }
        HBaseEntityMetadata metadata = BeanUtils.instantiateClass(metadataClass);
        // 缓存hbase的列 key是列簇#列
        HashMap<String, HColumnInfo> hcolumnInfos = new HashMap<>();
        // hbase实体的成员 rowkey 列 版本号
        ReflectionUtils.doWithFields(tableClazz, (field) -> {
            if (field.getAnnotation(HColumn.class) != null) {
                HColumn hcolumn = field.getAnnotation(HColumn.class);
                String family = hcolumn.family();
                String qualifier = this.determineQualifier(field, hcolumn);
                String hColumnFieldId = this.getHColumnFieldId(family, qualifier);
                if (hcolumnInfos.get(hColumnFieldId) != null) {
                    throw FrameworkException.getInstance("{0}存在多个相同@HColumn(family = \"{1}\",name = \"{2}\")", tableClazz.getName(), family, qualifier);
                }
                ReflectionUtils.makeAccessible(field);
                HColumnInfo hColumnInfo = new HColumnInfo(Bytes.toBytes(family), Bytes.toBytes(qualifier), field);
                // 列缓存起来
                hcolumnInfos.put(hColumnFieldId, hColumnInfo);
            } else if (field.getAnnotation(HRowKey.class) != null) {
                if (!this.isHRowKeySupportType(field.getType())) {
                    throw FrameworkException.getInstance("Hbase实体注解解析错误 @HRowKey: " + field.getName() + "属性的类型必须为String");
                }
                if (metadata.getRowKeyField() != null) {
                    throw FrameworkException.getInstance("Hbase实体注解解析错误 @HRowKey注解只能有一个");
                }
                ReflectionUtils.makeAccessible(field);
                metadata.setRowKeyField(field);
            } else if (field.getAnnotation(HVersion.class) != null) {
                if (!this.isHVersionSupportType(field.getType())) {
                    throw FrameworkException.getInstance("Hbase实体注解解析错误 @HVersion: " + field.getName() + "属性类型必须为long或java.lang.Long");
                }
                if (metadata.getVersionField() != null) {
                    throw FrameworkException.getInstance("Hbase实体注解解析错误 @HVersion注解的属性只能有一个");
                }
                ReflectionUtils.makeAccessible(field);
                metadata.setVersionField(field);
            }
        });
        if (metadata.getRowKeyField() == null) {
            throw FrameworkException.getInstance("Hbase实体注解解析错误 必须有一个@HRowKey属性");
        } else {
            // hbase实体
            metadata.setTabClass(tableClazz);
            // hbase表名
            metadata.setTableName(hTable.name());
            // hbase列
            metadata.setHcolumnInfos(hcolumnInfos);
            return metadata;
        }
    }

    private boolean isHRowKeySupportType(Class<?> type) {
        return type.isAssignableFrom(String.class);
    }

    private boolean isHVersionSupportType(Class<?> type) {
        return type.isAssignableFrom(Long.TYPE) || type.isAssignableFrom(Long.class);
    }

    public <T> HbasePutEntity parsePutRequestEntity(T entity) {
        Class<?> aClass = entity.getClass();
        HBaseEntityMetadata metadata = this.getEntityMetadata(aClass);
        HbasePutEntity hbasePutEntity = new HbasePutEntity();
        hbasePutEntity.setTableName(this.getTableName(aClass));
        try {
            hbasePutEntity.setKey(this.getRowkeyValue(entity, metadata));
            hbasePutEntity.setVersion(this.getVersionValue(entity, metadata));
            Map<String, List<HbasePutEntity.PutInfo>> columnsValue = this.getColumnsValueGroupByFamily(entity);
            hbasePutEntity.setGroupedPutInfos(columnsValue);
            return hbasePutEntity;
        } catch (IllegalAccessException e) {
            throw FrameworkException.getInstance(e, "读取Hbase实体属性值时发生异常");
        }
    }

    public <T> GetRequest parseGetRequestEntity(Class<T> clazz, byte[] rowKey) {
        byte[] table = this.getTableName(clazz);
        return new GetRequest(table, rowKey);
    }

    public <T> DeleteRequest parseDeleteRequestEntity(Class<T> clazz, byte[] rowKey) {
        byte[] table = this.getTableName(clazz);
        return new DeleteRequest(table, rowKey);
    }

    public <T> T returnGetResponse(Class<T> clazz, List<KeyValue> keyValueList) throws IllegalAccessException, InstantiationException {
        if (CollectionUtils.isEmpty(keyValueList)) {
            return null;
        }
        HBaseEntityMetadata metadata = this.getEntityMetadata(clazz);
        T entity = clazz.newInstance();
        for (KeyValue keyValue : keyValueList) {
            Field field = this.findHColumnField(metadata, keyValue);
            if (field != null) {
                HColumn hcolumn = field.getAnnotation(HColumn.class);
                HbaseCellDataCodec codecInstance = this.getCodecInstance(hcolumn.codec());
                Object obj = codecInstance != null ? codecInstance.decode(keyValue.value()) : this.bytesToObject(field, keyValue.value());
                if (obj != null) {
                    field.set(entity, obj);
                }
            }
        }
        Field versionField = metadata.getVersionField();
        if (versionField != null) {
            versionField.set(entity, keyValueList.get(0).timestamp());
        }
        Field rowKeyField = metadata.getRowKeyField();
        if (rowKeyField.getType().isAssignableFrom(String.class)) {
            rowKeyField.set(entity, new String(keyValueList.get(0).key()));
        }
        return entity;
    }

    private Field findHColumnField(HBaseEntityMetadata metadata, KeyValue keyValue) {
        String family = new String(keyValue.family());
        String qualifier = new String(keyValue.qualifier());
        Field field = null;
        if (metadata.getHcolumnInfos() != null) {
            HColumnInfo hColumnInfo = metadata.getHcolumnInfos().get(this.getHColumnFieldId(family, qualifier));
            field = hColumnInfo != null ? hColumnInfo.getField() : null;
        }
        return field;
    }

    public <T> byte[] getTableName(Class<T> clazz) {
        HBaseEntityMetadata metadata = this.getEntityMetadata(clazz);
        return metadata.getTabName();
    }

    private HbaseCellDataCodec getCodecInstance(Class<? extends HbaseCellDataCodec> codecClass) {
        return codecClass != null && codecClass != HbaseCellDataCodec.class && codecClass != HbaseCellDataObjCodec.class ?
            CODEC_INSTANCES_CACHE.computeIfAbsent(codecClass, this::newInstance) : null;
    }

    private HbaseCellDataCodec newInstance(Class<? extends HbaseCellDataCodec> codecClass) {
        try {
            return codecClass.newInstance();
        } catch (Exception e) {
            throw FrameworkException.getInstance(e, "该HBase数据编解码器无法被实例化{0} message={1}", codecClass.getName(), e.getMessage());
        }
    }

    /**
     * @return key是列簇 value是列簇下列
     */
    private <T> Map<String, List<HbasePutEntity.PutInfo>> getColumnsValueGroupByFamily(T entity) throws IllegalAccessException {
        /**
         * 从被{@link HTable}注解的java实体中解析出hbase表信息
         */
        HBaseEntityMetadata metadata = this.getEntityMetadata(entity.getClass());
        /**
         * key是列簇
         * value是列簇下的列
         */
        HashMap<String, List<HbasePutEntity.PutInfo>> columnsValueGroupByFamily = new HashMap<>();
        Collection<HColumnInfo> hcolumnFields = metadata.getHcolumnInfos().values();
        for (HColumnInfo hcolumnInfo : hcolumnFields) {
            Field hcolumnField = hcolumnInfo.getField();
            Object value = hcolumnField.get(entity);
            if (value != null) {
                HColumn hcolumn = hcolumnField.getAnnotation(HColumn.class);
                List<HbasePutEntity.PutInfo> putInfos = columnsValueGroupByFamily.computeIfAbsent(hcolumn.family(), (k) -> new ArrayList<>());
                HbaseCellDataCodec codecInstance = this.getCodecInstance(hcolumn.codec());
                byte[] valueBytes = codecInstance != null ? codecInstance.encode(value) : this.objectToBytes(hcolumnField, value);
                HbasePutEntity.PutInfo info = new HbasePutEntity.PutInfo(hcolumnInfo.getFamily(), hcolumnInfo.getQualifier(), valueBytes);
                putInfos.add(info);
            }
        }
        return columnsValueGroupByFamily;
    }

    /**
     * hbase列名 注解{@link HColumn}打在java实体成员字段上 解析出行hbase的列名
     * 注解指定了列名就用指定的 没有指定就用java成员名作为hbase列名
     * @param field {@link HColumn}注解的java实体字段
     * @param hcolumn 从{@link HColumn}注解解析出hbase列名
     * @return hbase列名
     */
    public String determineQualifier(Field field, HColumn hcolumn) {
        String columnName = hcolumn.name();
        return Objects.nonNull(columnName) && StringUtils.isNotBlank(columnName) ? columnName : field.getName();
    }

    /**
     * 拼接列簇跟列名
     * @param family 列簇
     * @param qualifier 列名
     * @return ${family}#${qualifier}
     */
    public String getHColumnFieldId(String family, String qualifier) {
        return family + "#" + qualifier;
    }

    /**
     * java实体字段序列化
     * @param field java实体字段
     * @param value java类型的值
     * @return 字节数组 跟hbase交互使用
     */
    private byte[] objectToBytes(Field field, Object value) {
        if (value instanceof Date) {
            Date date = (Date) value;
            return Bytes.toBytes(date.getTime());
        } else if (TypeUtil.isBooleanType(field)) {
            return Bytes.toBytes((Boolean) value);
        } else if (TypeUtil.isShortType(field)) {
            return Bytes.toBytes((Short) value);
        } else if (TypeUtil.isIntType(field)) {
            return Bytes.toBytes((Integer) value);
        } else if (TypeUtil.isLongType(field)) {
            return Bytes.toBytes((Long) value);
        } else if (TypeUtil.isFloatType(field)) {
            return Bytes.toBytes((Float) value);
        } else if (TypeUtil.isDoubleType(field)) {
            return Bytes.toBytes((Double) value);
        } else if (TypeUtil.isBigDecimalType(field)) {
            return Bytes.toBytes((BigDecimal) value);
        } else if (TypeUtil.isStringType(field)) {
            return Bytes.toBytes(value.toString());
        } else if (field.getType().isAssignableFrom(byte[].class)) {
            return (byte[]) value;
        } else if (!field.getType().isAssignableFrom(Byte[].class)) {
            throw FrameworkException.getInstance("Hbase实体注解解析错误 " + field.getName() + "属性的值类型不合法 现只支持 [" + TypeUtil.getSuitableTypes() + "]");
        } else {
            Byte[] bytesObj = (Byte[]) value;
            byte[] result = new byte[bytesObj.length];
            for (int i = 0; i < bytesObj.length; ++i) {
                result[i] = bytesObj[i];
            }
            return result;
        }
    }

    /**
     * hbase响应反序列化java实体字段
     * @param field java实体字段
     * @param bytes hbase响应
     * @return java类型
     */
    private Object bytesToObject(Field field, byte[] bytes) {
        if (bytes == null) {
            return null;
        } else if (field.getType().isAssignableFrom(Date.class)) {
            long val = Bytes.toLong(bytes);
            return new Date(val);
        } else if (TypeUtil.isBooleanType(field)) {
            return Bytes.toBoolean(bytes);
        } else if (TypeUtil.isShortType(field)) {
            return Bytes.toShort(bytes);
        } else if (TypeUtil.isIntType(field)) {
            return Bytes.toInt(bytes);
        } else if (TypeUtil.isLongType(field)) {
            return Bytes.toLong(bytes);
        } else if (TypeUtil.isFloatType(field)) {
            return Bytes.toFloat(bytes);
        } else if (TypeUtil.isDoubleType(field)) {
            return Bytes.toDouble(bytes);
        } else if (TypeUtil.isBigDecimalType(field)) {
            return Bytes.toBigDecimal(bytes);
        } else if (field.getType().isAssignableFrom(String.class)) {
            return new String(bytes);
        } else if (field.getType().isAssignableFrom(byte[].class)) {
            return bytes;
        } else if (!field.getType().isAssignableFrom(Byte[].class)) {
            return null;
        } else {
            Byte[] result = new Byte[bytes.length];
            for (int i = 0; i < bytes.length; ++i) {
                result[i] = bytes[i];
            }
            return result;
        }
    }

    /**
     * rowkey
     */
    private <T> byte[] getRowkeyValue(T entity, HBaseEntityMetadata metadata) throws IllegalAccessException {
        Field rowKeyField = metadata.getRowKeyField();
        Object rowKeyValue = rowKeyField.get(entity);
        // rowkey有效性
        if(Objects.isNull(rowKeyValue) || StringUtils.isBlank(rowKeyField.toString())) {
            throw FrameworkException.getInstance("Hbase实体注解解析错误 " + rowKeyField.getName() + "属性的值不能为空");
        }
        return Bytes.toBytes(rowKeyValue.toString());
    }

    /**
     * hbase版本号
     */
    private <T> long getVersionValue(T entity, HBaseEntityMetadata metadata) throws IllegalAccessException {
        Field versionField = metadata.getVersionField();
        /**
         * 没有在java实体中用注解{@link HVersion}注解标识默认版本号0
         */
        if (versionField == null) {
            return 0L;
        } else {
            Object v = versionField.get(entity);
            return Objects.isNull(v) ? 0L : (Long) v;
        }
    }

    public static boolean isWrapClass(Class<?> clz) {
        try {
            return ((Class<?>) clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }
}

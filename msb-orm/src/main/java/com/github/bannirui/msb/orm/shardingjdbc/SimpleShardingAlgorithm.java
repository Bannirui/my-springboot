package com.github.bannirui.msb.orm.shardingjdbc;

public class SimpleShardingAlgorithm implements IShardingAlgorithm {
    public static final String MOD = "mod";
    public static final String HASH = "hash";
    private TableConfig tableConfig;
    private SimpleShardingAlgorithm.DatabaseShardingAlgorithm dataBaseShardingAlgorithm = new SimpleShardingAlgorithm.DatabaseShardingAlgorithm();
    private SimpleShardingAlgorithm.TableShardingAlgorithm tableShardingAlgorithm = new SimpleShardingAlgorithm.TableShardingAlgorithm();

    public SimpleShardingAlgorithm() {
    }

    public void setTableConfig(TableConfig tableConfig) {
        this.tableConfig = tableConfig;
    }

    public SimpleShardingAlgorithm.DatabaseShardingAlgorithm getDataBaseShardingAlgorithm() {
        return this.dataBaseShardingAlgorithm;
    }

    public void setDataBaseShardingAlgorithm(SimpleShardingAlgorithm.DatabaseShardingAlgorithm dataBaseShardingAlgorithm) {
        this.dataBaseShardingAlgorithm = dataBaseShardingAlgorithm;
    }

    public SimpleShardingAlgorithm.TableShardingAlgorithm getTableShardingAlgorithm() {
        return this.tableShardingAlgorithm;
    }

    public void setTableShardingAlgorithm(SimpleShardingAlgorithm.TableShardingAlgorithm tableShardingAlgorithm) {
        this.tableShardingAlgorithm = tableShardingAlgorithm;
    }

    public class TableShardingAlgorithm implements SingleKeyTableShardingAlgorithm<Long> {
        public TableShardingAlgorithm() {
        }

        protected String getTableName(String logicTableName, Object number, Integer dbSize) {
            Long tableSize = SimpleShardingAlgorithm.this.tableConfig.getSize();
            Long value = null;
            if (SimpleShardingAlgorithm.this.tableConfig.getStrategy() != null && !SimpleShardingAlgorithm.this.tableConfig.getStrategy().equalsIgnoreCase("mod")) {
                if (!SimpleShardingAlgorithm.this.tableConfig.getStrategy().equalsIgnoreCase("hash")) {
                    throw FrameworkException.getInstance("sharding jdbc 策略设置非法:{}", new Object[]{SimpleShardingAlgorithm.this.tableConfig.getStrategy()});
                }

                int hashcode = number.hashCode();
                if (hashcode < 0) {
                    hashcode = Math.abs(hashcode);
                }

                value = (long)hashcode;
            } else {
                value = Long.parseLong(number.toString());
            }

            Long moduloValue = value % tableSize;
            return ShardingJdbcUtil.generationCurrentTableName(logicTableName, moduloValue, SimpleShardingAlgorithm.this.tableConfig.getFormat());
        }

        public String doEqualSharding(Collection<String> collection, ShardingValue<Long> shardingValue) {
            return this.getTableName(shardingValue.getLogicTableName(), shardingValue.getValue(), collection.size());
        }

        public Collection<String> doInSharding(Collection<String> collection, ShardingValue<Long> shardingValue) {
            List<String> tableNames = new ArrayList(shardingValue.getValues().size());
            Iterator var4 = shardingValue.getValues().iterator();

            while(var4.hasNext()) {
                Object e = var4.next();
                String tableName = this.getTableName(shardingValue.getLogicTableName(), e, collection.size());
                tableNames.add(tableName);
            }

            return tableNames;
        }

        public Collection<String> doBetweenSharding(Collection<String> collection, ShardingValue<Long> shardingValue) {
            List<String> tableNames = new ArrayList();
            Range<Long> range = shardingValue.getValueRange();
            Object lowerEndpointNumber = range.lowerEndpoint();
            Object upperEndpointNumber = range.upperEndpoint();

            for(Long i = Long.parseLong(lowerEndpointNumber.toString()); i <= Long.parseLong(upperEndpointNumber.toString()); i = i + 1L) {
                String tableName = this.doEqualSharding(collection, new ShardingValue(shardingValue.getLogicTableName(), shardingValue.getColumnName(), i));
                tableNames.add(tableName);
            }

            return tableNames;
        }
    }

    public class DatabaseShardingAlgorithm implements SingleKeyDatabaseShardingAlgorithm<Long> {
        public DatabaseShardingAlgorithm() {
        }

        protected String getDbName(Object obj, Integer dbSize) {
            if (dbSize == 1) {
                return ShardingJdbcUtil.generationCurrentDataBaseName(0L);
            } else {
                Long value = null;
                if (SimpleShardingAlgorithm.this.tableConfig.getStrategy() != null && !SimpleShardingAlgorithm.this.tableConfig.getStrategy().equalsIgnoreCase("mod")) {
                    if (!SimpleShardingAlgorithm.this.tableConfig.getStrategy().equalsIgnoreCase("hash")) {
                        throw FrameworkException.getInstance("sharding jdbc 策略设置非法:{}", new Object[]{SimpleShardingAlgorithm.this.tableConfig.getStrategy()});
                    }

                    int hashcode = obj.hashCode();
                    if (hashcode < 0) {
                        hashcode = Math.abs(hashcode);
                    }

                    value = (long)hashcode;
                } else {
                    value = Long.parseLong(obj.toString());
                }

                Long tableSize = SimpleShardingAlgorithm.this.tableConfig.getSize();
                Long size = (tableSize - tableSize / (long)dbSize) / (long)(dbSize - 1);
                size = tableSize % size == 0L ? size : size + 1L;
                Long ds = value % tableSize / size;
                return ShardingJdbcUtil.generationCurrentDataBaseName(ds);
            }
        }

        public String doEqualSharding(Collection<String> collection, ShardingValue<Long> shardingValue) {
            return this.getDbName(shardingValue.getValue(), collection.size());
        }

        public Collection<String> doInSharding(Collection<String> collection, ShardingValue<Long> shardingValue) {
            List<String> dbNames = new ArrayList(shardingValue.getValues().size());
            Iterator var4 = shardingValue.getValues().iterator();

            while(var4.hasNext()) {
                Object e = var4.next();
                String dbName = this.getDbName(e, collection.size());
                dbNames.add(dbName);
            }

            return dbNames;
        }

        public Collection<String> doBetweenSharding(Collection<String> collection, ShardingValue<Long> shardingValue) {
            List<String> dbNames = new ArrayList();
            Range<Long> range = shardingValue.getValueRange();
            Object lowerEndpointNumber = range.lowerEndpoint();
            Object upperEndpointNumber = range.upperEndpoint();

            for(Long i = Long.parseLong(lowerEndpointNumber.toString()); i <= Long.parseLong(upperEndpointNumber.toString()); i = i + 1L) {
                String dbName = this.doEqualSharding(collection, new ShardingValue(shardingValue.getLogicTableName(), shardingValue.getColumnName(), i));
                dbNames.add(dbName);
            }

            return dbNames;
        }
    }
}

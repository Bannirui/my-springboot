package com.github.bannirui.msb.orm.mapper;

public class OptimisticLockerUpdateProvider extends MapperTemplate {
    public static final String METHOD_NAME = "updateByExampleAndVersionSelective";

    public OptimisticLockerUpdateProvider(Class<?> mapperClass, MapperHelper mapperHelper) {
        super(mapperClass, mapperHelper);
    }

    public String updateByExampleAndVersionSelective(MappedStatement ms) {
        Class<?> entityClass = this.getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        if (this.isCheckExampleEntityClass()) {
            sql.append(SqlHelper.exampleCheck(entityClass));
        }

        sql.append(SqlHelper.updateTable(entityClass, this.tableName(entityClass), "example"));
        sql.append(SqlHelper.updateSetColumns(entityClass, "record", true, this.isNotEmpty()));
        sql.append(SqlHelper.updateByExampleWhereClause());
        return sql.toString();
    }
}

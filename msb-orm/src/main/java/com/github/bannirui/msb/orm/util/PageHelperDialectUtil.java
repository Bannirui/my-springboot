package com.github.bannirui.msb.orm.util;

import com.github.bannirui.msb.ex.FrameworkException;
import com.github.pagehelper.dialect.helper.Db2Dialect;
import com.github.pagehelper.dialect.helper.HsqldbDialect;
import com.github.pagehelper.dialect.helper.InformixDialect;
import com.github.pagehelper.dialect.helper.MySqlDialect;
import com.github.pagehelper.dialect.helper.OracleDialect;
import com.github.pagehelper.dialect.helper.SqlServer2012Dialect;
import com.github.pagehelper.dialect.helper.SqlServerDialect;
import java.util.HashMap;
import java.util.Map;

public class PageHelperDialectUtil {
    private static Map<String, Class<?>> dialectAliasMap = new HashMap<>();

    public static String getDialectClassName(String jdbcUrl) {
        String dbType = fromJdbcUrl(jdbcUrl);
        Class<?> dialectClass = dialectAliasMap.get(dbType);
        if (dialectClass != null) {
            return dialectClass.getName();
        } else {
            throw FrameworkException.getInstance("根据数据库连接信息{0}获取数据库方言信息出错", dbType);
        }
    }

    private static String fromJdbcUrl(String jdbcUrl) {
        for (String dialect : dialectAliasMap.keySet()) {
            if (jdbcUrl.contains(":" + dialect + ":")) {
                return dialect;
            }
        }
        return null;
    }

    static {
        dialectAliasMap.put("hsqldb", HsqldbDialect.class);
        dialectAliasMap.put("h2", HsqldbDialect.class);
        dialectAliasMap.put("postgresql", HsqldbDialect.class);
        dialectAliasMap.put("mysql", MySqlDialect.class);
        dialectAliasMap.put("mariadb", MySqlDialect.class);
        dialectAliasMap.put("sqlite", MySqlDialect.class);
        dialectAliasMap.put("oracle", OracleDialect.class);
        dialectAliasMap.put("db2", Db2Dialect.class);
        dialectAliasMap.put("informix", InformixDialect.class);
        dialectAliasMap.put("sqlserver", SqlServerDialect.class);
        dialectAliasMap.put("sqlserver2012", SqlServer2012Dialect.class);
        dialectAliasMap.put("derby", SqlServer2012Dialect.class);
    }
}

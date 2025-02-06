package com.github.pagehelper.page;

public class PageAutoDialect {
    private static Map<String, Class<?>> dialectAliasMap = new HashMap();
    private boolean autoDialect = true;
    private boolean closeConn = true;
    private Properties properties;
    private Map<DataSource, AbstractHelperDialect> urlDialectMap = new ConcurrentHashMap();
    private ReentrantLock lock = new ReentrantLock();
    private AbstractHelperDialect delegate;
    private ThreadLocal<AbstractHelperDialect> dialectThreadLocal = new ThreadLocal();

    public PageAutoDialect() {
    }

    public void initDelegateDialect(MappedStatement ms) {
        if (this.delegate == null) {
            if (this.autoDialect) {
                this.delegate = this.getDialect(ms);
            } else {
                this.dialectThreadLocal.set(this.getDialect(ms));
            }
        }

    }

    public AbstractHelperDialect getDelegate() {
        return this.delegate != null ? this.delegate : (AbstractHelperDialect)this.dialectThreadLocal.get();
    }

    public void clearDelegate() {
        this.dialectThreadLocal.remove();
    }

    private String fromJdbcUrl(String jdbcUrl) {
        Iterator var2 = dialectAliasMap.keySet().iterator();

        String dialect;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            dialect = (String)var2.next();
        } while(jdbcUrl.indexOf(":" + dialect + ":") == -1);

        return dialect;
    }

    private Class resloveDialectClass(String className) throws Exception {
        return dialectAliasMap.containsKey(className.toLowerCase()) ? (Class)dialectAliasMap.get(className.toLowerCase()) : Class.forName(className);
    }

    private AbstractHelperDialect initDialect(String dialectClass, Properties properties) {
        if (StringUtil.isEmpty(dialectClass)) {
            throw new PageException("使用 PageHelper 分页插件时，必须设置 helper 属性");
        } else {
            AbstractHelperDialect dialect;
            try {
                Class sqlDialectClass = this.resloveDialectClass(dialectClass);
                if (!AbstractHelperDialect.class.isAssignableFrom(sqlDialectClass)) {
                    throw new PageException("使用 PageHelper 时，方言必须是实现 " + AbstractHelperDialect.class.getCanonicalName() + " 接口的实现类!");
                }

                dialect = (AbstractHelperDialect)sqlDialectClass.newInstance();
            } catch (Exception var5) {
                throw new PageException("初始化 helper [" + dialectClass + "]时出错:" + var5.getMessage(), var5);
            }

            dialect.setProperties(properties);
            return dialect;
        }
    }

    private String getUrl(DataSource dataSource) {
        String result = null;

        try {
            Callable<String> callable = new PageAutoDialect.GetUrlCallable(dataSource);
            FutureTask<String> task = new FutureTask(callable);
            (new Thread(task, "pageHelper-getUrl")).start();
            result = (String)task.get();
            return result;
        } catch (Exception var5) {
            var5.printStackTrace();
            return null;
        }
    }

    private AbstractHelperDialect getDialect(MappedStatement ms) {
        DataSource dataSource = ms.getConfiguration().getEnvironment().getDataSource();
        AbstractHelperDialect helperDialect = (AbstractHelperDialect)this.urlDialectMap.get(dataSource);
        if (helperDialect != null) {
            return helperDialect;
        } else {
            AbstractHelperDialect var4;
            try {
                this.lock.lock();
                helperDialect = (AbstractHelperDialect)this.urlDialectMap.get(dataSource);
                if (helperDialect == null) {
                    String url = this.getUrl(dataSource);
                    if (StringUtil.isEmpty(url)) {
                        throw new PageException("无法自动获取jdbcUrl，请在分页插件中配置dialect参数!");
                    }

                    String dialectStr = this.fromJdbcUrl(url);
                    if (dialectStr == null) {
                        throw new PageException("无法自动获取数据库类型，请通过 helperDialect 参数指定!");
                    }

                    AbstractHelperDialect dialect = this.initDialect(dialectStr, this.properties);
                    this.urlDialectMap.put(dataSource, dialect);
                    AbstractHelperDialect var7 = dialect;
                    return var7;
                }

                var4 = helperDialect;
            } finally {
                this.lock.unlock();
            }

            return var4;
        }
    }

    public void setProperties(Properties properties) {
        String closeConn = properties.getProperty("closeConn");
        if (StringUtil.isNotEmpty(closeConn)) {
            this.closeConn = Boolean.parseBoolean(closeConn);
        }

        String dialect = properties.getProperty("helperDialect");
        String runtimeDialect = properties.getProperty("autoRuntimeDialect");
        if (StringUtil.isNotEmpty(runtimeDialect) && runtimeDialect.equalsIgnoreCase("TRUE")) {
            this.autoDialect = false;
            this.properties = properties;
        } else if (StringUtil.isEmpty(dialect)) {
            this.autoDialect = true;
            this.properties = properties;
        } else {
            this.autoDialect = false;
            this.delegate = this.initDialect(dialect, properties);
        }

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

    private class GetUrlCallable implements Callable<String> {
        private DataSource dataSource;

        public GetUrlCallable(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        public String call() throws Exception {
            Connection conn = null;

            String var2;
            try {
                conn = this.dataSource.getConnection();
                var2 = conn.getMetaData().getURL();
            } catch (SQLException var11) {
                throw new PageException(var11);
            } finally {
                if (conn != null) {
                    try {
                        if (PageAutoDialect.this.closeConn) {
                            conn.close();
                        }
                    } catch (SQLException var10) {
                    }
                }

            }

            return var2;
        }
    }
}

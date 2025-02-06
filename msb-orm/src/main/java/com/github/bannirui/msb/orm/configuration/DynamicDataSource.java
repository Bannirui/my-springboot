package com.github.bannirui.msb.orm.configuration;

public class DynamicDataSource extends AbstractDataSource implements DisposableBean, Closeable {
    private static final Logger log = LoggerFactory.getLogger(DynamicDataSource.class);
    private volatile HikariDataSource dataSource;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock;
    private final Lock writeLock;

    protected DynamicDataSource() {
        this.readLock = this.readWriteLock.readLock();
        this.writeLock = this.readWriteLock.writeLock();
    }

    public DynamicDataSource(HikariConfig hikariConfig) {
        this.readLock = this.readWriteLock.readLock();
        this.writeLock = this.readWriteLock.writeLock();
        hikariConfig.setPoolName(DataSourceHelp.generatePoolName(hikariConfig.getPoolName()));
        this.dataSource = new HikariDataSource(hikariConfig);
    }

    public void updateDataSource(HikariConfig hikariConfig, long forceCloseWaitSeconds) {
        Exception exception = null;

        try {
            this.writeLock.lock();
            this.createNewDataSource(hikariConfig, forceCloseWaitSeconds);
        } catch (Exception var9) {
            exception = var9;
        } finally {
            this.writeLock.unlock();
        }

        if (exception != null) {
            log.error("更新数据源时出现异常:", exception);
        }

    }

    public DataSource determineTargetDataSource() {
        try {
            this.readLock.lock();
            HikariDataSource var1 = this.dataSource;
            return var1;
        } catch (Exception var5) {
            log.error("获取数据源时出现异常:", var5);
        } finally {
            this.readLock.unlock();
        }

        throw new DataSourceLookupFailureException("未查询到数据源");
    }

    private void createNewDataSource(HikariConfig hikariConfig, long forceCloseWaitSeconds) {
        HikariDataSource releaseDataSource = this.dataSource;
        hikariConfig.setPoolName(DataSourceHelp.generatePoolName(hikariConfig.getPoolName()));
        this.dataSource = new HikariDataSource(hikariConfig);
        if (releaseDataSource != null) {
            DataSourceHelp.asyncRelease(new ReleaseDataSourceRunnable(releaseDataSource, forceCloseWaitSeconds));
        }

    }

    public HikariDataSource getDataSource() {
        return this.dataSource != null ? this.dataSource : (HikariDataSource)this.determineTargetDataSource();
    }

    public String getPoolName() {
        return this.getDataSource().getPoolName();
    }

    public void destroy() throws Exception {
        DataSourceHelp.shutdown();
        this.close();
    }

    public void close() throws IOException {
        if (this.dataSource != null) {
            this.dataSource.close();
            this.dataSource = null;
        }

    }

    public Connection getConnection() throws SQLException {
        return this.determineTargetDataSource().getConnection();
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return this.determineTargetDataSource().getConnection(username, password);
    }

    public int getLoginTimeout() throws SQLException {
        return this.determineTargetDataSource().getLoginTimeout();
    }

    public void setLoginTimeout(int timeout) throws SQLException {
        this.determineTargetDataSource().setLoginTimeout(timeout);
    }

    public PrintWriter getLogWriter() {
        try {
            return this.determineTargetDataSource().getLogWriter();
        } catch (SQLException var2) {
            var2.printStackTrace();
            return super.getLogWriter();
        }
    }

    public void setLogWriter(PrintWriter pw) throws SQLException {
        this.determineTargetDataSource().setLogWriter(pw);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return this.determineTargetDataSource().unwrap(iface);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return this.determineTargetDataSource().isWrapperFor(iface);
    }

    public java.util.logging.Logger getParentLogger() {
        try {
            return this.determineTargetDataSource().getParentLogger();
        } catch (SQLFeatureNotSupportedException var2) {
            var2.printStackTrace();
            return super.getParentLogger();
        }
    }
}

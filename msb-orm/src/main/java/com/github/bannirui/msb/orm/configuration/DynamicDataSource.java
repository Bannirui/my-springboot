package com.github.bannirui.msb.orm.configuration;

import com.github.bannirui.msb.orm.util.DataSourceHelp;
import com.github.bannirui.msb.orm.util.ReleaseDataSourceRunnable;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
        try {
            this.writeLock.lock();
            this.createNewDataSource(hikariConfig, forceCloseWaitSeconds);
        } catch (Exception e) {
            log.error("更新数据源时出现异常:", e);
        } finally {
            this.writeLock.unlock();
        }
    }

    public DataSource determineTargetDataSource() {
        try {
            this.readLock.lock();
            return this.dataSource;
        } catch (Exception e) {
            log.error("获取数据源时出现异常:", e);
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

    @Override
    public void destroy() throws Exception {
        DataSourceHelp.shutdown();
        this.close();
    }

    @Override
    public void close() throws IOException {
        if (this.dataSource != null) {
            this.dataSource.close();
            this.dataSource = null;
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.determineTargetDataSource().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return this.determineTargetDataSource().getConnection(username, password);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return this.determineTargetDataSource().getLoginTimeout();
    }

    @Override
    public void setLoginTimeout(int timeout) throws SQLException {
        this.determineTargetDataSource().setLoginTimeout(timeout);
    }

    @Override
    public PrintWriter getLogWriter() {
        try {
            return this.determineTargetDataSource().getLogWriter();
        } catch (SQLException e) {
            e.printStackTrace();
            return super.getLogWriter();
        }
    }

    @Override
    public void setLogWriter(PrintWriter pw) throws SQLException {
        this.determineTargetDataSource().setLogWriter(pw);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return this.determineTargetDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return this.determineTargetDataSource().isWrapperFor(iface);
    }

    @Override
    public java.util.logging.Logger getParentLogger() {
        try {
            return this.determineTargetDataSource().getParentLogger();
        } catch (SQLFeatureNotSupportedException e) {
            e.printStackTrace();
            return super.getParentLogger();
        }
    }
}

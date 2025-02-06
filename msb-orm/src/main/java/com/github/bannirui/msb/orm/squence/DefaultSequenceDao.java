package com.github.bannirui.msb.orm.squence;

public class DefaultSequenceDao extends AbstractLifecycle implements SequenceDao {
    private static final Logger logger = LoggerFactory.getLogger(DefaultSequenceDao.class);
    private static final int MIN_STEP = 1;
    private static final int MAX_STEP = 100000;
    private static final int DEFAULT_STEP = 1000;
    private static final int DEFAULT_RETRY_TIMES = 150;
    private static final String DEFAULT_TABLE_NAME = "sequence";
    private static final String DEFAULT_NAME_COLUMN_NAME = "name";
    private static final String DEFAULT_VALUE_COLUMN_NAME = "value";
    private static final String DEFAULT_GMT_MODIFIED_COLUMN_NAME = "gmt_modified";
    private static final long DELTA = 100000000L;
    private DataSource dataSource;
    private int retryTimes = 150;
    private int step = 1000;
    private String tableName = "sequence";
    private String nameColumnName = "name";
    private String valueColumnName = "value";
    private String gmtModifiedColumnName = "gmt_modified";
    protected int innerStep = 1000;
    protected String configStr = "";
    private volatile String selectSql;
    private volatile String updateSql;
    private Long minValue = 0L;
    private Long maxValue = 9223372036754775807L;

    public DefaultSequenceDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void adjust(String name) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = this.dataSource.getConnection();
            stmt = conn.prepareStatement(this.getSelectSql());
            stmt.setString(1, name);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                logger.info("数据库中未配置该sequence！请往数据库中插入sequence记录，或者启动adjust开关");
                this.adjustInsert(name);
            }
        } catch (SQLException var9) {
            if (var9 == null || var9.getMessage() == null || var9.getMessage().indexOf("ORA-00001") < 0 && var9.getMessage().indexOf("Duplicate entry") < 0) {
                logger.error("初值校验和自适应过程中出错.", var9);
                throw var9;
            }

            logger.warn("数据库中插入sequence记录重复,sequenceName{}", name);
        } finally {
            closeDbResource(rs, stmt, conn);
        }

    }

    protected static void closeDbResource(ResultSet rs, Statement stmt, Connection conn) {
        closeResultSet(rs);
        closeStatement(stmt);
        closeConnection(conn);
    }

    private void adjustInsert(String name) throws SQLException {
        long newValue = (long)this.innerStep;
        Connection conn = null;
        PreparedStatement stmt = null;
        Object rs = null;

        try {
            conn = this.dataSource.getConnection();
            stmt = conn.prepareStatement(this.getInsertSql());
            stmt.setString(1, name);
            stmt.setLong(2, newValue);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("faild to auto adjust init value at  " + name + " update affectedRow =0");
            }

            logger.info("Sequence   name:" + name + "插入初值:" + name + "value:" + newValue);
        } catch (SQLException var11) {
            logger.error("由于SQLException,插入初值自适应失败！Sequence，sequence Name：" + name + "   value:" + newValue, var11);
            throw FrameworkException.getInstance(var11, "由于SQLException,插入初值自适应失败！dbGroupIndex，sequence Name：" + name + "   value:" + newValue, new Object[0]);
        } finally {
            closeDbResource((ResultSet)rs, stmt, conn);
        }

    }

    protected String getInsertSql() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("insert into ").append(this.getTableName()).append("(");
        buffer.append(this.getNameColumnName()).append(",");
        buffer.append(this.getValueColumnName()).append(",");
        buffer.append(this.getGmtModifiedColumnName()).append(") values(?,?,?);");
        return buffer.toString();
    }

    public SequenceRange nextRange(String name) {
        if (name == null) {
            throw new IllegalArgumentException("序列名称不能为空");
        } else {
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            for(int i = 0; i < this.retryTimes + 1; ++i) {
                String sql = this.getSelectSql();

                long oldValue;
                long newValue;
                try {
                    conn = this.dataSource.getConnection();
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, name);
                    rs = stmt.executeQuery();
                    if (!rs.next()) {
                        this.adjustInsert(name);
                        continue;
                    }

                    oldValue = rs.getLong(1);
                    StringBuilder message;
                    if (oldValue < this.minValue) {
                        message = new StringBuilder();
                        message.append("Sequence value cannot be less than zero, value = ").append(oldValue);
                        message.append(", please check table ").append(this.getTableName());
                        throw new RuntimeException(message.toString());
                    }

                    if (oldValue > this.maxValue) {
                        message = new StringBuilder();
                        message.append("Sequence value overflow, value = ").append(oldValue);
                        message.append(", please check table ").append(this.getTableName());
                        throw new RuntimeException(message.toString());
                    }

                    newValue = oldValue + (long)this.getStep();
                } catch (Exception var25) {
                    logger.warn("获取 sequence 失败 sql{}", sql, var25);
                    continue;
                } finally {
                    closeResultSet(rs);
                    rs = null;
                    closeStatement(stmt);
                    stmt = null;
                    closeConnection(conn);
                    conn = null;
                }

                try {
                    conn = this.dataSource.getConnection();
                    stmt = conn.prepareStatement(this.getUpdateSql());
                    stmt.setLong(1, newValue);
                    stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                    stmt.setString(3, name);
                    stmt.setLong(4, oldValue);
                    int affectedRows = stmt.executeUpdate();
                    if (affectedRows != 0) {
                        SequenceRange sequenceRange = new SequenceRange(oldValue + 1L, newValue);
                        SequenceRange var13 = sequenceRange;
                        return var13;
                    }
                } catch (Exception var23) {
                    logger.warn("获取 sequence 失败 sql{}", sql, var23);
                } finally {
                    closeResultSet(rs);
                    rs = null;
                    closeStatement(stmt);
                    stmt = null;
                    closeConnection(conn);
                    conn = null;
                }
            }

            throw FrameworkException.getInstance("Retried too many times, retryTimes = " + this.retryTimes, new Object[0]);
        }
    }

    private String getSelectSql() {
        if (this.selectSql == null) {
            synchronized(this) {
                if (this.selectSql == null) {
                    StringBuilder buffer = new StringBuilder();
                    buffer.append("select ").append(this.getValueColumnName());
                    buffer.append(" from ").append(this.getTableName());
                    buffer.append(" where ").append(this.getNameColumnName()).append(" = ?");
                    this.selectSql = buffer.toString();
                }
            }
        }

        return this.selectSql;
    }

    private String getUpdateSql() {
        if (this.updateSql == null) {
            synchronized(this) {
                if (this.updateSql == null) {
                    StringBuilder buffer = new StringBuilder();
                    buffer.append("update ").append(this.getTableName());
                    buffer.append(" set ").append(this.getValueColumnName()).append(" = ?, ");
                    buffer.append(this.getGmtModifiedColumnName()).append(" = ? where ");
                    buffer.append(this.getNameColumnName()).append(" = ? and ");
                    buffer.append(this.getValueColumnName()).append(" = ?");
                    this.updateSql = buffer.toString();
                }
            }
        }

        return this.updateSql;
    }

    private static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException var2) {
                logger.debug("Could not close JDBC ResultSet", var2);
            } catch (Throwable var3) {
                logger.debug("Unexpected exception on closing JDBC ResultSet", var3);
            }
        }

    }

    private static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException var2) {
                logger.debug("Could not close JDBC Statement", var2);
            } catch (Throwable var3) {
                logger.debug("Unexpected exception on closing JDBC Statement", var3);
            }
        }

    }

    private static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException var2) {
                logger.debug("Could not close JDBC Connection", var2);
            } catch (Throwable var3) {
                logger.debug("Unexpected exception on closing JDBC Connection", var3);
            }
        }

    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int getRetryTimes() {
        return this.retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        if (retryTimes < 0) {
            throw new IllegalArgumentException("Property retryTimes cannot be less than zero, retryTimes = " + retryTimes);
        } else {
            this.retryTimes = retryTimes;
        }
    }

    public int getStep() {
        return this.step;
    }

    public void setStep(int step) {
        if (step >= 1 && step <= 100000) {
            this.step = step;
        } else {
            StringBuilder message = new StringBuilder();
            message.append("Property step out of range [").append(1);
            message.append(",").append(100000).append("], step = ").append(step);
            throw new IllegalArgumentException(message.toString());
        }
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getNameColumnName() {
        return this.nameColumnName;
    }

    public void setNameColumnName(String nameColumnName) {
        this.nameColumnName = nameColumnName;
    }

    public String getValueColumnName() {
        return this.valueColumnName;
    }

    public void setValueColumnName(String valueColumnName) {
        this.valueColumnName = valueColumnName;
    }

    public String getGmtModifiedColumnName() {
        return this.gmtModifiedColumnName;
    }

    public void setGmtModifiedColumnName(String gmtModifiedColumnName) {
        this.gmtModifiedColumnName = gmtModifiedColumnName;
    }

    public String getConfigStr() {
        if (StringUtil.isEmpty(this.configStr)) {
            String format = "[type:simple] [step:{0}] [retryTimes:{1}] [tableInfo:{2}({3},{4},{5})]";
            this.configStr = MessageFormat.format(format, String.valueOf(this.step), String.valueOf(this.retryTimes), this.tableName, this.nameColumnName, this.valueColumnName, this.gmtModifiedColumnName);
        }

        return this.configStr;
    }

    public Long getMinValue() {
        return this.minValue;
    }

    public void setMinValue(Long minValue) {
        this.minValue = minValue;
    }

    public Long getMaxValue() {
        return this.maxValue;
    }

    public void setMaxValue(Long maxValue) {
        this.maxValue = maxValue;
    }
}

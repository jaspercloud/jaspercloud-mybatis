package com.jaspercloud.mybatis.support.jdbc;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class ProxyConnection implements Connection {

    private Connection master;
    private Connection slave;

    public ProxyConnection(Connection master, Connection slave) {
        this.master = master;
        this.slave = slave;
    }

    private Connection selectConnection() {
        if (RouteDataSource.isSlave() && null != slave) {
            return slave;
        }
        return master;
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        master.setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return master.getAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
        master.commit();
    }

    @Override
    public void rollback() throws SQLException {
        master.rollback();
    }

    @Override
    public void close() throws SQLException {
        master.close();
        if (null != slave) {
            slave.close();
        }
    }

    @Override
    public boolean isClosed() throws SQLException {
        return master.isClosed();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        master.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return master.isReadOnly();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        master.setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return master.getTransactionIsolation();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return master.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return master.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        master.rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        master.releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement() throws SQLException {
        return selectConnection().createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return selectConnection().prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return selectConnection().prepareCall(sql);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return selectConnection().nativeSQL(sql);
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return selectConnection().getMetaData();
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        selectConnection().setCatalog(catalog);
    }

    @Override
    public String getCatalog() throws SQLException {
        return selectConnection().getCatalog();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return selectConnection().getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        selectConnection().clearWarnings();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return selectConnection().createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return selectConnection().prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return selectConnection().prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return selectConnection().getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        selectConnection().setTypeMap(map);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        selectConnection().setHoldability(holdability);
    }

    @Override
    public int getHoldability() throws SQLException {
        return selectConnection().getHoldability();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return selectConnection().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return selectConnection().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return selectConnection().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return selectConnection().prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return selectConnection().prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return selectConnection().prepareStatement(sql, columnNames);
    }

    @Override
    public Clob createClob() throws SQLException {
        return selectConnection().createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        return selectConnection().createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return selectConnection().createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return selectConnection().createSQLXML();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return selectConnection().isValid(timeout);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        selectConnection().setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        selectConnection().setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return selectConnection().getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return selectConnection().getClientInfo();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return selectConnection().createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return selectConnection().createStruct(typeName, attributes);
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        selectConnection().setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException {
        return selectConnection().getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        selectConnection().abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        selectConnection().setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return selectConnection().getNetworkTimeout();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return selectConnection().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return selectConnection().isWrapperFor(iface);
    }
}

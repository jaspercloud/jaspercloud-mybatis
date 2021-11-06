package com.jaspercloud.mybatis.support.jdbc;

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public abstract class AbstractConnection extends ProxyWrapper implements Connection {

    @Override
    public SQLWarning getWarnings() throws SQLException {
        throw new SQLFeatureNotSupportedException("getWarnings");
    }

    @Override
    public void clearWarnings() throws SQLException {
        throw new SQLFeatureNotSupportedException("clearWarnings");
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        throw new SQLFeatureNotSupportedException("setSavepoint");
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        throw new SQLFeatureNotSupportedException("setSavepoint name");
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        throw new SQLFeatureNotSupportedException("releaseSavepoint");
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        throw new SQLFeatureNotSupportedException("rollback savepoint");
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        throw new SQLFeatureNotSupportedException("abort");
    }

    @Override
    public String getCatalog() {
        return null;
    }

    @Override
    public void setCatalog(String catalog) {
    }

    @Override
    public String getSchema() {
        return null;
    }

    @Override
    public void setSchema(String schema) {
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        throw new SQLFeatureNotSupportedException("getTypeMap");
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException("setTypeMap");
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        throw new SQLFeatureNotSupportedException("getNetworkTimeout");
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        throw new SQLFeatureNotSupportedException("setNetworkTimeout");
    }

    @Override
    public Clob createClob() throws SQLException {
        throw new SQLFeatureNotSupportedException("createClob");
    }

    @Override
    public Blob createBlob() throws SQLException {
        throw new SQLFeatureNotSupportedException("createBlob");
    }

    @Override
    public NClob createNClob() throws SQLException {
        throw new SQLFeatureNotSupportedException("createNClob");
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        throw new SQLFeatureNotSupportedException("createSQLXML");
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        throw new SQLFeatureNotSupportedException("createArrayOf");
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        throw new SQLFeatureNotSupportedException("createStruct");
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        throw new SQLFeatureNotSupportedException("isValid");
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        throw new SQLFeatureNotSupportedException("getClientInfo");
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        throw new SQLFeatureNotSupportedException("getClientInfo name");
    }

    @Override
    public void setClientInfo(String name, String value) {
        throw new UnsupportedOperationException("setClientInfo name value");
    }

    @Override
    public void setClientInfo(Properties properties) {
        throw new UnsupportedOperationException("setClientInfo properties");
    }
}

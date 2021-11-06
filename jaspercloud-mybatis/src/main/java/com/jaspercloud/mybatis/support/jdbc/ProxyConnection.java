package com.jaspercloud.mybatis.support.jdbc;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProxyConnection extends AbstractConnection {

    private ConnectionHolder connectionHolder;

    public ProxyConnection(ConnectionHolder connectionHolder) {
        this.connectionHolder = connectionHolder;
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        connectionHolder.setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return connectionHolder.isAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
        connectionHolder.commit();
    }

    @Override
    public void rollback() throws SQLException {
        connectionHolder.rollback();
    }

    @Override
    public void close() throws SQLException {
        connectionHolder.close();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return connectionHolder.isClosed();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        connectionHolder.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return connectionHolder.isReadOnly();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        connectionHolder.setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return connectionHolder.getTransactionIsolation();
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        StringBuilder builder = new StringBuilder();
        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(sql, connectionHolder.getDbType());
        if (sqlStatements.size() > 1) {
            for (SQLStatement sqlStatement : sqlStatements) {
                Connection connection = connectionHolder.getConnection(sqlStatement);
                builder.append(connection.nativeSQL(sqlStatement.toString()));
                builder.append(System.lineSeparator());
            }
        }
        SQLStatement sqlStatement = sqlStatements.get(0);
        Connection connection = connectionHolder.getConnection(sqlStatement);
        builder.append(connection.nativeSQL(sqlStatement.toString()));
        builder.append(System.lineSeparator());
        return builder.toString();
    }

    @Override
    public Statement createStatement() throws SQLException {
        return new ProxyStatement(connectionHolder);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return new ProxyStatement(connectionHolder);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return new ProxyStatement(connectionHolder);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException("prepareCall");
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        throw new SQLFeatureNotSupportedException("prepareCall");
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throw new SQLFeatureNotSupportedException("prepareCall");
    }

    private ProxyPreparedStatement parsePreparedStatement(String sql, SQLFunction<Connection, PreparedStatement> action) throws SQLException {
        List<ExecuteStatement<PreparedStatement>> statementList = new ArrayList<>();
        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(sql, connectionHolder.getDbType());
        if (sqlStatements.size() > 1) {
            for (SQLStatement sqlStatement : sqlStatements) {
                Connection connection = connectionHolder.getConnection(sqlStatement);
                PreparedStatement statement = action.apply(connection);
                statementList.add(new ExecuteStatement<>(sqlStatement, statement));
            }
            return new ProxyPreparedStatement(null, statementList);
        }
        SQLStatement sqlStatement = sqlStatements.get(0);
        Connection connection = connectionHolder.getConnection(sqlStatement);
        PreparedStatement statement = action.apply(connection);
        statementList.add(new ExecuteStatement<>(sqlStatement, statement));
        return new ProxyPreparedStatement(connectionHolder, statementList);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return parsePreparedStatement(sql, e -> e.prepareStatement(sql));
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return parsePreparedStatement(sql, e -> e.prepareStatement(sql, resultSetType, resultSetConcurrency));
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return parsePreparedStatement(sql, e -> e.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return parsePreparedStatement(sql, e -> e.prepareStatement(sql, autoGeneratedKeys));
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return parsePreparedStatement(sql, e -> e.prepareStatement(sql, columnIndexes));
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return parsePreparedStatement(sql, e -> e.prepareStatement(sql, columnNames));
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return connectionHolder.getMasterConnection().getMetaData();
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {

    }

    @Override
    public int getHoldability() throws SQLException {
        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }
}

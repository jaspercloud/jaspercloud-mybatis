package com.jaspercloud.mybatis.support.jdbc;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProxyStatement<T extends Statement> extends ProxyWrapper implements Statement {

    private ConnectionHolder connectionHolder;
    private boolean closed;
    private boolean poolable;
    private int fetchSize;
    private List<ExecuteStatement<T>> statementList = new ArrayList<>();

    protected List<ExecuteStatement<T>> getStatementList() {
        return statementList;
    }

    protected ExecuteStatement parseSingleStatement(String sql) throws SQLException {
        List<ExecuteStatement<T>> statementList = parseStatement(sql);
        if (statementList.size() > 1) {
            throw new SQLFeatureNotSupportedException("not support multi sql Statement");
        }
        return statementList.iterator().next();
    }

    protected List<ExecuteStatement<T>> parseStatement(String sql) throws SQLException {
        List<ExecuteStatement<T>> statList = new ArrayList<>();
        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(sql, connectionHolder.getDbType());
        for (SQLStatement sqlStatement : sqlStatements) {
            Connection connection = connectionHolder.getConnection(sqlStatement);
            Statement statement = connection.createStatement();
            statList.add(new ExecuteStatement(sqlStatement, statement));
        }
        statementList.addAll(statList);
        return statList;
    }

    public ProxyStatement(ConnectionHolder connectionHolder) {
        this.connectionHolder = connectionHolder;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        ExecuteStatement executeStatement = parseSingleStatement(sql);
        return executeStatement.getStatement().executeQuery(executeStatement.getSqlStatement().toString());
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        int ret = 0;
        List<ExecuteStatement<T>> list = parseStatement(sql);
        for (ExecuteStatement statement : list) {
            ret += statement.getStatement().executeUpdate(statement.getSqlStatement().toString());
        }
        return ret;
    }

    @Override
    public void close() throws SQLException {
        closed = true;
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return getStatementList().isEmpty() ? 0 : getStatementList()
                .iterator().next()
                .getStatement().getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        for (ExecuteStatement statement : getStatementList()) {
            statement.getStatement().setMaxFieldSize(max);
        }
    }

    @Override
    public int getMaxRows() throws SQLException {
        return getStatementList().isEmpty() ? -1 : getStatementList()
                .iterator().next()
                .getStatement().getMaxRows();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        for (ExecuteStatement statement : getStatementList()) {
            statement.getStatement().setMaxRows(max);
        }
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        for (ExecuteStatement statement : getStatementList()) {
            statement.getStatement().setEscapeProcessing(enable);
        }
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return getStatementList().isEmpty() ? 0 : getStatementList()
                .iterator().next()
                .getStatement().getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        for (ExecuteStatement statement : getStatementList()) {
            statement.getStatement().setQueryTimeout(seconds);
        }
    }

    @Override
    public void cancel() throws SQLException {
        for (ExecuteStatement statement : getStatementList()) {
            statement.getStatement().cancel();
        }
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void clearWarnings() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        Statement statement = connectionHolder.getMasterConnection().createStatement();
        statementList.add(new ExecuteStatement(null, statement));
        return statement.execute(sql);
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        if (getStatementList().size() > 1) {
            throw new SQLFeatureNotSupportedException("not support multi sql PreparedStatement");
        }
        return getStatementList()
                .iterator().next()
                .getStatement().getResultSet();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        int count = 0;
        for (ExecuteStatement statement : getStatementList()) {
            count += statement.getStatement().getUpdateCount();
        }
        return count;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getFetchDirection() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Connection getConnection() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        this.fetchSize = rows;
        for (ExecuteStatement statement : getStatementList()) {
            statement.getStatement().setFetchSize(rows);
        }
    }

    @Override
    public int getFetchSize() throws SQLException {
        return fetchSize;
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        if (getStatementList().size() > 1) {
            throw new SQLFeatureNotSupportedException("not support multi sql PreparedStatement");
        }
        return getStatementList()
                .iterator().next()
                .getStatement().getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        if (getStatementList().size() > 1) {
            throw new SQLFeatureNotSupportedException("not support multi sql PreparedStatement");
        }
        return getStatementList()
                .iterator().next()
                .getStatement().getResultSetType();
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        parseStatement(sql);
    }

    @Override
    public void clearBatch() throws SQLException {
        statementList.clear();
    }

    @Override
    public int[] executeBatch() throws SQLException {
        List<Integer> list = new ArrayList<>();
        List<ExecuteStatement<T>> statementList = getStatementList();
        for (ExecuteStatement statement : statementList) {
            int[] ret = statement.getStatement().executeBatch();
            for (int r : ret) {
                list.add(r);
            }
        }
        int[] rets = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            rets[i] = list.get(i);
        }
        return rets;
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        if (getStatementList().size() > 1) {
            throw new SQLFeatureNotSupportedException("not support multi sql PreparedStatement");
        }
        return getStatementList()
                .iterator().next()
                .getStatement().getGeneratedKeys();
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        int ret = 0;
        List<ExecuteStatement<T>> list = parseStatement(sql);
        for (ExecuteStatement statement : list) {
            ret += statement.getStatement().executeUpdate(statement.getSqlStatement().toString(), autoGeneratedKeys);
        }
        return ret;
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        int ret = 0;
        List<ExecuteStatement<T>> list = parseStatement(sql);
        for (ExecuteStatement statement : list) {
            ret += statement.getStatement().executeUpdate(statement.getSqlStatement().toString(), columnIndexes);
        }
        return ret;
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        int ret = 0;
        List<ExecuteStatement<T>> list = parseStatement(sql);
        for (ExecuteStatement statement : list) {
            ret += statement.getStatement().executeUpdate(statement.getSqlStatement().toString(), columnNames);
        }
        return ret;
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        Connection masterConnection = connectionHolder.getMasterConnection();
        Statement statement = masterConnection.createStatement();
        statementList.add(new ExecuteStatement(null, statement));
        return statement.execute(sql, autoGeneratedKeys);
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        Connection masterConnection = connectionHolder.getMasterConnection();
        Statement statement = masterConnection.createStatement();
        statementList.add(new ExecuteStatement(null, statement));
        return statement.execute(sql, columnIndexes);
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        Connection masterConnection = connectionHolder.getMasterConnection();
        Statement statement = masterConnection.createStatement();
        statementList.add(new ExecuteStatement(null, statement));
        return statement.execute(sql, columnNames);
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return closed;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        this.poolable = poolable;
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return poolable;
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public long getLargeUpdateCount() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setLargeMaxRows(long max) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public long getLargeMaxRows() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public long[] executeLargeBatch() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public long executeLargeUpdate(String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}

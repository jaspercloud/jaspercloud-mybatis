package com.jaspercloud.mybatis.support.jdbc;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGInsertStatement;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGSelectQueryBlock;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGSelectStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ConnectionHolder {

    private static Logger logger = LoggerFactory.getLogger(ConnectionHolder.class);

    private static final String DefaultName = "default";

    private RouteDataSource routeDataSource;
    private DbType dbType;
    private Set<String> transactionTable = new HashSet<>();
    private Connection masterConnection;
    private Connection slaveConnection;
    private Boolean autoCommit;
    private boolean closed;
    private boolean readOnly;
    private int level;

    public int getTransactionIsolation() {
        return level;
    }

    public void setTransactionIsolation(int level) throws SQLException {
        this.level = level;
        if (null != masterConnection) {
            masterConnection.setTransactionIsolation(level);
        }
        if (null != slaveConnection) {
            slaveConnection.setTransactionIsolation(level);
        }
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        this.readOnly = readOnly;
        if (null != masterConnection) {
            masterConnection.setReadOnly(readOnly);
        }
        if (null != slaveConnection) {
            slaveConnection.setReadOnly(readOnly);
        }
    }

    public boolean isAutoCommit() {
        return null == autoCommit ? true : autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        this.autoCommit = autoCommit;
        if (null != masterConnection) {
            masterConnection.setAutoCommit(autoCommit);
        }
        if (null != slaveConnection) {
            slaveConnection.setAutoCommit(autoCommit);
        }
    }

    public boolean isClosed() {
        return closed;
    }

    public DbType getDbType() {
        return dbType;
    }

    public ConnectionHolder(RouteDataSource routeDataSource, DbType dbType) {
        this.routeDataSource = routeDataSource;
        this.dbType = dbType;
    }

    public Connection getConnection(SQLStatement statement) throws SQLException {
        if (statement instanceof SQLInsertStatement) {
            String tableName = ((PGInsertStatement) statement).getTableName().getSimpleName();
            return getConnection(tableName, true);
        } else if (statement instanceof SQLUpdateStatement) {
            String tableName = ((SQLUpdateStatement) statement).getTableName().getSimpleName();
            return getConnection(tableName, true);
        } else if (statement instanceof SQLDeleteStatement) {
            String tableName = ((SQLDeleteStatement) statement).getTableName().getSimpleName();
            return getConnection(tableName, true);
        } else if (statement instanceof SQLSelectStatement) {
            PGSelectQueryBlock query = (PGSelectQueryBlock) ((PGSelectStatement) statement).getSelect().getQuery();
            SQLTableSource sqlTableSource = query.getFrom();
            if (null == sqlTableSource) {
                return getConnection(DefaultName, true);
            } else {
                Set<String> tables = new HashSet<>();
                parseTable(sqlTableSource, tables);
                for (String table : tables) {
                    if (transactionTable.contains(table)) {
                        return getConnection(DefaultName, true);
                    }
                }
                return getConnection(DefaultName, false);
            }
        } else {
            return getConnection(DefaultName, true);
        }
    }

    private void parseTable(SQLTableSource sqlTableSource, Set<String> tables) {
        if (sqlTableSource instanceof SQLExprTableSource) {
            SQLExpr expr = ((SQLExprTableSource) sqlTableSource).getExpr();
            if (expr instanceof SQLIdentifierExpr) {
                SQLIdentifierExpr identifierExpr = (SQLIdentifierExpr) expr;
                String tableName = identifierExpr.getName();
                tables.add(tableName);
            } else {
                parseSQLExpr(expr, tables);
            }
        } else if (sqlTableSource instanceof SQLJoinTableSource) {
            SQLJoinTableSource joinTableSource = (SQLJoinTableSource) sqlTableSource;
            parseTable(joinTableSource.getLeft(), tables);
            parseTable(joinTableSource.getRight(), tables);
        } else if (sqlTableSource instanceof SQLSubqueryTableSource) {
            SQLSelectQueryBlock queryBlock = (SQLSelectQueryBlock) ((SQLSubqueryTableSource) sqlTableSource).getSelect().getQuery();
            parseTable(queryBlock.getFrom(), tables);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private void parseSQLExpr(SQLExpr expr, Set<String> tables) {
        if (expr instanceof SQLQueryExpr) {
            SQLSelect subQuery = ((SQLQueryExpr) expr).getSubQuery();
            SQLSelectQueryBlock selectQuery = (SQLSelectQueryBlock) subQuery.getQuery();
            parseTable(selectQuery.getFrom(), tables);
        }
    }

    public Connection getMasterConnection() throws SQLException {
        return getConnection(DefaultName, true);
    }

    private Connection getConnection(String tableName, boolean master) throws SQLException {
        if (transactionTable.contains(tableName)) {
            return masterConnection;
        }
        Connection connection;
        if (master) {
            transactionTable.add(tableName);
            if (null == masterConnection) {
                masterConnection = routeDataSource.getMasterConnection();
                if (null != autoCommit) {
                    masterConnection.setAutoCommit(autoCommit);
                }
            }
            connection = masterConnection;
        } else {
            if (null == slaveConnection) {
                slaveConnection = routeDataSource.getSlaveConnection();
                if (null != autoCommit) {
                    slaveConnection.setAutoCommit(autoCommit);
                }
            }
            connection = slaveConnection;
        }
        return connection;
    }

    public void commit() throws SQLException {
        if (null != masterConnection) {
            masterConnection.commit();
        }
    }

    public void rollback() throws SQLException {
        if (null != masterConnection) {
            masterConnection.rollback();
        }
    }

    public void close() {
        if (null != masterConnection) {
            try {
                masterConnection.close();
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (null != slaveConnection) {
            try {
                slaveConnection.close();
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }
        closed = true;
    }
}

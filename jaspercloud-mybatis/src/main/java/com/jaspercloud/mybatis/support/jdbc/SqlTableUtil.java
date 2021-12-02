package com.jaspercloud.mybatis.support.jdbc;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.ast.statement.SQLUnionQueryTableSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SqlTableUtil {

    private SqlTableUtil() {

    }

    public static Set<String> parseSql(String sql, DbType dbType) {
        SQLStatement statement = SQLUtils.parseSingleStatement(sql, dbType);
        if (statement instanceof SQLSelectStatement) {
            return parseSelectStatement((SQLSelectStatement) statement);
        }
        throw new UnsupportedOperationException();
    }

    public static Set<String> parseSelectStatement(SQLSelectStatement statement) {
        Set<String> tables = new HashSet<>();
        SQLSelectQueryBlock query = (SQLSelectQueryBlock) statement.getSelect().getQuery();
        SQLTableSource sqlTableSource = query.getFrom();
        if (null != sqlTableSource) {
            parseTable(sqlTableSource, tables);
        }
        return tables;
    }

    private static void parseTable(SQLTableSource sqlTableSource, Set<String> tables) {
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
        } else if (sqlTableSource instanceof SQLUnionQueryTableSource) {
            SQLUnionQueryTableSource unionQueryTableSource = (SQLUnionQueryTableSource) sqlTableSource;
            SQLUnionQuery unionQuery = unionQueryTableSource.getUnion();
            List<SQLSelectQuery> relations = unionQuery.getRelations();
            for (SQLSelectQuery selectQuery : relations) {
                SQLSelectQueryBlock queryBlock = (SQLSelectQueryBlock) selectQuery;
                parseTable(queryBlock.getFrom(), tables);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private static void parseSQLExpr(SQLExpr expr, Set<String> tables) {
        if (expr instanceof SQLQueryExpr) {
            SQLSelect subQuery = ((SQLQueryExpr) expr).getSubQuery();
            SQLSelectQueryBlock selectQuery = (SQLSelectQueryBlock) subQuery.getQuery();
            parseTable(selectQuery.getFrom(), tables);
        }
    }

}

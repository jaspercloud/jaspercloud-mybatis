package com.jaspercloud.mybatis.support.jdbc;

import com.alibaba.druid.sql.ast.SQLStatement;

import java.sql.Statement;

public class ExecuteStatement<T extends Statement> {

    private SQLStatement sqlStatement;
    private T statement;

    public SQLStatement getSqlStatement() {
        return sqlStatement;
    }

    public T getStatement() {
        return statement;
    }

    public ExecuteStatement(SQLStatement sqlStatement, T statement) {
        this.sqlStatement = sqlStatement;
        this.statement = statement;
    }
}

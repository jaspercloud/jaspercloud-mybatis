package com.jaspercloud.mybatis.support.jdbc;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLFunction<T, R> {

    R apply(T t) throws SQLException;
}

package com.jaspercloud.mybatis.support.plus.annotation;

import org.apache.ibatis.mapping.StatementType;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SelectKey {

    String statement();

    boolean before();

    StatementType statementType() default StatementType.PREPARED;
}

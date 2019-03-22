package com.jaspercloud.plus.annotation;

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

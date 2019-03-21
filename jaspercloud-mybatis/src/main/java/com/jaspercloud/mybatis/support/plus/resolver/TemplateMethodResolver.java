package com.jaspercloud.mybatis.support.plus.resolver;

import com.jaspercloud.mybatis.support.plus.JasperMybatisConfiguration;

import java.lang.reflect.Method;

public interface TemplateMethodResolver {

    void resolver(JasperMybatisConfiguration config, Class<?> type, Class<?> modelClass, Method method);
}

package com.jaspercloud.plus.resolver;

import com.jaspercloud.plus.JasperMybatisConfiguration;

import java.lang.reflect.Method;

public interface TemplateMethodResolver {

    void resolver(JasperMybatisConfiguration config, Class<?> type, Class<?> modelClass, Method method);
}

package com.jaspercloud.mybatis.autoconfigure;

import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class JasperCloudMybatisAutoConfigurationFilter implements AutoConfigurationImportFilter {

    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
        boolean[] booleans = new boolean[autoConfigurationClasses.length];
        for (int i = 0; i < autoConfigurationClasses.length; i++) {
            String clazz = autoConfigurationClasses[i];
            if (MybatisAutoConfiguration.class.getName().equals(clazz)) {
                booleans[i] = false;
            } else {
                booleans[i] = true;
            }
        }
        return booleans;
    }
}

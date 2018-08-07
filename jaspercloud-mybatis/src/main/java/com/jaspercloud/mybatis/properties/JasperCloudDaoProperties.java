package com.jaspercloud.mybatis.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by TimoRD on 2017/11/24.
 */
@ConfigurationProperties(prefix = "jaspercloud.dao")
public class JasperCloudDaoProperties {

    public static final String BeanName = "jaspercloud.dao-" + JasperCloudDaoProperties.class.getName();

    private Map<String, DataSourceProperties> datasource = new LinkedHashMap<>();
    private Map<String, MybatisProperties> mybatis = new LinkedHashMap<>();

    public Map<String, DataSourceProperties> getDatasource() {
        return datasource;
    }

    public void setDatasource(Map<String, DataSourceProperties> datasource) {
        this.datasource = datasource;
    }

    public Map<String, MybatisProperties> getMybatis() {
        return mybatis;
    }

    public void setMybatis(Map<String, MybatisProperties> mybatis) {
        this.mybatis = mybatis;
    }

    public JasperCloudDaoProperties() {
    }
}

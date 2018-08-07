package com.jaspercloud.mybatis.support;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.jaspercloud.mybatis.properties.JasperCloudDaoProperties;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Created by TimoRD on 2017/9/7.
 */
public class JasperCloudDataSourceFactoryBean implements InitializingBean, FactoryBean<DataSource> {

    private String name;
    private JasperCloudDaoProperties jasperCloudDaoProperties;
    private DataSource dataSource;

    public void setJasperCloudDaoProperties(JasperCloudDaoProperties jasperCloudDaoProperties) {
        this.jasperCloudDaoProperties = jasperCloudDaoProperties;
    }

    public JasperCloudDataSourceFactoryBean(String name) {
        this.name = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, String> map = jasperCloudDaoProperties.getDatasource().get(name).toMap();
        dataSource = DruidDataSourceFactory.createDataSource(map);
    }

    @Override
    public DataSource getObject() throws Exception {
        return dataSource;
    }

    @Override
    public Class<?> getObjectType() {
        return DataSource.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}

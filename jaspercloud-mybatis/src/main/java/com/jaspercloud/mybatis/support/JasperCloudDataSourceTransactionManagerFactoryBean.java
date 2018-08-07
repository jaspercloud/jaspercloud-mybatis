package com.jaspercloud.mybatis.support;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * Created by TimoRD on 2017/9/8.
 */
public class JasperCloudDataSourceTransactionManagerFactoryBean implements InitializingBean, FactoryBean<DataSourceTransactionManager> {

    private String name;
    private DataSource dataSource;
    private DataSourceTransactionManager dataSourceTransactionManager;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public JasperCloudDataSourceTransactionManagerFactoryBean(String name) {
        this.name = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
        dataSourceTransactionManager.afterPropertiesSet();
    }

    @Override
    public DataSourceTransactionManager getObject() throws Exception {
        return dataSourceTransactionManager;
    }

    @Override
    public Class<?> getObjectType() {
        return DataSourceTransactionManager.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}

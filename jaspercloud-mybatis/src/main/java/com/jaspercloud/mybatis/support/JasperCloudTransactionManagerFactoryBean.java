package com.jaspercloud.mybatis.support;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * Created by TimoRD on 2017/9/8.
 */
public class JasperCloudTransactionManagerFactoryBean implements InitializingBean, FactoryBean<PlatformTransactionManager> {

    private String name;
    private DataSource dataSource;
    private DataSourceTransactionManager dataSourceTransactionManager;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public JasperCloudTransactionManagerFactoryBean(String name) {
        this.name = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
        dataSourceTransactionManager.afterPropertiesSet();
    }

    @Override
    public PlatformTransactionManager getObject() throws Exception {
        return dataSourceTransactionManager;
    }

    @Override
    public Class<?> getObjectType() {
        return PlatformTransactionManager.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}

package com.jaspercloud.mybatis.support;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public class JasperCloudTransactionTemplateFactoryBean implements FactoryBean<TransactionTemplate>, InitializingBean {

    private String name;
    private PlatformTransactionManager transactionManager;
    private TransactionTemplate transactionTemplate;

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public JasperCloudTransactionTemplateFactoryBean(String name) {
        this.name = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.afterPropertiesSet();
    }

    @Override
    public TransactionTemplate getObject() throws Exception {
        return transactionTemplate;
    }

    @Override
    public Class<?> getObjectType() {
        return TransactionTemplate.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}

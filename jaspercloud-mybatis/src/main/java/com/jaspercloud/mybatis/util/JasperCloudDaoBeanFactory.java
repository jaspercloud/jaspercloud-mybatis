package com.jaspercloud.mybatis.util;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.Ordered;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * Created by TimoRD on 2017/9/23.
 */
public final class JasperCloudDaoBeanFactory implements BeanFactoryAware, Ordered {

    private static BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public <T> T getBean(Class<T> clazz) {
        T bean = getBean(clazz, null);
        return bean;
    }

    public <T> T getBean(Class<T> clazz, String name) {
        if (clazz.isAssignableFrom(DataSource.class)) {
            T bean = beanFactory.getBean(name + "DataSource", clazz);
            return bean;
        } else if (clazz.isAssignableFrom(SqlSessionFactory.class)) {
            T bean = beanFactory.getBean(name + "SqlSessionFactory", clazz);
            return bean;
        } else if (clazz.isAssignableFrom(PlatformTransactionManager.class)) {
            T bean = beanFactory.getBean(name, clazz);
            return bean;
        } else if (null != name) {
            T bean = beanFactory.getBean(name, clazz);
            return bean;
        } else {
            T bean = beanFactory.getBean(clazz);
            return bean;
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

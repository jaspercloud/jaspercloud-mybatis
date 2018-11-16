package com.jaspercloud.mybatis.support;

import com.jaspercloud.mybatis.properties.JasperCloudDaoProperties;
import com.jaspercloud.mybatis.properties.MybatisProperties;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Created by TimoRD on 2017/9/8.
 */
public class JasperCloudSqlSessionTemplateFactoryBean implements InitializingBean, FactoryBean<SqlSessionTemplate> {

    private String name;
    private JasperCloudDaoProperties jasperCloudDaoProperties;
    private SqlSessionFactory sqlSessionFactory;
    private SqlSessionTemplate sqlSessionTemplate;

    public void setJasperCloudDaoProperties(JasperCloudDaoProperties jasperCloudDaoProperties) {
        this.jasperCloudDaoProperties = jasperCloudDaoProperties;
    }

    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public JasperCloudSqlSessionTemplateFactoryBean(String name) {
        this.name = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MybatisProperties mybatisProperties = jasperCloudDaoProperties.getMybatis().get(name);
        if (null != mybatisProperties && null != mybatisProperties.getExecutorType()) {
            ExecutorType executorType = mybatisProperties.getExecutorType();
            sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory, executorType);
        } else {
            sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory);
        }
    }

    @Override
    public SqlSessionTemplate getObject() throws Exception {
        return sqlSessionTemplate;
    }

    @Override
    public Class<?> getObjectType() {
        return SqlSessionTemplate.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}

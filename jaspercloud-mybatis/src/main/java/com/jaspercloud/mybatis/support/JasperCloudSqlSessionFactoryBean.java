package com.jaspercloud.mybatis.support;

import com.jaspercloud.mybatis.autoconfigure.MybatisConfigurationCustomizer;
import com.jaspercloud.mybatis.properties.JasperCloudDaoProperties;
import com.jaspercloud.mybatis.properties.MybatisProperties;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Created by TimoRD on 2017/9/8.
 */
public class JasperCloudSqlSessionFactoryBean implements InitializingBean, ApplicationContextAware, FactoryBean<SqlSessionFactory> {

    private String name;
    private ApplicationContext applicationContext;
    private DataSource dataSource;
    private JasperCloudDaoProperties jasperCloudDaoProperties;
    private SqlSessionFactory sqlSessionFactory;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setJasperCloudDaoProperties(JasperCloudDaoProperties jasperCloudDaoProperties) {
        this.jasperCloudDaoProperties = jasperCloudDaoProperties;
    }

    public JasperCloudSqlSessionFactoryBean(String name) {
        this.name = name;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        MybatisProperties mybatisProperties = jasperCloudDaoProperties.getMybatis().get(name);
        Configuration configuration = mybatisProperties.getConfiguration();
        if (null == configuration) {
            configuration = new Configuration();
        }
        Map<String, MybatisConfigurationCustomizer> customizerMap = applicationContext.getBeansOfType(MybatisConfigurationCustomizer.class);
        for (MybatisConfigurationCustomizer customizer : customizerMap.values()) {
            customizer.customize(name, configuration);
        }
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setConfiguration(configuration);
        sqlSessionFactoryBean.setVfs(SpringBootVFS.class);
        sqlSessionFactoryBean.setTransactionFactory(new SpringManagedTransactionFactory());

        if (null != mybatisProperties) {
            Resource[] resources = mybatisProperties.resolveMapperLocations();
            sqlSessionFactoryBean.setMapperLocations(resources);
        }

        sqlSessionFactoryBean.afterPropertiesSet();
        sqlSessionFactory = sqlSessionFactoryBean.getObject();
    }

    @Override
    public SqlSessionFactory getObject() throws Exception {
        return sqlSessionFactory;
    }

    @Override
    public Class<?> getObjectType() {
        return SqlSessionFactory.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}

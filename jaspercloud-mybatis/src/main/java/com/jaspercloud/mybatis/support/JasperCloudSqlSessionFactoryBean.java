package com.jaspercloud.mybatis.support;

import com.jaspercloud.mybatis.properties.MybatisProperties;
import com.jaspercloud.mybatis.properties.JasperCloudDaoProperties;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

/**
 * Created by TimoRD on 2017/9/8.
 */
public class JasperCloudSqlSessionFactoryBean implements InitializingBean, FactoryBean<SqlSessionFactory> {

    private String name;
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
    public void afterPropertiesSet() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setVfs(SpringBootVFS.class);

        MybatisProperties mybatisProperties = jasperCloudDaoProperties.getMybatis().get(name);
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

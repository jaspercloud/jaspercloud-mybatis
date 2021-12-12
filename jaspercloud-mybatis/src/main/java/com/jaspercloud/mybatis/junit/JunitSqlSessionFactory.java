package com.jaspercloud.mybatis.junit;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.incrementer.IKeyGenerator;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.core.mapper.Mapper;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.jaspercloud.mybatis.exception.ResourceException;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JunitSqlSessionFactory implements FactoryBean<SqlSessionFactory>, InitializingBean {

    private DataSource dataSource;
    private IKeyGenerator keyGenerator;
    private ISqlInjector sqlInjector;
    private MybatisConfiguration configuration;
    private TransactionFactory transactionFactory;
    private SqlSessionFactory sqlSessionFactory;
    private List<Class<? extends Mapper>> mapperList = new ArrayList<>();
    private List<String> mapperLocationList = new ArrayList<>();

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setKeyGenerator(IKeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    public void setSqlInjector(ISqlInjector sqlInjector) {
        this.sqlInjector = sqlInjector;
    }

    public void setConfiguration(MybatisConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setTransactionFactory(TransactionFactory transactionFactory) {
        this.transactionFactory = transactionFactory;
    }

    public void addMapper(Class<? extends Mapper> clazz) {
        this.mapperList.add(clazz);
    }

    public void addMapperLocation(String location) {
        this.mapperLocationList.add(location);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        GlobalConfig globalConfig = GlobalConfigUtils.defaults();
        if (null != keyGenerator) {
            globalConfig.getDbConfig().setKeyGenerator(keyGenerator);
        }
        globalConfig.setSqlInjector(null == sqlInjector ? new DefaultSqlInjector() : sqlInjector);
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setConfiguration(null == configuration ? new MybatisConfiguration() : configuration);
        factory.setTransactionFactory(null == transactionFactory ? new JdbcTransactionFactory() : transactionFactory);
        factory.setGlobalConfig(globalConfig);
        ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
        factory.setMapperLocations(mapperLocationList.stream()
                .map(mapperLocation -> {
                    try {
                        Resource[] mappers = resourceResolver.getResources(mapperLocation);
                        return Arrays.asList(mappers);
                    } catch (IOException e) {
                        throw new ResourceException(e.getMessage(), e);
                    }
                }).flatMap(e -> e.stream())
                .collect(Collectors.toList()).toArray(new Resource[0]));
        factory.afterPropertiesSet();
        sqlSessionFactory = factory.getObject();
        for (Class<? extends Mapper> mapper : mapperList) {
            factory.getConfiguration().addMapper(mapper);
        }
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

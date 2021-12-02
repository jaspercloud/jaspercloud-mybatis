package com.jaspercloud.mybatis.junit;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.incrementer.IKeyGenerator;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.core.mapper.Mapper;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class JunitSqlSessionFactory implements FactoryBean<SqlSessionFactory>, InitializingBean {

    private DataSource dataSource;
    private IKeyGenerator keyGenerator;
    private ISqlInjector sqlInjector;
    private MybatisConfiguration configuration;
    private TransactionFactory transactionFactory;
    private SqlSessionFactory sqlSessionFactory;
    private List<Class<? extends Mapper>> mapperList = new ArrayList<>();

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

package com.jaspercloud.mybatis.support;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.incrementer.IKeyGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.jaspercloud.mybatis.autoconfigure.MybatisConfigurationCustomizer;
import com.jaspercloud.mybatis.autoconfigure.MybatisConfigurationFactory;
import com.jaspercloud.mybatis.properties.JasperCloudDaoProperties;
import com.jaspercloud.mybatis.properties.MybatisProperties;
import com.jaspercloud.mybatis.support.table.TableKeyGenerator;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.net.URI;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by TimoRD on 2017/9/8.
 */
public class JasperCloudSqlSessionFactoryBean implements InitializingBean, ApplicationContextAware, ResourceLoaderAware, FactoryBean<SqlSessionFactory> {

    private String name;
    private Interceptor[] interceptors;
    private TypeHandler[] typeHandlers;
    private LanguageDriver[] languageDrivers;
    private ApplicationContext applicationContext;
    private ResourceLoader resourceLoader;
    private DataSource dataSource;
    private JasperCloudDaoProperties jasperCloudDaoProperties;
    private SqlSessionFactory sqlSessionFactory;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setJasperCloudDaoProperties(JasperCloudDaoProperties jasperCloudDaoProperties) {
        this.jasperCloudDaoProperties = jasperCloudDaoProperties;
    }

    public JasperCloudSqlSessionFactoryBean(String name,
                                            ObjectProvider<Interceptor[]> interceptorsProvider,
                                            ObjectProvider<TypeHandler[]> typeHandlersProvider,
                                            ObjectProvider<LanguageDriver[]> languageDriversProvider) {
        this.name = name;
        this.interceptors = interceptorsProvider.getIfAvailable();
        this.typeHandlers = typeHandlersProvider.getIfAvailable();
        this.languageDrivers = languageDriversProvider.getIfAvailable();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MybatisProperties properties = jasperCloudDaoProperties.getMybatis().get(name);
        MybatisConfiguration configuration = createConfiguration();
        Map<String, MybatisConfigurationCustomizer> customizerMap = applicationContext.getBeansOfType(MybatisConfigurationCustomizer.class);
        for (MybatisConfigurationCustomizer customizer : customizerMap.values()) {
            customizer.customize(name, configuration);
        }

        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setConfiguration(configuration);
        factory.setTransactionFactory(new SpringManagedTransactionFactory());
        factory.setVfs(SpringBootVFS.class);
        if (StringUtils.hasText(properties.getConfigLocation())) {
            factory.setConfigLocation(resourceLoader.getResource(properties.getConfigLocation()));
        }
        if (properties.getConfigurationProperties() != null) {
            factory.setConfigurationProperties(properties.getConfigurationProperties());
        }
        if (StringUtils.hasLength(properties.getTypeAliasesPackage())) {
            factory.setTypeAliasesPackage(properties.getTypeAliasesPackage());
        }
        if (StringUtils.hasLength(properties.getTypeHandlersPackage())) {
            factory.setTypeHandlersPackage(properties.getTypeHandlersPackage());
        }
        if (!ObjectUtils.isEmpty(properties.resolveMapperLocations())) {
            factory.setMapperLocations(properties.resolveMapperLocations());
        }
        if (!ObjectUtils.isEmpty(interceptors)) {
            factory.setPlugins(interceptors);
        }
        if (!ObjectUtils.isEmpty(typeHandlers)) {
            factory.setTypeHandlers(typeHandlers);
        }
        if (!ObjectUtils.isEmpty(languageDrivers)) {
            factory.setScriptingLanguageDrivers(languageDrivers);
        }
        //此处必为非 NULL
        GlobalConfig globalConfig = GlobalConfigUtils.defaults();
        //注入填充器
        getBeanThen(MetaObjectHandler.class, globalConfig::setMetaObjectHandler);
        //注入主键生成器
        getBeanThen(TableKeyGenerator.class, gen -> {
            String url = jasperCloudDaoProperties.getDatasource().get(name).getUrl().replaceAll("^jdbc:", "");
            String scheme = URI.create(url).getScheme();
            IKeyGenerator generator = gen.getGenerator(scheme + "KeyGenerator");
            if (null == generator) {
                throw new IllegalArgumentException(scheme);
            }
            globalConfig.getDbConfig().setKeyGenerator(generator);
        });
        //注入sql注入器
        getBeanThen(ISqlInjector.class, globalConfig::setSqlInjector);
        //注入ID生成器
        getBeanThen(IdentifierGenerator.class, globalConfig::setIdentifierGenerator);
        //设置 GlobalConfig 到 MybatisSqlSessionFactoryBean
        factory.setGlobalConfig(globalConfig);
        sqlSessionFactory = factory.getObject();
    }

    /**
     * 检查spring容器里是否有对应的bean,有则进行消费
     *
     * @param clazz    class
     * @param consumer 消费
     * @param <T>      泛型
     */
    private <T> void getBeanThen(Class<T> clazz, Consumer<T> consumer) {
        if (applicationContext.getBeanNamesForType(clazz, false, false).length > 0) {
            consumer.accept(applicationContext.getBean(clazz));
        }
    }

    private MybatisConfiguration createConfiguration() {
        try {
            MybatisConfigurationFactory factory = applicationContext.getBean(MybatisConfigurationFactory.class);
            MybatisConfiguration configuration = factory.create();
            return configuration;
        } catch (NoSuchBeanDefinitionException e) {
            return new MybatisConfiguration();
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

package com.jaspercloud.mybatis.autoconfigure;

import com.jaspercloud.mybatis.properties.JasperCloudDaoProperties;
import com.jaspercloud.mybatis.support.JasperCloudDataSourceFactoryBean;
import com.jaspercloud.mybatis.support.JasperCloudDataSourceTransactionManagerFactoryBean;
import com.jaspercloud.mybatis.support.JasperCloudSqlSessionFactoryBean;
import com.jaspercloud.mybatis.util.JasperCloudDaoBeanFactory;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

/**
 * Created by TimoRD on 2018/2/3.
 */
public class JasperCloudMybatisBeanDefinitionRegistry implements EnvironmentAware, BeanDefinitionRegistryPostProcessor, Ordered {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        String[] names = environment.getProperty("jaspercloud.db.names", new String[]{}.getClass());
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            boolean primary = (0 == i) ? true : false;
            registerJasperCloudDataSourceFactoryBean(registry, name, primary);
            registerJasperCloudSqlSessionFactoryBean(registry, name, primary);
            registerJasperCloudDataSourceTransactionManagerFactoryBean(registry, name, primary);
            registerMapperScannerConfigurer(registry, name, primary);
        }
        registerJasperCloudDaoBeanFactory(registry);
    }

    private void registerMapperScannerConfigurer(BeanDefinitionRegistry registry, String name, boolean primary) {
        String beanName = name + "MapperScannerConfigurer";
        String basePackages = environment.getProperty(String.format("jaspercloud.dao.mybatis.%s.basePackages", name), String.class);
        if (null == basePackages) {
            return;
        }
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
        definitionBuilder.addPropertyValue("sqlSessionFactoryBeanName", name + "SqlSessionFactory");
        definitionBuilder.addPropertyValue("basePackage", basePackages);
        AbstractBeanDefinition beanDefinition = definitionBuilder.getBeanDefinition();
        registry.registerBeanDefinition(beanName, beanDefinition);
    }

    private void registerJasperCloudDataSourceTransactionManagerFactoryBean(BeanDefinitionRegistry registry, String name, boolean primary) {
        String beanName = name + "DataSourceTransactionManager";
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(JasperCloudDataSourceTransactionManagerFactoryBean.class);
        definitionBuilder.addConstructorArgValue(name);
        definitionBuilder.addPropertyReference("dataSource", name + "DataSource");
        AbstractBeanDefinition beanDefinition = definitionBuilder.getBeanDefinition();
        beanDefinition.setPrimary(primary);
        registry.registerBeanDefinition(beanName, beanDefinition);
        registry.registerAlias(beanName, name);
    }

    private void registerJasperCloudSqlSessionFactoryBean(BeanDefinitionRegistry registry, String name, boolean primary) {
        String beanName = name + "SqlSessionFactory";
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(JasperCloudSqlSessionFactoryBean.class);
        definitionBuilder.addConstructorArgValue(name);
        definitionBuilder.addPropertyReference("dataSource", name + "DataSource");
        definitionBuilder.addPropertyReference("jasperCloudDaoProperties", JasperCloudDaoProperties.BeanName);
        AbstractBeanDefinition beanDefinition = definitionBuilder.getBeanDefinition();
        beanDefinition.setPrimary(primary);
        registry.registerBeanDefinition(beanName, beanDefinition);
    }

    private void registerJasperCloudDataSourceFactoryBean(BeanDefinitionRegistry registry, String name, boolean primary) {
        {
            String beanName = name + "DataSource";
            BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(JasperCloudDataSourceFactoryBean.class);
            definitionBuilder.addConstructorArgValue(name);
            definitionBuilder.addPropertyReference("jasperCloudDaoProperties", JasperCloudDaoProperties.BeanName);
            AbstractBeanDefinition beanDefinition = definitionBuilder.getBeanDefinition();
            registry.registerBeanDefinition(beanName, beanDefinition);
        }
        if (primary) {
            BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(JasperCloudDataSourceFactoryBean.class);
            definitionBuilder.addConstructorArgValue(name);
            definitionBuilder.addPropertyReference("jasperCloudDaoProperties", JasperCloudDaoProperties.BeanName);
            AbstractBeanDefinition beanDefinition = definitionBuilder.getBeanDefinition();
            beanDefinition.setPrimary(primary);
            registry.registerBeanDefinition("dataSource", beanDefinition);
        }
    }

    private void registerJasperCloudDaoBeanFactory(BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(JasperCloudDaoBeanFactory.class);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        registry.registerBeanDefinition("jasperCloudDaoBeanFactory", beanDefinition);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}

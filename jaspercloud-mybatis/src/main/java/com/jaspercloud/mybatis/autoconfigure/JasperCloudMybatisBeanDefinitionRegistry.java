package com.jaspercloud.mybatis.autoconfigure;

import com.jaspercloud.mybatis.properties.JasperCloudDaoProperties;
import com.jaspercloud.mybatis.support.JasperCloudDataSourceFactoryBean;
import com.jaspercloud.mybatis.support.JasperCloudSqlSessionFactoryBean;
import com.jaspercloud.mybatis.support.JasperCloudSqlSessionTemplateFactoryBean;
import com.jaspercloud.mybatis.support.JasperCloudTransactionManagerFactoryBean;
import com.jaspercloud.mybatis.support.JasperCloudTransactionTemplateFactoryBean;
import com.jaspercloud.mybatis.util.JasperCloudDaoBeanFactory;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Created by TimoRD on 2018/2/3.
 */
public class JasperCloudMybatisBeanDefinitionRegistry implements ApplicationContextAware, EnvironmentAware, ImportBeanDefinitionRegistrar, Ordered {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private ApplicationContext applicationContext;
    private Environment environment;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        String[] names = environment.getProperty("spring.jaspercloud.db.names", new String[]{}.getClass());
        if (null == names || names.length <= 0) {
            logger.info("disable JasperCloudMybatis");
            return;
        }
        logger.info("enable JasperCloudMybatis");
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            boolean primary = (0 == i) ? true : false;
            registerJasperCloudDataSourceFactoryBean(registry, name, primary);
            registerJasperCloudSqlSessionFactoryBean(registry, name, primary);
            registerJasperCloudSqlSessionTemplateFactoryBean(registry, name, primary);
            registerJasperCloudDataSourceTransactionManagerFactoryBean(registry, name, primary);
            registerJasperCloudTransactionTemplateFactoryBean(registry, name, primary);
            registerMapperScannerConfigurer(registry, name, primary);
        }
        registerJasperCloudDaoBeanFactory(registry);
    }

    private void registerMapperScannerConfigurer(BeanDefinitionRegistry registry, String name, boolean primary) {
        String beanName = name + "MapperScannerConfigurer";
        String basePackages = environment.getProperty(String.format("spring.jaspercloud.mybatis.%s.basePackages", name), String.class);
        if (null == basePackages) {
            return;
        }
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
        definitionBuilder.addPropertyValue("annotationClass", Mapper.class);
        definitionBuilder.addPropertyValue("sqlSessionFactoryBeanName", name + "SqlSessionFactory");
        definitionBuilder.addPropertyValue("sqlSessionTemplateBeanName", name + "SqlSessionTemplate");
        definitionBuilder.addPropertyValue("basePackage", basePackages);
        AbstractBeanDefinition beanDefinition = definitionBuilder.getBeanDefinition();
        registry.registerBeanDefinition(beanName, beanDefinition);
    }

    private void registerJasperCloudDataSourceTransactionManagerFactoryBean(BeanDefinitionRegistry registry, String name, boolean primary) {
        String beanName = name + "DataSourceTransactionManager";
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(JasperCloudTransactionManagerFactoryBean.class);
        definitionBuilder.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR);
        definitionBuilder.addConstructorArgValue(name);
        definitionBuilder.addPropertyReference("dataSource", name + "DataSource");
        AbstractBeanDefinition beanDefinition = definitionBuilder.getBeanDefinition();
        beanDefinition.setPrimary(primary);
        registry.registerBeanDefinition(beanName, beanDefinition);
        registry.registerAlias(beanName, name);
    }

    private void registerJasperCloudTransactionTemplateFactoryBean(BeanDefinitionRegistry registry, String name, boolean primary) {
        String beanName = name + "TransactionTemplate";
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(JasperCloudTransactionTemplateFactoryBean.class);
        definitionBuilder.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR);
        definitionBuilder.addConstructorArgValue(name);
        definitionBuilder.addPropertyReference("transactionManager", name + "DataSourceTransactionManager");
        AbstractBeanDefinition beanDefinition = definitionBuilder.getBeanDefinition();
        beanDefinition.setPrimary(primary);
        registry.registerBeanDefinition(beanName, beanDefinition);
    }

    private void registerJasperCloudSqlSessionFactoryBean(BeanDefinitionRegistry registry, String name, boolean primary) {
        String beanName = name + "SqlSessionFactory";
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(JasperCloudSqlSessionFactoryBean.class);
        definitionBuilder.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR);
        definitionBuilder.addConstructorArgValue(name);
        definitionBuilder.addPropertyReference("dataSource", name + "DataSource");
        definitionBuilder.addPropertyReference("jasperCloudDaoProperties", JasperCloudDaoProperties.BeanName);
        AbstractBeanDefinition beanDefinition = definitionBuilder.getBeanDefinition();
        beanDefinition.setPrimary(primary);
        registry.registerBeanDefinition(beanName, beanDefinition);
    }

    private void registerJasperCloudSqlSessionTemplateFactoryBean(BeanDefinitionRegistry registry, String name, boolean primary) {
        String beanName = name + "SqlSessionTemplate";
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(JasperCloudSqlSessionTemplateFactoryBean.class);
        definitionBuilder.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR);
        definitionBuilder.addConstructorArgValue(name);
        definitionBuilder.addPropertyReference("sqlSessionFactory", name + "SqlSessionFactory");
        definitionBuilder.addPropertyReference("jasperCloudDaoProperties", JasperCloudDaoProperties.BeanName);
        AbstractBeanDefinition beanDefinition = definitionBuilder.getBeanDefinition();
        beanDefinition.setPrimary(primary);
        registry.registerBeanDefinition(beanName, beanDefinition);

    }

    private void registerJasperCloudDataSourceFactoryBean(BeanDefinitionRegistry registry, String name, boolean primary) {
        {
            String beanName = name + "DataSource";
            BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(JasperCloudDataSourceFactoryBean.class);
            definitionBuilder.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR);
            definitionBuilder.addConstructorArgValue(name);
            definitionBuilder.addPropertyReference("jasperCloudDaoProperties", JasperCloudDaoProperties.BeanName);
            definitionBuilder.addPropertyReference("ddlExecuter", "ddlExecuter");
            AbstractBeanDefinition beanDefinition = definitionBuilder.getBeanDefinition();
            registry.registerBeanDefinition(beanName, beanDefinition);
        }
        if (primary) {
            BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(JasperCloudDataSourceFactoryBean.class);
            definitionBuilder.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR);
            definitionBuilder.addConstructorArgValue(name);
            definitionBuilder.addPropertyReference("jasperCloudDaoProperties", JasperCloudDaoProperties.BeanName);
            definitionBuilder.addPropertyReference("ddlExecuter", "ddlExecuter");
            AbstractBeanDefinition beanDefinition = definitionBuilder.getBeanDefinition();
            beanDefinition.setPrimary(primary);
            registry.registerBeanDefinition("dataSource", beanDefinition);
        }
    }

    private void registerJasperCloudDaoBeanFactory(BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(JasperCloudDaoBeanFactory.class);
        builder.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        registry.registerBeanDefinition("jasperCloudDaoBeanFactory", beanDefinition);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}

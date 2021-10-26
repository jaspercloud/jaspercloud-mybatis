package com.jaspercloud.mybatis.autoconfigure;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.incrementer.IKeyGenerator;
import com.baomidou.mybatisplus.extension.incrementer.H2KeyGenerator;
import com.baomidou.mybatisplus.extension.incrementer.PostgreKeyGenerator;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.jaspercloud.mybatis.properties.JasperCloudDaoProperties;
import com.jaspercloud.mybatis.support.ddl.DdlExecuter;
import com.jaspercloud.mybatis.support.ddl.DdlMigrateScanner;
import com.jaspercloud.mybatis.support.plugin.StatementInterceptor;
import com.jaspercloud.mybatis.support.table.TableKeyGenerator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Map;


/**
 * Created by TimoRD on 2017/9/7.
 */
@EnableConfigurationProperties(JasperCloudDaoProperties.class)
@Import(JasperCloudMybatisBeanDefinitionRegistry.class)
@EnableTransactionManagement
@Configuration
public class JasperCloudMybatisAutoConfiguration {

    @Bean
    public DdlMigrateScanner ddlMigrateScanner() {
        return new DdlMigrateScanner();
    }

    @Bean
    public DdlExecuter ddlExecuter() {
        return new DdlExecuter();
    }

    @Bean
    @ConditionalOnMissingBean
    public MybatisConfigurationFactory mybatisConfigurationFactory() {
        return new MybatisConfigurationFactory() {
            @Override
            public MybatisConfiguration create() {
                return new MybatisConfiguration();
            }
        };
    }

    @Bean
    public StatementInterceptor statementInterceptor() {
        return new StatementInterceptor();
    }

    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

    @Bean
    public TableKeyGenerator tableKeyGenerator(ObjectProvider<Map<String, IKeyGenerator>> provider) {
        return new TableKeyGenerator(provider.getIfAvailable());
    }

    @ConditionalOnClass(name = "org.postgresql.Driver")
    @Bean(name = "postgresqlKeyGenerator")
    public PostgreKeyGenerator postgreKeyGenerator() {
        return new PostgreKeyGenerator();
    }

    @ConditionalOnClass(name = "org.h2.Driver")
    @Bean(name = "h2KeyGenerator")
    public H2KeyGenerator h2KeyGenerator() {
        return new H2KeyGenerator();
    }
}

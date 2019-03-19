package com.jaspercloud.mybatis.autoconfigure;

import com.jaspercloud.mybatis.properties.JasperCloudDaoProperties;
import com.jaspercloud.mybatis.support.ddl.DdlExecuter;
import com.jaspercloud.mybatis.support.ddl.DdlMigrateScanner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;


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
}

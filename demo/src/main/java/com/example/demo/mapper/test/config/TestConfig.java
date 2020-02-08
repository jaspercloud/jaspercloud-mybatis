package com.example.demo.mapper.test.config;

import com.baomidou.mybatisplus.extension.incrementer.PostgreKeyGenerator;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class TestConfig {

    //    @Bean
//    public MybatisConfigurationCustomizer mybatisConfigurationCustomizer() {
//        return new MybatisConfigurationCustomizer() {
//            @Override
//            public void customize(String name, JasperMybatisConfiguration config) {
//                config.setMapUnderscoreToCamelCase(true);
//            }
//        };
//    }

    @Bean
    public PostgreKeyGenerator postgreKeyGenerator() {
        return new PostgreKeyGenerator();
    }
}

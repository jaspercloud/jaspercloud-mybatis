package com.jaspercloud.plus.autoconfigure;

import com.jaspercloud.mybatis.autoconfigure.JasperCloudMybatisAutoConfiguration;
import com.jaspercloud.mybatis.autoconfigure.MybatisConfigurationFactory;
import com.jaspercloud.plus.JasperMybatisConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Created by TimoRD on 2017/9/7.
 */
@Configuration
@AutoConfigureBefore(JasperCloudMybatisAutoConfiguration.class)
public class JasperCloudMybatisPlusAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MybatisConfigurationFactory mybatisConfigurationFactory() {
        return new MybatisConfigurationFactory() {
            @Override
            public org.apache.ibatis.session.Configuration create() {
                return new JasperMybatisConfiguration();
            }
        };
    }
}

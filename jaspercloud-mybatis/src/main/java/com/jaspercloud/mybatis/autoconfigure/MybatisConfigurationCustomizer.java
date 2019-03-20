package com.jaspercloud.mybatis.autoconfigure;

import org.apache.ibatis.session.Configuration;

public interface MybatisConfigurationCustomizer {

    void customize(String name, Configuration configuration);
}

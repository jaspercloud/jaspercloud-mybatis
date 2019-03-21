package com.jaspercloud.mybatis.autoconfigure;

import org.apache.ibatis.session.Configuration;

public interface MybatisConfigurationFactory {

    Configuration create();
}

package com.jaspercloud.mybatis.support.ddl;

import org.springframework.jdbc.core.JdbcTemplate;

public interface DdlMigrate {

    MigrateInfo getMigrateInfo();

    void execute(JdbcTemplate jdbcTemplate);
}

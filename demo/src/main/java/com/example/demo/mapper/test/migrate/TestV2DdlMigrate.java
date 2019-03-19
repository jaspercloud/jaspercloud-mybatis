package com.example.demo.mapper.test.migrate;

import com.jaspercloud.mybatis.support.ddl.DdlMigrate;
import com.jaspercloud.mybatis.support.ddl.MigrateInfo;
import org.springframework.jdbc.core.JdbcTemplate;

public class TestV2DdlMigrate implements DdlMigrate {

    @Override
    public MigrateInfo getMigrateInfo() {
        return new MigrateInfo("test", 2);
    }

    @Override
    public void execute(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("insert into test (id,content) values (?,?)", new Object[]{1, "test"});
    }
}

package com.jaspercloud.mybatis.support.ddl;

import org.springframework.jdbc.core.JdbcTemplate;

public class DdlMigrateSql implements DdlMigrate {

    private MigrateInfo migrateInfo;
    private String sql;

    public DdlMigrateSql(MigrateInfo migrateInfo, String sql) {
        this.migrateInfo = migrateInfo;
        this.sql = sql;
    }

    @Override
    public MigrateInfo getMigrateInfo() {
        return migrateInfo;
    }

    @Override
    public void execute(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update(sql);
    }
}

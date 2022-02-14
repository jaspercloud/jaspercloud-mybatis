package com.jaspercloud.mybatis.support.ddl;

import com.jaspercloud.mybatis.properties.DatabaseDdlProperties;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DdlExecuter {

    @Autowired
    private DdlMigrateScanner ddlMigrateScanner;

    public DdlExecuter() {
    }

    public void execute(DatabaseDdlProperties properties, DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.afterPropertiesSet();
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        transactionManager.afterPropertiesSet();
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.afterPropertiesSet();
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                doTransaction(properties, jdbcTemplate, status);
            }
        });
    }

    private void doTransaction(DatabaseDdlProperties properties, JdbcTemplate jdbcTemplate, TransactionStatus status) {
        List<DdlMigrate> migrateList = new ArrayList<>();
        List<DdlMigrate> scanClassList = ddlMigrateScanner.scanSQLClass(properties.getClassLocation());
        List<DdlMigrate> scanFileList = ddlMigrateScanner.scanFile(properties.getLocation());
        migrateList.addAll(scanClassList);
        migrateList.addAll(scanFileList);
        checkTable(jdbcTemplate);
        List<DdlMigrate> list;
        if (BooleanUtils.isTrue(properties.getAutoMigrate())) {
            //autoMigrate
            list = migrateList.stream().sorted(new Comparator<DdlMigrate>() {
                @Override
                public int compare(DdlMigrate o1, DdlMigrate o2) {
                    return new Integer(o1.getMigrateInfo().getUpdateVersion()).compareTo(new Integer(o2.getMigrateInfo().getUpdateVersion()));
                }
            }).collect(Collectors.toList());
            list = list.stream().filter(e -> {
                Integer ver = getVersion(jdbcTemplate, properties, e.getMigrateInfo().getUpdateVersion());
                return null == ver;
            }).collect(Collectors.toList());
        } else {
            //migrate from maxVersion
            int maxVersion = getMaxVersion(properties, jdbcTemplate);
            list = migrateList.stream().sorted(new Comparator<DdlMigrate>() {
                @Override
                public int compare(DdlMigrate o1, DdlMigrate o2) {
                    return new Integer(o1.getMigrateInfo().getUpdateVersion()).compareTo(new Integer(o2.getMigrateInfo().getUpdateVersion()));
                }
            }).filter(new Predicate<DdlMigrate>() {
                @Override
                public boolean test(DdlMigrate ddlMigrate) {
                    if (!properties.getName().equals(ddlMigrate.getMigrateInfo().getMigrateName())) {
                        return false;
                    }
                    if (ddlMigrate.getMigrateInfo().getUpdateVersion() <= maxVersion) {
                        return false;
                    }
                    return true;
                }
            }).collect(Collectors.toList());
        }
        list.forEach(new Consumer<DdlMigrate>() {
            @Override
            public void accept(DdlMigrate ddlMigrate) {
                ddlMigrate.execute(jdbcTemplate);
                int version = ddlMigrate.getMigrateInfo().getUpdateVersion();
                updateVersion(properties, jdbcTemplate, version);
            }
        });
    }

    private void updateVersion(DatabaseDdlProperties properties, JdbcTemplate jdbcTemplate, int version) {
        String sql = "insert into ddl_history (name,version,create_time) values (?,?,?)";
        jdbcTemplate.update(sql, new Object[]{properties.getName(), version, new Date()});
    }

    private void checkTable(JdbcTemplate jdbcTemplate) {
        String createTableSql = "create table if not exists ddl_history (" +
                "name varchar(128)," +
                "version int4," +
                "create_time timestamp not null default now()," +
                "unique (name, version)" +
                ")";
        jdbcTemplate.update(createTableSql);
    }

    private Integer getVersion(JdbcTemplate jdbcTemplate, DatabaseDdlProperties properties, int version) {
        try {
            String sql = "select version from ddl_history where name=? and version=?";
            Object[] args = {properties.getName(), version};
            Integer ver = jdbcTemplate.queryForObject(sql, args, Integer.class);
            return ver;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private Integer getMaxVersion(DatabaseDdlProperties properties, JdbcTemplate jdbcTemplate) {
        Integer version;
        try {
            String maxVersionSql = "select max(version) from ddl_history where name=?";
            version = jdbcTemplate.queryForObject(maxVersionSql, new Object[]{properties.getName()}, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            version = 0;
        }
        if (null == version) {
            version = 0;
        }
        return version;
    }
}

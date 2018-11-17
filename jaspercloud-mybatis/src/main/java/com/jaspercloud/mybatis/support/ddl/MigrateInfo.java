package com.jaspercloud.mybatis.support.ddl;

public class MigrateInfo {

    private String migrateName;
    private int updateVersion;

    public String getMigrateName() {
        return migrateName;
    }

    public int getUpdateVersion() {
        return updateVersion;
    }

    public MigrateInfo(String migrateName, int updateVersion) {
        this.migrateName = migrateName;
        this.updateVersion = updateVersion;
    }
}

package com.jaspercloud.mybatis.properties;

public class DatabaseDdlProperties {

    private String name = "ddl";
    private String[] location;
    private String[] classLocation;
    private Boolean autoMigrate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getLocation() {
        return location;
    }

    public void setLocation(String[] location) {
        this.location = location;
    }

    public String[] getClassLocation() {
        return classLocation;
    }

    public void setClassLocation(String[] classLocation) {
        this.classLocation = classLocation;
    }

    public Boolean getAutoMigrate() {
        return autoMigrate;
    }

    public void setAutoMigrate(Boolean autoMigrate) {
        this.autoMigrate = autoMigrate;
    }

    public DatabaseDdlProperties() {
    }
}

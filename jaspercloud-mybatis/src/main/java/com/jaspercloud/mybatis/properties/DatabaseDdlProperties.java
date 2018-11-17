package com.jaspercloud.mybatis.properties;

public class DatabaseDdlProperties {

    private String name = "ddl";
    private String[] location;

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

    public DatabaseDdlProperties() {
    }
}

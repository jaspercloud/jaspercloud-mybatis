package com.jaspercloud.mybatis.support.jdbc;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RouteDataSource extends AbstractDataSource {

    public static final String Master = "master";
    public static final String Slave = "slave";

    private static ThreadLocal<String> threadLocal = new InheritableThreadLocal<>();
    private DataSource master;
    private List<String> slaveLabels;
    private Map<String, DataSource> slaves;

    public RouteDataSource(DataSource master, Map<String, DataSource> slaves) {
        this.master = master;
        this.slaveLabels = new ArrayList<>(slaves.keySet());
        this.slaves = slaves;
    }

    public static void master() {
        threadLocal.set(Master);
    }

    public static void slave() {
        threadLocal.set(Slave);
    }

    public static void remove() {
        threadLocal.remove();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return selectDataSource().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return selectDataSource().getConnection(username, password);
    }

    private DataSource selectDataSource() {
        String key = threadLocal.get();
        if (StringUtils.isEmpty(key)) {
            return master;
        }
        if (StringUtils.equals(key, Master)) {
            return master;
        }
        int rand = RandomUtils.nextInt(0, slaveLabels.size());
        String label = slaveLabels.get(rand);
        DataSource dataSource = slaves.get(label);
        return dataSource;
    }
}

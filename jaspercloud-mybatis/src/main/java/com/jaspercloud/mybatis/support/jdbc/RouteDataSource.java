package com.jaspercloud.mybatis.support.jdbc;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RouteDataSource extends AbstractDataSource {

    private static Logger logger = LoggerFactory.getLogger(RouteDataSource.class);

    private static final String Master = "master";
    private static final String Slave = "slave";

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
        logger.debug("set master");
        threadLocal.set(Master);
    }

    public static void slave() {
        logger.debug("set slave");
        threadLocal.set(Slave);
    }

    public static boolean isSlave() {
        String label = threadLocal.get();
        if (StringUtils.equals(label, Slave)) {
            return true;
        }
        return false;
    }

    public static void remove() {
        threadLocal.remove();
    }

    @Override
    public Connection getConnection() throws SQLException {
        DataSource slave = selectSlaveDataSource();
        return new ProxyConnection(master.getConnection(), null != slave ? slave.getConnection() : null);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        DataSource slave = selectSlaveDataSource();
        return new ProxyConnection(master.getConnection(username, password), null != slave ? slave.getConnection(username, password) : null);
    }

    private DataSource selectSlaveDataSource() {
        if (slaveLabels.isEmpty()) {
            return null;
        }
        int rand = RandomUtils.nextInt(0, slaveLabels.size());
        String label = slaveLabels.get(rand);
        logger.debug("selectSlaveDataSource: label={}", label);
        DataSource dataSource = slaves.get(label);
        return dataSource;
    }
}

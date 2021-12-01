package com.jaspercloud.mybatis.support.jdbc;

import com.alibaba.druid.DbType;
import com.alibaba.druid.pool.DruidDataSource;
import org.apache.commons.lang3.RandomUtils;
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

    private DruidDataSource master;
    private List<String> slaveLabels;
    private Map<String, DataSource> slaves;

    public boolean isDefaultAutoCommit() {
        return master.isDefaultAutoCommit();
    }

    public RouteDataSource(DruidDataSource master, Map<String, DataSource> slaves) {
        this.master = master;
        this.slaveLabels = new ArrayList<>(slaves.keySet());
        this.slaves = slaves;
    }

    public Connection getMasterConnection() throws SQLException {
        return master.getConnection();
    }

    public Connection getSlaveConnection() throws SQLException {
        DataSource slave = selectSlaveDataSource();
        if (null == slave) {
            return master.getConnection();
        }
        return slave.getConnection();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return new ProxyConnection(new ConnectionHolder(this, DbType.postgresql));
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return new ProxyConnection(new ConnectionHolder(this, DbType.postgresql));
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

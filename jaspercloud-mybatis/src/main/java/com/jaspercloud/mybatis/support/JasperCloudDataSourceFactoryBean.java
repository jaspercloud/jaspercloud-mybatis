package com.jaspercloud.mybatis.support;

import com.alibaba.druid.DbType;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.jaspercloud.mybatis.properties.DataSourceProperties;
import com.jaspercloud.mybatis.properties.DatabaseDdlProperties;
import com.jaspercloud.mybatis.properties.JasperCloudDaoProperties;
import com.jaspercloud.mybatis.support.ddl.DdlExecuter;
import com.jaspercloud.mybatis.support.jdbc.RouteDataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by TimoRD on 2017/9/7.
 */
public class JasperCloudDataSourceFactoryBean implements InitializingBean, FactoryBean<DataSource> {

    private String name;
    private JasperCloudDaoProperties jasperCloudDaoProperties;
    private DdlExecuter ddlExecuter;
    private DataSource dataSource;

    public void setJasperCloudDaoProperties(JasperCloudDaoProperties jasperCloudDaoProperties) {
        this.jasperCloudDaoProperties = jasperCloudDaoProperties;
    }

    public void setDdlExecuter(DdlExecuter ddlExecuter) {
        this.ddlExecuter = ddlExecuter;
    }

    public JasperCloudDataSourceFactoryBean(String name) {
        this.name = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //source
        DataSourceProperties dataSourceProperties = jasperCloudDaoProperties.getDatasource().get(name);
        Map<String, String> map = dataSourceProperties.toMap();
        DruidDataSource master = (DruidDataSource) DruidDataSourceFactory.createDataSource(map);
        Map<String, DataSource> slaves = new HashMap<>();
        for (Map.Entry<String, DataSourceProperties> entry : dataSourceProperties.getSlaves().entrySet()) {
            DataSource slave = DruidDataSourceFactory.createDataSource(merge(map, entry.getValue().toMap()));
            slaves.put(entry.getKey(), slave);
        }
        if (!slaves.isEmpty()) {
            //master slave
            String url = map.get("url").replaceAll("^jdbc:", "");
            String scheme = URI.create(url).getScheme();
            DbType dbType = DbType.of(scheme);
            RouteDataSource routeDataSource = new RouteDataSource(dbType, master, slaves);
            dataSource = routeDataSource;
        } else {
            //single
            dataSource = master;
        }
        //ddl
        DatabaseDdlProperties properties = jasperCloudDaoProperties.getDdl().get(name);
        if (null != properties) {
            try {
                ddlExecuter.execute(properties, dataSource);
            } catch (Throwable e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }

    private Map<String, String> merge(Map<String, String> master, Map<String, String> slave) {
        Map<String, String> map = new HashMap<>();
        map.putAll(master);
        for (Map.Entry<String, String> entry : slave.entrySet()) {
            if (StringUtils.isNotEmpty(entry.getValue())) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return map;
    }

    @Override
    public DataSource getObject() throws Exception {
        return dataSource;
    }

    @Override
    public Class<?> getObjectType() {
        return DataSource.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}

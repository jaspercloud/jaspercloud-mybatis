package com.jaspercloud.mybatis.support.plugin;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.jaspercloud.mybatis.support.jdbc.ProxyConnection;
import com.jaspercloud.mybatis.support.jdbc.RouteDataSource;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.jdbc.ConnectionLogger;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.sql.Connection;

@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class StatementInterceptor implements Interceptor {

    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            StatementHandler statementHandler = PluginUtils.realTarget(invocation.getTarget());
            MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
            MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
            ProxyConnection connection = (ProxyConnection) getConnection((Connection) invocation.getArgs()[0]);
            boolean masterTransaction = connection.isMasterTransaction();
            boolean selectKey = mappedStatement.getId().endsWith("insert!selectKey");
            boolean nextval = statementHandler.getBoundSql().getSql().toLowerCase().contains("nextval");
            if (true == masterTransaction) {
                RouteDataSource.master();
            } else if (nextval) {
                RouteDataSource.master();
                connection.setMasterTransaction();
            } else if (selectKey) {
                RouteDataSource.master();
                connection.setMasterTransaction();
            } else if (!SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType())) {
                RouteDataSource.master();
                connection.setMasterTransaction();
            } else {
                RouteDataSource.slave();
            }
            return invocation.proceed();
        } finally {
            RouteDataSource.remove();
        }
    }


    private Connection getConnection(Connection proxy) throws Exception {
        if (!Proxy.isProxyClass(proxy.getClass())) {
            return proxy;
        }
        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        ConnectionLogger connectionLogger = (ConnectionLogger) h.get(proxy);
        Connection connection = connectionLogger.getConnection();
        return connection;
    }
}

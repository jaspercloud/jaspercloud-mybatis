package com.jaspercloud.mybatis.support.plugin;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.jaspercloud.mybatis.support.jdbc.RouteDataSource;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

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
            boolean selectKey = mappedStatement.getId().endsWith("insert!selectKey");
            if (selectKey) {
                RouteDataSource.master();
            } else if (!SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType())) {
                RouteDataSource.master();
            } else {
                RouteDataSource.slave();
            }
            return invocation.proceed();
        } finally {
            RouteDataSource.remove();
        }
    }
}

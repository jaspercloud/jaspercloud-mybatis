package com.jaspercloud.mybatis.support.plugin;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.jaspercloud.mybatis.support.jdbc.RouteDataSource;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;


/**
 * 数据库主从拦截
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update",
                args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "queryCursor",
                args = {MappedStatement.class, Object.class, RowBounds.class})
})
public class DBMsInterceptor implements Interceptor {

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Executor executor = PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(executor);
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        try {
            if (!SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType())) {
                RouteDataSource.master();
            }
            return invocation.proceed();
        } finally {
            RouteDataSource.remove();
        }
    }
}

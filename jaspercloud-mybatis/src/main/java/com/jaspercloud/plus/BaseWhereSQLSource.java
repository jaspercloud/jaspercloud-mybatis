package com.jaspercloud.plus;

import com.jaspercloud.plus.resolver.SelectWhereTemplateMethodResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.TextSqlNode;
import org.apache.ibatis.session.Configuration;

import java.util.Map;

public class BaseWhereSQLSource implements SqlSource {

    private Configuration configuration;
    private String baseSql;
    private Class<?> modelClass;

    public BaseWhereSQLSource(Configuration configuration, String baseSql, Class<?> modelClass) {
        this.configuration = configuration;
        this.baseSql = baseSql;
        this.modelClass = modelClass;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        MapperMethod.ParamMap paramMap = (MapperMethod.ParamMap) parameterObject;
        String where = (String) paramMap.get(SelectWhereTemplateMethodResolver.WHERE);
        String sql = new StringBuilder(baseSql).append(StringUtils.isNotEmpty(where) ? where : "").toString();
        Map<String, Object> map = (Map<String, Object>) paramMap.get(SelectWhereTemplateMethodResolver.PARAMS);
        paramMap.putAll(map);
        SqlSource sqlSource = new DynamicSqlSource(configuration, new TextSqlNode(sql));
        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        return boundSql;
    }
}

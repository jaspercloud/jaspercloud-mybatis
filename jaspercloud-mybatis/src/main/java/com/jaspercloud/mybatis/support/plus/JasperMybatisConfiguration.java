package com.jaspercloud.mybatis.support.plus;

import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;

import java.util.HashMap;
import java.util.Map;

public class JasperMybatisConfiguration extends Configuration {

    private MapperRegistry mapperRegistry = new JasperMybatisMapperRegistry(this);
    private Map<Class<?>, TableInfo> tableInfoMap = new HashMap<>();

    @Override
    public MapperRegistry getMapperRegistry() {
        return mapperRegistry;
    }

    @Override
    public void addMappers(String packageName, Class<?> superType) {
        mapperRegistry.addMappers(packageName, superType);
    }

    @Override
    public void addMappers(String packageName) {
        mapperRegistry.addMappers(packageName);
    }

    @Override
    public <T> void addMapper(Class<T> type) {
        mapperRegistry.addMapper(type);
    }

    @Override
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mapperRegistry.getMapper(type, sqlSession);
    }

    @Override
    public boolean hasMapper(Class<?> type) {
        return mapperRegistry.hasMapper(type);
    }

    public void addTableInfo(Class<?> clazz, TableInfo tableInfo) {
        tableInfoMap.put(clazz, tableInfo);
    }

    public TableInfo getTableInfo(Class<?> clazz) {
        TableInfo tableInfo = tableInfoMap.get(clazz);
        return tableInfo;
    }
}

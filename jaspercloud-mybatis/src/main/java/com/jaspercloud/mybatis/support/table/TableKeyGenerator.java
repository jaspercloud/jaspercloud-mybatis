package com.jaspercloud.mybatis.support.table;

import com.baomidou.mybatisplus.core.incrementer.IKeyGenerator;

import java.util.Map;

public class TableKeyGenerator {

    private Map<String, IKeyGenerator> generatorMap;

    public TableKeyGenerator(Map<String, IKeyGenerator> generatorMap) {
        this.generatorMap = generatorMap;
    }

    public IKeyGenerator getGenerator(String dbName) {
        return generatorMap.get(dbName);
    }
}

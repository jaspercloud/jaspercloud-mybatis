package com.jaspercloud.plus;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;

import java.sql.Statement;
import java.util.Map;
import java.util.function.BiConsumer;

public class MultiKeyGenerator implements KeyGenerator {

    private Map<String, KeyGenerator> map;

    public MultiKeyGenerator(Map<String, KeyGenerator> map) {
        this.map = map;
    }

    @Override
    public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        map.forEach(new BiConsumer<String, KeyGenerator>() {
            @Override
            public void accept(String s, KeyGenerator keyGenerator) {
                keyGenerator.processBefore(executor, ms, stmt, parameter);
            }
        });
    }

    @Override
    public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        map.forEach(new BiConsumer<String, KeyGenerator>() {
            @Override
            public void accept(String s, KeyGenerator keyGenerator) {
                keyGenerator.processAfter(executor, ms, stmt, parameter);
            }
        });
    }
}

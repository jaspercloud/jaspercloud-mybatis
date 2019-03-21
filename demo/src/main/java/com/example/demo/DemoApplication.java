package com.example.demo;

import com.example.demo.mapper.test.entity.Test;
import com.example.demo.mapper.test.mapper.TestMapper;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class DemoApplication implements InitializingBean {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Autowired
    private TestMapper testMapper;

    @Override
    public void afterPropertiesSet() throws Exception {
        {
            Test test = new Test();
            test.setContent("test");
            test.setNickName("test");
            test.setUserName("test");
            test.setPassword("test");
            long result = testMapper.insert(test);
            System.out.println();
        }
        {
            Test test = new Test();
            test.setId(100L);
            test.setContent("update");
            test.setNickName("update");
            test.setUserName("update");
            test.setPassword("update");
            long result = testMapper.updateById(test);
            System.out.println();
        }
        {
            long result = testMapper.deleteById(100L);
            System.out.println();
        }
        {
            String sql = new SQL() {
                {
                    SELECT("*");
                    FROM("test");
                    WHERE("id=2000");
                }
            }.toString();
            List<Test> tests = testMapper.selectSQL(sql);
            System.out.println();
        }
        {
            String sql = new SQL() {
                {
                    DELETE_FROM("test");
                    WHERE("id=2000");
                }
            }.toString();
            long result = testMapper.updateSQL(sql);
            System.out.println();
        }
        {
            Map<String, Object> map = new HashMap<>();
            map.put("content", "test1");
            map.put("nickName", "test2");
            Test test = testMapper.selectOneByMap(map);
            System.out.println();
        }
        {
            Map<String, Object> map = new HashMap<>();
            map.put("content", "test1");
            map.put("nickName", "test2");
            List<Test> tests = testMapper.selectByMap(map);
            System.out.println();
        }
        {
            Test test = testMapper.selectById(2000L);
            List<Test> tests = testMapper.selectAll();
            System.out.println();
        }
        {
            long count = testMapper.selectCount();
            System.out.println();
        }
    }
}

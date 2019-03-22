package com.example.demo;

import com.example.demo.mapper.test.entity.Test;
import com.example.demo.mapper.test.mapper.TestMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;
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
            Map<String, Object> map = new HashMap<>();
            map.put("id", 100L);
            map.put("minId", 20L);
            List<Test> tests = testMapper.selectListWhere("where id=#{id}", map);
            List<Test> tests1 = testMapper.selectListWhere("", map);
            long id = testMapper.deleteWhere("where id=#{id}", Collections.singletonMap("id", 20L));
            System.out.println();
        }
        {
            Map<String, Object> map = new HashMap<>();
            map.put("id", 100L);
            List<Test> tests = testMapper.selectListWhere("where id=#{id}", map);
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
            List<Test> tests = testMapper.selectListByMap(map);
            System.out.println();
        }
        {
            Test test = testMapper.selectById(2000L);
            List<Test> tests = testMapper.selectList();
            System.out.println();
        }
        {
            long count = testMapper.selectCount("where id>=#{id}", Collections.singletonMap("id", 20L));
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
    }
}

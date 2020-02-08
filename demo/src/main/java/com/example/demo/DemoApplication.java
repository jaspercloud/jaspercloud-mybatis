package com.example.demo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.mapper.test.entity.Test;
import com.example.demo.mapper.test.mapper.TestMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class DemoApplication implements InitializingBean {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Autowired
    private TestMapper testMapper;

    @Override
    public void afterPropertiesSet() throws Exception {
        testMapper.delete(new QueryWrapper<Test>().eq("imsi", "46000").lambda());
        System.out.println();

        Test test = new Test();
        test.setImsi("46000");
        test.setImei("86000");
        test.setSchoolId(1L);
        testMapper.insert(test);
        System.out.println();

        List<Test> list = testMapper.selectList(new QueryWrapper<Test>().eq("imsi", "46000").lambda());
        System.out.println();

        test.setSchoolId(2L);
        testMapper.updateById(test);
        System.out.println();
    }
}

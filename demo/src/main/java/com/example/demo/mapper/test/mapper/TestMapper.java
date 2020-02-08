package com.example.demo.mapper.test.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.mapper.test.entity.Test;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestMapper extends BaseMapper<Test> {

}

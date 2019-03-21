package com.example.demo.mapper.test.mapper;

import com.example.demo.mapper.test.entity.Test;
import com.jaspercloud.mybatis.support.plus.BaseMapper;
import org.apache.ibatis.annotations.*;

@Mapper
public interface TestMapper extends BaseMapper<Test> {

    @Select("select * from test where id=#{id}")
    Test selectByIdTest(@Param("id") Long id);

    @SelectKey(statement = "select nextval('seq_test')", keyProperty = "id", resultType = Long.class, before = true)
    @Insert("insert into test (id,content) values (#{id},#{content})")
    void saveTest(Test test);
}

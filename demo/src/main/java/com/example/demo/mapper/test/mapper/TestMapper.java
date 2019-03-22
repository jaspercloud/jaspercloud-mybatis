package com.example.demo.mapper.test.mapper;

import com.example.demo.mapper.test.entity.Test;
import com.jaspercloud.mybatis.support.plus.BaseMapper;
import org.apache.ibatis.annotations.*;

@Mapper
public interface TestMapper extends BaseMapper<Test> {

    @Results(
            @Result(column = "pass_wd", property = "password")
    )
    @Select("select * from test where id=#{id} limit ${limit}")
    Test selectByIdTest(@Param("id") Long id, @Param("limit") Long limit);

    @SelectKey(statement = "select nextval('seq_test')", keyProperty = "id", resultType = Long.class, before = true)
    @Insert("insert into test (id,content) values (#{id},#{content})")
    void saveTest(Test test);
}

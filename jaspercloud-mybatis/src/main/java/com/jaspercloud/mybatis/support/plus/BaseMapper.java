package com.jaspercloud.mybatis.support.plus;

import com.jaspercloud.mybatis.support.plus.resolver.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface BaseMapper<T> {

    @TemplateMethod(InsertTemplateMethodResolver.class)
    long insert(T obj);

    @TemplateMethod(UpdateByIdTemplateMethodResolver.class)
    long updateById(T obj);

    @TemplateMethod(DeleteByIdTemplateMethodResolver.class)
    long deleteById(Long id);

    @TemplateMethod(SelectByIdTemplateMethodResolver.class)
    T selectById(Long id);

    @TemplateMethod(SelectAllTemplateMethodResolver.class)
    List<T> selectAll();

    @TemplateMethod(SelectByMapTemplateMethodResolver.class)
    T selectOneByMap(Map<String, Object> map);

    @TemplateMethod(SelectByMapTemplateMethodResolver.class)
    List<T> selectByMap(Map<String, Object> map);

    @TemplateMethod(SelectCountTemplateMethodResolver.class)
    long selectCount();

    @TemplateMethod(UpdateSQLTemplateMethodResolver.class)
    long updateSQL(@Param("sql") String sql);

    @TemplateMethod(SelectSQLTemplateMethodResolver.class)
    List<T> selectSQL(@Param("sql") String sql);
}

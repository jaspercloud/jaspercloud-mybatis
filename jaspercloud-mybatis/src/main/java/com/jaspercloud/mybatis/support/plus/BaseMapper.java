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

    @TemplateMethod(DeleteWhereTemplateMethodResolver.class)
    long deleteWhere(@Param(DeleteWhereTemplateMethodResolver.WHERE) String where,
                     @Param(DeleteWhereTemplateMethodResolver.PARAMS) Map<String, Object> map);

    @TemplateMethod(SelectByIdTemplateMethodResolver.class)
    T selectById(Long id);

    @TemplateMethod(SelectAllTemplateMethodResolver.class)
    List<T> selectList();

    @TemplateMethod(SelectByMapTemplateMethodResolver.class)
    T selectOneByMap(Map<String, Object> map);

    @TemplateMethod(SelectByMapTemplateMethodResolver.class)
    List<T> selectListByMap(Map<String, Object> map);

    @TemplateMethod(SelectWhereTemplateMethodResolver.class)
    T selectOneWhere(@Param(SelectWhereTemplateMethodResolver.WHERE) String where,
                     @Param(SelectWhereTemplateMethodResolver.PARAMS) Map<String, Object> map);

    @TemplateMethod(SelectWhereTemplateMethodResolver.class)
    List<T> selectListWhere(@Param(SelectWhereTemplateMethodResolver.WHERE) String where,
                            @Param(SelectWhereTemplateMethodResolver.PARAMS) Map<String, Object> map);

    @TemplateMethod(SelectCountWhereTemplateMethodResolver.class)
    long selectCount(@Param(SelectCountWhereTemplateMethodResolver.WHERE) String where,
                     @Param(SelectCountWhereTemplateMethodResolver.PARAMS) Map<String, Object> map);
}

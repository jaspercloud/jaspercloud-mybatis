package com.baomidou.mybatisplus.core.injector;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class JasperCloudSqlInjector extends DefaultSqlInjector {

    private static final Log logger = LogFactory.getLog(AbstractSqlInjector.class);

    private Map<Class<?>, Class<?>> config = new ConcurrentHashMap<>();

    public JasperCloudSqlInjector() {
    }

    /**
     * @param define 声明
     * @param entity mapper entity
     */
    public void addConfig(Class<?> define, Class<?> entity) {
        config.put(entity, define);
    }

    @Override
    public void inspectInject(MapperBuilderAssistant builderAssistant, Class<?> mapperClass) {
        Class<?> modelClass = super.extractModelClass(mapperClass);
        Class<?> define = config.get(modelClass);

        List<MappedStatement> before = new ArrayList<>(builderAssistant.getConfiguration().getMappedStatements());
        doInspectInject(builderAssistant, mapperClass);
        List<MappedStatement> after = new ArrayList<>(builderAssistant.getConfiguration().getMappedStatements());
        for (MappedStatement mappedStatement : after) {
            if (!before.contains(mappedStatement)) {
                List<ResultMap> resultMaps = mappedStatement.getResultMaps();
                for (ResultMap resultMap : resultMaps) {
                    Object type = getField(resultMap, "type");
                    if (Objects.equals(type, define)) {
                        replaceField(resultMap, "type", modelClass);
                    }
                }
            }
        }
    }

    private void doInspectInject(MapperBuilderAssistant builderAssistant, Class<?> mapperClass) {
        Class<?> modelClass = super.extractModelClass(mapperClass);
        Class<?> entityClass = config.get(modelClass);
        if (null == entityClass) {
            entityClass = modelClass;
        }
        final Class<?> finalEntityClass = entityClass;
        if (finalEntityClass != null) {
            String className = mapperClass.toString();
            Set<String> mapperRegistryCache = GlobalConfigUtils.getMapperRegistryCache(builderAssistant.getConfiguration());
            if (!mapperRegistryCache.contains(className)) {
                List<AbstractMethod> methodList = this.getMethodList(mapperClass);
                if (CollectionUtils.isNotEmpty(methodList)) {
                    TableInfo tableInfo = TableInfoHelper.initTableInfo(builderAssistant, finalEntityClass);
                    replaceField(tableInfo, "entityType", entityClass);
                    // 循环注入自定义方法
                    methodList.forEach(m -> m.inject(builderAssistant, mapperClass, modelClass, tableInfo));
                } else {
                    logger.debug(mapperClass.toString() + ", No effective injection method was found.");
                }
                mapperRegistryCache.add(className);
            }
        }
    }

    private Object getField(Object bean, String fieldName) {
        Field field = ReflectionUtils.findField(bean.getClass(), fieldName);
        ReflectionUtils.makeAccessible(field);
        Object value = ReflectionUtils.getField(field, bean);
        return value;
    }

    private void replaceField(Object bean, String fieldName, Object value) {
        Field field = ReflectionUtils.findField(bean.getClass(), fieldName);
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, bean, value);
    }
}

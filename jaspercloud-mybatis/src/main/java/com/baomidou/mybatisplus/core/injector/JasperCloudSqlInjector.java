package com.baomidou.mybatisplus.core.injector;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
        TableName tableName = define.getDeclaredAnnotation(TableName.class);
        if (null == tableName) {
            throw new IllegalArgumentException("not found @TableName");
        }
        List<String> defineFields = TableInfoHelper.getAllFields(define).stream()
                .map(e -> e.getName()).collect(Collectors.toList());
        List<String> entityFields = TableInfoHelper.getAllFields(entity).stream()
                .map(e -> e.getName()).collect(Collectors.toList());
        boolean equals = Arrays.equals(defineFields.toArray(), entityFields.toArray());
        if (!equals) {
            throw new IllegalArgumentException("lack define fields");
        }
        config.put(entity, define);
    }

    @Override
    public void inspectInject(MapperBuilderAssistant builderAssistant, Class<?> mapperClass) {
        Class<?> modelClass = super.extractModelClass(mapperClass);
        Class<?> defineClass = config.get(modelClass);
        if (null == defineClass) {
            defineClass = modelClass;
        }
        if (defineClass != null) {
            String className = mapperClass.toString();
            Set<String> mapperRegistryCache = GlobalConfigUtils.getMapperRegistryCache(builderAssistant.getConfiguration());
            if (!mapperRegistryCache.contains(className)) {
                List<AbstractMethod> methodList = this.getMethodList(mapperClass);
                if (CollectionUtils.isNotEmpty(methodList)) {
                    TableInfo tableInfo = TableInfoHelper.initTableInfo(builderAssistant, defineClass);
                    replaceField(tableInfo, "entityType", modelClass);
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

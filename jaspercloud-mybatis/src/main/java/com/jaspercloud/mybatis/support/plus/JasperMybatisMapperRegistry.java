package com.jaspercloud.mybatis.support.plus;

import com.jaspercloud.mybatis.support.plus.resolver.TemplateMethod;
import com.jaspercloud.mybatis.support.plus.resolver.TemplateMethodResolver;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.binding.MapperProxyFactory;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.Method;
import java.util.*;

public class JasperMybatisMapperRegistry extends MapperRegistry {

    private final JasperMybatisConfiguration config;
    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<Class<?>, MapperProxyFactory<?>>();

    public JasperMybatisMapperRegistry(JasperMybatisConfiguration config) {
        super(config);
        this.config = config;
    }

    @Override
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if (mapperProxyFactory == null) {
            throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
        }
        try {
            return mapperProxyFactory.newInstance(sqlSession);
        } catch (Exception e) {
            throw new BindingException("Error getting mapper instance. Cause: " + e, e);
        }
    }

    @Override
    public <T> boolean hasMapper(Class<T> type) {
        return knownMappers.containsKey(type);
    }

    @Override
    public <T> void addMapper(Class<T> type) {
        if (type.isInterface()) {
            if (hasMapper(type)) {
                throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
            }
            boolean loadCompleted = false;
            try {
                knownMappers.put(type, new MapperProxyFactory<T>(type));
                // It's important that the type is added before the parser is run
                // otherwise the binding may automatically be attempted by the
                // mapper parser. If the type is already known, it won't try.
                MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
                parser.parse();
                parseTemplateMethod(config, type);
                loadCompleted = true;
            } catch (Exception e) {
                throw new BindingException("addMapper Error: " + e, e);
            } finally {
                if (!loadCompleted) {
                    knownMappers.remove(type);
                }
            }
        }
    }

    private <T> void parseTemplateMethod(JasperMybatisConfiguration config, Class<T> type) throws Exception {
        Method[] methods = type.getMethods();
        for (Method method : methods) {
            TemplateMethod annotation = method.getAnnotation(TemplateMethod.class);
            if (null == annotation) {
                continue;
            }
            Class<? extends TemplateMethodResolver> cls = annotation.value();
            TemplateMethodResolver resolver = cls.newInstance();
            Class<?> modelClass = MapperUtil.extractModelClass(type);
            resolver.resolver(config, type, modelClass, method);
        }
    }

    /**
     * @since 3.2.2
     */
    @Override
    public Collection<Class<?>> getMappers() {
        return Collections.unmodifiableCollection(knownMappers.keySet());
    }

    /**
     * @since 3.2.2
     */
    @Override
    public void addMappers(String packageName, Class<?> superType) {
        ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<Class<?>>();
        resolverUtil.find(new ResolverUtil.IsA(superType), packageName);
        Set<Class<? extends Class<?>>> mapperSet = resolverUtil.getClasses();
        for (Class<?> mapperClass : mapperSet) {
            addMapper(mapperClass);
        }
    }

    /**
     * @since 3.2.2
     */
    @Override
    public void addMappers(String packageName) {
        addMappers(packageName, Object.class);
    }
}

package com.jaspercloud.mybatis.support.ddl;

import com.jaspercloud.mybatis.exception.ResourceException;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DdlMigrateScanner implements ResourceLoaderAware, EnvironmentAware {

    private ResourcePatternResolver resourcePatternResolver;
    private MetadataReaderFactory metadataReaderFactory;
    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);
    }

    protected String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(this.environment.resolveRequiredPlaceholders(basePackage));
    }

    public List<DdlMigrate> scanSQLClass(String... location) {
        if (null == location || location.length == 0) {
            return new ArrayList<>();
        }
        List<DdlMigrate> scanList = Arrays.stream(location).map(new Function<String, List<Resource>>() {
            @Override
            public List<Resource> apply(String location) {
                try {
                    String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + resolveBasePackage(location) + "/**/*.class";
                    Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
                    return Arrays.asList(resources);
                } catch (IOException e) {
                    throw new ResourceException(e.getMessage(), e);
                }
            }
        }).flatMap(new Function<List<Resource>, Stream<DdlMigrate>>() {
            @Override
            public Stream<DdlMigrate> apply(List<Resource> resources) {
                return resources.stream().filter(new Predicate<Resource>() {
                    @Override
                    public boolean test(Resource resource) {
                        return resource.isReadable();
                    }
                }).map(new Function<Resource, Class<?>>() {
                    @Override
                    public Class<?> apply(Resource resource) {
                        try {
                            MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                            String className = metadataReader.getClassMetadata().getClassName();
                            Class<?> clazz = Class.forName(className);
                            return clazz;
                        } catch (Exception e) {
                            throw new ResourceException(e.getMessage(), e);
                        }
                    }
                }).filter(new Predicate<Class<?>>() {
                    @Override
                    public boolean test(Class<?> clazz) {
                        return DdlMigrate.class.isAssignableFrom(clazz);
                    }
                }).map(new Function<Class<?>, DdlMigrate>() {
                    @Override
                    public DdlMigrate apply(Class<?> clazz) {
                        try {
                            return (DdlMigrate) clazz.newInstance();
                        } catch (Exception e) {
                            throw new ResourceException(e.getMessage(), e);
                        }
                    }
                });
            }
        }).collect(Collectors.toList());
        return scanList;
    }

    public List<DdlMigrate> scanFile(String... location) {
        if (null == location || location.length == 0) {
            return new ArrayList<>();
        }
        List<DdlMigrate> scanList = Arrays.stream(location).map(new Function<String, List<Resource>>() {
            @Override
            public List<Resource> apply(String location) {
                try {
                    Resource[] resources = resourcePatternResolver.getResources(location);
                    return Arrays.asList(resources);
                } catch (IOException e) {
                    throw new ResourceException(e.getMessage(), e);
                }
            }
        }).flatMap(new Function<List<Resource>, Stream<DdlMigrate>>() {
            @Override
            public Stream<DdlMigrate> apply(List<Resource> resources) {
                return resources.stream().map(new Function<Resource, DdlMigrate>() {
                    @Override
                    public DdlMigrate apply(Resource resource) {
                        InputStream inputStream = null;
                        try {
                            inputStream = resource.getInputStream();
                            byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
                            String sql = new String(bytes, Charset.forName("utf-8"));
                            String filename = resource.getFilename();
                            String[] splits = filename.split("__|\\.");
                            if (splits.length < 3) {
                                throw new IllegalArgumentException("sqlName: ${version}__${name}.sql");
                            }
                            int version = Integer.parseInt(splits[0]);
                            String name = splits[1];
                            DdlMigrate ddlMigrateSql = new DdlMigrateSql(new MigrateInfo(name, version), sql);
                            return ddlMigrateSql;
                        } catch (IOException e) {
                            throw new ResourceException(e.getMessage(), e);
                        } finally {
                            if (null != inputStream) {
                                try {
                                    inputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }
        }).collect(Collectors.toList());
        return scanList;
    }
}

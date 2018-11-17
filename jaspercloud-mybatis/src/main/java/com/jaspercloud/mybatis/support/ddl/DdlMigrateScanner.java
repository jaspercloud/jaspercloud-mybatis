package com.jaspercloud.mybatis.support.ddl;

import com.jaspercloud.mybatis.exception.ResourceException;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DdlMigrateScanner implements ResourceLoaderAware {

    private ResourcePatternResolver resourcePatternResolver;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
    }

    public List<DdlMigrate> scan(String... location) {
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
        }).reduce(new BinaryOperator<List<Resource>>() {
            @Override
            public List<Resource> apply(List<Resource> resources, List<Resource> resources2) {
                Set<Resource> set = new TreeSet<>(new Comparator<Resource>() {
                    @Override
                    public int compare(Resource o1, Resource o2) {
                        return o1.toString().compareTo(o2.toString());
                    }
                });
                set.addAll(resources);
                set.addAll(resources2);
                ArrayList<Resource> list = new ArrayList<>(set);
                return list;
            }
        }).get().stream().map(new Function<Resource, DdlMigrate>() {
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
        }).collect(Collectors.toList());
        return scanList;
    }
}

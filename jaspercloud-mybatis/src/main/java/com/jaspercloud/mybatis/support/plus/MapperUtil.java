package com.jaspercloud.mybatis.support.plus;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import javax.persistence.*;
import java.lang.reflect.*;

public final class MapperUtil {

    private MapperUtil() {

    }

    public static Class<?> getParameterType(Method method) {
        Class<?> parameterType = null;
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> currentParameterType : parameterTypes) {
            if (!RowBounds.class.isAssignableFrom(currentParameterType) && !ResultHandler.class.isAssignableFrom(currentParameterType)) {
                if (parameterType == null) {
                    parameterType = currentParameterType;
                } else {
                    parameterType = MapperMethod.ParamMap.class;
                }
            }
        }
        return parameterType;
    }

    public static TableInfo parseTableInfo(Class<?> modelClass) {
        TableInfo tableInfo = new TableInfo();
        Entity entity = ReflectUtils.getAnnotation(modelClass, Entity.class);
        if (null == entity) {
            throw new RuntimeException();
        }
        tableInfo.setTableName(entity.name());

        Field[] fields = ReflectUtils.getAllDeclaredFields(modelClass);
        for (Field field : fields) {
            Transient transientAnnotation = field.getAnnotation(Transient.class);
            String modifier = Modifier.toString(field.getModifiers());
            if (null != transientAnnotation || modifier.contains("transient")) {
                continue;
            }
            Id id = field.getAnnotation(Id.class);
            if (null != id) {
                tableInfo.setKeyColumn(new TableInfo.TableColumn(getColumnName(field), field.getName(), field.getType()));
            }
            GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
            if (null != generatedValue && null != tableInfo.getKeyColumn()) {
                TableInfo.TableColumn keyColumn = tableInfo.getKeyColumn();
                keyColumn.setGenerateSql(generatedValue.generator());
            }
            Column column = field.getAnnotation(Column.class);
            if (null != column) {
                tableInfo.addColumn(new TableInfo.TableColumn(column.name(), field.getName(), field.getType()));
            } else {
                tableInfo.addColumn(new TableInfo.TableColumn(getColumnName(field), field.getName(), field.getType()));
            }
        }
        return tableInfo;
    }

    public static String getColumnName(Field field) {
        String name = field.getName();
        char[] chars = name.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            if (Character.isUpperCase(ch) && i > 0) {
                builder.append("_");
                builder.append(Character.toLowerCase(ch));
            } else {
                builder.append(ch);
            }
        }
        String colName = builder.toString();
        return colName;
    }

    public static LanguageDriver getLanguageDriver(MapperBuilderAssistant assistant, Method method) {
        Lang lang = method.getAnnotation(Lang.class);
        Class<?> langClass = null;
        if (lang != null) {
            langClass = lang.value();
        }
        return assistant.getLanguageDriver(langClass);
    }

    public static KeyGenerator handleSelectKeyAnnotation(JasperMybatisConfiguration config,
                                                         MapperBuilderAssistant assistant,
                                                         String baseStatementId,
                                                         TableInfo tableInfo,
                                                         Class<?> modelClass,
                                                         LanguageDriver languageDriver) {
        String id = baseStatementId + SelectKeyGenerator.SELECT_KEY_SUFFIX;
        TableInfo.TableColumn tableKeyColumn = tableInfo.getKeyColumn();
        if (null == tableKeyColumn) {
            return NoKeyGenerator.INSTANCE;
        }
        String keyProperty = tableKeyColumn.getPropertyName();
        String keyColumn = tableKeyColumn.getColumnName();
        Class<?> resultTypeClass = tableKeyColumn.getType();
        StatementType statementType = StatementType.PREPARED;
        boolean executeBefore = true;
        // defaults
        boolean useCache = false;
        KeyGenerator keyGenerator = NoKeyGenerator.INSTANCE;
        Integer fetchSize = null;
        Integer timeout = null;
        boolean flushCache = false;
        String parameterMap = null;
        String resultMap = null;
        ResultSetType resultSetTypeEnum = null;

        SqlSource sqlSource = languageDriver.createSqlSource(config, tableKeyColumn.getGenerateSql(), tableKeyColumn.getType());
        SqlCommandType sqlCommandType = SqlCommandType.SELECT;
        assistant.addMappedStatement(id, sqlSource, statementType, sqlCommandType, fetchSize, timeout, parameterMap, modelClass, resultMap, resultTypeClass, resultSetTypeEnum,
                flushCache, useCache, false,
                keyGenerator, keyProperty, keyColumn, null, languageDriver, null);
        id = assistant.applyCurrentNamespace(id, false);
        MappedStatement keyStatement = config.getMappedStatement(id, false);
        SelectKeyGenerator answer = new SelectKeyGenerator(keyStatement, executeBefore);
        config.addKeyGenerator(id, answer);
        return answer;
    }

    public static Class<?> extractModelClass(Class<?> mapperClass) {
        Type[] types = mapperClass.getGenericInterfaces();
        ParameterizedType target = null;
        for (Type type : types) {
            if (type instanceof ParameterizedType) {
                Type[] typeArray = ((ParameterizedType) type).getActualTypeArguments();
                if (ArrayUtils.isNotEmpty(typeArray)) {
                    for (Type t : typeArray) {
                        if (t instanceof TypeVariable || t instanceof WildcardType) {
                            break;
                        } else {
                            target = (ParameterizedType) type;
                            break;
                        }
                    }
                }
                break;
            }
        }
        return target == null ? null : (Class<?>) target.getActualTypeArguments()[0];
    }
}

package com.jaspercloud.plus;

import com.jaspercloud.plus.annotation.SelectKey;
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
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static Class<?> getReturnType(Method method, Class<?> modelClass) {
        Class<?> returnType = method.getReturnType();
        int length = returnType.getTypeParameters().length;
        if (length > 0) {
            return modelClass;
        }
        return returnType;
    }

    public static String genResultMapName(Class<?> type, Method method) {
        StringBuilder suffix = new StringBuilder();
        suffix.append(type.getName());
        suffix.append(".");
        suffix.append(method.getName());
        for (Class<?> c : method.getParameterTypes()) {
            suffix.append("-");
            suffix.append(c.getSimpleName());
        }
        if (suffix.length() < 1) {
            suffix.append("-void");
        }
        String name = suffix.toString();
        return name;
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
            SelectKey selectKey = field.getAnnotation(SelectKey.class);
            Id id = field.getAnnotation(Id.class);
            if (null != id) {
                TableInfo.TableColumn keyColumn = new TableInfo.TableColumn(getColumnName(field), field.getName(), field.getType());
                keyColumn.setSelectKey(selectKey);
                tableInfo.setKeyColumn(keyColumn);
            }
            Column column = field.getAnnotation(Column.class);
            if (null != column) {
                TableInfo.TableColumn tableColumn = new TableInfo.TableColumn(column.name(), field.getName(), field.getType());
                tableColumn.setSelectKey(selectKey);
                tableInfo.addColumn(tableColumn);
            } else {
                TableInfo.TableColumn tableColumn = new TableInfo.TableColumn(getColumnName(field), field.getName(), field.getType());
                tableColumn.setSelectKey(selectKey);
                tableInfo.addColumn(tableColumn);
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
        List<TableInfo.TableColumn> tableColumns = tableInfo.getColumns();
        Map<String, KeyGenerator> map = getKeyGeneratorMap(tableColumns, baseStatementId, config, assistant, languageDriver, modelClass);
        if (map.isEmpty()) {
            return NoKeyGenerator.INSTANCE;
        }
        KeyGenerator keyGenerator = new MultiKeyGenerator(map);
        return keyGenerator;
    }

    private static Map<String, KeyGenerator> getKeyGeneratorMap(List<TableInfo.TableColumn> tableColumns,
                                                                String baseStatementId,
                                                                JasperMybatisConfiguration config,
                                                                MapperBuilderAssistant assistant,
                                                                LanguageDriver languageDriver,
                                                                Class<?> modelClass) {
        Map<String, KeyGenerator> map = new HashMap<>();
        for (TableInfo.TableColumn tableColumn : tableColumns) {
            SelectKey selectKey = tableColumn.getSelectKey();
            if (null == selectKey) {
                continue;
            }
            KeyGenerator keyGenerator = addKeyGenerator(tableColumn, baseStatementId, config, assistant, languageDriver, modelClass);
            map.put(tableColumn.getPropertyName(), keyGenerator);
        }
        return map;
    }

    private static KeyGenerator addKeyGenerator(TableInfo.TableColumn tableColumn,
                                                String baseStatementId,
                                                JasperMybatisConfiguration config,
                                                MapperBuilderAssistant assistant,
                                                LanguageDriver languageDriver,
                                                Class<?> modelClass) {
        SelectKey selectKey = tableColumn.getSelectKey();
        String id = baseStatementId + tableColumn.getPropertyName() + SelectKeyGenerator.SELECT_KEY_SUFFIX;
        String keyProperty = tableColumn.getPropertyName();
        String keyColumn = null;
        Class<?> resultTypeClass = tableColumn.getType();
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

        SqlSource sqlSource = languageDriver.createSqlSource(config, selectKey.statement(), resultTypeClass);
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

    public static String genResultMapName(MapperBuilderAssistant assistant, Class<?> type, Method method, TableInfo tableInfo, Class<?> modelClass) {
        List<ResultMapping> resultMappingList = new ArrayList<>();
        List<TableInfo.TableColumn> columns = tableInfo.getColumns();
        for (TableInfo.TableColumn column : columns) {
            Class<?> resultType = modelClass;
            String property = column.getPropertyName();
            String columnName = column.getColumnName();
            Class<?> javaType = null;
            JdbcType jdbcType = null;
            String nestedSelect = null;
            String nestedResultMap = null;
            String notNullColumn = null;
            String columnPrefix = null;
            Class<? extends TypeHandler<?>> typeHandler = null;
            List<ResultFlag> flags = new ArrayList<>();
            String resultSet = null;
            String foreignColumn = null;
            boolean lazy = false;
            ResultMapping mapping = assistant.buildResultMapping(
                    resultType,
                    property,
                    columnName,
                    javaType,
                    jdbcType,
                    nestedSelect,
                    nestedResultMap,
                    notNullColumn,
                    columnPrefix,
                    typeHandler,
                    flags,
                    resultSet,
                    foreignColumn,
                    lazy
            );
            resultMappingList.add(mapping);
        }
        String resultMapId = genResultMapName(type, method);
        assistant.addResultMap(resultMapId, modelClass, null, null, resultMappingList, null);
        return resultMapId;
    }
}

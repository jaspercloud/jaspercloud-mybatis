package com.jaspercloud.mybatis.support.plus;

import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ReflectUtils {

    private ReflectUtils() {

    }

    public static <A extends Annotation> A getAnnotation(Class<?> clazz, Class<A> annotationClass) {
        A annotation = clazz.getAnnotation(annotationClass);
        if (null == annotation) {
            if (Object.class.equals(clazz)) {
                return null;
            }
            annotation = getAnnotation(clazz.getSuperclass(), annotationClass);
        }
        return annotation;
    }

    public static Field[] getAllDeclaredFields(Class<?> clazz) {
        List<Field> list = new ArrayList<>();
        findDeclaredFields(clazz, list);
        Field[] fields = list.toArray(new Field[list.size()]);
        return fields;
    }

    private static void findDeclaredFields(Class<?> clazz, List<Field> list) {
        Field[] fields = clazz.getDeclaredFields();
        list.addAll(Arrays.asList(fields));
        Class<?> superClass = clazz.getSuperclass();
        if (!Object.class.equals(superClass)) {
            findDeclaredFields(superClass, list);
        }
    }

    public static FieldBuilder createFieldBuilder(Field field) {
        return new FieldBuilder(field);
    }

    public static class FieldBuilder {

        private Field field;

        public FieldBuilder(Field field) {
            this.field = field;
        }

        public FieldBuilder accessible() {
            ReflectionUtils.makeAccessible(field);
            return this;
        }

        public Object getValue(Object target) {
            Object value = ReflectionUtils.getField(field, target);
            return value;
        }

        public void setValue(Object target, Object value) {
            ReflectionUtils.setField(field, target, value);
        }
    }
}

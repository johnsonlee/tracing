package io.johnsonlee.tracing.util;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassFinder {

    private static final Map<String, Class<?>> CLASSES = new ConcurrentHashMap<>();

    private static final Class<? extends Annotation> CLASS_KOTLIN_METADATA = findClass("kotlin.Metadata");

    @SuppressWarnings("unchecked")
    public static <T> Class<T> findClass(final String fullQualifiedName) {
        Class<?> clazz = CLASSES.get(fullQualifiedName);
        if (null != clazz) {
            return (Class<T>) clazz;
        }

        try {
            clazz = Class.forName(fullQualifiedName);
            CLASSES.put(fullQualifiedName, clazz);
            return (Class<T>) clazz;
        } catch (final Throwable t) {
            return null;
        }
    }

    static String getFileName(final Class<?> clazz) {
        final String fqcn = clazz.getName();
        final int dot = fqcn.lastIndexOf('.');
        final int dollar = fqcn.lastIndexOf('$');
        return fqcn.substring(dot + 1, dollar > dot + 1 ? dollar : fqcn.length()) + getFileExtension(clazz);
    }

    static boolean isKotlinClass(final Class<?> clazz) {
        return null != clazz
                && null != CLASS_KOTLIN_METADATA
                && clazz.isAnnotationPresent(CLASS_KOTLIN_METADATA);
    }

    private static String getFileExtension(final Class<?> clazz) {
        return isKotlinClass(clazz) ? ".kt" : ".java";
    }

    private ClassFinder() {
        throw new UnsupportedOperationException();
    }

}

package com.framework.core;

import com.framework.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class FrameworkContext {
    private Map<Class<?>, Object> beans = new HashMap<>();

    public void registerBean(Class<?> clazz, Object instance) {
        beans.put(clazz, instance);
    }

    public <T> T getBean(Class<T> clazz) throws Exception {
        if (beans.containsKey(clazz)) {
            return clazz.cast(beans.get(clazz));
        }

        T instance = clazz.getDeclaredConstructor().newInstance();
        beans.put(clazz, instance);

        injectDependencies(instance);

        return instance;
    }

    private void injectDependencies(Object instance) throws Exception {
        Field[] fields = instance.getClass().getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Autowired.class)) {
                Object dependency = getBean(field.getType());

                field.setAccessible(true);
                field.set(instance, dependency);
            }
        }
    }
}
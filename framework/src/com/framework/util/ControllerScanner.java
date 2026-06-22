package com.framework.util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.framework.annotation.Controller;

public class ControllerScanner {

    public static List<Class<?>> scan(String packageName) throws Exception {
        List<Class<?>> controllers = new ArrayList<>();

        String path = packageName.replace(".", "/");

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);

        if (resource == null) {
            throw new RuntimeException("Package introuvable : " + packageName);
        }

        File directory = new File(resource.toURI());
        File[] files = directory.listFiles();

        if (files == null) {
            return controllers;
        }

        for (File file : files) {
            if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");

                Class<?> clazz = Class.forName(className);

                if (clazz.isAnnotationPresent(Controller.class)) {
                    controllers.add(clazz);
                }
            }
        }

        return controllers;
    }
}
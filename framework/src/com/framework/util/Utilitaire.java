package com.framework.util;

import com.framework.annotation.Controller;
import com.framework.annotation.Url;
import com.framework.core.Mapping;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;

public class Utilitaire {

    public static void getUrlAndMethod(String packageName, String realPath, HashMap<String, Mapping> urlMapping)
            throws Exception {

        File folder = new File(realPath);

        if (!folder.exists()) {
            throw new Exception("Dossier controller introuvable : " + realPath);
        }

        scanFolder(folder, packageName, urlMapping);
    }

    private static void scanFolder(File folder, String packageName, HashMap<String, Mapping> urlMapping)
            throws Exception {

        File[] files = folder.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {

            if (file.isDirectory()) {
                scanFolder(file, packageName + "." + file.getName(), urlMapping);
            }

            if (file.isFile() && file.getName().endsWith(".class") && !file.getName().contains("$")) {
                String className = packageName + "."
                        + file.getName().substring(0, file.getName().length() - 6);

                Class<?> clazz = Class.forName(className);

                if (clazz.isAnnotationPresent(Controller.class)) {
                    Method[] methods = clazz.getDeclaredMethods();

                    for (Method method : methods) {
                        if (method.isAnnotationPresent(Url.class)) {
                            Url urlAnnotation = method.getAnnotation(Url.class);

                            String url = urlAnnotation.value();

                            if (!url.startsWith("/")) {
                                url = "/" + url;
                            }

                            if (urlMapping.containsKey(url)) {
                                throw new Exception("URL déjà utilisée : " + url);
                            }

                            urlMapping.put(url, new Mapping(clazz, method));
                        }
                    }
                }
            }
        }
    }
}
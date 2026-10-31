package com.framework.util;

import com.framework.annotation.Param;
import com.framework.core.MapFile;
import com.framework.core.Model;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import jakarta.servlet.http.HttpSession;

public class ParameterBinder {

    public static Object[] bind(
            Method method,
            HttpServletRequest request,
            Model model
    ) {
        Parameter[] parameters = method.getParameters();
        Object[] arguments = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> type = parameter.getType();

            if (type == Model.class) {
                arguments[i] = model;
                continue;
            }
            // Sprint 9 : injection de la session dans le contrôleur.
             if (type == HttpSession.class) {
            arguments[i] = request.getSession();
            continue;
            }

            // Sprint 7 bis : objet contenant plusieurs attributs.
            if (!isSimple(type) && type != MapFile.class) {
                arguments[i] = bindObject(type, request);
                continue;
            }

            Param param = parameter.getAnnotation(Param.class);

            if (param == null || param.value().trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "@Param obligatoire pour : "
                                + parameter.getName()
                );
            }

            String name = param.value();

            // Sprint 8 : fichier reçu directement en paramètre.
            if (type == MapFile.class) {
                arguments[i] = bindFile(name, request);
                continue;
            }

            String value = request.getParameter(name);

            if (value == null) {
                if (type == int.class) {
                    throw new IllegalArgumentException(
                            "Parametre obligatoire manquant : " + name
                    );
                }
                continue;
            }

            arguments[i] = convert(value, type, name);
        }

        return arguments;
    }

    private static boolean isSimple(Class<?> type) {
        return type == String.class
                || type == int.class
                || type == Integer.class;
    }

    private static Object convert(
            String value,
            Class<?> type,
            String name
    ) {
        if (type == String.class) {
            return value;
        }

        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Un entier est attendu pour : " + name,
                    e
            );
        }
    }

    private static Object bindObject(
            Class<?> type,
            HttpServletRequest request
    ) {
        try {
            Object object = type.getConstructor().newInstance();

            for (Field field : type.getDeclaredFields()) {
                int modifiers = field.getModifiers();

                if (Modifier.isStatic(modifiers)
                        || Modifier.isFinal(modifiers)
                        || field.isSynthetic()) {
                    continue;
                }

                Object value;

                // Sprint 8 : fichier dans un objet, comme Employe.
                if (field.getType() == MapFile.class) {
                    value = bindFile(field.getName(), request);

                    if (value == null) {
                        continue;
                    }

                } else {
                    String text =
                            request.getParameter(field.getName());

                    if (text == null) {
                        continue;
                    }

                    if (!isSimple(field.getType())) {
                        throw new IllegalArgumentException(
                                "Type non accepte pour : "
                                        + field.getName()
                        );
                    }

                    value = convert(
                            text,
                            field.getType(),
                            field.getName()
                    );
                }

                field.setAccessible(true);
                field.set(object, value);
            }

            return object;

        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(
                    "Impossible de creer ou remplir : "
                            + type.getSimpleName(),
                    e
            );
        }
    }

    private static MapFile bindFile(
            String name,
            HttpServletRequest request
    ) {
        String contentType = request.getContentType();

        // Une requête GET classique ne contient pas de fichier.
        if (contentType == null
                || !contentType.regionMatches(
                        true, 0, "multipart/form-data", 0, 19
                )) {
            return null;
        }

        try {
            Part part = request.getPart(name);

            if (part == null) {
                return null;
            }

            String filename = part.getSubmittedFileName();

            // Aucun fichier sélectionné.
            if (filename == null || filename.isBlank()) {
                return null;
            }

            // Conserver uniquement le nom, sans chemin client.
            filename = filename.replace('\\', '/');
            filename = filename.substring(
                    filename.lastIndexOf('/') + 1
            );

            try (InputStream input = part.getInputStream()) {
                return new MapFile(
                        filename,
                        part.getContentType(),
                        input.readAllBytes()
                );
            }

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Impossible de recevoir le fichier : " + name
                            + ". Verifiez la configuration multipart"
                            + " et la taille du fichier.",
                    e
            );
        }
    }
}
package com.framework.util;

import com.framework.annotation.Param;
import com.framework.core.Model;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

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

            // Le framework fournit le Model.
            if (type == Model.class) {
                arguments[i] = model;
                continue;
            }

            // Sprint 7 bis : creer et remplir un objet.
            if (type != String.class
                    && type != int.class
                    && type != Integer.class) {
                arguments[i] = bindObject(type, request);
                continue;
            }

            // Sprint 7 : lire le nom du parametre simple.
            Param param = parameter.getAnnotation(Param.class);

            if (param == null || param.value().trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Chaque parametre simple doit avoir @Param."
                );
            }

            String name = param.value();
            String value = request.getParameter(name);

            if (value == null) {
                if (type == int.class) {
                    throw new IllegalArgumentException(
                            "Parametre obligatoire manquant : " + name
                    );
                }
                continue;
            }

            if (type == String.class) {
                arguments[i] = value;
            } else {
                try {
                    arguments[i] = Integer.valueOf(value.trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Le parametre " + name
                                    + " doit etre un nombre entier.",
                            e
                    );
                }
            }
        }

        return arguments;
    }

    private static Object bindObject(
            Class<?> type,
            HttpServletRequest request
    ) {
        try {
            // Constructeur public sans argument.
            Object object = type.getConstructor().newInstance();

            for (Field field : type.getDeclaredFields()) {
                int modifiers = field.getModifiers();

                if (Modifier.isStatic(modifiers)
                        || Modifier.isFinal(modifiers)
                        || field.isSynthetic()) {
                    continue;
                }

                String value = request.getParameter(field.getName());

                // Garder la valeur initiale si la donnee est absente.
                if (value == null) {
                    continue;
                }

                field.setAccessible(true);

                if (field.getType() == String.class) {
                    field.set(object, value);

                } else if (field.getType() == int.class
                        || field.getType() == Integer.class) {
                    field.set(object, Integer.valueOf(value.trim()));

                } else {
                    throw new IllegalArgumentException(
                            "Type non accepte pour : " + field.getName()
                    );
                }
            }

            return object;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Un entier est attendu.", e
            );
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(
                    "Impossible de creer ou remplir : "
                            + type.getSimpleName(),
                    e
            );
        }
    }
}
package com.framework.util;

import com.framework.annotation.Param;
import com.framework.core.Model;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ParameterBinder {

    public static Object[] bind(
            Method method,
            HttpServletRequest request,
            Model model
    ) {
        // Lire les paramètres de la méthode.
        Parameter[] parameters = method.getParameters();

        // Toutes les cases sont null au départ.
        Object[] arguments = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> type = parameter.getType();

            // Fournir directement le Model.
            if (type == Model.class) {
                arguments[i] = model;
                continue;
            }

            // Sprint 7 bis : créer et remplir un objet.
            if (type != String.class
             && type != int.class
             && type != Integer.class) {
            arguments[i] = bindObject(type, request);
            continue;
            }

            // Lire le nom indiqué dans @Param.
            Param param = parameter.getAnnotation(Param.class);

            if (param == null || param.value().trim().isEmpty()) {
                throw new IllegalArgumentException("@Param obligatoire.");
            }

            // Récupérer la donnée GET ou POST.
            String value = request.getParameter(param.value());

            if (value == null) {
                if (type == int.class) {
                    throw new IllegalArgumentException(
                            "Paramètre manquant : " + param.value()
                    );
                }
                continue;
            }

            // Préparer la valeur au bon type.
            if (type == String.class) {
                arguments[i] = value;
            } else {
                try {
                    arguments[i] = Integer.valueOf(value.trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Entier attendu pour : " + param.value(), e
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
        // Créer l'objet avec son constructeur public sans argument.
        Object object = type.getConstructor().newInstance();

        // Parcourir ses attributs.
        for (Field field : type.getDeclaredFields()) {
            int modifiers = field.getModifiers();

            if (Modifier.isStatic(modifiers)
                    || Modifier.isFinal(modifiers)
                    || field.isSynthetic()) {
                continue;
            }

            // Chercher la donnée portant le nom de l'attribut.
            String value = request.getParameter(field.getName());

            if (value == null) {
                continue;
            }

            field.setAccessible(true);

            // Convertir et affecter la valeur.
            if (field.getType() == String.class) {
                field.set(object, value);

            } else if (field.getType() == int.class
                    || field.getType() == Integer.class) {
                field.set(object, Integer.valueOf(value.trim()));

            } else {
                throw new IllegalArgumentException(
                        "Type non accepté pour : " + field.getName()
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
                "Impossible de créer ou remplir : "
                        + type.getSimpleName(),
                e
        );
    }
}
}
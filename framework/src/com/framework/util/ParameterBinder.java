package com.framework.util;

import com.framework.annotation.Param;
import com.framework.core.Model;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class ParameterBinder {

    public static Object[] bind(
            Method method,
            HttpServletRequest request,
            Model model
    ) {
        // obtient les informations venant de string nom,age,model
        Parameter[] parameters = method.getParameters();
        Object[] arguments = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class<?> type = parameter.getType();

            // Le framework fournit lui-même le Model.
            if (type == Model.class) {
                arguments[i] = model;
                continue;
            }

            // Cette version accepte uniquement String, int et Integer.
            if (type != String.class
                    && type != int.class
                    && type != Integer.class) {
                throw new IllegalArgumentException(
                        "Type non pris en charge : " + type.getName()
                );
            }

            Param param = parameter.getAnnotation(Param.class);

            if (param == null || param.value().trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Chaque paramètre doit avoir @Param."
                );
            }

            String name = param.value();
            String value = request.getParameter(name);

            // Les arguments sont null par défaut.
            if (value == null) {
                if (type == int.class) {
                    throw new IllegalArgumentException(
                            "Paramètre obligatoire manquant : " + name
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
                            "Le paramètre " + name
                            + " doit être un nombre entier.",
                            e
                    );
                }
            }
        }

        return arguments;
    }
}
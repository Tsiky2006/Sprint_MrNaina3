package com.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//Définition de l'annotation 
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
// indique chaque nom de donnée à récuperer 
public @interface Param {
    String value();
}
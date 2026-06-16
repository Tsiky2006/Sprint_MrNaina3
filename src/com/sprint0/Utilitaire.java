package com.sprint0;

public class Utilitaire {
    public static String salut(String nom) {
        if (nom == null || nom.isEmpty()) return "Bonjour";
        return "Bonjour, " + nom + "!";
    }
}

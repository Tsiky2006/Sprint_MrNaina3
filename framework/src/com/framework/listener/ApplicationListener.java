package com.framework.listener;

import com.framework.core.Mapping;
import com.framework.util.Utilitaire;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import java.util.HashMap;

public class ApplicationListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            ServletContext context = sce.getServletContext();

            String controllerPackage = context.getInitParameter("controller");

            if (controllerPackage == null || controllerPackage.trim().isEmpty()) {
                throw new Exception("Le paramètre 'controller' est introuvable dans web.xml");
            }

            String packagePath = controllerPackage.replace(".", "/");

            String realPath = context.getRealPath("/WEB-INF/classes/" + packagePath);

            HashMap<String, Mapping> urlMapping = new HashMap<>();

            Utilitaire.getUrlAndMethod(controllerPackage, realPath, urlMapping);

            context.setAttribute("urlMapping", urlMapping);

            context.setAttribute("viewPrefix", context.getInitParameter("viewPrefix"));
            context.setAttribute("viewSuffix", context.getInitParameter("viewSuffix"));

            System.out.println("====================================");
            System.out.println("Framework initialise");
            System.out.println("Package controller : " + controllerPackage);
            System.out.println("Nombre URL trouvees : " + urlMapping.size());
            System.out.println("URLs disponibles : " + urlMapping.keySet());
            System.out.println("====================================");

        } catch (Exception e) {
            throw new RuntimeException("Erreur initialisation framework", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
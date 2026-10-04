package com.framework.listener;

import com.framework.core.DatabaseConfig;
import com.framework.core.FrameworkContext;
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
            String repositoryPackage = context.getInitParameter("repository");

            if (controllerPackage == null || controllerPackage.trim().isEmpty()) {
                throw new Exception("Le paramètre controller est introuvable dans web.xml");
            }

            String jdbcDriver = context.getInitParameter("jdbcDriver");
            String jdbcUrl = context.getInitParameter("jdbcUrl");
            String jdbcUser = context.getInitParameter("jdbcUser");
            String jdbcPassword = context.getInitParameter("jdbcPassword");

            FrameworkContext frameworkContext = new FrameworkContext();

            DatabaseConfig databaseConfig = new DatabaseConfig(
                    jdbcDriver,
                    jdbcUrl,
                    jdbcUser,
                    jdbcPassword
            );

            frameworkContext.registerBean(DatabaseConfig.class, databaseConfig);

            if (repositoryPackage != null && !repositoryPackage.trim().isEmpty()) {
                String repositoryPath = repositoryPackage.replace(".", "/");
                String repositoryRealPath = context.getRealPath("/WEB-INF/classes/" + repositoryPath);

                Utilitaire.scanRepositories(repositoryPackage, repositoryRealPath, frameworkContext);
            }

            String controllerPath = controllerPackage.replace(".", "/");
            String controllerRealPath = context.getRealPath("/WEB-INF/classes/" + controllerPath);

            HashMap<String, Mapping> urlMapping = new HashMap<>();

            Utilitaire.getUrlAndMethod(controllerPackage, controllerRealPath, urlMapping);

            for (Mapping mapping : urlMapping.values()) {
                frameworkContext.getBean(mapping.getControllerClass());
            }

            context.setAttribute("urlMapping", urlMapping);
            context.setAttribute("frameworkContext", frameworkContext);
            context.setAttribute("viewPrefix", context.getInitParameter("viewPrefix"));
            context.setAttribute("viewSuffix", context.getInitParameter("viewSuffix"));

            System.out.println("====================================");
            System.out.println("Framework initialise Sprint 5-2");
            System.out.println("Package controller : " + controllerPackage);
            System.out.println("Package repository : " + repositoryPackage);
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
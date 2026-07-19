package com.framework;

import com.framework.core.Mapping;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class FrontController extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        try {
            HashMap<String, Mapping> urlMapping =
                    (HashMap<String, Mapping>) getServletContext().getAttribute("urlMapping");

            if (urlMapping == null) {
                throw new Exception("urlMapping introuvable dans ServletContext. Vérifie ApplicationListener.");
            }

            String url = request.getRequestURI();
            String contextPath = request.getContextPath();

            if (url.startsWith(contextPath)) {
                url = url.substring(contextPath.length());
            }

            if (url == null || url.trim().isEmpty()) {
                url = "/";
            }

            Mapping mapping = urlMapping.get(url);

            if (mapping == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);

                response.getWriter().println("<h1>URL inconnue</h1>");
                response.getWriter().println("<p>URL demandée : " + url + "</p>");
                response.getWriter().println("<h3>URLs disponibles :</h3>");
                response.getWriter().println("<ul>");

                for (String key : urlMapping.keySet()) {
                    response.getWriter().println("<li>" + key + "</li>");
                }

                response.getWriter().println("</ul>");
                return;
            }

            Class<?> controllerClass = mapping.getControllerClass();
            Method method = mapping.getMethod();

            Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

            Object result = method.invoke(controllerInstance);

            response.getWriter().println("<h1>Résultat Sprint 4</h1>");
            response.getWriter().println("<p>URL appelée : " + url + "</p>");
            response.getWriter().println("<p>Controller : " + controllerClass.getName() + "</p>");
            response.getWriter().println("<p>Méthode : " + method.getName() + "</p>");

            if (result != null) {
                response.getWriter().println("<p>Résultat : " + result.toString() + "</p>");
            } else {
                response.getWriter().println("<p>Résultat : null</p>");
            }

        } catch (Exception e) {
            throw new ServletException("Erreur dans FrontController", e);
        }
    }
}
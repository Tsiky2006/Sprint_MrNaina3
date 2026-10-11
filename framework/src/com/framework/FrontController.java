package com.framework;

import com.framework.core.FrameworkContext;
import com.framework.core.Mapping;
import com.framework.core.Model;
import com.framework.util.ParameterBinder;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


import com.framework.annotation.WebApi;
import com.google.gson.Gson;

public class FrontController extends HttpServlet {

    @SuppressWarnings("unchecked")
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        try {
            HashMap<String, Mapping> urlMapping =
                    (HashMap<String, Mapping>) getServletContext().getAttribute("urlMapping");

            FrameworkContext frameworkContext =
                    (FrameworkContext) getServletContext().getAttribute("frameworkContext");

            if (urlMapping == null) {
                throw new Exception("urlMapping introuvable dans ServletContext.");
            }

            if (frameworkContext == null) {
                throw new Exception("frameworkContext introuvable dans ServletContext.");
            }

            String viewPrefix = (String) getServletContext().getAttribute("viewPrefix");
            String viewSuffix = (String) getServletContext().getAttribute("viewSuffix");

            if (viewPrefix == null || viewPrefix.trim().isEmpty()) {
                viewPrefix = "/WEB-INF/views/";
            }

            if (viewSuffix == null || viewSuffix.trim().isEmpty()) {
                viewSuffix = ".jsp";
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

            Object controllerInstance = frameworkContext.getBean(controllerClass);
        
        // Sprint 7 : récupérer et convertir les paramètres GET ou POST.
        Model model = new Model();
        Object[] arguments;

        try {
            arguments = ParameterBinder.bind(method, request, model);
        } catch (IllegalArgumentException e) {
             response.sendError(
            HttpServletResponse.SC_BAD_REQUEST,
            e.getMessage()
        );
         return;
        }

        // Appeler la méthode avec les valeurs préparées.
        Object result = method.invoke(controllerInstance, arguments);

            // Sprint 6 : annotation sur la méthode OU sur la classe.
            if (method.isAnnotationPresent(WebApi.class)
                    || controllerClass.isAnnotationPresent(WebApi.class)) {

                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(new Gson().toJson(result));

                return; // Ne pas ouvrir de JSP.
            }

            // Sans @WebApi : continuer vers la vue JSP.
            if (!(result instanceof String)) {
                throw new Exception("La méthode " + method.getName()
                        + " doit retourner un String");
            }

          

            for (Map.Entry<String, Object> entry : model.getData().entrySet()) {
                request.setAttribute(entry.getKey(), entry.getValue());
            }

            String viewName = (String) result;

            if (viewName.startsWith("/")) {
                viewName = viewName.substring(1);
            }

            String viewPath = viewPrefix + viewName + viewSuffix;

            RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
            dispatcher.forward(request, response);

        } catch (Exception e) {
            throw new ServletException("Erreur dans FrontController", e);
        }
    }
}
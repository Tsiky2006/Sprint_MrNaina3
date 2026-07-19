package com.framework;

import com.framework.core.Mapping;
import com.framework.core.Model;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class FrontController extends HttpServlet {

    @SuppressWarnings("unchecked")
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

            Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

            Model model = new Model();
            Object result;

            if (method.getParameterCount() == 0) {
                result = method.invoke(controllerInstance);
            } else if (
                    method.getParameterCount() == 1 &&
                    method.getParameterTypes()[0].equals(Model.class)
            ) {
                result = method.invoke(controllerInstance, model);
            } else {
                throw new Exception("La méthode " + method.getName()
                        + " doit avoir 0 paramètre ou 1 paramètre de type Model");
            }

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
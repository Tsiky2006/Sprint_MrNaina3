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
import com.framework.annotation.WebApi;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

public class FrontController extends HttpServlet {

    @SuppressWarnings("unchecked")
    @Override
    protected void service(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        try {
            Map<String, Mapping> urlMapping =
                    (Map<String, Mapping>) getServletContext()
                            .getAttribute("urlMapping");

            FrameworkContext frameworkContext =
                    (FrameworkContext) getServletContext()
                            .getAttribute("frameworkContext");

            if (urlMapping == null || frameworkContext == null) {
                throw new ServletException(
                        "Le framework n'est pas initialisÃ©."
                );
            }

            String url = request.getRequestURI()
                    .substring(request.getContextPath().length());

            if (url.isEmpty()) {
                url = "/";
            }

            Mapping mapping = urlMapping.get(url);

            if (mapping == null) {
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "URL inconnue"
                );
                return;
            }

            // VÃ©rification HTTP pour les deux routes de cet exemple.
            // Le mapping gÃ©nÃ©ral GET/POST reste Ã  ajouter au framework.
            if ("/emp/new".equals(url)
                    && !"GET".equals(request.getMethod())) {
                response.setHeader("Allow", "GET");
                response.sendError(
                        HttpServletResponse.SC_METHOD_NOT_ALLOWED
                );
                return;
            }

            if ("/emp/save".equals(url)
                    && !"POST".equals(request.getMethod())) {
                response.setHeader("Allow", "POST");
                response.sendError(
                        HttpServletResponse.SC_METHOD_NOT_ALLOWED
                );
                return;
            }

            Method method = mapping.getMethod();

            Object controllerInstance =
                    frameworkContext.getBean(mapping.getControllerClass());

            Model model = new Model();
            Object[] arguments;

            try {
                // Sprint 7 : rÃ©cupÃ©rer et convertir les paramÃ¨tres.
                arguments = ParameterBinder.bind(method, request, model);
            } catch (IllegalArgumentException e) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        e.getMessage()
                );
                return;
            }

            // Appeler la mÃ©thode avec les valeurs rÃ©cupÃ©rÃ©es.
            Object result = method.invoke(controllerInstance, arguments);
            if ((method.isAnnotationPresent(WebApi.class) || mapping.getControllerClass().isAnnotationPresent(WebApi.class))) {
             response.setContentType("application/json;charset=UTF-8");
             response.getWriter().write(new Gson().toJson(result));
             return;
}

            if (!(result instanceof String)) {
                throw new ServletException(
                        "La mÃ©thode " + method.getName()
                        + " doit retourner un String."
                );
            }

            for (Map.Entry<String, Object> entry
                    : model.getData().entrySet()) {
                request.setAttribute(entry.getKey(), entry.getValue());
            }

            String viewPrefix = (String) getServletContext()
                    .getAttribute("viewPrefix");

            String viewSuffix = (String) getServletContext()
                    .getAttribute("viewSuffix");

            if (viewPrefix == null || viewPrefix.trim().isEmpty()) {
                viewPrefix = "/WEB-INF/views/";
            }

            if (viewSuffix == null || viewSuffix.trim().isEmpty()) {
                viewSuffix = ".jsp";
            }

            String viewName = (String) result;

            if (viewName.startsWith("/")) {
                viewName = viewName.substring(1);
            }

            String viewPath = viewPrefix + viewName + viewSuffix;

            RequestDispatcher dispatcher =
                    request.getRequestDispatcher(viewPath);

            dispatcher.forward(request, response);

        } catch (Exception e) {
            throw new ServletException("Erreur dans FrontController", e);
        }
    }
}

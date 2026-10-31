package com.framework;

import com.framework.annotation.WebApi;
import com.framework.core.FrameworkContext;
import com.framework.core.Mapping;
import com.framework.core.Model;
import com.framework.util.ParameterBinder;
import com.google.gson.Gson;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.framework.annotation.Authenticated;
import jakarta.servlet.http.HttpSession;
import com.framework.annotation.RolesAllowed;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Collection;

public class FrontController extends HttpServlet {

        private String authSessionAttribute;
        private String rolesSessionAttribute;


        @Override
public void init() throws ServletException {
    super.init();

    authSessionAttribute = getServletContext()
            .getInitParameter("auth-session-attribute");

    rolesSessionAttribute = getServletContext()
            .getInitParameter("role-session-attribute");

    if (authSessionAttribute == null
            || authSessionAttribute.trim().isEmpty()) {
        throw new ServletException(
                "Configuration manquante : auth-session-attribute"
        );
    }

    if (rolesSessionAttribute == null
            || rolesSessionAttribute.trim().isEmpty()) {
        throw new ServletException(
                "Configuration manquante : role-session-attribute"
        );
    }

    authSessionAttribute = authSessionAttribute.trim();
    rolesSessionAttribute = rolesSessionAttribute.trim();
}


@SuppressWarnings("unchecked")
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
                    "Le framework n'est pas initialise."
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

        Class<?> controllerClass = mapping.getControllerClass();
        Method method = mapping.getMethod();


// Sprints 10 et 10 bis : verifier connexion et roles.
RolesAllowed rolesAllowed =
        method.getAnnotation(RolesAllowed.class);

if (method.isAnnotationPresent(Authenticated.class)
        || rolesAllowed != null) {

    HttpSession session = request.getSession(false);

    // Verifier d'abord que l'utilisateur est connecte.
    if (session == null
            || session.getAttribute(authSessionAttribute) == null) {

        response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Vous devez etre connecte."
        );
        return;
    }

    // Puis verifier les roles si l'action en exige.
    if (rolesAllowed != null) {

        Object userRoles =
                session.getAttribute(rolesSessionAttribute);

        boolean autorise = false;

        for (String requiredRole : rolesAllowed.value()) {

            if (userRoles instanceof String
                    && requiredRole.equals(userRoles)) {
                autorise = true;
                break;
            }

            if (userRoles instanceof Collection<?>
                    && ((Collection<?>) userRoles)
                            .contains(requiredRole)) {
                autorise = true;
                break;
            }
        }

        if (!autorise) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Vous ne possedez pas le role necessaire."
            );
            return;
        }
    }
}

        Object controllerInstance =
        frameworkContext.getBean(controllerClass);

            // Sprints 7 et 7 bis : preparer les arguments.
            // miantso ny methode avec l'objet 
            Model model = new Model();
            Object[] arguments;

            try {
                arguments = ParameterBinder.bind(
                        method, request, model
                );
            } catch (IllegalArgumentException e) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        e.getMessage()
                );
                return;
            }

            // Appeler l'action du controleur.
            Object result = method.invoke(
                    controllerInstance, arguments
            );

            // Sprint 6 : retourner le resultat en JSON.
            if (method.isAnnotationPresent(WebApi.class)
                    || controllerClass.isAnnotationPresent(WebApi.class)) {

                response.setContentType(
                        "application/json;charset=UTF-8"
                );
                response.getWriter().write(
                        new Gson().toJson(result)
                );
                return;
            }

            // Sans WebApi, le resultat indique une vue JSP.
            if (!(result instanceof String)) {
                throw new ServletException(
                        "La methode " + method.getName()
                                + " doit retourner un String."
                );
            }

            for (Map.Entry<String, Object> entry
                    : model.getData().entrySet()) {
                request.setAttribute(
                        entry.getKey(), entry.getValue()
                );
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
            throw new ServletException(
                    "Erreur dans FrontController", e
            );
        }
    }
}
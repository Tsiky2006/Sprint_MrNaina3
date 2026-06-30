package com.framework;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.framework.annotation.Controller;
import com.framework.annotation.Param;
import com.framework.annotation.UrlMapping;
import com.framework.core.Mapping;
import com.framework.util.ControllerScanner;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {

    private List<Class<?>> controllers;
    private Map<String, Mapping> urlMappings;

    @Override
    public void init() throws ServletException {
        try {
            String packageName = getServletContext().getInitParameter("controller-package");

            if (packageName == null || packageName.isEmpty()) {
                throw new ServletException("Paramètre controller-package manquant dans web.xml");
            }

            controllers = ControllerScanner.scan(packageName);
            urlMappings = new HashMap<>();

            for (Class<?> controllerClass : controllers) {

                Method[] methods = controllerClass.getDeclaredMethods();

                for (Method method : methods) {

                    if (method.isAnnotationPresent(UrlMapping.class)) {

                        UrlMapping urlMapping = method.getAnnotation(UrlMapping.class);
                        String url = urlMapping.value();

                        if (url == null || url.trim().isEmpty()) {
                            url = "/" + controllerClass.getSimpleName() + "/" + method.getName();
                        }

                        urlMappings.put(
                                url,
                                new Mapping(
                                        controllerClass.getName(),
                                        method.getName()
                                )
                        );
                    }
                }
            }

            System.out.println("=== Controllers trouves ===");
            for (Class<?> controller : controllers) {
                System.out.println(controller.getName());
            }

            System.out.println("=== UrlMappings trouves ===");
            for (String url : urlMappings.keySet()) {
                Mapping mapping = urlMappings.get(url);
                System.out.println(url + " -> " + mapping.getClassName() + "." + mapping.getMethodName());
            }

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.substring(contextPath.length());

        PrintWriter out = response.getWriter();

        try {
            Mapping mapping = urlMappings.get(path);

            if (mapping == null) {
                out.println("<html>");
                out.println("<body>");
                out.println("<h1>URL inconnue</h1>");
                out.println("<p>URL demandée : " + path + "</p>");
                out.println("<h3>URLs disponibles :</h3>");
                out.println("<ul>");

                for (String url : urlMappings.keySet()) {
                    out.println("<li>" + url + "</li>");
                }

                out.println("</ul>");
                out.println("</body>");
                out.println("</html>");
                return;
            }

            Class<?> clazz = Class.forName(mapping.getClassName());
            Object controllerInstance = clazz.getDeclaredConstructor().newInstance();

            Method methodToCall = null;

            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(mapping.getMethodName())) {
                    methodToCall = method;
                    break;
                }
            }

            if (methodToCall == null) {
                out.println("<h1>Méthode introuvable</h1>");
                return;
            }

            Object[] arguments = buildArguments(methodToCall, request);
            Object result = methodToCall.invoke(controllerInstance, arguments);

            out.println("<html>");
            out.println("<body>");
            out.println("<h1>Sprint2 - UrlMapping OK</h1>");
            out.println("<p>URL appelée : " + path + "</p>");
            out.println("<p>Controller : " + mapping.getClassName() + "</p>");
            out.println("<p>Méthode : " + mapping.getMethodName() + "</p>");
            out.println("<p>Résultat : " + result + "</p>");
            out.println("</body>");
            out.println("</html>");

        } catch (Exception e) {
            out.println("<h1>Erreur</h1>");
            out.println("<pre>");
            e.printStackTrace(out);
            out.println("</pre>");
        }
    }

    private Object[] buildArguments(Method method, HttpServletRequest request) {
        Annotation[][] annotations = method.getParameterAnnotations();
        Class<?>[] parameterTypes = method.getParameterTypes();

        Object[] arguments = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            String paramName = null;

            for (Annotation annotation : annotations[i]) {
                if (annotation instanceof Param) {
                    paramName = ((Param) annotation).value();
                    break;
                }
            }

            String value = request.getParameter(paramName);

            if (parameterTypes[i] == int.class || parameterTypes[i] == Integer.class) {
                arguments[i] = Integer.parseInt(value);
            } else if (parameterTypes[i] == double.class || parameterTypes[i] == Double.class) {
                arguments[i] = Double.parseDouble(value);
            } else {
                arguments[i] = value;
            }
        }

        return arguments;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
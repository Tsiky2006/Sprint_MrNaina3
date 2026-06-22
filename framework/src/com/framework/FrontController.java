package com.framework;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import com.framework.annotation.Controller;
import com.framework.annotation.GetMapping;
import com.framework.annotation.Param;
import com.framework.annotation.RequestMapping;
import com.framework.util.ControllerScanner;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {

    private List<Class<?>> controllers;

    @Override
    public void init() throws ServletException {
        try {
            String packageName = getServletContext().getInitParameter("controller-package");

            if (packageName == null || packageName.trim().isEmpty()) {
                throw new ServletException("Paramètre controller-package manquant dans web.xml");
            }

            controllers = ControllerScanner.scan(packageName);

            System.out.println("Controllers trouvés : " + controllers.size());

            for (Class<?> controller : controllers) {
                System.out.println("Controller : " + controller.getName());
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
            for (Class<?> clazz : controllers) {

                if (!clazz.isAnnotationPresent(Controller.class)) {
                    continue;
                }

                RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                String baseUrl = "";

                if (requestMapping != null) {
                    baseUrl = requestMapping.value();
                }

                Object controllerInstance = clazz.getDeclaredConstructor().newInstance();

                Method[] methods = clazz.getDeclaredMethods();

                for (Method method : methods) {

                    if (!method.isAnnotationPresent(GetMapping.class)) {
                        continue;
                    }

                    GetMapping getMapping = method.getAnnotation(GetMapping.class);
                    String fullUrl = baseUrl + getMapping.value();

                    if (fullUrl.equals(path)) {

                        Object[] arguments = buildArguments(method, request);
                        Object result = method.invoke(controllerInstance, arguments);

                        out.println("<html>");
                        out.println("<head>");
                        out.println("<meta charset='UTF-8'>");
                        out.println("<title>Mini Framework MVC</title>");
                        out.println("</head>");
                        out.println("<body>");
                        out.println("<h1>Mini Spring MVC OK</h1>");
                        out.println("<p>URL appelée : " + path + "</p>");
                        out.println("<p>Controller : " + clazz.getName() + "</p>");
                        out.println("<p>Méthode : " + method.getName() + "</p>");
                        out.println("<p>Résultat : " + result + "</p>");
                        out.println("</body>");
                        out.println("</html>");
                        return;
                    }
                }
            }

            out.println("<h1>404 - URL non trouvée</h1>");
            out.println("<p>URL : " + path + "</p>");

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
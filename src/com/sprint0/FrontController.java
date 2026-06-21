package com.sprint0;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.sprint0.annotation.Controller;
import com.sprint0.annotation.RequestMapping;
import com.sprint0.annotation.GetMapping;
import com.sprint0.annotation.Param;
import com.sprint0.controller.EmployeController;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        String url = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = url.substring(contextPath.length());

        PrintWriter out = response.getWriter();

        try {
            Class<?> clazz = EmployeController.class;

            if (clazz.isAnnotationPresent(Controller.class)) {

                RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                String baseUrl = requestMapping.value();

                Object controllerInstance = clazz.getDeclaredConstructor().newInstance();

                Method[] methods = clazz.getDeclaredMethods();

                for (Method method : methods) {

                    if (method.isAnnotationPresent(GetMapping.class)) {

                        GetMapping getMapping = method.getAnnotation(GetMapping.class);
                        String fullUrl = baseUrl + getMapping.value();

                        if (fullUrl.equals(path)) {

                            Object[] arguments = buildArguments(method, request);
                            Object result = method.invoke(controllerInstance, arguments);

                            out.println("<html>");
                            out.println("<body>");
                            out.println("<h1>Mini Spring MVC OK</h1>");
                            out.println("<p>URL appelée : " + path + "</p>");
                            out.println("<p>Résultat : " + result + "</p>");
                            out.println("</body>");
                            out.println("</html>");
                            return;
                        }
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
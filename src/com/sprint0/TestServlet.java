package com.sprint0;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TestServlet extends HttpServlet {

    protected void processRequest(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        String url = request.getRequestURL().toString();

        String message =
                Utilitaire.getMessage(url);

        PrintWriter out =
                response.getWriter();

        out.println("<html>");
        out.println("<head>");
        out.println("<title>Sprint 0</title>");
        out.println("</head>");
        out.println("<body>");

        out.println("<h1>Sprint 0 OK</h1>");

        out.println("<p>");
        out.println(message);
        out.println("</p>");

        out.println("</body>");
        out.println("</html>");
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        processRequest(request, response);
    }
}
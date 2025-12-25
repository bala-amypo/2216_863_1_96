package com.example.demo.servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class SimpleHelloServlet extends HttpServlet {
    private String message;

    @Override
    public void init(ServletConfig config) throws ServletException {
        if (config != null) {
            super.init(config);
        }
        message = "Hello from servlet";
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/plain");
        PrintWriter writer = resp.getWriter();
        writer.write(message);
        writer.flush();
    }
}
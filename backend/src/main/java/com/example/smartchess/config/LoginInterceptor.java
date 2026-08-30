package com.example.smartchess.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String path = request.getRequestURI();

        System.out.println("========== LOGIN INTERCEPTOR ==========");
        System.out.println("URI: " + path);
        System.out.println("Method: " + request.getMethod());
        System.out.println("ContextPath: " + request.getContextPath());

        if (path.startsWith("/api/")) {
            System.out.println("API BYPASS: TRUE");
            System.out.println("=======================================");
            return true;
        }

        System.out.println("API BYPASS: FALSE");

        Object user = request.getSession().getAttribute("user");

        System.out.println("Session user exists: " + (user != null));

        if (user == null) {
            System.out.println("REDIRECT TO LOGIN");
            System.out.println("=======================================");
            response.sendRedirect("/login");
            return false;
        }

        System.out.println("LOGIN ALLOWED");
        System.out.println("=======================================");

        return true;
    }
}

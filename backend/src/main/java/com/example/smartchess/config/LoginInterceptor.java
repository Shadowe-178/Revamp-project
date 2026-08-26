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

        // API 不走網頁登入攔截
        if (path.startsWith("/api/")) {
            return true;
        }

        // 一般網頁需要登入
        Object user = request.getSession().getAttribute("user");

        if (user == null) {
            response.sendRedirect("/login");
            return false;
        }

        return true;
    }
}

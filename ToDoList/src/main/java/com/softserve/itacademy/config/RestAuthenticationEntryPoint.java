package com.softserve.itacademy.config;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        if (request.getRequestURI().startsWith("/api")) {
            response.sendError(response.SC_UNAUTHORIZED,
                    "Sorry, You're not authorized to access this resource.");
        } else response.sendRedirect(request.getContextPath() + "/login");
    }
}
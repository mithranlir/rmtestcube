package com.qfs.sandbox.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

    public CustomAuthenticationEntryPoint() {
        super();
    }

    public CustomAuthenticationEntryPoint(String realmName) {
        setRealmName(realmName);
    }

    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authException) throws IOException, ServletException {
        if (isPreflight(request)) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } else {
            super.commence(request, response, authException);
        }
    }

    private boolean isPreflight(HttpServletRequest request) {
        return "OPTIONS".equals(request.getMethod());
    }

}
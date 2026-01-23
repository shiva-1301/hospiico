package com.hospitalfinder.backend.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MultiReadHttpServletRequestFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            // Only wrap requests that have a body (POST/PUT/PATCH) to avoid overhead on
            // GETs
            String method = httpRequest.getMethod();
            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {
                MultiReadRequestWrapper wrappedRequest = new MultiReadRequestWrapper(httpRequest);
                chain.doFilter(wrappedRequest, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}

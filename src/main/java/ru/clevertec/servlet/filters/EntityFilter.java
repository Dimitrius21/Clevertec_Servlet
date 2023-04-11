package ru.clevertec.servlet.filters;


import ru.clevertec.servlet.service.Heplper;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebFilter(filterName = "EntityFilter", urlPatterns = {"/api/card/*", "/api/product/*"})
public class EntityFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String method = req.getMethod().toLowerCase();
        if ("put".equals(method) || "post".equals(method)){
            String body = Heplper.getRequestBody(req);
            if (body.isEmpty() || body.isBlank() || !req.getContentType().equals("application/json")) {
                resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
                return;
            }
            req.setAttribute("body", body);
        }
        chain.doFilter(request, response);
    }
}

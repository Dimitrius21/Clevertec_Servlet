package ru.clevertec.servlet.filters;


import ru.clevertec.servlet.Constants;
import ru.clevertec.servlet.SQLPoolConnection;
import ru.clevertec.servlet.service.Heplper;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;


@WebFilter(filterName = "PoolFilter", urlPatterns = {"/*"})
public class PoolFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        HttpServletRequest req = (HttpServletRequest) request;
        if (Objects.isNull(req.getServletContext().getAttribute(Constants.CONNECTION))) {
            throw new ServletException("PoolConnection hasn't been created");
        }
        chain.doFilter(request, response);
    }
}

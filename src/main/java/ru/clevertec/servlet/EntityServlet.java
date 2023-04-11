package ru.clevertec.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.clevertec.servlet.dao.SimpleDao;
import ru.clevertec.servlet.dao.card.JdbcCardDao;
import ru.clevertec.servlet.dao.product.JdbcProductDao;
import ru.clevertec.servlet.enities.DiscountCard;
import ru.clevertec.servlet.enities.Product;
import ru.clevertec.servlet.exception.DBException;
import ru.clevertec.servlet.exception.NotPresentException;
import ru.clevertec.servlet.service.EntityService;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.*;

/**
 * Сервлет по работе с сущностями приложения (Продукт и Скидочная карта)
 */
@WebServlet("/api/*")
public class EntityServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter pw = resp.getWriter();
        Map.Entry<Integer, Object> result = EntityService.getEntities(req);
        ObjectMapper objectMapper = new ObjectMapper();
        int responseCode;
        try {
            if (result.getKey() == HttpServletResponse.SC_OK) {
                String response = objectMapper.writeValueAsString(result.getValue());
                resp.setContentType("application/json");
                pw.println(response);
            }
            responseCode = result.getKey();
        } catch (JsonProcessingException e) {
            responseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
        if (responseCode == HttpServletResponse.SC_OK) {
            resp.setStatus(responseCode);
        } else {
            resp.sendError(responseCode);
        }
        pw.close();
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] command = req.getPathInfo().split("/");
        SQLPoolConnection pool = ((SQLPoolConnection)req.getServletContext().getAttribute(Constants.CONNECTION));
        Connection con = pool.getConnection();
        try {
            SimpleDao dao = EntityService.getDao(command[1], con).getValue();
            dao.deleteById(Long.parseLong(command[2]));
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (DBException ex) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (NotPresentException ex) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        finally {
            pool.closeConnection(con);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] command = req.getPathInfo().split("/");
        SQLPoolConnection pool = ((SQLPoolConnection)req.getServletContext().getAttribute(Constants.CONNECTION));
        Connection con = pool.getConnection();
        Map.Entry<Class, SimpleDao> daoWithClass = EntityService.getDao(command[1], con);
        int responseCode = HttpServletResponse.SC_CREATED;
        try {
            String body = (String) req.getAttribute("body");
            ObjectMapper objectMapper = new ObjectMapper();
            Object entity = objectMapper.readValue(body, daoWithClass.getKey());
            entity = daoWithClass.getValue().create(entity);
            String response = objectMapper.writeValueAsString(entity);
            resp.setContentType("application/json");
            PrintWriter pw = resp.getWriter();
            pw.println(response);
            pw.close();
        } catch (JsonProcessingException ex) {
            responseCode = HttpServletResponse.SC_NOT_ACCEPTABLE;
        } catch (DBException | IOException e) {
            responseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
        pool.closeConnection(con);
        if (responseCode == HttpServletResponse.SC_CREATED) {
            resp.setStatus(responseCode);
        } else {
            resp.sendError(responseCode);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] command = req.getPathInfo().split("/");
        SQLPoolConnection pool = ((SQLPoolConnection)req.getServletContext().getAttribute(Constants.CONNECTION));
        Connection con = pool.getConnection();
        Map.Entry<Class, SimpleDao> daoWithClass = EntityService.getDao(command[1], con);
        int responseCode = HttpServletResponse.SC_OK;
        try {
            String body = (String) req.getAttribute("body");
            ObjectMapper objectMapper = new ObjectMapper();
            Object entity = objectMapper.readValue(body, daoWithClass.getKey());
            int q = daoWithClass.getValue().update(entity);
            if (q == 0) {
                responseCode = HttpServletResponse.SC_NOT_FOUND;
            }
        } catch (JsonProcessingException ex) {
            responseCode = HttpServletResponse.SC_NOT_ACCEPTABLE;
        } catch (DBException ex) {
            responseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
        pool.closeConnection(con);
        if (responseCode == HttpServletResponse.SC_OK) {
            resp.setStatus(responseCode);
        } else {
            resp.sendError(responseCode);
        }
    }
}

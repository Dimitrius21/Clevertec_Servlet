package ru.clevertec.servlet;

import ru.clevertec.servlet.action.PrintToPdf;
import ru.clevertec.servlet.dao.card.JdbcCardDao;
import ru.clevertec.servlet.dao.product.JdbcProductDao;
import ru.clevertec.servlet.enities.Check;
import ru.clevertec.servlet.exception.DataException;
import ru.clevertec.servlet.exception.ServerException;
import ru.clevertec.servlet.service.ServiceClass;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.Objects;

/**
 * По запросу /check?параметры - формирует и возвращает чек ввиде .pdf
 * В случае неверных параметров - ошибка с кодом 400
 */
@WebServlet("/check")
public class CheckServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SQLPoolConnection pool;
        pool = ((SQLPoolConnection) req.getServletContext().getAttribute(Constants.CONNECTION));
        Connection con = pool.getConnection();
        OutputStream os = resp.getOutputStream();
        ServiceClass checkService = new ServiceClass(new JdbcProductDao(con), new JdbcCardDao(con));
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("Clevertec.pdf");
        String params = req.getQueryString();

        try {
            if (params != null) {
                String[] data = params.split("(%20)+");
                Check check = checkService.getCheck(data);
                resp.setContentType("application/pdf");
                PrintToPdf printPdf = new PrintToPdf(is, os);
                printPdf.createPdf(check);
                os.close();
                resp.setStatus(HttpServletResponse.SC_OK);
            }
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (ServerException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (DataException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } finally {
            pool.closeConnection(con);
        }
    }
}

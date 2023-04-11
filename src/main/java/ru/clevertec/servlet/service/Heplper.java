package ru.clevertec.servlet.service;

import ru.clevertec.servlet.dao.SimpleDao;
import ru.clevertec.servlet.dao.card.JdbcCardDao;
import ru.clevertec.servlet.dao.product.JdbcProductDao;
import ru.clevertec.servlet.enities.DiscountCard;
import ru.clevertec.servlet.enities.Product;
import ru.clevertec.servlet.exception.NotPresentException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.util.Map;

public class Heplper {
    /**
     * Метод читает тело и запроса и возвращает его ввиде строки
     * @param req - объект с HttpServletRequest запросом
     * @return - тело запроса ввиде строки
     * @throws IOException
     */
    public static String getRequestBody(HttpServletRequest req) throws IOException {
        StringBuilder json = new StringBuilder();
        String line = null;
        BufferedReader reader = req.getReader();
        while ((line = reader.readLine()) != null) {
            json.append(line);
        }
        return json.toString();
    }

    /**
     * Определяет класс для работы с базой данной для указанной сущности (DAO) и также Class данной сущности
     *
     * @param entity - строковое значение необходимой сущности
     * @param req    - входные параметры для Сервлета в формате HttpServletRequest
     * @return - Map.Entry с классом описывающим сущность и DAO для нее
     * @throws NotPresentException - исключение в случее если входная строка с сущностью не соответсвует имеющимся в приложении
     */
    public static Map.Entry<Class, SimpleDao> getDao(String entity, HttpServletRequest req) throws NotPresentException {
        ServletContext context = req.getServletContext();
        Connection con = (Connection) context.getAttribute("connection");
        switch (entity) {
            case "product":
                return Map.entry(Product.class, new JdbcProductDao(con));
            case "card":
                return Map.entry(DiscountCard.class, new JdbcCardDao(con));
            default:
                throw new NotPresentException();
        }
    }
}

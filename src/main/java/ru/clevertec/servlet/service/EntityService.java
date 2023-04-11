package ru.clevertec.servlet.service;

import ru.clevertec.servlet.Constants;
import ru.clevertec.servlet.SQLPoolConnection;
import ru.clevertec.servlet.dao.SimpleDao;
import ru.clevertec.servlet.dao.card.JdbcCardDao;
import ru.clevertec.servlet.dao.product.JdbcProductDao;
import ru.clevertec.servlet.enities.DiscountCard;
import ru.clevertec.servlet.enities.Product;
import ru.clevertec.servlet.exception.DBException;
import ru.clevertec.servlet.exception.NotPresentException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EntityService {
    private static final int ITEM_PER_Page = 10;
    /**
     * Выполняет чтение сущности/сущностей из базы данных исходя из параметров запроса
     * @param req - входные параметры ввиде HttpServletRequest req
     * @return - Map.Entry с кодом ответа сервера на запрос и полученной из базы данных сущности(списка сущностей).
     * В случае отсутсвия записи в базе по входному id - код 404 и null ввиде объекта.
     * Код 500 и null при ощибке в базе данных
     */
    public static Map.Entry<Integer, Object> getEntities(HttpServletRequest req) {
        String[] params = req.getPathInfo().split("/");
        SQLPoolConnection pool = (SQLPoolConnection)req.getServletContext().getAttribute(Constants.CONNECTION);
        Connection con = pool.getConnection();
        Map.Entry<Integer, Object> result = Map.entry(HttpServletResponse.SC_NOT_FOUND, new Object());
        switch (params[1]) {
            case "product":
                result = getOne(new JdbcProductDao(con), params); break;
            case "card":
                result = getOne(new JdbcCardDao(con), params); break;
            case "products":
                result = getAll(new JdbcProductDao(con), params); break;
            case "cards":
                result = getAll(new JdbcCardDao(con), params);
        }
        pool.closeConnection(con);
        return result;
    }

    /**
     * Производит чтение из базы данных сущности и ее возврашает ее содержащий объект.
     * @param dao  - класс для работы с базой данной сущности (DAO)
     * @param params массив с строками - разделенная URI
     * @return - Map.Entry -  с кодом ответа сервера на запрос и полученную из базы данных сущность. В случае отсутсвия
     * записи в базе по входному id - код 404 и null ввиде объекта. Код 500 и null при ощибке в базе данных
     */
    public static Map.Entry<Integer, Object> getOne(SimpleDao dao, String[] params) {
        int responseCode = HttpServletResponse.SC_NOT_FOUND;
        Optional res = Optional.empty();
        try {
            if (params.length == 3) {
                long id = Long.parseLong(params[2]);
                res = dao.getById(id);
                if (res.isPresent()) {
                    responseCode = HttpServletResponse.SC_OK;
                }
            }
        } catch (NumberFormatException ex) {
            responseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
        return Map.entry(responseCode, res.orElse(new Object()));
    }

    /**
     * Производит чтение из базы данных сущностей и возврашает содержащий их список.
     * @param dao  - класс для работы с базой данной сущности (DAO)
     * @param params массив с строками - разделенная URI
     * @return Map.Entry -  с кодом ответа сервера на запрос и полученным из базы списком сущностей.
     * Код 500 и null при ощибке в базе данных
     */
    public static Map.Entry<Integer, Object> getAll(SimpleDao dao, String[] params) {
        int page = 0;
        int itemPerPage = ITEM_PER_Page;
        int responseCode = HttpServletResponse.SC_OK;
        List<?> res = null;
        if (params.length == 2) {
            page = -1;
        } else {
            page = Integer.parseInt(params[2]);
            if (params.length == 4) {
                itemPerPage = Integer.parseInt(params[3]);
            }
            page *= itemPerPage;
        }
        try {
            res = dao.getAll(page, itemPerPage);
        } catch (DBException e) {
            responseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
        return Map.entry(responseCode, res);
    }
    /**
     * Определяет класс для работы с базой данной для указанной сущности (DAO) и также Class данной сущности
     *
     * @param entity - строковое значение необходимой сущности
     * @param con    - Connection к базе данных
     * @return - Map.Entry с классом описывающим сущность и DAO для нее
     * @throws NotPresentException - исключение в случее если входная строка с сущностью не соответсвует имеющимся в приложении
     */
    public static Map.Entry<Class, SimpleDao> getDao(String entity, Connection con) throws NotPresentException {
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

package ru.clevertec.servlet.dao.card;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.clevertec.servlet.enities.DiscountCard;
import ru.clevertec.servlet.enities.Product;
import ru.clevertec.servlet.exception.DBException;

import java.sql.*;
import java.util.*;

import static java.util.Map.entry;

/**
 * Класс по работе с базой данных для сущности DiscountCard
 */

public class JdbcCardDao implements CardDaoInterface {
    private static final Logger logger = LoggerFactory.getLogger(JdbcCardDao.class);
    private Connection con;

    public JdbcCardDao(Connection con) {
        this.con = con;
    }

    public JdbcCardDao() {
    }

    public void setCon(Connection con) {
        this.con = con;
    }

    /**
     * Метод получения объекта DiscountCard по его номеру
     *
     * @param number - номер DiscountCard для поиска записи
     * @return - Объект Optional с найденным значением или empty
     */
    @Override
    public Optional<DiscountCard> getById(long number) {
        final String getByIdSQL = "SELECT * FROM cards WHERE number = ?";
        try (PreparedStatement pst = con.prepareStatement(getByIdSQL)) {
            pst.setLong(1, number);
            ResultSet res = pst.executeQuery();
            DiscountCard card = null;
            while (res.next()) {
                card = new DiscountCard(res.getLong("number"), res.getInt("discount"));
            }
            if (card == null) {
                logger.error("DiscountCard with number {} hasn't been found", number);
                return Optional.empty();
            } else
                return Optional.of(card);
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }
    /**
     * Метод получения списка заданного количесвта объектов Product
     * @param page - позиция в БД с какой производить выборку. Если значение аргумента отрицательное
     *             - выборка всех элементов из базы
     * @param quantity - количество объектов в возвращаемом списке
     * @return - список выбранных элементов из базы
     */
    public List<DiscountCard> getAll(int page, int quantity) {
        List<DiscountCard> list = new ArrayList<>();
        String getAllSQL;
        if (page >= 0) {
            getAllSQL = "SELECT * FROM cards OFFSET ? LIMIT ?";
        } else {
            getAllSQL = "SELECT * FROM cards";
        }
        try (PreparedStatement pst = con.prepareStatement(getAllSQL)) {
            if (page >= 0) {
                pst.setInt(1, page);
                pst.setInt(2, quantity);
            }
            ResultSet res = pst.executeQuery();
            while (res.next()) {
                DiscountCard card = new DiscountCard(res.getLong("number"), res.getInt("discount"));
                list.add(card);
            }
            return list;
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    /**
     * Метод сохранения объекта DiscountCard в БД
     *
     * @param card - сохраняемый объект DiscountCard
     * @return - сохраненный объект с присвоенным номером
     */
    @Override
    public DiscountCard create(DiscountCard card) {
        final String createSQL = "INSERT INTO cards (discount) VALUES (?)";
        try (PreparedStatement ps = con.prepareStatement(createSQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, card.getDiscount());
            ps.execute();
            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            long number = rs.getLong("number");
            card.setNumber(number);
            return card;
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    /**
     * Метод обновления объекта DiscountCard в БД
     * @param card - обновляемый объект DiscountCard
     * @return
     */
    @Override
    public int update(DiscountCard card) {
        final String updateSQL = "UPDATE cards SET discount = ? WHERE number = ?";
        try (PreparedStatement ps = con.prepareStatement(updateSQL)) {
            ps.setInt(1, card.getDiscount());
            ps.setLong(2, card.getNumber());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    /**
     * Метод удаления объекта DiscountCard в БД
     * @param id - номер удаляемого объекта DiscountCard
     */
    @Override
    public void deleteById(long id) {
        final String deleteSQL = "DELETE FROM cards WHERE number = ?";
        try (PreparedStatement ps = con.prepareStatement(deleteSQL)) {
            ps.setLong(1, id);
            ps.execute();
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }
}

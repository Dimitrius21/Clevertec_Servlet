package ru.clevertec.servlet.dao.product;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.clevertec.servlet.enities.Product;
import ru.clevertec.servlet.exception.DBException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Класс по работе с базой данных для сущности Product
 */

public class JdbcProductDao implements ProductDaoInterface {
    private static final Logger logger = LoggerFactory.getLogger(JdbcProductDao.class);
    private Connection con;

    public JdbcProductDao(Connection con) {
        this.con = con;
    }

    public JdbcProductDao() {
    }

    public void setCon(Connection con) {
        this.con = con;
    }

    /**
     * Метод получения объекта Product по его id
     * @param id - первичный ключ для поиска записи
     * @return - Объект Optional с найденным значением или empty
     */
    @Override
    public Optional<Product> getById(long id) {
        final String getByIdSQL = "SELECT * FROM products WHERE id = ?";
        try (PreparedStatement pst = con.prepareStatement(getByIdSQL)) {
            pst.setLong(1, id);
            ResultSet res = pst.executeQuery();
            Product product = null;
            while (res.next()) {
                product = new Product(res.getLong("id"), res.getString("name"),
                        res.getInt("price"), res.getInt("discountType"));
            }
            if (product == null) {
                logger.error("Product with id {} hasn't been found", id);
                return Optional.empty();
            } else
                return Optional.of(product);
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    /**
     * Метод сохранения объекта Product в БД
     * @param product - сохраняемый объект Product
     * @return - сохраненный объект с присвоенным первичным ключом хранилища
     */
    @Override
    public Product create(Product product) {
        final String createSQL = "INSERT INTO products (name, price, discountType) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(createSQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, product.getName());
            ps.setInt(2, product.getPrice());
            ps.setInt(3, product.getDiscountType());
            ps.execute();
            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            long id = rs.getLong("id");
            product.setId(id);
            return product;
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    /**
     * Метод обновления объекта Product в БД
     * @param product - обновляемый объект Product
     * @return
     */
    @Override
    public int update(Product product) {
        final String updateSQL = "UPDATE products SET name = ?, price = ?, discountType = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(updateSQL)) {
            ps.setString(1, product.getName());
            ps.setInt(2, product.getPrice());
            ps.setInt(3, product.getDiscountType());
            ps.setLong(4, product.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    /**
     * Метод удаления объекта Product в БД
     * @param id - первичный ключ удаляемого объекта
     */
    @Override
    public void deleteById(long id) {
        final String deleteSQL = "DELETE FROM  products WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(deleteSQL)) {
            ps.setLong(1, id);
            ps.execute();
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
    public List<Product> getAll(int page, int quantity) {
        List<Product> list = new ArrayList<>();
        String getAllSQL;
        if (page >= 0) {
            getAllSQL = "SELECT * FROM products OFFSET ? LIMIT ?";
        } else {
            getAllSQL = "SELECT * FROM products";
        }
        try (PreparedStatement pst = con.prepareStatement(getAllSQL)) {
            if (page >= 0) {
                pst.setInt(1, page);
                pst.setInt(2, quantity);
            }
            ResultSet res = pst.executeQuery();
            while (res.next()) {
                Product product = new Product(res.getLong("id"), res.getString("name"),
                        res.getInt("price"), res.getInt("discountType"));
                list.add(product);
            }
            return list;
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }
}

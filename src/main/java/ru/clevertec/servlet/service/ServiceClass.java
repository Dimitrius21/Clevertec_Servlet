package ru.clevertec.servlet.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.clevertec.servlet.dao.card.CardDaoInterface;
import ru.clevertec.servlet.dao.product.ProductDaoInterface;
import ru.clevertec.servlet.enities.Check;
import ru.clevertec.servlet.enities.DiscountCard;
import ru.clevertec.servlet.enities.Item;
import ru.clevertec.servlet.enities.Product;
import ru.clevertec.servlet.exception.DataException;

import java.util.*;

/**
 * Класс формирующий объект класса Check по полученнным входным параметрам
 */
public class ServiceClass {
    private static final Logger logger = LoggerFactory.getLogger(ServiceClass.class);
    private static final String CARD_INDICATION = "card";
    private final ProductDaoInterface productDao;
    private final CardDaoInterface cardDao;
    private final List<Map.Entry<Product, Integer>> products = new ArrayList<>();
    private Optional<DiscountCard> gotCard = Optional.empty();


    public ServiceClass(ProductDaoInterface productDao, CardDaoInterface cardDao) {
        this.productDao = productDao;
        this.cardDao = cardDao;
    }

    /**
     * формирующий объект класса Check
     * @param notes - входные данные ввиде массива строк
     * @return сформированный объект класса Check
     * @throws DataException - в случае ошибок в процессе обработки данных
     */
    public Check getCheck(String[] notes) throws DataException {
        convert(notes);
        Check check = new Check();
        DiscountCard card = gotCard.orElse(null);
        products.stream().forEach(note -> {
            Item item = new Item.ItemBuilder(note.getKey())
                    .setQuantity(note.getValue())
                    .addDiscountByCard(card)
                    .build();
            check.addItem(item);
        });
        return check;
    }

    /**
     * Формирует список из записей с описанием товара (Product) его количеством, а также объект DiscountCard в случае наличия
     * @param args - список входных параметров в текстовом представлении
     * @throws DataException - в случае некорректгых входных данных
     */
    private void convert(String[] args) throws DataException {
        for (String st : args) {
            String[] field = st.split("-");
            if (CARD_INDICATION.equals(field[0])) {
                int cardNumber;
                try {
                    cardNumber = Integer.parseInt(field[1]);
                } catch (NumberFormatException ex) {
                    throw new DataException("Illegal input data");
                }
                gotCard = cardDao.getById(cardNumber);
            } else {
                products.add(getProductNoteById(field));
            }
        }
    }

    /**
     * Метод преобразует входное строковое представление параметров одной позиции товара в запись с описаниеем товара -
     * объект Product и целочисленным представлением количества данного товара
     * @param note - строковое представление параметров одной позиции товара в чеке
     * @return - запись Map.Entry с объектом Product и количеством данного товара
     * @throws DataException - в случае неверного задания входных параметров
     */
    public Map.Entry<Product, Integer> getProductNoteById(String[] note) throws DataException {
        try {
            int id = Integer.parseInt(note[0]);
            int quantity = Integer.parseInt(note[1]);
            Optional<Product> product = productDao.getById(id);
            return Map.entry(product.orElseThrow(), quantity);
        } catch (NumberFormatException ex) {
            logger.error("Illegal input data {}", Arrays.toString(note));
            throw new DataException("Illegal input data");
        } catch (NoSuchElementException ex) {
            logger.error("Product is absent");
            throw new DataException("Product with indicated ID is absent");
        }
    }
}

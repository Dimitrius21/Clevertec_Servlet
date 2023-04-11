package ru.clevertec.servlet.dao.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.clevertec.servlet.dao.card.JdbcCardDao;
import ru.clevertec.servlet.dao.product.JdbcProductDao;
import ru.clevertec.servlet.enities.DiscountCard;
import ru.clevertec.servlet.enities.Product;
import ru.clevertec.servlet.service.EntityService;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static ru.clevertec.servlet.service.EntityService.getOne;


@ExtendWith(MockitoExtension.class)
public class ServiceClassTest {
    @Mock
    JdbcProductDao productDao;
    @Mock
    JdbcCardDao cardDao;

    @Test
    public void getOneTest() {
        String[] params = "/card/1234".split("/");
        DiscountCard cardExp = new DiscountCard(1234, 5);
        doReturn(Optional.of(cardExp)).when(cardDao).getById(1234);
        Map.Entry<Integer, Object> map = getOne(cardDao, params);
        verify(cardDao).getById(1234);
    }

    @Test
    public void getAllOnceTest() {
        String[] params = "/products".split("/");
        doReturn(new ArrayList<Product>()).when(productDao).getAll(-1, 10);
        EntityService.getAll(productDao, params);
        verify(productDao).getAll(-1, 10);
    }
    @Test
    public void getAllOnePageTest() {
        String[] params = "/products/1".split("/");
        doReturn(new ArrayList<Product>()).when(productDao).getAll(1*10, 10);
        EntityService.getAll(productDao, params);
        verify(productDao).getAll(1*10, 10);
    }
    @Test
    public void getAllOnePageWithQuantityTest() {
        String[] params = "/products/1/5".split("/");
        doReturn(new ArrayList<Product>()).when(productDao).getAll(1*5, 5);
        EntityService.getAll(productDao, params);
        verify(productDao).getAll(1*5, 5);
    }
}
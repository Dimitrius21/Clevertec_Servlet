package ru.clevertec.servlet.enities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Класс описывающий сущность Чек
 */
public class Check {
    private String name = "CASH RECEIPT";
    private String shop = "SHOP";
    private LocalDateTime timeCreating;
    private List<Item> items = new ArrayList<>();
    private long checkNumber;

    public Check() {
        timeCreating = LocalDateTime.now();
    }
    public Check(Map<String, String> initData) {
        if (initData.containsKey("name")) {
            name = initData.get("name");
        }
        if (initData.containsKey("shop")) {
            name = initData.get("shop");
        }
        timeCreating = LocalDateTime.now();
    }

    public String getName() {
        return name;
    }

    public String getShop() {
        return shop;
    }

    public LocalDateTime getTimeCreating() {
        return timeCreating;
    }

    public List<Item> getItems() {
        return items;
    }

    public long getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(long checkNumber) {
        this.checkNumber = checkNumber;
    }

    public void addItem(Item item) {
        items.add(item);
    }
}

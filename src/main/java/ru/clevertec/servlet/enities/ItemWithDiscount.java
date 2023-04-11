package ru.clevertec.servlet.enities;

/**
 * Абстрактный класс описывающий сущность - Позиция(купленный товар) Чека к которой может быть применена скидка
 */
public abstract class ItemWithDiscount extends Item {
    Item item;

    public ItemWithDiscount(Item item) {
        super();
        this.item = item;
    }

    public String getProductName() {
        return item.getProductName();
    }

    public int getPrice() {
        return item.getPrice();
    }

    public int getQuantity() {
        return item.getQuantity();
    }

    @Override
    public int getAmount() {
        return item.getAmount();
    }

    abstract public int getDiscount();
}

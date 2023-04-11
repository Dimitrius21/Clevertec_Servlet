package ru.clevertec.servlet.enities;

import java.util.Objects;

/**
 * Класс описывающий сущность Дисконтная карта
 */
public class DiscountCard {
    private long number;
    private int discount;

    public DiscountCard() {
    }

    public DiscountCard(long number, int discount) {
        this.number = number;
        this.discount = discount;
    }

    public long getNumber() {
        return number;
    }

    public int getDiscount() {
        return discount;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscountCard card = (DiscountCard) o;
        return number == card.number && discount == card.discount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, discount);
    }
}

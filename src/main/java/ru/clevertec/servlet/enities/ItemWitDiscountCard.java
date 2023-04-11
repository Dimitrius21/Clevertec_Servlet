package ru.clevertec.servlet.enities;

/**
 * Класс описывающий сущность - Позиция(купленный товар) Чека к которой применяется скидка при наличии дисконтной карты
 * Скидка по карте не применяется также при наличии какой либо другой скидки
 */
public class ItemWitDiscountCard extends ItemWithDiscount {
    DiscountCard card;

    public ItemWitDiscountCard(Item item, DiscountCard cart) {
        super(item);
        this.card = cart;
    }

    @Override
    public int getDiscount() {
        int lastDiscount = item.getDiscount();
        if (card == null || lastDiscount>0) {
            return lastDiscount;
        }else {
            int discount= (int) Math.round(getAmount()*((card.getDiscount())/100.00));
            return discount;
        }
    }
}

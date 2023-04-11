package ru.clevertec.servlet.enities;

/**
 *  Класс описывающий сущность - Позиция(купленный товар) Чека к которой применяется скидка за количество приобретенного
 *  товара
 */
public class ItemWithQuantityDiscount extends ItemWithDiscount {
    private static final int DISCOUNT_SIZE = 10;
    private static final int DISCOUNT_QUANTITY = 5;

    public ItemWithQuantityDiscount(Item item) {
        super(item);
    }

    @Override
    public int getDiscount() {
        int lastDiscount = item.getDiscount();
        int quantity = getQuantity();
        if (quantity <= DISCOUNT_QUANTITY) {
            return lastDiscount;
        }else {
            int discount= (int) Math.round(getAmount()*(DISCOUNT_SIZE/100.00));
            return lastDiscount+discount;
        }
    }
}

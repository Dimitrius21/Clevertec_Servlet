package ru.clevertec.servlet.enities;


/**
 * Класс описывающий сущность  - Позиция(купленный товар) Чека -
 */
public class Item implements ItemInterface {

    private Product product;
    private int quantity;

    public Item(Product product, int quantity) {
        this.quantity = quantity;
        this.product = product;
    }
    protected Item(){}

    public String getProductName() {
        return product.getName();
    }

    public int getQuantity() {
        return quantity;
    }

    public int getPrice() {
        return product.getPrice();
    }

    public int getAmount() {
        return quantity * product.getPrice();
    }

    public int getDiscount() {
        return 0;
    }

    private int getDiscountType() {
        return product.getDiscountType();
    }

    /**
     * Внутренний класс реализующий Билдер для построения данной сущности
     */
    public static class ItemBuilder{
        private Product product;
        private int quantity;
        private boolean discountByCard = false;
        private DiscountCard card;


        public ItemBuilder(Product product) {
            this.product = product;
        }

        public ItemBuilder setQuantity(int quantity){
            this.quantity = quantity;
            return this;
        }

         public ItemBuilder addDiscountByCard(DiscountCard cart){
            discountByCard =true;
            this.card = cart;
            return this;
         }

         public Item build(){
            Item item = new Item(product, quantity);
            if (product.getDiscountType()==1){
                item = new ItemWithQuantityDiscount(item);
            }
            if (discountByCard) {
                item = new ItemWitDiscountCard(item, card);
            }
            return item;
         }
    }
}

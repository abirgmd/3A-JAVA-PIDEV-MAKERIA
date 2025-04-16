package com.abircode.cruddp.entities;
public class OrdersDetails {

    private int id;
    private int quantity;
    private int price;
    private Orders orders;



    public OrdersDetails() {
    }

    public OrdersDetails(int quantity, int price, Orders orders) {
        this.quantity = quantity;
        this.price = price;
        this.orders = orders;
    }

    // Getters & Setters

    public int getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Orders getOrders() {
        return orders;
    }

    public void setOrders(Orders orders) {
        this.orders = orders;
    }

    // Méthode total
    public int totalPrice() {
        return this.price * this.quantity;
    }

    @Override
    public String toString() {
        return "OrdersDetails{" +
                "id=" + id +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}

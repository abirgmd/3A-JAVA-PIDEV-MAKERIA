package com.abircode.cruddp.entities;

public class OrdersDetails {
    private int id;
    private Orders order;     // relation vers Orders
    private Produit produit;  // relation vers Produit
    private int quantity;
    private double price;
    private String image_large;
    private String nomprod;

    public OrdersDetails() {}

    public OrdersDetails(int id, Orders order, Produit produit, int quantity, double price) {
        this.id = id;
        this.order = order;
        this.produit = produit;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Orders getOrder() {
        return order;
    }

    public void setOrder(Orders order) {
        this.order = order;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // Utilitaire : image du produit (pour panier)
    public String getImageProduit() {
        if (produit != null) {
            return produit.getImage(); // ou getImageSmall1() selon ce que tu veux
        }
        return null;
    }

    // Utilitaire : nom du produit (pour panier)
    public String getNomProduit() {
        if (produit != null) {
            return produit.getNom();
        }
        return "";
    }

    @Override
    public String toString() {
        return "OrdersDetails{" +
                "id=" + id +
                ", order=" + order +
                ", produit=" + produit +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}

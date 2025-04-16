package com.abircode.cruddp.entities;

public class Orders {
    private int id;
    private String reference;

    // Constructeur vide
    public Orders() {}

    // Constructeur paramétré
    public Orders(int id, String reference) {
        this.id = id;
        this.reference = reference;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public String toString() {
        return "Orders{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                '}';
    }
}

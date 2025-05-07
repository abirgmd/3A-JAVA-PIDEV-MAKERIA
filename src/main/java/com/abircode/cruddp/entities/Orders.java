
package com.abircode.cruddp.entities;

import java.time.LocalDateTime;
import java.util.List;

public class Orders {
    private int id;
    private int userId;
    private String reference;
    private boolean paid;
    private LocalDateTime createdAt;

    // Optionnel : Liste des détails associés (utile pour une logique métier plus tard)
    private List<OrdersDetails> orderDetails;

    public Orders() {}

    public Orders(int id, int userId, String reference, boolean paid, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.reference = reference;
        this.paid = paid;
        this.createdAt = createdAt;
    }

    public Orders(int userId, String reference, boolean paid, LocalDateTime createdAt) {
        this.userId = userId;
        this.reference = reference;
        this.paid = paid;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrdersDetails> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrdersDetails> orderDetails) {
        this.orderDetails = orderDetails;
    }

    @Override
    public String toString() {
        return "Orders{" +
                "id=" + id +
                ", userId=" + userId +
                ", reference='" + reference + '\'' +
                ", paid=" + paid +
                ", createdAt=" + createdAt +
                '}';
    }
}

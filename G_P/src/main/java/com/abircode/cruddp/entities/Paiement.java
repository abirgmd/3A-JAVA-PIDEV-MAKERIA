package com.abircode.cruddp.entities;

public class Paiement {
    private int id;
    private String paymentMethod;
    private String cardDetails;
    private String cardNumber;
    private java.sql.Date expiryDate;
    private int cvv;
    // Constructeurs
    public Paiement() {
    }

    public Paiement(String paymentMethod, String cardDetails, String cardNumber, java.sql.Date expiryDate, int cvv, int orderId) {
        this.paymentMethod = paymentMethod;
        this.cardDetails = cardDetails;
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
    }

    // Getters et setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCardDetails() {
        return cardDetails;
    }

    public void setCardDetails(String cardDetails) {
        this.cardDetails = cardDetails;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public java.sql.Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(java.sql.Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getCvv() {
        return cvv;
    }

    public void setCvv(int cvv) {
        this.cvv = cvv;
    }


    @Override
    public String toString() {
        return "Paiement{" +
                "id=" + id +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", cardDetails='" + cardDetails + '\'' +
                ", cardNumber='" + cardNumber + '\'' +
                ", expiryDate=" + expiryDate +
                ", cvv=" + cvv +
                '}';
    }
}

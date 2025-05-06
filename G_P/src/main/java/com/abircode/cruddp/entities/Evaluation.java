package com.abircode.cruddp.entities;

public class Evaluation {
    private int id;
    private int userId;
    private int produitId;
    private String reviewText;
    private int note;
    private String datePublication;
    private String userName;
    private String userMail;
    private Produit produit;

    public Evaluation() {
    }

    public Evaluation(int id, int userId, int produitId, String reviewText, int note,
                      String datePublication, String userName, String userMail) {
        this.id = id;
        this.userId = userId;
        this.produitId = produitId;
        this.reviewText = reviewText;
        this.note = note;
        this.datePublication = datePublication;
        this.userName = userName;
        this.userMail = userMail;
    }

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

    public int getProduitId() {
        return produitId;
    }

    public void setProduitId(int produitId) {
        this.produitId = produitId;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public String getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(String datePublication) {
        this.datePublication = datePublication;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserMail() {
        return userMail;
    }

    public void setUserMail(String userMail) {
        this.userMail = userMail;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    @Override
    public String toString() {
        return "Evaluation{" +
                "id=" + id +
                ", userId=" + userId +
                ", produitId=" + produitId +
                ", reviewText='" + reviewText + '\'' +
                ", note=" + note +
                ", datePublication='" + datePublication + '\'' +
                ", userName='" + userName + '\'' +
                ", userMail='" + userMail + '\'' +
                '}';
    }
}

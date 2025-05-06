package com.abircode.cruddp.entities;

import java.time.LocalDateTime;

public class Produit {
    private int id;
    private String nomprod;
    private String descriptionprod;
    private String avantages;
    private String sizeprod;
    private String image_large;
    private String image_small1;
    private String image_small2;
    private String image_small3;
    private int nombre_produits_en_stock;
    private double prixprod;
    private Categorie categorie; // Remplacement de categorie_id par un objet Categorie

    // Champs de promotion
    private LocalDateTime date_promotion;
    private double reduction;
    private double prixPromo;
    private LocalDateTime promotionExpireAt;

    // Getters et setters pour chaque champ
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() { // Correspond à "nom" utilisé dans PropertyValueFactory
        return nomprod;
    }

    public void setNom(String nomprod) {
        this.nomprod = nomprod;
    }

    public String getDescription() { // Correspond à "description"
        return descriptionprod;
    }

    public void setDescription(String descriptionprod) {
        this.descriptionprod = descriptionprod;
    }

    public String getAvantages() {
        return avantages;
    }

    public void setAvantages(String avantages) {
        this.avantages = avantages;
    }

    public String getSize() { // Correspond à "size"
        return sizeprod;
    }

    public void setSize(String sizeprod) {
        this.sizeprod = sizeprod;
    }

    public String getImage() { // Correspond à "image"
        return image_large;
    }

    public void setImage(String image_large) {
        this.image_large = image_large;
    }

    public int getStock() { // Correspond à "stock"
        return nombre_produits_en_stock;
    }

    public void setStock(int nombre_produits_en_stock) {
        this.nombre_produits_en_stock = nombre_produits_en_stock;
    }

    public double getPrix() { // Correspond à "prix"
        return prixprod;
    }

    public void setPrix(double prixprod) {
        this.prixprod = prixprod;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public String getImageSmall1() {
        return image_small1;
    }

    public void setImageSmall1(String image_small1) {
        this.image_small1 = image_small1;
    }

    public String getImageSmall2() {
        return image_small2;
    }

    public void setImageSmall2(String image_small2) {
        this.image_small2 = image_small2;
    }

    public String getImageSmall3() {
        return image_small3;
    }

    public void setImageSmall3(String image_small3) {
        this.image_small3 = image_small3;
    }

    public LocalDateTime getDate_promotion() {
        return date_promotion;
    }

    public void setDate_promotion(LocalDateTime date_promotion) {
        this.date_promotion = date_promotion;
    }

    public double getReduction() {
        return reduction;
    }

    public void setReduction(double reduction) {
        this.reduction = reduction;
    }

    public double getPrixPromo() {
        return prixPromo;
    }

    public void setPrixPromo(double prixPromo) {
        this.prixPromo = prixPromo;
    }

    public LocalDateTime getPromoExpireAt() {
        return promotionExpireAt;
    }

    public void setPromoExpireAt(LocalDateTime promoExpireAt) {
        this.promotionExpireAt = promoExpireAt;
    }
}

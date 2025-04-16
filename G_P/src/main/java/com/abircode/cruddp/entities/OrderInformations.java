package com.abircode.cruddp.entities;

public class OrderInformations {
    private int id;
    private String ville;
    private String codePostal;
    private String adresseLivraison;
    private String prenom;
    private String nom;
    private String numTel;
    private String email;

    public OrderInformations() {}

    public OrderInformations(String ville, String codePostal, String adresseLivraison, String prenom,
                             String nom, String numTel, String email) {
        this.ville = ville;
        this.codePostal = codePostal;
        this.adresseLivraison = adresseLivraison;
        this.prenom = prenom;
        this.nom = nom;
        this.numTel = numTel;
        this.email = email;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public String getAdresseLivraison() {
        return adresseLivraison;
    }

    public void setAdresseLivraison(String adresseLivraison) {
        this.adresseLivraison = adresseLivraison;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getNumTel() {
        return numTel;
    }

    public void setNumTel(String numTel) {
        this.numTel = numTel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

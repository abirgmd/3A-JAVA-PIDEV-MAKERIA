package com.abircode.cruddp.entities;

public class Categorie {
    private int id; // Correspond à la colonne "id" dans la base de données
    private String nomcat; // Correspond à la colonne "nomcat"
    private String descriptioncat; // Correspond à la colonne "descriptioncat"

    // Constructeur sans arguments
    public Categorie() {
    }

    // Constructeur avec tous les champs
    public Categorie(int id, String nomcat, String descriptioncat) {
        this.id = id;
        this.nomcat = nomcat;
        this.descriptioncat = descriptioncat;
    }

    // Constructeur avec nomcat et descriptioncat uniquement
    public Categorie(String nomcat, String descriptioncat) {
        this.nomcat = nomcat;
        this.descriptioncat = descriptioncat;
    }

    // Getters et setters pour les champs
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomcat() {
        return nomcat;
    }

    public void setNomcat(String nomcat) {
        this.nomcat = nomcat;
    }

    public String getDescriptioncat() {
        return descriptioncat;
    }

    public void setDescriptioncat(String descriptioncat) {
        this.descriptioncat = descriptioncat;
    }

    @Override
    public String toString() {
        return nomcat; // Affichage du nom dans le ComboBox
    }
}

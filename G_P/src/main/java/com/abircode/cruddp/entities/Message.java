package com.abircode.cruddp.entities;

public class Message {
    private int id;
    private String titreMsg;
    private String descriptionMsg;
    private String contenu;
    private String dateMsg;

    public Message() {
    }

    public Message(int id, String titreMsg, String descriptionMsg, String contenu, String dateMsg, String image) {
        this.id = id;
        this.titreMsg = titreMsg;
        this.descriptionMsg = descriptionMsg;
        this.contenu = contenu;
        this.dateMsg = dateMsg;
    }

    public Message(String titreMsg, String descriptionMsg, String contenu, String dateMsg) {
        this.titreMsg = titreMsg;
        this.descriptionMsg = descriptionMsg;
        this.contenu = contenu;
        this.dateMsg = dateMsg;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitreMsg() {
        return titreMsg;
    }

    public void setTitreMsg(String titreMsg) {
        this.titreMsg = titreMsg;
    }

    public String getDescriptionMsg() {
        return descriptionMsg;
    }

    public void setDescriptionMsg(String descriptionMsg) {
        this.descriptionMsg = descriptionMsg;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getDateMsg() {
        return dateMsg;
    }

    public void setDateMsg(String dateMsg) {
        this.dateMsg = dateMsg;
    }



    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", titreMsg='" + titreMsg + '\'' +
                ", descriptionMsg='" + descriptionMsg + '\'' +
                ", contenu='" + contenu + '\'' +
                ", dateMsg='" + dateMsg + '\'' +
                '}';
    }
}

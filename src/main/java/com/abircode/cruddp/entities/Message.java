package com.abircode.cruddp.entities;

public class Message implements Cloneable {
    private int id;
    private String titreMsg;
    private String descriptionMsg;
    private String contenu;
    private String dateMsg;
    private String image;
    private User user; // Relation avec l'entité User

    public Message() {
    }

    public Message(int id, String titreMsg, String descriptionMsg, String contenu, String dateMsg, String image, User user) {
        this.id = id;
        this.titreMsg = titreMsg;
        this.descriptionMsg = descriptionMsg;
        this.contenu = contenu;
        this.dateMsg = dateMsg;
        this.image = image;
        this.user = user;
    }

    public Message(String titreMsg, String descriptionMsg, String contenu, String dateMsg, String image, User user) {
        this.titreMsg = titreMsg;
        this.descriptionMsg = descriptionMsg;
        this.contenu = contenu;
        this.dateMsg = dateMsg;
        this.image = image;
        this.user = user;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", titreMsg='" + titreMsg + '\'' +
                ", descriptionMsg='" + descriptionMsg + '\'' +
                ", contenu='" + contenu + '\'' +
                ", dateMsg='" + dateMsg + '\'' +
                ", image='" + image + '\'' +
                ", user=" + user +
                '}';
    }

    // --- Ajout de la méthode clone() ---
    @Override
    public Message clone() {
        try {
            Message cloned = (Message) super.clone();
            // Si User implémente aussi Cloneable, décommentez la ligne suivante pour clonage profond :
            // cloned.user = (user != null) ? user.clone() : null;
            // Sinon, on fait un clonage superficiel (référence même user)
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // Ne devrait jamais arriver car on implémente Cloneable
        }
    }
}

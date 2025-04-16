package com.abircode.cruddp.entities;

import java.time.LocalDateTime;

public class Commentaire {
    private int id;
    private Event event;
    private String nomcomment;
    private LocalDateTime timecomment;
    private String text_commentaire;
    private int userId; // Ajout du champ userId

    // Constructeur sans paramètres
    public Commentaire() {}

    // Constructeur avec paramètres
    public Commentaire(int id, Event event, String nomcomment, LocalDateTime timecomment, String text_commentaire, int userId) {
        this.id = id;
        this.event = event;
        this.nomcomment = nomcomment;
        this.timecomment = timecomment;
        this.text_commentaire = text_commentaire;
        this.userId = userId;  // Initialisation du userId
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getNomcomment() {
        return nomcomment;
    }

    public void setNomcomment(String nomcomment) {
        this.nomcomment = nomcomment;
    }

    public LocalDateTime getTimecomment() {
        return timecomment;
    }

    public void setTimecomment(LocalDateTime timecomment) {
        this.timecomment = timecomment;
    }

    public String getText_commentaire() {
        return text_commentaire;
    }

    public void setText_commentaire(String text_commentaire) {
        this.text_commentaire = text_commentaire;
    }

    public int getUserId() {  // Getter pour userId
        return userId;
    }

    public void setUserId(int userId) {  // Setter pour userId
        this.userId = userId;
    }
}

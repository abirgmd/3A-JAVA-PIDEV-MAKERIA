package com.abircode.cruddp.entities;

import java.time.LocalDateTime;

public class Event {
    private int id;
    private LocalDateTime date_debut;
    private LocalDateTime date_fin;
    private String titreevents;
    private String descriptionevents;
    private String image1events;
    private String image2events;
    private int user_id;

    // Getter for userId
    public int getUserId() {
        return user_id;
    }

    public Event(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", date_debut=" + date_debut +
                ", date_fin=" + date_fin +
                ", titreevents='" + titreevents + '\'' +
                ", descriptionevents='" + descriptionevents + '\'' +
                ", image1events='" + image1events + '\'' +
                ", image2events='" + image2events + '\'' +
                ", user_id=" + user_id +
                '}';
    }

    // Setter for userId
    public void setUserId(int userId) {
        this.user_id = userId;
    }
    public Event() {
    }

    public Event(int id, LocalDateTime date_debut, LocalDateTime date_fin, String titreevents,
                 String descriptionevents, String image1events, String image2events, int i) {
        this.id = id;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.titreevents = titreevents;
        this.descriptionevents = descriptionevents;
        this.image1events = image1events;
        this.image2events = image2events;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getDate_debut() {
        return date_debut;
    }

    public void setDate_debut(LocalDateTime date_debut) {
        this.date_debut = date_debut;
    }

    public LocalDateTime getDate_fin() {
        return date_fin;
    }

    public void setDate_fin(LocalDateTime date_fin) {
        this.date_fin = date_fin;
    }

    public String getTitreevents() {
        return titreevents;
    }

    public void setTitreevents(String titreevents) {
        this.titreevents = titreevents;
    }

    public String getDescriptionevents() {
        return descriptionevents;
    }

    public void setDescriptionevents(String descriptionevents) {
        this.descriptionevents = descriptionevents;
    }

    public String getImage1events() {
        return image1events;
    }

    public void setImage1events(String image1events) {
        this.image1events = image1events;
    }

    public String getImage2events() {
        return image2events;
    }

    public void setImage2events(String image2events) {
        this.image2events = image2events;
    }
}
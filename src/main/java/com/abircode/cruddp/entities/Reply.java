package com.abircode.cruddp.entities;

public class Reply {
    private int id;
    private int messageId;
    private String contenuReply;
    private String createdAt;
    private User user; // <-- Nouveau : Relation avec l'utilisateur

    public Reply() {
    }

    // Constructeur modifié pour inclure l'utilisateur
    public Reply(int id, int messageId, String contenuReply, String createdAt, User user) {
        this.id = id;
        this.messageId = messageId;
        this.contenuReply = contenuReply;
        this.createdAt = createdAt;
        this.user = user;
    }

    // Constructeur pour création sans ID
    public Reply(int messageId, String contenuReply, String createdAt, User user) {
        this.messageId = messageId;
        this.contenuReply = contenuReply;
        this.createdAt = createdAt;
        this.user = user;
    }

    // Getters/Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getContenuReply() {
        return contenuReply;
    }

    public void setContenuReply(String contenuReply) {
        this.contenuReply = contenuReply;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // Nouveaux getters/setters pour User
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Reply{" +
                "id=" + id +
                ", messageId=" + messageId +
                ", contenuReply='" + contenuReply + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", user=" + (user != null ? user.getName() : "null") + // Affichage du nom de l'utilisateur
                '}';
    }
}

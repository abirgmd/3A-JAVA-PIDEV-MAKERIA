package com.abircode.cruddp.entities;

public class Reply {
    private int id;
    private int messageId;  // Correspond à message_id dans la table
    private String contenuReply;  // Correspond à contenuReply dans la table
    private String createdAt;  // Correspond à created_at dans la table

    public Reply() {
    }

    public Reply(int id, int messageId, String contenuReply, String createdAt) {
        this.id = id;
        this.messageId = messageId;
        this.contenuReply = contenuReply;
        this.createdAt = createdAt;
    }

    public Reply(int messageId, String contenuReply, String createdAt) {
        this.messageId = messageId;
        this.contenuReply = contenuReply;
        this.createdAt = createdAt;
    }

    // Getters et Setters
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

    @Override
    public String toString() {
        return "Reply{" +
                "id=" + id +
                ", messageId=" + messageId +
                ", contenuReply='" + contenuReply + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
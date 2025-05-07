package com.abircode.cruddp.entities;

import java.time.LocalDateTime;

public class Reaction {
    private int id;
    private int messageId;
    private int userId;
    private String emoji;
    private LocalDateTime createdAt;

    public Reaction() {}

    // Constructeur sans id (pour création)
    public Reaction(int messageId, int userId, String emoji) {
        this.messageId = messageId;
        this.userId = userId;
        this.emoji = emoji;
        this.createdAt = LocalDateTime.now();
    }

    // Getters et setters
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
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public String getEmoji() {
        return emoji;
    }
    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Reaction{" +
                "id=" + id +
                ", messageId=" + messageId +
                ", userId=" + userId +
                ", emoji='" + emoji + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}


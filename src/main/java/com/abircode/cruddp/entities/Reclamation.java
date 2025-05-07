package com.abircode.cruddp.entities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Reclamation {
    private int id;
    private Date dateRec;
    private String descriptionRec;
    private StatutReclamation statRec;
    private String type;
    private User user;

    public Reclamation() {
    }

    public Reclamation(Date dateRec, String descriptionRec, StatutReclamation statRec, String type, User user) {
        this.dateRec = dateRec;
        this.descriptionRec = descriptionRec;
        this.statRec = statRec;
        this.type = type;
        this.user = user;
    }

    public Reclamation(int id, Date dateRec, String descriptionRec, StatutReclamation statRec, String type, User user) {
        this.id = id;
        this.dateRec = dateRec;
        this.descriptionRec = descriptionRec;
        this.statRec = statRec;
        this.type = type;
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDateRec() {
        return dateRec;
    }

    public void setDateRec(Date dateRec) {
        this.dateRec = dateRec;
    }

    public String getDescriptionRec() {
        return descriptionRec;
    }

    public void setDescriptionRec(String descriptionRec) {
        this.descriptionRec = descriptionRec;
    }

    public StatutReclamation getStatRec() {
        return statRec;
    }

    public void setStatRec(StatutReclamation statRec) {
        this.statRec = statRec;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reclamation that = (Reclamation) o;
        return Objects.equals(dateRec, that.dateRec) &&
                Objects.equals(descriptionRec, that.descriptionRec) &&
                Objects.equals(statRec, that.statRec) &&
                Objects.equals(type, that.type) &&
                Objects.equals(user, that.user);
    }

    /*@Override
    public int hashCode() {
        return Objects.hash(dateRec, descriptionRec, statRec, type, user);
    }

    public String toStringForResponse() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return "Réclamation associée:\n\n" +
                "Date: " + sdf.format(dateRec) + "\n\n" +
                "Description:" + wrapTextAtFirstSpace(descriptionRec, 50) + "\n\n" +
                "Status: " + statRec + "\n\n" +
                "Type: " + type + "\n\n" +
                "User: " + (user != null ? user.getName() : "N/A");
    }*/
    @Override
    public String toString() {
        String wrappedDescription = wrapTextAtFirstSpace(descriptionRec, 50);
        return "Reclamation Details:\n" +
                "-------------------------\n" +
                "Date          : " + dateRec + "\n" +
                "Description   : " + wrappedDescription + "\n" +
                "Status        : " + statRec + "\n" +
                "Type          : " + type + "\n" +
                "User          : " + (user != null ? user.getName() : "null");
    }

    private String wrapTextAtFirstSpace(String text, int lineLength) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder result = new StringBuilder();
        int index = 0;
        while (index < text.length()) {
            if (text.length() - index <= lineLength) {
                result.append(text.substring(index));
                break;
            }
            int breakIndex = index + lineLength;
            int spaceIndex = text.indexOf(' ', breakIndex);
            if (spaceIndex == -1) {
                result.append(text.substring(index));
                break;
            }
            result.append(text, index, spaceIndex);
            result.append("\n             ");
            index = spaceIndex + 1;
        }
        return result.toString();
    }
}

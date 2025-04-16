package com.abircode.cruddp.entities;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.Date;

public class Reclamation {
    private int id;
    private Date dateRec;//
    private String descriptionRec;
    private StatutReclamation statRec;
    private String type;
    private int id_utilisateur;

    public Reclamation() {
    }

    public Reclamation(Date dateRec, String descriptionRec, StatutReclamation statRec, String type, int id_utilisateur) {
        this.dateRec = dateRec;
        this.descriptionRec = descriptionRec;
        this.statRec = statRec;
        this.type = type;
        this.id_utilisateur = id_utilisateur;
    }

    public Reclamation(int id, Date dateRec, String descriptionRec, StatutReclamation statRec, String type, int id_utilisateur) {
        this.id = id;
        this.dateRec = dateRec;
        this.descriptionRec = descriptionRec;
        this.statRec = statRec;
        this.type = type;
        this.id_utilisateur = id_utilisateur;
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

    public int getId_utilisateur() {
        return id_utilisateur;
    }

    public void setId_utilisateur(int id_utilisateur) {
        this.id_utilisateur = id_utilisateur;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reclamation that = (Reclamation) o;
        return id_utilisateur == that.id_utilisateur && Objects.equals(dateRec, that.dateRec) && Objects.equals(descriptionRec, that.descriptionRec) && Objects.equals(statRec, that.statRec) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateRec, descriptionRec, statRec, type, id_utilisateur);
    }

    @Override
    public String toString() {
        String wrappedDescription = wrapTextAtFirstSpace(descriptionRec, 50);
        return "Reclamation Details:\n" +
                "-------------------------\n" +
                "Date          : " + dateRec + "\n" +
                "Description   : " + wrappedDescription + "\n" +
                "Status        : " + statRec + "\n" +
                "Type          : " + type + "\n" +
                "User ID       : " + id_utilisateur;
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
            result.append(text.substring(index, spaceIndex));
            result.append("\n             ");
            index = spaceIndex + 1;
        }
        return result.toString();
    }


}

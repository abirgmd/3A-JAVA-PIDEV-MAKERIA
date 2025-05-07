package com.abircode.cruddp.entities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Reponses {
    private int id;
    private Date dateRep;
    private String contenuRep;

    private Reclamation reclamation; // Remplacement ici
    private int id_admin;

    public Reponses() {
    }

    public Reponses(Date dateRep, String contenuRep, Reclamation reclamation, int id_admin) {
        this.dateRep = dateRep;
        this.contenuRep = contenuRep;
        this.reclamation = reclamation;
        this.id_admin = id_admin;
    }

    public Reponses(int id, Date dateRep, String contenuRep, Reclamation reclamation, int id_admin) {
        this.id = id;
        this.dateRep = dateRep;
        this.contenuRep = contenuRep;
        this.reclamation = reclamation;
        this.id_admin = id_admin;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDateRep() {
        return dateRep;
    }

    public void setDateRep(Date dateRep) {
        this.dateRep = dateRep;
    }

    public String getContenuRep() {
        return contenuRep;
    }

    public void setContenuRep(String contenuRep) {
        this.contenuRep = contenuRep;
    }

    public Reclamation getReclamation() {
        return reclamation;
    }

    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;
    }

    public int getId_admin() {
        return id_admin;
    }

    public void setId_admin(int id_admin) {
        this.id_admin = id_admin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reponses reponses = (Reponses) o;
        return id == reponses.id &&
                id_admin == reponses.id_admin &&
                Objects.equals(dateRep, reponses.dateRep) &&
                Objects.equals(contenuRep, reponses.contenuRep) &&
                Objects.equals(reclamation, reponses.reclamation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dateRep, contenuRep, reclamation, id_admin);
    }

    @Override
    public String toString() {
        String wrappedDescription = wrapTextAtFirstSpace(contenuRep, 50);
        return "Reponses{" +
                ", dateRep=" + dateRep +
                ", contenuRep='" + wrappedDescription + '\'' +
                "\n reclamation=" + reclamation +
                '}';
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

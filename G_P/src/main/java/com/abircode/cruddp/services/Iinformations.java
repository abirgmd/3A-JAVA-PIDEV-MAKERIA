package com.abircode.cruddp.services;

import com.abircode.cruddp.entities.OrderInformations;
import javafx.event.ActionEvent;

import java.sql.SQLException;
import java.util.List;

public interface Iinformations {
    void ajouter(OrderInformations info) throws SQLException;
    void modifier(OrderInformations info) throws SQLException;
    void supprimer(int id) throws SQLException;
    List<OrderInformations> afficher() throws SQLException;

    // Ces méthodes sont pour les besoins JavaFX (optionnelles si tu les utilises ailleurs)
    void viderChamps(ActionEvent event); // à implémenter dans le contrôleur ou dans un service UI
}

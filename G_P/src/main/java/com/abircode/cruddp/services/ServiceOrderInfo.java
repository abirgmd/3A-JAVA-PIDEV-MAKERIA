package com.abircode.cruddp.services;

import com.abircode.cruddp.entities.OrderInformations;
import com.abircode.cruddp.utils.DBConnexion;
import javafx.event.ActionEvent;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceOrderInfo implements Iinformations {

    private final Connection con;

    public ServiceOrderInfo() {
        con = DBConnexion.getCon();
    }

    @Override
    public void ajouter(OrderInformations info) throws SQLException {
        String query = "INSERT INTO order_informations (ville, code_postal, adresselivraison, prenom, nom, num_tel, email) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, info.getVille());
            ps.setString(2, info.getCodePostal());
            ps.setString(3, info.getAdresseLivraison());
            ps.setString(4, info.getPrenom());
            ps.setString(5, info.getNom());
            ps.setString(6, info.getNumTel());
            ps.setString(7, info.getEmail());

            ps.executeUpdate();
        }
    }

    @Override
    public void modifier(OrderInformations info) throws SQLException {
        String query = "UPDATE order_informations SET ville=?, code_postal=?, adresselivraison=?, prenom=?, nom=?, num_tel=?, email=?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, info.getVille());
            ps.setString(2, info.getCodePostal());
            ps.setString(3, info.getAdresseLivraison());
            ps.setString(4, info.getPrenom());
            ps.setString(5, info.getNom());
            ps.setString(6, info.getNumTel());
            ps.setString(7, info.getEmail());
            ps.setInt(8, info.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM order_informations WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<OrderInformations> afficher() throws SQLException {
        List<OrderInformations> list = new ArrayList<>();
        String query = "SELECT * FROM order_informations";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                OrderInformations info = new OrderInformations();
                info.setId(rs.getInt("id"));
                info.setVille(rs.getString("ville"));
                info.setCodePostal(rs.getString("code_postal"));
                info.setAdresseLivraison(rs.getString("adresselivraison"));
                info.setPrenom(rs.getString("prenom"));
                info.setNom(rs.getString("nom"));
                info.setNumTel(rs.getString("num_tel"));
                info.setEmail(rs.getString("email"));
                // Charger l'objet Orders si nécessaire avec un service de Orders
                list.add(info);
            }
        }
        return list;
    }

    @Override
    public void viderChamps(ActionEvent event) {

    }
}


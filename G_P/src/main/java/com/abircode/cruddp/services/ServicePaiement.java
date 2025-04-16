package com.abircode.cruddp.services;

import com.abircode.cruddp.entities.Paiement;
import com.abircode.cruddp.utils.DBConnexion;
import javafx.event.ActionEvent;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePaiement implements IPaiement<Paiement> {

    private final Connection con;

    public ServicePaiement() {
        con = DBConnexion.getCon();
    }

    // ✅ Ajouter un paiement
    @Override
    public void creatPaiement(Paiement paiement) {
        String req = "INSERT INTO paiement (payment_method, card_details, card_number, expiry_date, cvv) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setString(1, paiement.getPaymentMethod());
            ps.setString(2, paiement.getCardDetails());
            ps.setString(3, paiement.getCardNumber());
            ps.setTimestamp(4, new Timestamp(paiement.getExpiryDate().getTime()));
            ps.setInt(5, paiement.getCvv());

            ps.executeUpdate();
            System.out.println("✅ Paiement ajouté !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur ajout paiement : " + e.getMessage());
        }
    }

    // ✅ Afficher tous les paiements
    @Override
    public List<Paiement> showPaiements() {
        String req = "SELECT * FROM paiement";
        List<Paiement> paiements = new ArrayList<>();

        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Paiement p = new Paiement();
                p.setId(rs.getInt("id"));
                p.setPaymentMethod(rs.getString("payment_method"));
                p.setCardDetails(rs.getString("card_details"));
                p.setCardNumber(rs.getString("card_number"));
                p.setExpiryDate(rs.getDate("expiry_date"));
                p.setCvv(rs.getInt("cvv"));

                paiements.add(p);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la lecture des paiements : " + e.getMessage());
        }

        // Affichage console pour debug
        for (Paiement p : paiements) {
            System.out.println(p);
        }

        return paiements;
    }

    // ✅ Supprimer un paiement par ID
    @Override
    public void deletePaiement(int id) {
        String req = "DELETE FROM paiement WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setInt(1, id);
            int deleted = ps.executeUpdate();
            if (deleted > 0)
                System.out.println("🗑️ Paiement avec ID " + id + " supprimé.");
            else
                System.out.println("⚠️ Aucun paiement trouvé avec l'ID " + id);
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression : " + e.getMessage());
        }
    }

    // ✅ Mettre à jour un paiement
    @Override
    public void updatePaiement(Paiement paiement) {
        String req = "UPDATE paiement SET payment_method=?, card_details=?, card_number=?, expiry_date=?, cvv=? WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(req)) {
            ps.setString(1, paiement.getPaymentMethod());
            ps.setString(2, paiement.getCardDetails());
            ps.setString(3, paiement.getCardNumber());
            ps.setDate(4, Date.valueOf(paiement.getExpiryDate().toString()));
            ps.setInt(5, paiement.getCvv());
            ps.setInt(6, paiement.getId());  // ID du paiement à mettre à jour

            ps.executeUpdate();
            System.out.println("✏️ Paiement mis à jour !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour : " + e.getMessage());
        }
    }

    // ✅ Sélection d’un paiement (à implémenter si besoin)
    @Override
    public void selectPaiement() {
        System.out.println("ℹ️ Paiement sélectionné (fonction à compléter selon l’UI).");
    }

    // ✅ Vider les champs (à connecter avec les contrôleurs FXML si besoin)
    @Override
    public void viderChamps(ActionEvent event) {
        System.out.println("🧼 Champs vidés.");
    }
}

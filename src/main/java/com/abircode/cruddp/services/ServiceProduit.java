package com.abircode.cruddp.services;

import com.abircode.cruddp.entities.Categorie;
import com.abircode.cruddp.entities.Produit;
import com.abircode.cruddp.utils.DBConnexion;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceProduit {
    private Connection connection;

    public ServiceProduit() {
        connection = DBConnexion.getCon();
    }

    public void ajouter(Produit produit) throws SQLException {
        String sql = "INSERT INTO produit (nomprod, descriptionprod, avantages, sizeprod, image_large, " +
                "image_small1, image_small2, image_small3, nombre_produits_en_stock, prixprod, " +
                "categorie_id, date_promotion, reduction, prixPromo, promotionExpireAt) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, produit.getNom());
            pst.setString(2, produit.getDescription());
            pst.setString(3, produit.getAvantages());
            pst.setString(4, produit.getSize());
            pst.setString(5, produit.getImage());
            pst.setString(6, produit.getImageSmall1());
            pst.setString(7, produit.getImageSmall2());
            pst.setString(8, produit.getImageSmall3());
            pst.setInt(9, produit.getStock());
            pst.setDouble(10, produit.getPrix());
            pst.setInt(11, produit.getCategorie().getId());
            pst.setTimestamp(12, produit.getDate_promotion() != null ? Timestamp.valueOf(produit.getDate_promotion()) : null);
            pst.setDouble(13, produit.getReduction());
            pst.setDouble(14, produit.getPrixPromo());
            pst.setTimestamp(15, produit.getPromoExpireAt() != null ? Timestamp.valueOf(produit.getPromoExpireAt()) : null);

            pst.executeUpdate();
        }
    }

    public void modifier(Produit produit) throws SQLException {
        String sql = "UPDATE produit SET nomprod=?, descriptionprod=?, avantages=?, sizeprod=?, " +
                "image_large=?, image_small1=?, image_small2=?, image_small3=?, " +
                "nombre_produits_en_stock=?, prixprod=?, categorie_id=?, " +
                "date_promotion=?, reduction=?, prixPromo=?, promotionExpireAt=? WHERE id=?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, produit.getNom());
            pst.setString(2, produit.getDescription());
            pst.setString(3, produit.getAvantages());
            pst.setString(4, produit.getSize());
            pst.setString(5, produit.getImage());
            pst.setString(6, produit.getImageSmall1());
            pst.setString(7, produit.getImageSmall2());
            pst.setString(8, produit.getImageSmall3());
            pst.setInt(9, produit.getStock());
            pst.setDouble(10, produit.getPrix());
            pst.setInt(11, produit.getCategorie().getId());
            pst.setTimestamp(12, produit.getDate_promotion() != null ? Timestamp.valueOf(produit.getDate_promotion()) : null);
            pst.setDouble(13, produit.getReduction());
            pst.setDouble(14, produit.getPrixPromo());
            pst.setTimestamp(15, produit.getPromoExpireAt() != null ? Timestamp.valueOf(produit.getPromoExpireAt()) : null);
            pst.setInt(16, produit.getId());

            pst.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM produit WHERE id=?";
        PreparedStatement pst = connection.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
    }

    public List<Produit> afficher() throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT p.*, c.nomcat FROM produit p " +
                "LEFT JOIN categorie c ON p.categorie_id = c.id";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Produit produit = new Produit();
                produit.setId(rs.getInt("id"));
                produit.setNom(rs.getString("nomprod"));
                produit.setDescription(rs.getString("descriptionprod"));
                produit.setAvantages(rs.getString("avantages"));
                produit.setSize(rs.getString("sizeprod"));
                produit.setImage(rs.getString("image_large"));
                produit.setImageSmall1(rs.getString("image_small1"));
                produit.setImageSmall2(rs.getString("image_small2"));
                produit.setImageSmall3(rs.getString("image_small3"));
                produit.setStock(rs.getInt("nombre_produits_en_stock"));
                produit.setPrix(rs.getDouble("prixprod"));

                // Gestion des dates de promotion
                Timestamp datePromo = rs.getTimestamp("date_promotion");
                if (datePromo != null) {
                    produit.setDate_promotion(datePromo.toLocalDateTime());
                }

                Timestamp promoExpireAt = rs.getTimestamp("promotionExpireAt");
                if (promoExpireAt != null) {
                    produit.setPromoExpireAt(promoExpireAt.toLocalDateTime());
                }

                produit.setReduction(rs.getDouble("reduction"));
                produit.setPrixPromo(rs.getDouble("prixPromo"));

                Categorie categorie = new Categorie();
                categorie.setId(rs.getInt("categorie_id"));
                categorie.setNomcat(rs.getString("nomcat"));
                produit.setCategorie(categorie);

                produits.add(produit);
            }
        }
        return produits;
    }

    public List<Produit> getProduitsEnPromotion() throws SQLException {
        List<Produit> list = new ArrayList<>();
        String req = "SELECT p.*, c.nom as categorie_nom FROM produit p " +
                "LEFT JOIN categorie c ON p.categorie_id = c.id " +
                "WHERE p.date_promotion IS NOT NULL " +
                "AND p.promotionExpireAt > CURRENT_TIMESTAMP";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Produit p = new Produit();
            p.setId(rs.getInt("id"));
            p.setNom(rs.getString("nomprod"));
            p.setDescription(rs.getString("descriptionprod"));
            p.setAvantages(rs.getString("avantages"));
            p.setSize(rs.getString("sizeprod"));
            p.setImage(rs.getString("image_large"));
            p.setStock(rs.getInt("nombre_produits_en_stock"));
            p.setPrix(rs.getDouble("prixprod"));

            p.setDate_promotion(rs.getTimestamp("date_promotion").toLocalDateTime());
            p.setReduction(rs.getDouble("reduction"));
            p.setPrixPromo(rs.getDouble("prixPromo"));
            p.setPromoExpireAt(rs.getTimestamp("promotionExpireAt").toLocalDateTime());

            Categorie c = new Categorie();
            c.setId(rs.getInt("categorie_id"));
            c.setNomcat(rs.getString("categorie_nom"));
            p.setCategorie(c);

            list.add(p);
        }
        return list;
    }

    public void appliquerPromotion(int id, LocalDateTime datePromotion, double reduction, LocalDateTime promoExpireAt) throws SQLException {
        String req = "UPDATE produit SET date_promotion=?, reduction=?, prixPromo=?, promotionExpireAt=? WHERE id=?";
        PreparedStatement pst = connection.prepareStatement(req);
        pst.setTimestamp(1, Timestamp.valueOf(datePromotion));
        pst.setDouble(2, reduction);

        // Calcul du prix promo
        Produit p = getProduitById(id);
        if (p != null) {
            double prixPromo = p.getPrix() * (1 - reduction / 100);
            pst.setDouble(3, prixPromo);
        }

        pst.setTimestamp(4, Timestamp.valueOf(promoExpireAt));
        pst.setInt(5, id);
        pst.executeUpdate();
    }

    public Produit getProduitById(int id) throws SQLException {
        String sql = "SELECT p.*, c.nomcat FROM produit p " +
                "LEFT JOIN categorie c ON p.categorie_id = c.id " +
                "WHERE p.id = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                Produit produit = new Produit();
                produit.setId(rs.getInt("id"));
                produit.setNom(rs.getString("nomprod"));
                produit.setDescription(rs.getString("descriptionprod"));
                produit.setAvantages(rs.getString("avantages"));
                produit.setSize(rs.getString("sizeprod"));
                produit.setImage(rs.getString("image_large"));
                produit.setImageSmall1(rs.getString("image_small1"));
                produit.setImageSmall2(rs.getString("image_small2"));
                produit.setImageSmall3(rs.getString("image_small3"));
                produit.setStock(rs.getInt("nombre_produits_en_stock"));
                produit.setPrix(rs.getDouble("prixprod"));

                // Gestion des dates de promotion
                Timestamp datePromo = rs.getTimestamp("date_promotion");
                if (datePromo != null) {
                    produit.setDate_promotion(datePromo.toLocalDateTime());
                }

                Timestamp promoExpireAt = rs.getTimestamp("promotionExpireAt");
                if (promoExpireAt != null) {
                    produit.setPromoExpireAt(promoExpireAt.toLocalDateTime());
                }

                produit.setReduction(rs.getDouble("reduction"));
                produit.setPrixPromo(rs.getDouble("prixPromo"));

                Categorie categorie = new Categorie();
                categorie.setId(rs.getInt("categorie_id"));
                categorie.setNomcat(rs.getString("nomcat"));
                produit.setCategorie(categorie);

                return produit;
            }
        }
        return null;
    }
} 
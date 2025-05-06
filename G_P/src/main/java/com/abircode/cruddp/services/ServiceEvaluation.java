package com.abircode.cruddp.services;
import com.abircode.cruddp.entities.Evaluation;
import com.abircode.cruddp.entities.Produit;
import com.abircode.cruddp.utils.DBConnexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceEvaluation implements IService<Evaluation> {

    private Connection connection;
    private ServiceProduit serviceProduit;

    public ServiceEvaluation() {
        connection = DBConnexion.getCon();
        serviceProduit = new ServiceProduit();
    }

    @Override
    public void ajouter(Evaluation evaluation) throws SQLException {
        String sql = "INSERT INTO evaluation`(user_id`, produit_id, review_text, note, date_publication, user_name, user_mail) VALUES (?,?,?,?,?,?,?)";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setInt(1, evaluation.getUserId());
        pst.setInt(2, evaluation.getProduitId());
        pst.setString(3, evaluation.getReviewText());
        pst.setInt(4, evaluation.getNote());
        pst.setString(5, evaluation.getDatePublication());
        pst.setString(6, evaluation.getUserName());
        pst.setString(7, evaluation.getUserMail());
        pst.executeUpdate();
    }

    @Override
    public void modifier(Evaluation evaluation) throws SQLException {
        String sql = "UPDATE evaluation SET user_id`=?, produit_id`=?, review_text`=?, note`=?, date_publication`=?, user_name`=?, `user_mail`=? WHERE id=?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setInt(1, evaluation.getUserId());
        pst.setInt(2, evaluation.getProduitId());
        pst.setString(3, evaluation.getReviewText());
        pst.setInt(4, evaluation.getNote());
        pst.setString(5, evaluation.getDatePublication());
        pst.setString(6, evaluation.getUserName());
        pst.setString(7, evaluation.getUserMail());
        pst.setInt(8, evaluation.getId());
        pst.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM evaluation WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Evaluation> afficher() throws SQLException {
        List<Evaluation> evaluations = new ArrayList<>();
        String sql = "SELECT * FROM evaluation";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            Evaluation e = new Evaluation();
            e.setId(rs.getInt("id"));
            e.setUserId(rs.getInt("user_id"));
            e.setProduitId(rs.getInt("produit_id"));
            e.setReviewText(rs.getString("review_text"));
            e.setNote(rs.getInt("note"));
            e.setDatePublication(rs.getString("date_publication"));
            e.setUserName(rs.getString("user_name"));
            e.setUserMail(rs.getString("user_mail"));

            // Charger le produit associé
            try {
                Produit produit = serviceProduit.getProduitById(e.getProduitId());
                e.setProduit(produit);
            } catch (SQLException ex) {
                System.err.println("Erreur lors du chargement du produit pour l'évaluation " + e.getId() + ": " + ex.getMessage());
            }

            evaluations.add(e);
        }
        return evaluations;
    }

    public Evaluation getById(int id) throws SQLException {
        return afficher().stream().filter(e -> e.getId() == id).findFirst().orElse(null);
    }

    public List<Evaluation> getByProduitId(int produitId) throws SQLException {
        return afficher().stream().filter(e -> e.getProduitId() == produitId).toList();
    }
} 
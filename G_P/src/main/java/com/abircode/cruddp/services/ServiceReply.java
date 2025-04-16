package com.abircode.cruddp.services;

import com.abircode.cruddp.entities.Reply;
import com.abircode.cruddp.utils.DBConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReply implements IService<Reply> {
    Connection connection;

    public ServiceReply() {
        connection = DBConnexion.getCon();
    }

    @Override
    public void ajouter(Reply reply) throws SQLException {
        String req = "INSERT INTO reply (message_id, contenuReply, created_at) " +
                "VALUES (?, ?, ?)";

        PreparedStatement preparedStatement = connection.prepareStatement(req);
        preparedStatement.setInt(1, reply.getMessageId());
        preparedStatement.setString(2, reply.getContenuReply());
        preparedStatement.setString(3, reply.getCreatedAt());

        preparedStatement.executeUpdate();
        System.out.println("Réponse ajoutée");
    }

    @Override
    public void modifier(Reply reply) throws SQLException {
        String req = "UPDATE reply SET contenuReply = ?, created_at = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(req)) {
            pstmt.setString(1, reply.getContenuReply());
            pstmt.setString(2, reply.getCreatedAt());
            pstmt.setInt(3, reply.getId());
            pstmt.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM reply WHERE id=?";

        PreparedStatement preparedStatement = connection.prepareStatement(req);
        preparedStatement.setInt(1, id);

        preparedStatement.executeUpdate();
        System.out.println("Réponse supprimée");
    }

    @Override
    public List<Reply> afficher() throws SQLException {
        List<Reply> replies = new ArrayList<>();
        String req = "SELECT * FROM reply";

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(req);

        while (rs.next()) {
            Reply reply = new Reply();
            reply.setId(rs.getInt("id"));
            reply.setMessageId(rs.getInt("message_id"));
            reply.setContenuReply(rs.getString("contenureply"));
            reply.setCreatedAt(rs.getString("created_at"));

            replies.add(reply);
        }

        return replies;
    }

    // Méthode pour trouver une réponse par son ID
    public Reply trouverParId(int id) throws SQLException {
        String req = "SELECT * FROM reply WHERE id=?";

        PreparedStatement preparedStatement = connection.prepareStatement(req);
        preparedStatement.setInt(1, id);

        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            Reply reply = new Reply();
            reply.setId(rs.getInt("id"));
            reply.setMessageId(rs.getInt("message_id"));
            reply.setContenuReply(rs.getString("contenureply"));
            reply.setCreatedAt(rs.getString("created_at"));
            return reply;
        }
        return null;
    }
    public void supprimerToutesReponsesPourMessage(int messageId) throws SQLException {
        String req = "DELETE FROM reply WHERE message_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(req)) {
            pstmt.setInt(1, messageId);
            pstmt.executeUpdate();
        }
    }
    // Méthode supplémentaire pour trouver toutes les réponses d'un message
    public List<Reply> trouverParMessageId(int messageId) throws SQLException {
        List<Reply> replies = new ArrayList<>();
        String req = "SELECT * FROM reply WHERE message_id=?";

        PreparedStatement preparedStatement = connection.prepareStatement(req);
        preparedStatement.setInt(1, messageId);

        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            Reply reply = new Reply();
            reply.setId(rs.getInt("id"));
            reply.setMessageId(rs.getInt("message_id"));
            reply.setContenuReply(rs.getString("contenureply"));
            reply.setCreatedAt(rs.getString("created_at"));

            replies.add(reply);
        }

        return replies;
    }
}
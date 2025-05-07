package com.abircode.cruddp.services;

import com.abircode.cruddp.entities.Reaction;
import com.abircode.cruddp.entities.User;
import com.abircode.cruddp.utils.DBConnexion;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ServiceReaction {
    private Connection cnx;

    // Emojis de base avec leurs descriptions
    public static final Map<String, String> DEFAULT_EMOJIS = Map.of(
            "❤️", "J'adore",
            "😢", "Triste",
            "😂😂", "Hilarant",
            "😮😮😮", "Surprenant",
            "😡😡😡😡", "Fâché"
    );

    public ServiceReaction() {
        cnx = DBConnexion.getCon();
    }

    // Ajouter une réaction avec vérification des doublons pour le même emoji
    public boolean ajouterReaction(Reaction r) throws SQLException {
        // Supprimer d'abord toute réaction existante de cet utilisateur sur ce message
        supprimerReactionsUtilisateur(r.getMessageId(), r.getUserId());

        String insertQuery = "INSERT INTO reaction (message_id, user_id, emoji, created_at) VALUES (?, ?, ?, ?)";
        PreparedStatement psInsert = cnx.prepareStatement(insertQuery);
        psInsert.setInt(1, r.getMessageId());
        psInsert.setInt(2, r.getUserId());
        psInsert.setString(3, r.getEmoji());
        psInsert.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
        return psInsert.executeUpdate() > 0;
    }

    // Supprimer toutes les réactions d'un utilisateur sur un message
    public boolean supprimerReactionsUtilisateur(int messageId, int userId) throws SQLException {
        String deleteQuery = "DELETE FROM reaction WHERE message_id = ? AND user_id = ?";
        PreparedStatement psDelete = cnx.prepareStatement(deleteQuery);
        psDelete.setInt(1, messageId);
        psDelete.setInt(2, userId);
        return psDelete.executeUpdate() > 0;
    }

    // Obtenir le décompte des réactions par emoji pour un message
    public Map<String, Integer> getReactionsCountParMessage(int messageId) throws SQLException {
        Map<String, Integer> reactionsCount = new HashMap<>();
        String query = "SELECT emoji, COUNT(*) as count FROM reaction WHERE message_id = ? GROUP BY emoji";
        PreparedStatement ps = cnx.prepareStatement(query);
        ps.setInt(1, messageId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            reactionsCount.put(rs.getString("emoji"), rs.getInt("count"));
        }
        return reactionsCount;
    }

    // Obtenir les emojis utilisés par un utilisateur sur un message
    public List<String> getEmojisParUserEtMessage(int userId, int messageId) throws SQLException {
        String query = "SELECT emoji FROM reaction WHERE user_id = ? AND message_id = ?";
        PreparedStatement ps = cnx.prepareStatement(query);
        ps.setInt(1, userId);
        ps.setInt(2, messageId);
        ResultSet rs = ps.executeQuery();

        List<String> emojis = new ArrayList<>();
        while (rs.next()) {
            emojis.add(rs.getString("emoji"));
        }
        return emojis;
    }

    // Obtenir les utilisateurs ayant réagi avec un emoji spécifique
    public List<User> getUtilisateursParReaction(int messageId, String emoji) throws SQLException {
        String query = "SELECT u.id, u.name FROM reaction r JOIN user u ON r.user_id = u.id WHERE r.message_id = ? AND r.emoji = ?";
        PreparedStatement ps = cnx.prepareStatement(query);
        ps.setInt(1, messageId);
        ps.setString(2, emoji);
        ResultSet rs = ps.executeQuery();

        List<User> users = new ArrayList<>();
        while (rs.next()) {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name"));
            users.add(user);
        }
        return users;
    }

    // Vérifier si un utilisateur a réagi à un message
    public boolean utilisateurAReact(int messageId, int userId) throws SQLException {
        String query = "SELECT id FROM reaction WHERE message_id = ? AND user_id = ?";
        PreparedStatement ps = cnx.prepareStatement(query);
        ps.setInt(1, messageId);
        ps.setInt(2, userId);
        return ps.executeQuery().next();
    }
}
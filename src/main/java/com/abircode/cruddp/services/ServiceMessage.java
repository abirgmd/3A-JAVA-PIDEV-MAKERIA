package com.abircode.cruddp.services;

import com.abircode.cruddp.entities.Message;
import com.abircode.cruddp.entities.User;
import com.abircode.cruddp.utils.DBConnexion;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class ServiceMessage implements IService<Message> {
    Connection connection;

    public ServiceMessage() {
        connection = DBConnexion.getCon();
    }

    @Override
    public void ajouter(Message message) throws SQLException {
        String req = "INSERT INTO message (titremsg, descriptionmsg, contenu, datemsg, image, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(req)) {
            preparedStatement.setString(1, message.getTitreMsg());
            preparedStatement.setString(2, message.getDescriptionMsg());
            preparedStatement.setString(3, message.getContenu());
            preparedStatement.setString(4, message.getDateMsg());
            preparedStatement.setString(5, message.getImage());
            preparedStatement.setInt(6, message.getUser().getId());

            preparedStatement.executeUpdate();
            System.out.println("Message ajouté");
        }
    }

    @Override
    public void modifier(Message message) throws SQLException {
        String req = "UPDATE message SET titremsg=?, descriptionmsg=?, contenu=?, datemsg=?, image=?, user_id=? " +
                "WHERE id=?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(req)) {
            preparedStatement.setString(1, message.getTitreMsg());
            preparedStatement.setString(2, message.getDescriptionMsg());
            preparedStatement.setString(3, message.getContenu());
            preparedStatement.setString(4, message.getDateMsg());
            preparedStatement.setString(5, message.getImage());
            preparedStatement.setInt(6, message.getUser().getId());
            preparedStatement.setInt(7, message.getId());

            preparedStatement.executeUpdate();
            System.out.println("Message modifié");
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM message WHERE id=?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(req)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
            System.out.println("Message supprimé");
        }
    }

    @Override
    public List<Message> afficher() throws SQLException {
        List<Message> messages = new ArrayList<>();
        String req = "SELECT m.*, u.email AS user_email, u.name AS user_name " +
                "FROM message m " +
                "JOIN user u ON m.user_id = u.id";

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(req)) {

            while (rs.next()) {
                Message message = new Message();
                message.setId(rs.getInt("id"));
                message.setTitreMsg(rs.getString("titremsg"));
                message.setDescriptionMsg(rs.getString("descriptionmsg"));
                message.setContenu(rs.getString("contenu"));
                message.setDateMsg(rs.getString("datemsg"));
                message.setImage(rs.getString("image"));

                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setEmail(rs.getString("user_email"));
                user.setName(rs.getString("user_name"));
                message.setUser(user);

                messages.add(message);
            }
        }
        return messages;
    }

    public Message trouverParId(int id) throws SQLException {
        String req = "SELECT m.*, u.email AS user_email, u.name AS user_name " +
                "FROM message m " +
                "JOIN user u ON m.user_id = u.id " +
                "WHERE m.id=?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(req)) {
            preparedStatement.setInt(1, id);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                Message message = new Message();
                message.setId(rs.getInt("id"));
                message.setTitreMsg(rs.getString("titremsg"));
                message.setDescriptionMsg(rs.getString("descriptionmsg"));
                message.setContenu(rs.getString("contenu"));
                message.setDateMsg(rs.getString("datemsg"));
                message.setImage(rs.getString("image"));

                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setEmail(rs.getString("user_email"));
                user.setName(rs.getString("user_name"));
                message.setUser(user);

                return message;
            }
        }
        return null;
    }

    // Ajoutez ces méthodes si elles n'existent pas déjà :

    public int getTotalMessagesCount() throws SQLException {
        String req = "SELECT COUNT(*) FROM message";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(req)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int getActiveUsersCount() throws SQLException {
        String req = "SELECT COUNT(DISTINCT user_id) FROM message";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(req)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public double getDailyAverageMessages() throws SQLException {
        String req = "SELECT COUNT(*) / COUNT(DISTINCT datemsg) FROM message";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(req)) {
            if (rs.next()) return rs.getDouble(1);
        }
        return 0.0;
    }

    public Map<User, Long> getTopActiveUsers(int limit) throws SQLException {
        Map<User, Long> result = new LinkedHashMap<>();
        String req = "SELECT u.id, u.name, u.email, COUNT(m.id) as message_count " +
                "FROM user u LEFT JOIN message m ON u.id = m.user_id " +
                "GROUP BY u.id, u.name, u.email " +
                "ORDER BY message_count DESC LIMIT ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                long count = rs.getLong("message_count");
                result.put(user, count);
            }
        }
        return result;
    }

    public Map<String, Long> getMessagesByPeriod(String period) throws SQLException {
        Map<String, Long> result = new LinkedHashMap<>();
        String req;

        if ("mois".equalsIgnoreCase(period)) {
            req = "SELECT DATE_FORMAT(datemsg, '%Y-%m') AS periode, COUNT(*) AS count FROM message GROUP BY periode ORDER BY periode";
        } else {
            throw new IllegalArgumentException("Période non supportée : " + period);
        }

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(req)) {
            while (rs.next()) {
                result.put(rs.getString("periode"), rs.getLong("count"));
            }
        }
        return result;
    }


    public Map<String, Long> getMessagesByCategory() throws SQLException {
        Map<String, Long> result = new LinkedHashMap<>();
        String req = "SELECT descriptionmsg, COUNT(*) as count FROM message GROUP BY descriptionmsg";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(req)) {
            while (rs.next()) {
                result.put(rs.getString(1), rs.getLong(2));
            }
        }
        return result;
    }

}

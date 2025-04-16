package com.abircode.cruddp.services;
import com.abircode.cruddp.entities.Message;
import com.abircode.cruddp.utils.DBConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceMessage implements IService<Message> {
    Connection connection;

    public ServiceMessage() {
        connection =DBConnexion.getCon();
    }

    @Override
    public void ajouter(Message message) throws SQLException {
        String req = "INSERT INTO message (titremsg, descriptionmsg, contenu, datemsg) " +
                "VALUES (?, ?, ?, ?)";

        PreparedStatement preparedStatement = connection.prepareStatement(req);
        preparedStatement.setString(1, message.getTitreMsg());
        preparedStatement.setString(2, message.getDescriptionMsg());
        preparedStatement.setString(3, message.getContenu());
        preparedStatement.setString(4, message.getDateMsg());

        preparedStatement.executeUpdate();
        System.out.println("Message ajouté");
    }

    @Override
    public void modifier(Message message) throws SQLException {
        String req = "UPDATE message SET titremsg=?, descriptionmsg=?, contenu=?, datemsg=? " +
                "WHERE id=?";

        PreparedStatement preparedStatement = connection.prepareStatement(req);
        preparedStatement.setString(1, message.getTitreMsg());
        preparedStatement.setString(2, message.getDescriptionMsg());
        preparedStatement.setString(3, message.getContenu());
        preparedStatement.setString(4, message.getDateMsg());
        preparedStatement.setInt(5, message.getId());

        preparedStatement.executeUpdate();
        System.out.println("Message modifié");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM message WHERE id=?";

        PreparedStatement preparedStatement = connection.prepareStatement(req);
        preparedStatement.setInt(1, id);

        preparedStatement.executeUpdate();
        System.out.println("Message supprimé");
    }

    @Override
    public List<Message> afficher() throws SQLException {
        List<Message> messages = new ArrayList<>();
        String req = "SELECT * FROM message";

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(req);

        while (rs.next()) {
            Message message = new Message();
            message.setId(rs.getInt("id"));
            message.setTitreMsg(rs.getString("titremsg"));
            message.setDescriptionMsg(rs.getString("descriptionmsg"));
            message.setContenu(rs.getString("contenu"));
            message.setDateMsg(rs.getString("datemsg"));

            messages.add(message);
        }

        return messages;
    }

    // Méthode supplémentaire pour trouver un message par son ID
    public Message trouverParId(int id) throws SQLException {
        String req = "SELECT * FROM message WHERE id=?";

        PreparedStatement preparedStatement = connection.prepareStatement(req);
        preparedStatement.setInt(1, id);

        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            Message message = new Message();
            message.setId(rs.getInt("id"));
            message.setTitreMsg(rs.getString("titremsg"));
            message.setDescriptionMsg(rs.getString("descriptionmsg"));
            message.setContenu(rs.getString("contenu"));
            message.setDateMsg(rs.getString("datemsg"));
            return message;
        }
        return null;
    }
}


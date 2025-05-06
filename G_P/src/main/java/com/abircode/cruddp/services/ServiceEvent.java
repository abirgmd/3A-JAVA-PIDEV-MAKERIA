package com.abircode.cruddp.services;
import com.abircode.cruddp.entities.Event;
import com.abircode.cruddp.utils.DBConnexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceEvent implements IService<Event> {

    private Connection connection;

    public ServiceEvent() {
        connection = DBConnexion.getCon();
    }

    @Override
    public void ajouter(Event event) throws SQLException {
        String sql = "INSERT INTO `event`(`titreevents`, `descriptionevents`, `image1events`, `image2events`, `date_debut`, `date_fin`) VALUES (?,?,?,?,?,?)";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1, event.getTitreevents());
        pst.setString(2, event.getDescriptionevents());
        pst.setString(3, event.getImage1events());
        pst.setString(4, event.getImage2events());
        pst.setTimestamp(5, Timestamp.valueOf(event.getDate_debut()));
        pst.setTimestamp(6, Timestamp.valueOf(event.getDate_fin()));
        pst.executeUpdate();
    }

    @Override
    public void modifier(Event event) throws SQLException {
        String sql = "UPDATE `event` SET `titreevents`=?, `descriptionevents`=?, `image1events`=?, `image2events`=?, `date_debut`=?, `date_fin`=? WHERE id=?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1, event.getTitreevents());
        pst.setString(2, event.getDescriptionevents());
        pst.setString(3, event.getImage1events());
        pst.setString(4, event.getImage2events());
        pst.setTimestamp(5, Timestamp.valueOf(event.getDate_debut()));
        pst.setTimestamp(6, Timestamp.valueOf(event.getDate_fin()));
        pst.setInt(7, event.getId());
        pst.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM `event` WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Event> afficher() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM event";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            Event e = new Event();
            e.setId(rs.getInt("id"));
            e.setTitreevents(rs.getString("titreevents"));
            e.setDescriptionevents(rs.getString("descriptionevents"));
            e.setImage1events(rs.getString("image1events"));
            e.setImage2events(rs.getString("image2events"));
            e.setDate_debut(rs.getTimestamp("date_debut").toLocalDateTime());
            e.setDate_fin(rs.getTimestamp("date_fin").toLocalDateTime());
            events.add(e);
        }
        return events;
    }
    
    public Event getById(int id) throws SQLException {
        return afficher().stream().filter(e -> e.getId() == id).findFirst().orElse(null);
    }
    public List<Event> getAllEvents() throws SQLException {
        List<Event> events = new ArrayList<>();
        String query = "SELECT * FROM event"; // Requête pour récupérer tous les événements

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Event event = new Event();
                event.setId(resultSet.getInt("id"));
                event.setTitreevents(resultSet.getString("titreevents"));
                event.setDescriptionevents(resultSet.getString("descriptionevents"));
                event.setDate_debut(resultSet.getDate("date_debut").toLocalDate().atStartOfDay());
                event.setDate_fin(resultSet.getDate("date_fin").toLocalDate().atStartOfDay());
                event.setImage1events(resultSet.getString("image1events"));

                events.add(event);
            }
        }

        return events;
    }
} 
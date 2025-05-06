package com.abircode.cruddp.services;
import com.abircode.cruddp.entities.Commentaire;
import com.abircode.cruddp.entities.Event;
import com.abircode.cruddp.utils.DBConnexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCommentaire implements IService<Commentaire> {

    private Connection connection;

    public ServiceCommentaire() {
        connection = DBConnexion.getCon();
    }

    @Override
    public void ajouter(Commentaire commentaire) throws SQLException {
        String sql = "INSERT INTO `commentaire`(`nomcomment`, `text_commentaire`, `timecomment`, `event_id`, `user_id`) VALUES (?,?,?,?,?)";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1, commentaire.getNomcomment());
        pst.setString(2, commentaire.getText_commentaire());
        pst.setTimestamp(3, Timestamp.valueOf(commentaire.getTimecomment()));
        pst.setInt(4, commentaire.getEvent().getId());
        pst.setInt(5, commentaire.getUserId());
        pst.executeUpdate();
    }

    @Override
    public void modifier(Commentaire commentaire) throws SQLException {
        String sql = "UPDATE `commentaire` SET `nomcomment`=?, `text_commentaire`=?, `timecomment`=?, `event_id`=?, `user_id`=? WHERE id=?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1, commentaire.getNomcomment());
        pst.setString(2, commentaire.getText_commentaire());
        pst.setTimestamp(3, Timestamp.valueOf(commentaire.getTimecomment()));
        pst.setInt(4, commentaire.getEvent().getId());
        pst.setInt(5, commentaire.getUserId());
        pst.setInt(6, commentaire.getId());
        pst.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM `commentaire` WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Commentaire> afficher() throws SQLException {
        List<Commentaire> commentaires = new ArrayList<>();
        String sql = "SELECT c.*, e.id as event_id, e.titreevents, e.descriptionevents, e.image1events, e.image2events, e.date_debut, e.date_fin FROM commentaire c JOIN event e ON c.event_id = e.id";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            Commentaire c = new Commentaire();
            c.setId(rs.getInt("id"));
            c.setNomcomment(rs.getString("nomcomment"));
            c.setText_commentaire(rs.getString("text_commentaire"));
            c.setTimecomment(rs.getTimestamp("timecomment").toLocalDateTime());
            c.setUserId(rs.getInt("user_id"));
            
            // Créer l'objet Event
            Event e = new Event();
            e.setId(rs.getInt("event_id"));
            e.setTitreevents(rs.getString("titreevents"));
            e.setDescriptionevents(rs.getString("descriptionevents"));
            e.setImage1events(rs.getString("image1events"));
            e.setImage2events(rs.getString("image2events"));
            e.setDate_debut(rs.getTimestamp("date_debut").toLocalDateTime());
            e.setDate_fin(rs.getTimestamp("date_fin").toLocalDateTime());
            c.setEvent(e);
            
            commentaires.add(c);
        }
        return commentaires;
    }
    
    public Commentaire getById(int id) throws SQLException {
        return afficher().stream().filter(c -> c.getId() == id).findFirst().orElse(null);
    }
    
    public List<Commentaire> getByEventId(int eventId) throws SQLException {
        return afficher().stream().filter(c -> c.getEvent().getId() == eventId).toList();
    }
} 
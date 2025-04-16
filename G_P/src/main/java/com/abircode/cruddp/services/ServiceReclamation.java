package com.abircode.cruddp.services;
import com.abircode.cruddp.entities.Reclamation;
import com.abircode.cruddp.entities.StatutReclamation;
import com.abircode.cruddp.utils.DBConnexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReclamation implements IService<Reclamation> {

    private Connection connection;

    public ServiceReclamation() {
        connection = DBConnexion.getCon(); // Corrigé ici
    }

    @Override
    public void ajouter(Reclamation reclamation) throws SQLException {
        String sql = "INSERT INTO `reclamation`( `date_rec`, `description_rec`, `stat_rec`, `type`, `id_utilisateur`) VALUES (?,?,?,?,?)";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setDate(1, new java.sql.Date(reclamation.getDateRec().getTime()));
        pst.setString(2, reclamation.getDescriptionRec());
        pst.setString(3, reclamation.getStatRec().name());
        pst.setString(4, reclamation.getType());
        pst.setInt(5, reclamation.getId_utilisateur());
        pst.executeUpdate();
    }
    @Override
    public void modifier(Reclamation reclamation) throws SQLException {
        String sql = "UPDATE `reclamation` SET `date_rec`=?, `description_rec`=?, `stat_rec`=?, `type`=?, `id_utilisateur`=? WHERE id=?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setDate(1, new java.sql.Date(reclamation.getDateRec().getTime()));
        pst.setString(2, reclamation.getDescriptionRec());
        pst.setString(3, reclamation.getStatRec().name());
        pst.setString(4, reclamation.getType());
        pst.setInt(5, reclamation.getId_utilisateur());
        pst.setInt(6, reclamation.getId());
        pst.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM `reclamation` WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Reclamation> afficher() throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();
        String sql = "SELECT * FROM reclamation";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            Reclamation r = new Reclamation();
            r.setId(rs.getInt("id"));
            r.setDateRec(rs.getDate("date_rec"));
            r.setDescriptionRec(rs.getString("description_rec"));
            r.setStatRec(StatutReclamation.valueOf(rs.getString("stat_rec")));
            r.setType(rs.getString("type"));
            r.setId_utilisateur(rs.getInt("id_utilisateur"));
            reclamations.add(r);
        }
        return reclamations;
    }
    public Reclamation getById(int id) throws SQLException{
        return afficher().stream().filter(r->r.getId()==id).findFirst().orElse(null);
    }


}

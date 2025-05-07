package com.abircode.cruddp.services;

import com.abircode.cruddp.entities.Reponses;
import com.abircode.cruddp.entities.Reclamation;
import com.abircode.cruddp.utils.DBConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReponses implements IService<Reponses> {

    private Connection connection;

    public ServiceReponses() {
        connection = DBConnexion.getCon();
    }

    @Override
    public void ajouter(Reponses reponse) throws SQLException {
        String sql = "INSERT INTO `reponses`(`id_rec_id`, `date_rep`, `contenu_rep`, `id_admin`) VALUES (?,?,?,?)";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setInt(1, reponse.getReclamation().getId()); // récupération de l'id depuis l'objet Reclamation
        pst.setDate(2, new java.sql.Date(reponse.getDateRep().getTime()));
        pst.setString(3, reponse.getContenuRep());
        pst.setInt(4, reponse.getId_admin());
        pst.executeUpdate();
    }

    @Override
    public void modifier(Reponses reponse) throws SQLException {
        String sql = "UPDATE `reponses` SET `id_rec_id`=?, `date_rep`=?, `contenu_rep`=?, `id_admin`=? WHERE id=?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setInt(1, reponse.getReclamation().getId());
        pst.setDate(2, new java.sql.Date(reponse.getDateRep().getTime()));
        pst.setString(3, reponse.getContenuRep());
        pst.setInt(4, reponse.getId_admin());
        pst.setInt(5, reponse.getId());
        pst.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM `reponses` WHERE id = ?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setInt(1, id);
        pst.executeUpdate();
    }

    @Override
    public List<Reponses> afficher() throws SQLException {
        ServiceReclamation sr = new ServiceReclamation();
        List<Reponses> reponsesList = new ArrayList<>();
        String sql = "SELECT * FROM reponses";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            Reponses r = new Reponses();
            r.setId(rs.getInt("id"));
            r.setDateRep(rs.getDate("date_rep"));
            r.setContenuRep(rs.getString("contenu_rep"));
            r.setId_admin(rs.getInt("id_admin"));


            r.setReclamation(sr.getById(rs.getInt("id_rec_id")));

            reponsesList.add(r);
        }
        return reponsesList;
    }
}

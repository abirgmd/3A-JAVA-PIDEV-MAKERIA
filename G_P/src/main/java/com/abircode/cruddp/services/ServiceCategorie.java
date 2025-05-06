package com.abircode.cruddp.services;
import com.abircode.cruddp.entities.Categorie;
import com.abircode.cruddp.utils.DBConnexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCategorie implements IService<Categorie> {

    private Connection connection;

    public ServiceCategorie() {
        connection = DBConnexion.getCon();
    }

    @Override
    public void ajouter(Categorie categorie) throws SQLException {
        String sql = "INSERT INTO `categorie`(`nomcat`, `descriptioncat`) VALUES (?,?)";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1, categorie.getNomcat());
        pst.setString(2, categorie.getDescriptioncat());
        pst.executeUpdate();
    }

    @Override
    public void modifier(Categorie categorie) throws SQLException {
        String sql = "UPDATE `categorie` SET `nomcat`=?, `descriptioncat`=? WHERE id=?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1, categorie.getNomcat());
        pst.setString(2, categorie.getDescriptioncat());
        pst.setInt(3, categorie.getId());
        pst.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM `categorie` WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Categorie> afficher() throws SQLException {
        List<Categorie> categories = new ArrayList<>();
        String sql = "SELECT * FROM categorie";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            Categorie c = new Categorie();
            c.setId(rs.getInt("id"));
            c.setNomcat(rs.getString("nomcat"));
            c.setDescriptioncat(rs.getString("descriptioncat"));
            categories.add(c);
        }
        return categories;
    }
    
    public Categorie getById(int id) throws SQLException {
        return afficher().stream().filter(c -> c.getId() == id).findFirst().orElse(null);
    }

} 
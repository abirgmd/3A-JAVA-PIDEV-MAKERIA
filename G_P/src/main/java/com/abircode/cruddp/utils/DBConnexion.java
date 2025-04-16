package com.abircode.cruddp.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class DBConnexion {
    static String user = "root";  // Change this to your MySQL username
    static String password = "";  // Change this to your MySQL password
    static String url = "jdbc:mysql://localhost:3306/makeria";  // Using pidev database
    static String driver = "com.mysql.cj.jdbc.Driver";

    public static Connection getCon() {
        Connection con = null;
        try {
            Class.forName(driver);
            System.out.println("Driver chargé avec succès.");

            try {
                // Établir la connexion
                con = DriverManager.getConnection(url, user, password);
                System.out.println("Connexion à la base de données réussie.");
            } catch (SQLException e) {
                System.err.println("Erreur lors de la connexion à la base de données : " + e.getMessage());
                throw new RuntimeException(e);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC introuvable : " + e.getMessage());
            throw new RuntimeException(e);
        }
        return con;
    }
}

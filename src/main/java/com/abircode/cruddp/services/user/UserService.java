package com.abircode.cruddp.services.user;


import com.abircode.cruddp.entities.User;
import com.abircode.cruddp.utils.MyDataBase;
import com.abircode.cruddp.utils.PasswordHasher;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService {
    private Connection connection;

    public UserService() {
        connection = MyDataBase.getInstance().getCnx();
    }



    // Create a new user (registration)
    public void createUser(User user) throws SQLException {
        String query = "INSERT INTO user (email, password, name, lastname, image, blok, accepted, phone, roles) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, PasswordHasher.hashPassword(user.getPassword()));
            stmt.setString(3, user.getName());
            stmt.setString(4, user.getLastname());
            stmt.setString(5, user.getImage() != null ? user.getImage() : "default.png");
            stmt.setBoolean(6, user.isBlok());
            stmt.setObject(7, user.getAccepted());
            stmt.setObject(8, user.getPhone());
            stmt.setString(9, user.getRoles() != null ? user.getRoles() : "[\"ROLE_USER\"]");

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    // Create a new artist (specific registration)
    public void createArtist(User artist) throws SQLException {
        artist.setRoles("[\"ROLE_ARTIST\"]");
        artist.setBlok(false);
        createUser(artist);
    }

    // Read operations
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                     users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }



    public Map<String, Integer> getUserStatistics() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();

        // Total users
        String totalQuery = "SELECT COUNT(*) FROM user";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(totalQuery)) {
            if (rs.next()) {
                stats.put("total", rs.getInt(1));
            }
        }

        // Active users
        String activeQuery = "SELECT COUNT(*) FROM user WHERE blok = false";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(activeQuery)) {
            if (rs.next()) {
                stats.put("active", rs.getInt(1));
            }
        }

        // Blocked users
        String blockedQuery = "SELECT COUNT(*) FROM user WHERE blok = true";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(blockedQuery)) {
            if (rs.next()) {
                stats.put("blocked", rs.getInt(1));
            }
        }

        return stats;
    }
    public void updateBlockStatus(int userId, boolean blocked) throws SQLException {
        String query = "UPDATE user SET blok = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setBoolean(1, blocked);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }
    // Get all users with pagination
    public List<User> getAllUsers(int page, int itemsPerPage) throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user LIMIT ? OFFSET ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, itemsPerPage);
            stmt.setInt(2, (page - 1) * itemsPerPage);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    // Get user by ID
    public User getUserById(int id) throws SQLException {
        String query = "SELECT * FROM user WHERE id = ?";

        try (PreparedStatement x = connection.prepareStatement(query)) {
            x.setInt(1, id);
            ResultSet rs = x.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        }
        return null;
    }
    // Search users by query (email, name, or lastname)
    public List<User> searchUsers(String searchQuery) throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user WHERE email LIKE ? OR name LIKE ? OR lastname LIKE ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            String likeParam = "%" + searchQuery + "%";
            stmt.setString(1, likeParam);
            stmt.setString(2, likeParam);
            stmt.setString(3, likeParam);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    // Update user
    public void updateUser(User user) throws SQLException {
        String query = "UPDATE user SET email = ?, name = ?, lastname = ?, image = ?, " +
                "blok = ?, accepted = ?, phone = ?, roles = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getLastname());
            stmt.setString(4, user.getImage());
            stmt.setBoolean(5, user.isBlok());
            stmt.setObject(6, user.getAccepted());
            stmt.setObject(7, user.getPhone());
            stmt.setString(8, user.getRoles());
            stmt.setInt(9, user.getId());

            stmt.executeUpdate();
        }
    }

    // Update password
    public void updatePassword(int userId, String newPassword) throws SQLException {
        String query = "UPDATE user SET password = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, PasswordHasher.hashPassword(newPassword));
            stmt.setInt(2, userId);

            stmt.executeUpdate();
        }
    }

    // Delete user
    public void deleteUser(int userId) throws SQLException {
        String query = "DELETE FROM user WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    // Statistics

    // Count users by block status
    public Map<String, Integer> countUsersByBlockStatus() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        String query = "SELECT blok, COUNT(id) as userCount FROM user GROUP BY blok";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                boolean isBlocked = rs.getBoolean("blok");
                int count = rs.getInt("userCount");
                stats.put(isBlocked ? "Blocked" : "Not Blocked", count);
            }
        }
        return stats;
    }

    // Authentication

    // Login
    public User login(String email, String password) throws SQLException {
        // Vérifier d'abord si le compte est bloqué
        LoginAttemptManager attemptManager = LoginAttemptManager.getInstance();
        if (attemptManager.isBlocked(email)) {
            long remainingTime = attemptManager.getRemainingBlockTime(email);
            if (remainingTime > 0) {
                return null; // Compte toujours bloqué
            }
        }

        String query = "SELECT * FROM user WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                if (PasswordHasher.checkPassword(password, hashedPassword)) {
                    // Réussite - réinitialiser les tentatives
                    attemptManager.resetAttempts(email);
                    return mapResultSetToUser(rs);
                } else {
                    // Échec - enregistrer la tentative
                    attemptManager.recordFailedAttempt(email);
                }
            }
            return null;
        }
    }
    // Helper method to map ResultSet to User object
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setName(rs.getString("name"));
        user.setLastname(rs.getString("lastname"));
        user.setImage(rs.getString("image"));
        user.setBlok(rs.getBoolean("blok"));
        user.setAccepted(rs.getBoolean("accepted"));
        user.setPhone(rs.getInt("phone"));
        user.setRoles(rs.getString("roles"));
        return user;
    }
    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM user WHERE email = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, email);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by email: " + e.getMessage());
        }
        return null;
    }
    // Additional utility methods

    public boolean emailExists(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM user WHERE email = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public int getTotalUserCount() throws SQLException {
        String query = "SELECT COUNT(*) FROM user";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    // Add this to your UserService class
    public void updatePasswordByEmail(String email, String newPassword) throws SQLException {
        String query = "UPDATE user SET password = ? WHERE email = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, PasswordHasher.hashPassword(newPassword));
            stmt.setString(2, email);
            stmt.executeUpdate();
        }
    }
}
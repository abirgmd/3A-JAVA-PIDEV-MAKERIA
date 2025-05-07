package com.abircode.cruddp.Controller.user;

import com.abircode.cruddp.entities.User;
import com.abircode.cruddp.services.user.UserService;
import com.abircode.cruddp.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdminDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label userCountLabel;
    @FXML private Label timeLabel;
    @FXML private Label roleLabel;
    @FXML private HBox headerBox;
    @FXML private Pane contentPane;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        User currentUser = SessionManager.getCurrentUser();
        updateHeader(currentUser);
        loadStatistics();
        startClock();
    }

    private void updateHeader(User user) {
        welcomeLabel.setText("Welcome, " + user.getName() + " " + user.getLastname());
        roleLabel.setText("Admin Dashboard");

        String initials = user.getName().charAt(0) + "" + user.getLastname().charAt(0);
        Label avatar = new Label(initials);
        avatar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 14px; " +
                "-fx-padding: 5px; -fx-background-radius: 50%; " +
                "-fx-min-width: 30px; -fx-min-height: 30px; " +
                "-fx-alignment: center;");
        headerBox.getChildren().add(avatar);
    }

    private void loadStatistics() {
        try {
            int totalUsers = userService.getTotalUserCount();
            userCountLabel.setText("Total Users: " + totalUsers);
        } catch (Exception e) {
            showAlert("Error", "Failed to load statistics", Alert.AlertType.ERROR);
        }
    }

    private void startClock() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss | EEEE, MMMM d");
        timeLabel.setText(LocalDateTime.now().format(formatter));

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(() -> {
                        timeLabel.setText(LocalDateTime.now().format(formatter));
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @FXML
    private void handleLogout() {
        try {
            SessionManager.clearSession();

            // Load login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) headerBox.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to logout", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void showUserManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/userManagement.fxml"));
            Parent root = loader.load();

            contentPane.getChildren().clear();
            contentPane.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace(); // 👈 Obligatoire pour diagnostiquer précisément

            System.out.println(e);
              showAlert("Error", "Failed to load user management", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void showAnalytics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/analytics.fxml"));
            Parent root = loader.load();

            contentPane.getChildren().clear();
            contentPane.getChildren().add(root);
        } catch (IOException e) {
            showAlert("Error", "Failed to load analytics", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
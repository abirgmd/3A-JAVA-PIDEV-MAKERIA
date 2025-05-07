package com.abircode.cruddp.Controller.user;

import com.abircode.cruddp.entities.User;
import com.abircode.cruddp.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label lastLoginLabel;
    @FXML private Label timeLabel;
    @FXML private Label roleLabel;
    @FXML private HBox headerBox;
    @FXML private Pane contentPane; // Add this to match your FXML

    @FXML
    public void initialize() {
        // Get current user from session
        User currentUser = SessionManager.getCurrentUser();

        // Update header information
        updateHeader(currentUser);

        // Start clock
        startClock();

        // Load user-specific data
        loadUserData();
    }

    private void updateHeader(User user) {
        welcomeLabel.setText("Welcome, " + user.getName());
        roleLabel.setText(user.getRoles().contains("ROLE_ARTIST") ? "Artist Account" : "User Account");



        // Add avatar
        Label avatar = new Label(user.getName().charAt(0) + "" + user.getLastname().charAt(0));
        avatar.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 14px; " +
                "-fx-padding: 5px; -fx-background-radius: 50%; " +
                "-fx-min-width: 30px; -fx-min-height: 30px; " +
                "-fx-alignment: center;");

        headerBox.getChildren().add(avatar);
    }

    private void startClock() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm | MMM d");

        // Update time immediately
        timeLabel.setText(LocalDateTime.now().format(formatter));

        // Update time every minute
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000);
                    javafx.application.Platform.runLater(() -> {
                        timeLabel.setText(LocalDateTime.now().format(formatter));
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void loadUserData() {
         lastLoginLabel.setText("Last login: Today at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        // Load login screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Stage stage = (Stage) headerBox.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Login");
        stage.show();    }
    @FXML
    void showdash() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/dashboard.fxml"));

        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        contentPane.getChildren().clear();
        contentPane.getChildren().add(root);
        UserProfileController controller = loader.getController();
        controller.initializeUserData(SessionManager.getCurrentUser().getId());



    }
    @FXML
    private void showProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/profile.fxml"));

            Parent root = loader.load();

            contentPane.getChildren().clear();
            contentPane.getChildren().add(root);
            UserProfileController controller = loader.getController();
            controller.initializeUserData(SessionManager.getCurrentUser().getId());




        } catch (IOException e) {
            e.printStackTrace();

        }    }

    @FXML
    private void gotoedit() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/editUser.fxml"));
            Parent root = loader.load();


            contentPane.getChildren().clear();
            contentPane.getChildren().add(root);

            EditUserController controller = loader.getController();
            controller.setUser(SessionManager.getCurrentUser());


        } catch (IOException e) {
            e.printStackTrace();

        }    }


}
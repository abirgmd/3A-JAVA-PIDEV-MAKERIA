package com.abircode.cruddp.Controller.user.admin;

import com.abircode.cruddp.Controller.user.UserProfileController;
import com.abircode.cruddp.entities.User;
import com.abircode.cruddp.services.user.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class UserCardController {
    @FXML private ImageView userImageView;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private Label statusLabel;
    @FXML private HBox actionsBox;

    private User user;
    private UserService userService = new UserService();
    private UserManagementController parentController;

    public void setUser(User user) {
        this.user = user;
        nameLabel.setText(user.getName() + " " + user.getLastname());
        emailLabel.setText(user.getEmail());
        roleLabel.setText(user.getRoles());

        // Set status
        statusLabel.setText(user.isBlok() ? "BLOCKED" : "Active");
        statusLabel.setStyle(user.isBlok() ? "-fx-text-fill: red;" : "-fx-text-fill: green;");

        // Load user image (same logic as UserProfileController)
        try {
            if (user.getImage() != null && !user.getImage().isEmpty()) {
                File file = new File("src/main/resources/uploads/users/" + user.getImage());

                System.out.println("Image path: " + file.getAbsolutePath());
                System.out.println("Exists? " + file.exists());

                if (file.exists()) {
                    userImageView.setImage(new Image(file.toURI().toString()));
                } else {
                    // Default fallback image
                    userImageView.setImage(new Image(getClass().getResource("/uploads/users/default.png").toExternalForm()));
                }
            } else {
                userImageView.setImage(new Image(getClass().getResource("/uploads/users/default.png").toExternalForm()));
            }
        } catch (Exception e) {
            e.printStackTrace();
         }

        // Create action buttons
        createActionButtons();
    }

    public void setParentController(UserManagementController controller) {
        this.parentController = controller;
    }

    private void createActionButtons() {
        actionsBox.getChildren().clear();

        Button profileBtn = new Button("Profile");
        profileBtn.getStyleClass().add("profile-btn");
        profileBtn.setOnAction(e -> handleShowProfile());

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("edit-btn");
        editBtn.setOnAction(e -> parentController.handleEditUser(user));

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("delete-btn");
        deleteBtn.setOnAction(e -> parentController.handleDeleteUser(user));

        Button blockBtn = new Button(user.isBlok() ? "Unblock" : "Block");
        blockBtn.getStyleClass().add(user.isBlok() ? "unblock-btn" : "block-btn");
        blockBtn.setOnAction(e -> parentController.handleBlockToggle(user));

        actionsBox.getChildren().addAll(profileBtn, editBtn, deleteBtn, blockBtn);
    }

    @FXML
    private void handleShowProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/profile.fxml"));
            Parent root = loader.load();

            UserProfileController controller = loader.getController();
            controller.initializeUserData(user.getId());

             Stage stage = new Stage();
             stage.setTitle("Profile");
             stage.setScene(new Scene(root));
             stage.initModality(Modality.WINDOW_MODAL);
             stage.initOwner(userImageView.getScene().getWindow());
             stage.showAndWait();



        } catch (IOException e) {
            e.printStackTrace();

        }

}
}

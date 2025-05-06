package com.abircode.cruddp.Controller.user;

import com.abircode.cruddp.entities.User;
import com.abircode.cruddp.services.user.UserService;
import com.abircode.cruddp.utils.FileUploader;
import com.abircode.cruddp.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class EditUserController {
    @FXML private ImageView userImageView;
    @FXML private TextField emailField;
    @FXML private TextField nameField;
    @FXML private TextField lastnameField;
    @FXML private TextField phoneField;


    private User user;
    private UserService userService = new UserService();

    public void setUser(User user) {
        this.user = user;
        populateFields();
    }

    private void populateFields() {
        emailField.setText(user.getEmail());
        nameField.setText(user.getName());
        lastnameField.setText(user.getLastname());
        phoneField.setText(user.getPhone() != null ? String.valueOf(user.getPhone()) : "");

         String role = user.getRoles().replace("ROLE_", ""); // Remove 'ROLE_' prefix


        // Load user image
        loadUserImage();
    }
    private File selectedImageFile; // Track the selected image file

    private void loadUserImage() {
        if (user.getImage() != null && !user.getImage().isEmpty()) {
            // Check if we have a newly selected file first
            if (selectedImageFile != null) {
                userImageView.setImage(new Image(selectedImageFile.toURI().toString()));
            } else {
                // Load from saved path
                File file = new File("src/main/resources/uploads/users/" + user.getImage());
                if (file.exists()) {
                    userImageView.setImage(new Image(file.toURI().toString()));
                } else {
                    // Set default image if no image exists
                    setDefaultImage();
                }
            }
        } else {
            setDefaultImage();
        }
    }
    private void setDefaultImage() {
        try {
            Image defaultImage = new Image(getClass().getResource("/uploads/users/default.png").toExternalForm());
            userImageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Could not load default image");
        }
    }

    @FXML
    private void handleChangeImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        selectedImageFile = fileChooser.showOpenDialog(userImageView.getScene().getWindow());
        if (selectedImageFile != null) {
            try {
                userImageView.setImage(new Image(selectedImageFile.toURI().toString()));
            } catch (Exception e) {
                showAlert("Error", "Could not load the selected image");
                e.printStackTrace();
            }
        }
    }

    @FXML
    void resetpassword( ) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/resetpassword.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();




            ResetPasswordController controller = loader.getController();
            controller.setCurrentUser(SessionManager.getCurrentUser());


        } catch (IOException e) {
            e.printStackTrace();

        }    }




    @FXML
    public void handleSave() {
        try {
            // Handle image upload first if new image was selected
            if (selectedImageFile != null) {
                String uploadedImagePath = FileUploader.uploadImage(selectedImageFile);
                user.setImage(uploadedImagePath);
            }

            // Update user fields
            user.setEmail(emailField.getText());
            user.setName(nameField.getText());
            user.setLastname(lastnameField.getText());
            user.setPhone(Integer.valueOf(phoneField.getText()));

            // Handle roles - ensure clean JSON format




            // Save the user
            userService.updateUser(user);
            showAlert("Success", "mise a jour avec succées");


            if (onUserUpdated != null) {
                onUserUpdated.run();
            }


        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update user: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow(); // Just close the window without saving
    }

    private void closeWindow() {
        userImageView.getScene().getWindow().hide(); // Closes the current window
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Runnable onUserUpdated;

 }

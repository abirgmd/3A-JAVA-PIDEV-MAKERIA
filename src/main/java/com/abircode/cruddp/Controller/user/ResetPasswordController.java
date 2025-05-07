package com.abircode.cruddp.Controller.user;

import com.abircode.cruddp.entities.User;
import com.abircode.cruddp.services.user.UserService;
import com.abircode.cruddp.utils.PasswordHasher;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;

public class ResetPasswordController {
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;

    private User currentUser;
    private UserService userService = new UserService();

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void handleSave() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();

        // Validate inputs
        if (oldPassword.isEmpty() || newPassword.isEmpty()) {
            showAlert("Error", "Please fill in all fields");
            return;
        }

        if (newPassword.length() < 6) {
            showAlert("Error", "New password must be at least 6 characters long");
            return;
        }

        try {
            // Verify old password
            if (!PasswordHasher.checkPassword(oldPassword, currentUser.getPassword())) {
                showAlert("Error", "Old password is incorrect");
                return;
            }

            // Update password
            userService.updatePassword(currentUser.getId(), newPassword);

            // Show success message
            showAlert("Success", "Password changed successfully");

            // Close the window
            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to change password: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        oldPasswordField.getScene().getWindow().hide();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(
                message.startsWith("Success") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
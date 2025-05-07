package com.abircode.cruddp.Controller.user;

import com.abircode.cruddp.entities.User;
import com.abircode.cruddp.services.user.QRCodeService;
import com.abircode.cruddp.services.user.UserService;
import com.abircode.cruddp.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;

public class UserProfileController {
    @FXML
    public VBox contentPane;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phone;
    @FXML private ImageView qrCodeImage;
    @FXML private ImageView profileImage;

    private final UserService userService = new UserService();
    private final QRCodeService qrCodeService = new QRCodeService();

    public void initializeUserData(int userId) {
        try {
            User user = userService.getUserById(userId);
            if (user != null) {
                nameLabel.setText(user.getName() + " " + user.getLastname());
                emailLabel.setText("Email : " +user.getEmail());
                phone.setText("Phone number : "+user.getPhone().toString());

                // Load profile image from file
                if (user.getImage() != null && !user.getImage().isEmpty()) {
                    File file = new File("src/main/resources/uploads/users/" + user.getImage());
                    if (file.exists()) {
                        profileImage.setImage(new Image(file.toURI().toString()));
                    }
                }

                // Generate and show QR code
                String qrData = qrCodeService.generateUserQRCodeData(user);
                Image qrImage = qrCodeService.generateQRCodeImage(qrData, 300, 300);
                qrCodeImage.setImage(qrImage);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Échec de chargement du profil utilisateur.");
        }
    }
    @FXML
    void edit(ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/editUser.fxml"));
            Parent root = loader.load();


            contentPane.getChildren().clear();
            contentPane.getChildren().add(root);

            EditUserController controller = loader.getController();
            controller.setUser(SessionManager.getCurrentUser());


        } catch (IOException e) {
            e.printStackTrace();

        }
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

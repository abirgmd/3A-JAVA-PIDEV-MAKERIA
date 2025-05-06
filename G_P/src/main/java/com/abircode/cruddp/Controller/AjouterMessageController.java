package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Message;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import com.abircode.cruddp.services.ServiceMessage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AjouterMessageController {

    @FXML private TextField tf_titre;
    @FXML private TextField tf_description;
    @FXML private TextArea ta_contenu;

    @FXML private Label error_titre;
    @FXML private Label error_description;
    @FXML private Label error_contenu;


    @FXML
    public void AjouterMessage(ActionEvent actionEvent) {

        if (!validerChamps()) {
            return; // Bloquer l'ajout si erreur
        }

        String dateNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        ServiceMessage serviceMessage = new ServiceMessage();
        Message message = new Message(
                tf_titre.getText(),
                tf_description.getText(),
                ta_contenu.getText(),
                dateNow
        );

        try {
            serviceMessage.ajouter(message);
            showAlert("Succès", "Message ajouté avec succès", Alert.AlertType.INFORMATION);
            clearFields();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout du message: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validerChamps() {
        boolean isValid = true;

        error_titre.setText("");
        error_description.setText("");
        error_contenu.setText("");

        if (tf_titre.getText().isEmpty()) {
            error_titre.setText("Le titre est obligatoire.");
            isValid = false;
        } else if (tf_titre.getText().length() > 20) {
            error_titre.setText("Le titre ne doit pas dépasser 20 caractères.");
            isValid = false;
        }

        if (tf_description.getText().isEmpty()) {
            error_description.setText("La description est obligatoire.");
            isValid = false;
        } else if (tf_description.getText().length() > 50) {
            error_description.setText("La description ne doit pas dépasser 50 caractères.");
            isValid = false;
        }


        if (ta_contenu.getText().isEmpty()) {
            error_contenu.setText("Le contenu est obligatoire.");
            isValid = false;
        } else if (ta_contenu.getText().length() > 1000) {
            error_contenu.setText("Le contenu ne doit pas dépasser 1000 caractères.");
            isValid = false;
        }


        return isValid;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearFields() {
        tf_titre.clear();
        tf_description.clear();
        ta_contenu.clear();

        error_titre.setText("");
        error_description.setText("");
        error_contenu.setText("");
    }
}

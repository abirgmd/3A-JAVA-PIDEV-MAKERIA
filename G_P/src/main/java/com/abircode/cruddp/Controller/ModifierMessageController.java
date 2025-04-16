package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Message;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import com.abircode.cruddp.services.ServiceMessage;

public class ModifierMessageController {

    @FXML private TextField titreField;
    @FXML private Label titreError;
    @FXML private TextArea descriptionField;
    @FXML private Label descriptionError;
    @FXML private TextArea contenuField;
    @FXML private Label contenuError;

    private final ServiceMessage serviceMessage = new ServiceMessage();
    private Message message;

    public void setMessage(Message message) {
        this.message = message;
        titreField.setText(message.getTitreMsg());
        descriptionField.setText(message.getDescriptionMsg());
        contenuField.setText(message.getContenu());
    }

    @FXML
    private void handleSave() {
        clearErrorLabels();

        boolean isValid = true;

        if (titreField.getText().isEmpty()) {
            titreError.setText("Le titre est obligatoire.");
            isValid = false;
        } else if (titreField.getText().length() > 20) {
            titreError.setText("Le titre ne doit pas dépasser 20 caractères.");
            isValid = false;
        }

        if (descriptionField.getText().isEmpty()) {
            descriptionError.setText("La description est obligatoire.");
            isValid = false;
        } else if (descriptionField.getText().length() > 50) {
            descriptionError.setText("La description ne doit pas dépasser 50 caractères.");
            isValid = false;
        }

        if (contenuField.getText().isEmpty()) {
            contenuError.setText("Le contenu est obligatoire.");
            isValid = false;
        } else if (contenuField.getText().length() > 500) {
            contenuError.setText("Le contenu ne doit pas dépasser 500 caractères.");
            isValid = false;
        }

        if (!isValid) return;

        try {
            message.setTitreMsg(titreField.getText());
            message.setDescriptionMsg(descriptionField.getText());
            message.setContenu(contenuField.getText());

            serviceMessage.modifier(message);
            closeWindow();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de modifier le message : " + e.getMessage());
        }
    }
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) titreField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void clearErrorLabels() {
        titreError.setText("");
        descriptionError.setText("");
        contenuError.setText("");
    }

    private void closeWindow() {
        Stage stage = (Stage) titreField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.OrderInformations;
import com.abircode.cruddp.services.ServiceOrderInfo;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;

public class ModifierOrderInfoController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField villeField;
    @FXML private TextField adresseField;
    @FXML private TextField codePostalField;
    @FXML private TextField telField;
    @FXML private TextField emailField;

    private OrderInformations currentOrder;
    private final ServiceOrderInfo service = new ServiceOrderInfo();

    public void setOrder(OrderInformations order) {
        this.currentOrder = order;

        // Pré-remplir les champs
        nomField.setText(order.getNom());
        prenomField.setText(order.getPrenom());
        villeField.setText(order.getVille());
        adresseField.setText(order.getAdresseLivraison());
        codePostalField.setText(order.getCodePostal());
        telField.setText(order.getNumTel());
        emailField.setText(order.getEmail());
    }

    @FXML
    private void handleSave() {
        if (!validateFields()) {
            return;
        }

        try {
            currentOrder.setNom(nomField.getText());
            currentOrder.setPrenom(prenomField.getText());
            currentOrder.setVille(villeField.getText());
            currentOrder.setAdresseLivraison(adresseField.getText());
            currentOrder.setCodePostal(codePostalField.getText());
            currentOrder.setNumTel(telField.getText());
            currentOrder.setEmail(emailField.getText());

            service.modifier(currentOrder);

            // Fermer la fenêtre
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        // Fermer la fenêtre sans rien faire
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    private boolean validateFields() {
        // Vérifier si un champ est vide
        if (nomField.getText().isEmpty() ||
                prenomField.getText().isEmpty() ||
                villeField.getText().isEmpty() ||
                adresseField.getText().isEmpty() ||
                codePostalField.getText().isEmpty() ||
                telField.getText().isEmpty() ||
                emailField.getText().isEmpty()) {

            showAlert("Erreur", "Champs manquants", "Veuillez remplir tous les champs obligatoires.", AlertType.WARNING);
            return false;
        }

        // Validation pour le code postal (doit être composé de 4 ou 5 chiffres)
        if (!codePostalField.getText().matches("\\d{4,5}")) {
            showAlert("Erreur", "Code Postal invalide", "Le code postal doit être composé de 4 ou 5 chiffres.", AlertType.WARNING);
            return false;
        }

        // Validation pour le numéro de téléphone (doit être composé de 10 chiffres)
        if (!telField.getText().matches("\\d{10}")) {
            showAlert("Erreur", "Numéro de téléphone invalide", "Le numéro de téléphone doit être composé de 10 chiffres.", AlertType.WARNING);
            return false;
        }

        // Validation pour l'email
        if (!emailField.getText().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showAlert("Erreur", "Email invalide", "Veuillez entrer une adresse email valide.", AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void showAlert(String title, String header, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

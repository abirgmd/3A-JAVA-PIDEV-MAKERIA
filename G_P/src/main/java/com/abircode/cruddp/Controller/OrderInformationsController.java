package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.OrderInformations;
import com.abircode.cruddp.services.ServiceOrderInfo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;


import java.sql.SQLException;

public class OrderInformationsController {

    @FXML
    private TextField villeField;
    @FXML
    private TextField codePostalField;
    @FXML
    private TextField adresseLivraisonField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField nomField;
    @FXML
    private TextField numTelField;
    @FXML
    private TextField emailField;

    private final ServiceOrderInfo serviceOrderInfo = new ServiceOrderInfo();
    private Integer currentOrderId = null; // Pour suivre l'ID en cas de modification

    @FXML
    public void handleEnregistrerClick(ActionEvent event) {
        if (!validateFields()) {
            return;
        }

        OrderInformations orderInformations = createOrderFromFields();

        try {
            if (currentOrderId == null) {
                serviceOrderInfo.ajouter(orderInformations);
                showAlert("Succès", "Ajout réussi", "Les informations ont été enregistrées avec succès.", AlertType.INFORMATION);
            } else {
                orderInformations.setId(currentOrderId);
                serviceOrderInfo.modifier(orderInformations);
                showAlert("Succès", "Mise à jour réussie", "Les informations ont été mises à jour avec succès.", AlertType.INFORMATION);
            }
            clearFields();
            // Redirection vers PaiementUser.fxml
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/PaiementUser.fxml"));
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Paiement");
                stage.show();

                // Fermer l’ancienne fenêtre si tu veux
                Stage currentStage = (Stage) villeField.getScene().getWindow();
                currentStage.close();

            } catch (IOException e) {
                showAlert("Erreur", "Erreur de chargement", "Impossible de charger l'interface de paiement.", Alert.AlertType.ERROR);
                e.printStackTrace();
            }

            clearFields();

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de base de données", "Erreur lors de l'opération: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void setOrderInfoData(OrderInformations order) {
        if (order != null) {
            this.currentOrderId = order.getId();
            villeField.setText(order.getVille());
            codePostalField.setText(order.getCodePostal());
            adresseLivraisonField.setText(order.getAdresseLivraison());
            prenomField.setText(order.getPrenom());
            nomField.setText(order.getNom());
            numTelField.setText(order.getNumTel());
            emailField.setText(order.getEmail());
        }
    }

    private OrderInformations createOrderFromFields() {
        return new OrderInformations(
                villeField.getText(),
                codePostalField.getText(),
                adresseLivraisonField.getText(),
                prenomField.getText(),
                nomField.getText(),
                numTelField.getText(),
                emailField.getText()
        );
    }

    private boolean validateFields() {
        // Vérifier si un champ est vide
        if (villeField.getText().isEmpty() ||
                codePostalField.getText().isEmpty() ||
                adresseLivraisonField.getText().isEmpty() ||
                prenomField.getText().isEmpty() ||
                nomField.getText().isEmpty() ||
                numTelField.getText().isEmpty() ||
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
        if (!numTelField.getText().matches("\\d{8}")) {
            showAlert("Erreur", "Numéro de téléphone invalide", "Le numéro de téléphone doit être composé de 8 chiffres.", AlertType.WARNING);
            return false;
        }

        // Validation pour l'email
        if (!emailField.getText().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showAlert("Erreur", "Email invalide", "Veuillez entrer une adresse email valide.", AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void clearFields() {
        villeField.clear();
        codePostalField.clear();
        adresseLivraisonField.clear();
        prenomField.clear();
        nomField.clear();
        numTelField.clear();
        emailField.clear();
    }

    private void showAlert(String title, String header, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

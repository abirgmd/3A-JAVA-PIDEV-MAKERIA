package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Paiement;
import com.abircode.cruddp.services.ServicePaiement;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Date;
import java.time.LocalDate;

public class ModifierPaiement {

    @FXML private TextField methodeField;
    @FXML private TextField cardDetailsField;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryField;
    @FXML private TextField cvvField;

    private Paiement paiement;
    private final ServicePaiement service = new ServicePaiement();

    public void setPaiement(Paiement paiement) {
        this.paiement = paiement;

        // Pré-remplir les champs
        methodeField.setText(paiement.getPaymentMethod());
        cardDetailsField.setText(paiement.getCardDetails());
        cardNumberField.setText(paiement.getCardNumber());
        expiryField.setText(paiement.getExpiryDate().toString());
        cvvField.setText(String.valueOf(paiement.getCvv()));
    }

    @FXML
    private void handleSave() {
        // Validation des champs avant traitement
        if (!validateFields()) {
            return;
        }

        try {
            // Mettre à jour les informations du paiement
            paiement.setPaymentMethod(methodeField.getText());
            paiement.setCardDetails(cardDetailsField.getText());
            paiement.setCardNumber(cardNumberField.getText());
            paiement.setExpiryDate(Date.valueOf(expiryField.getText()));
            paiement.setCvv(Integer.parseInt(cvvField.getText()));

            // Appeler le service pour mettre à jour le paiement
            service.updatePaiement(paiement);

            // Fermer la fenêtre
            Stage stage = (Stage) methodeField.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        // Fermer la fenêtre sans enregistrer les modifications
        Stage stage = (Stage) methodeField.getScene().getWindow();
        stage.close();
    }

    private boolean validateFields() {
        // Vérification que la méthode de paiement n'est pas vide
        if (methodeField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Veuillez entrer la méthode de paiement.");
            return false;
        }

        // Vérification que les détails de la carte ne sont pas vides
        if (cardDetailsField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Veuillez entrer les détails de la carte.");
            return false;
        }

        // Validation du numéro de carte (doit être composé de 16 chiffres)
        String cardNumber = cardNumberField.getText();
        if (cardNumber.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Veuillez entrer le numéro de la carte.");
            return false;
        }
        if (!cardNumber.matches("\\d{16}")) {
            showAlert(Alert.AlertType.WARNING, "Le numéro de carte doit être composé de 16 chiffres.");
            return false;
        }

        // Validation de la date d'expiration (doit être dans le futur)
        String expiry = expiryField.getText();
        if (expiry.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Veuillez entrer la date d'expiration.");
            return false;
        }

        String[] parts = expiry.split("/");
        if (parts.length != 2 || !expiry.matches("\\d{2}/\\d{2}")) {
            showAlert(Alert.AlertType.WARNING, "Veuillez entrer une date d'expiration valide au format MM/AA.");
            return false;
        }

        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]);
        LocalDate currentDate = LocalDate.now();
        LocalDate expiryDate = LocalDate.of(2000 + year, month, 1);

        if (expiryDate.isBefore(currentDate)) {
            showAlert(Alert.AlertType.WARNING, "La date d'expiration doit être supérieure à la date actuelle.");
            return false;
        }

        // Validation du CVV (doit être composé de 3 chiffres)
        String cvv = cvvField.getText();
        if (cvv.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Veuillez entrer le CVV.");
            return false;
        }
        if (!cvv.matches("\\d{3}")) {
            showAlert(Alert.AlertType.WARNING, "Le CVV doit être composé de 3 chiffres.");
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Paiement;
import com.abircode.cruddp.services.ServicePaiement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

import java.sql.Date;
import java.time.LocalDate;

public class PaiementController {

    @FXML private RadioButton creditCardRadio;
    @FXML private RadioButton debitCardRadio;
    @FXML private RadioButton paypalRadio;
    @FXML private ToggleGroup paymentGroup;

    @FXML private TextField cardNameField;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryDateField;
    @FXML private TextField cvvField;

    private final ServicePaiement servicePaiement = new ServicePaiement();

    @FXML
    private void handlePayment(ActionEvent event) {
        // Validation des champs avant traitement
        if (!validateFields()) {
            return;
        }

        try {
            // Récupérer la méthode de paiement
            String method = "";
            if (creditCardRadio.isSelected()) method = "Carte Crédit";
            else if (debitCardRadio.isSelected()) method = "Carte Débit";
            else if (paypalRadio.isSelected()) method = "PayPal";

            // Récupérer les autres champs
            String cardName = cardNameField.getText();
            String cardNumber = cardNumberField.getText();
            String expiry = expiryDateField.getText(); // format attendu MM/AA
            String[] parts = expiry.split("/");
            String expiryDateFormatted = "20" + parts[1] + "-" + parts[0] + "-28"; // "2025-12-28"
            Date expiryDate = Date.valueOf(expiryDateFormatted);

            int cvv = Integer.parseInt(cvvField.getText());

            // Créer l'objet Paiement
            Paiement paiement = new Paiement();
            paiement.setPaymentMethod(method);
            paiement.setCardDetails(cardName);
            paiement.setCardNumber(cardNumber);
            paiement.setExpiryDate(expiryDate);
            paiement.setCvv(cvv);

            // Enregistrer dans la base via le service
            servicePaiement.creatPaiement(paiement);

            // Message de succès
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Paiement enregistré avec succès !");
            alert.showAndWait();

        } catch (Exception e) {
            // Message d’erreur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Problème lors du paiement");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void clearFields() {
        paymentGroup.selectToggle(null);
        cardNameField.clear();
        cardNumberField.clear();
        expiryDateField.clear();
        cvvField.clear();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean validateFields() {
        // Vérification de la méthode de paiement sélectionnée
        if (paymentGroup.getSelectedToggle() == null) {
            showAlert(Alert.AlertType.WARNING, "Veuillez choisir une méthode de paiement.");
            return false;
        }

        // Vérification que le nom sur la carte n'est pas vide
        if (cardNameField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Veuillez entrer le nom sur la carte.");
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
        String expiry = expiryDateField.getText();
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
}

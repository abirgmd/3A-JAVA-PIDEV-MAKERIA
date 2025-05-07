package com.abircode.cruddp.Controller;

import com.abircode.cruddp.services.PaiementService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.awt.*;
import java.net.URI;

public class PaiementController {

    @FXML
    private TextField montantField;

    @FXML
    private Button payerButton;

    @FXML
    void handlePaiement(ActionEvent event) {
        try {
            double montant = Double.parseDouble(montantField.getText());

            Session session = PaiementService.getInstance().createCheckoutSession(montant);

            // Ouvre Stripe Checkout dans le navigateur
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(session.getUrl()));
            } else {
                showAlert("Erreur", "Impossible d’ouvrir le navigateur.");
            }

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer un montant valide.");
        } catch (StripeException e) {
            showAlert("Stripe Error", e.getMessage());
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur s’est produite : " + e.getMessage());
        }
    }

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

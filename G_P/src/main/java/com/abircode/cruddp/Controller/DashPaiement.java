package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Paiement;
import com.abircode.cruddp.services.ServicePaiement;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

public class DashPaiement {

    @FXML
    private VBox paiementContainer;

    private final ServicePaiement service = new ServicePaiement();

    @FXML
    public void initialize() {
        try {
            showPaiements();
        } catch (SQLException e) {
            showErrorAlert("Erreur d'initialisation",
                    "Erreur lors du chargement des paiements",
                    e.getMessage());
        }
    }

    public void showPaiements() throws SQLException {
        List<Paiement> paiements = service.showPaiements();
        paiementContainer.getChildren().clear();

        for (Paiement p : paiements) {
            VBox box = new VBox(5);
            box.setStyle("-fx-padding: 10; -fx-background-color: #ffffff; " +
                    "-fx-border-color: #dcdcdc; -fx-border-radius: 5;");

            Label idLabel = new Label("ID : " + p.getId());
            Label methodLabel = new Label("Méthode : " + p.getPaymentMethod());
            Label cardLabel = new Label("Numéro de carte : " + p.getCardNumber());
            Label expiryLabel = new Label("Expiration : " + p.getExpiryDate());
            Label cvvLabel = new Label("CVV : " + p.getCvv());

            HBox buttonsBox = new HBox(10);
            Button deleteButton = new Button("Supprimer");
            Button updateButton = new Button("Modifier");

            deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            updateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

            deleteButton.setOnAction(e -> handleDelete(p));
            updateButton.setOnAction(e -> handleUpdate(p));

            buttonsBox.getChildren().addAll(updateButton, deleteButton);
            box.getChildren().addAll(idLabel, methodLabel, cardLabel, expiryLabel, cvvLabel, buttonsBox);
            paiementContainer.getChildren().add(box);
        }
    }

    private void handleDelete(Paiement p) {
        try {
            service.deletePaiement(p.getId());
            showPaiements(); // Rafraîchir la liste
        } catch (SQLException ex) {
            showErrorAlert("Erreur de suppression",
                    "Impossible de supprimer le paiement",
                    ex.getMessage());
        }
    }

    private void handleUpdate(Paiement p) {
        try {
            URL fxmlUrl = getClass().getResource("/Fxml/ModifierPaiement.fxml");
            if (fxmlUrl == null) throw new IOException("Fichier FXML introuvable");

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Modifier le paiement");
            stage.setScene(new Scene(root));
            stage.showAndWait(); // attendre la fermeture

            showPaiements(); // rafraîchir après modification
        } catch (IOException | SQLException ex) {
            showErrorAlert("Erreur d'édition",
                    "Impossible d'ouvrir l'éditeur",
                    "Le fichier d'interface est introuvable ou corrompu.");
        }
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.getDialogPane().setStyle(
                "-fx-background-color: #f8f8f8;" +
                        "-fx-text-fill: #333333;" +
                        "-fx-border-color: #c0c0c0;"
        );

        alert.showAndWait();
    }
}

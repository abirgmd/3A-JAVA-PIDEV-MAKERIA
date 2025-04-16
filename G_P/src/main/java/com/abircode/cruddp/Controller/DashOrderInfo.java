package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.OrderInformations;
import com.abircode.cruddp.services.ServiceOrderInfo;
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

public class DashOrderInfo {

    @FXML
    private VBox ordersContainer;

    private final ServiceOrderInfo service = new ServiceOrderInfo();

    @FXML
    public void initialize() {
        try {
            showOrders();
        } catch (SQLException e) {
            showErrorAlert("Erreur d'initialisation",
                    "Erreur lors du chargement des commandes",
                    e.getMessage());
        }
    }

    public void showOrders() throws SQLException {
        List<OrderInformations> Informations = service.afficher();
        ordersContainer.getChildren().clear();

        for (OrderInformations cmd : Informations) {
            VBox box = new VBox(5);
            box.setStyle("-fx-padding: 10; -fx-background-color: #ffffff; " +
                    "-fx-border-color: #dcdcdc; -fx-border-radius: 5;");

            Label idLabel = new Label("ID : " + cmd.getId());
            Label nameLabel = new Label("Nom : " + cmd.getNom());
            Label villeLabel = new Label("Ville : " + cmd.getVille());
            Label adresseLabel = new Label("Adresse : " + cmd.getAdresseLivraison());
            Label codePostalLabel = new Label("Code Postal : " + cmd.getCodePostal());

            HBox buttonsBox = new HBox(10);
            Button deleteButton = new Button("Supprimer");
            Button updateButton = new Button("Modifier");

            deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            updateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

            deleteButton.setOnAction(e -> handleDelete(cmd));
            updateButton.setOnAction(e -> handleUpdate(cmd));

            buttonsBox.getChildren().addAll(updateButton, deleteButton);
            box.getChildren().addAll(idLabel, nameLabel, villeLabel,
                    adresseLabel, codePostalLabel, buttonsBox);
            ordersContainer.getChildren().add(box);
        }
    }

    private void handleDelete(OrderInformations cmd) {
        try {
            service.supprimer(cmd.getId());
            showOrders(); // Rafraîchir la liste
        } catch (SQLException ex) {
            showErrorAlert("Erreur de suppression",
                    "Impossible de supprimer la commande",
                    ex.getMessage());
        }
    }

    private void handleUpdate(OrderInformations cmd) {
        try {
            URL fxmlUrl = getClass().getResource("/Fxml/ModifierOrderInfo.fxml");
            if (fxmlUrl == null) {
                throw new IOException("Fichier FXML introuvable");
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            ModifierOrderInfoController controller = loader.getController();
            controller.setOrder(cmd); // Passer l'objet à modifier

            Stage stage = new Stage();
            stage.setTitle("Modifier la commande");
            stage.setScene(new Scene(root));
            stage.showAndWait(); // Attendre la fermeture

            showOrders(); // Rafraîchir après modification
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

        // Style personnalisé pour l'alerte
        alert.getDialogPane().setStyle(
                "-fx-background-color: #f8f8f8;" +
                        "-fx-text-fill: #333333;" +
                        "-fx-border-color: #c0c0c0;"
        );

        alert.showAndWait();
    }
}
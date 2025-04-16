package com.abircode.cruddp.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.net.URL;

public class ProduitController {
    @FXML
    private StackPane contentPane;

    @FXML

    void showCommentPane(ActionEvent event) {
        loadUI("commentaire.fxml"); // Charger le formulaire ici
    }

    @FXML
    void showCategoriesPane(ActionEvent event) {
        loadUI("CategorielForm.fxml");
    }


    @FXML
    void showeventPane(ActionEvent event) {
        loadUI("event.fxml");
    }

    @FXML
    void showEvaluationsPane(ActionEvent event) {
        loadUI("EvaluationForm.fxml");
    }

    @FXML
    void showProduitsPane(ActionEvent event) {
        loadUI("ProduitForm.fxml");
    }
    @FXML
    void showForumPane(ActionEvent event) {
        loadUI("AfficherMessage.fxml");
    }
    @FXML
    void addMessage(ActionEvent event) {
        loadUI("AjouterMessage.fxml");
    }
    @FXML
    void showReclamationPane(ActionEvent event) {
        loadUI("afficher-reclamation.fxml"); // Charger le formulaire ici
    }
    @FXML
    void showReclamationUserPane(ActionEvent event) {
        loadUI("afficher-reclamation.fxml");
    }
    @FXML
    void showReponsesPane(ActionEvent event) {
        loadUI("afficher-reponses.fxml"); // Charger le formulaire ici
    }
    @FXML
    void addReclamation(ActionEvent event) {
        loadUI("ajouter-reclamation.fxml"); // Charger le formulaire ici
    }





    private void loadUI(String fxml) {
        try {
            // Charger le fichier FXML avec le chemin absolu
            URL fxmlURL = getClass().getResource("/Fxml/" + fxml);

            // Vérifier que le fichier FXML existe
            if (fxmlURL != null) {
                FXMLLoader loader = new FXMLLoader(fxmlURL);
                Node node = loader.load(); // Utilisation de la méthode load() de FXMLLoader
                contentPane.getChildren().setAll(node);
            } else {
                System.err.println("Fichier FXML non trouvé : " + fxml);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du fichier FXML : " + fxml);
            e.printStackTrace();
        }
    }
}

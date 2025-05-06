package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Evaluation;
import com.abircode.cruddp.services.ServiceEvaluation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Text;
import javafx.scene.control.Alert;

import java.sql.SQLException;
import java.util.List;

public class EvaluationController {

    @FXML
    private GridPane cardGrid;

    @FXML
    private TextField searchField;

    @FXML
    private Button nextButton;

    @FXML
    private Button prevButton;
    @FXML
    private ComboBox<String> sortByComboBox;

    @FXML
    private ComboBox<String> sortOrderComboBox;
    private ServiceEvaluation serviceEvaluation = new ServiceEvaluation();

    private int currentPage = 0;
    private int itemsPerPage = 6;
    private ObservableList<Evaluation> evaluations = FXCollections.observableArrayList();

    @FXML
    void initialize() {
        loadEvaluations();
    }

    private void loadEvaluations() {
        evaluations.clear();
        evaluations.addAll(getEvaluations());
        showEvaluations();
        sortByComboBox.getItems().addAll("Note", "Review");

        // Remplir le ComboBox pour l'ordre
        sortOrderComboBox.getItems().addAll("Croissant", "Décroissant");

        // Assurer qu'on a les bonnes actions pour les combos
        sortByComboBox.getSelectionModel().selectFirst(); // Sélectionne le premier item par défaut
        sortOrderComboBox.getSelectionModel().selectFirst(); //
    }

    private void showEvaluations() {
        cardGrid.getChildren().clear();
        
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, evaluations.size());
        
        int row = 0;
        int col = 0;
        
        for (int i = startIndex; i < endIndex; i++) {
            Evaluation evaluation = evaluations.get(i);
            VBox card = createEvaluationCard(evaluation);
            
            cardGrid.add(card, col, row);
            
            col++;
            if (col > 2) {
                col = 0;
                row++;
            }
        }
        
        // Mettre à jour l'état des boutons de pagination
        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(endIndex >= evaluations.size());
    }

    public ObservableList<Evaluation> getEvaluations() {
        ObservableList<Evaluation> result = FXCollections.observableArrayList();
        try {
            List<Evaluation> evaluationsList = serviceEvaluation.afficher();
            result.addAll(evaluationsList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la récupération des évaluations: " + e.getMessage());
        }
        return result;
    }

    private VBox createEvaluationCard(Evaluation evaluation) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);");
        
        // Nom de l'utilisateur
        Text userNameText = new Text(evaluation.getUserName());
        userNameText.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-fill: #b11d85;");
        
        // Note (étoiles)
        Text ratingText = new Text(getStarsForRating(evaluation.getNote()));
        ratingText.setStyle("-fx-font-size: 14; -fx-fill: gold;");
        
        // Texte de l'évaluation
        Text reviewText = new Text(evaluation.getReviewText());
        reviewText.setWrappingWidth(300);
        reviewText.setStyle("-fx-font-size: 14; -fx-fill: black;");
        
        // Date de publication
        Text dateText = new Text(evaluation.getDatePublication());
        dateText.setStyle("-fx-font-size: 12; -fx-fill: gray;");
        
        // Bouton de suppression
        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #0A2647; -fx-text-fill: white; -fx-background-radius: 5;");
        deleteButton.setOnAction(e -> deleteEvaluation(evaluation));
        
        card.getChildren().addAll(userNameText, ratingText, reviewText, dateText, deleteButton);
        return card;
    }

    private String getStarsForRating(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < rating) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }

    private void deleteEvaluation(Evaluation evaluation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette évaluation?");
        
        if (alert.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
            try {
                serviceEvaluation.supprimer(evaluation.getId());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation supprimée avec succès!");
                loadEvaluations();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression de l'évaluation: " + e.getMessage());
            }
        }
    }

    @FXML
    private void searchEvaluations() {
        String searchText = searchField.getText().toLowerCase();
        evaluations.clear();
        
        try {
            List<Evaluation> allEvaluations = serviceEvaluation.afficher();
            for (Evaluation evaluation : allEvaluations) {
                if (evaluation.getUserName().toLowerCase().contains(searchText) || 
                    evaluation.getReviewText().toLowerCase().contains(searchText)) {
                    evaluations.add(evaluation);
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la recherche d'évaluations: " + e.getMessage());
        }
        
        currentPage = 0;
        showEvaluations();
    }

    @FXML
    private void nextPage(ActionEvent event) {
        currentPage++;
        showEvaluations();
    }

    @FXML
    private void prevPage(ActionEvent event) {
        currentPage--;
        showEvaluations();
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void sortEvaluations(ActionEvent event) {
        String sortBy = sortByComboBox.getValue(); // Récupérer le critère de tri (Note ou Review)
        String sortOrder = sortOrderComboBox.getValue(); // Récupérer l'ordre (Croissant ou Décroissant)

        // Vérifier si l'utilisateur a bien sélectionné un critère de tri et un ordre
        if (sortBy == null || sortOrder == null) return;

        boolean ascending = sortOrder.equals("Croissant");

        if (sortBy.equals("Note")) {
            sortEvaluationsByRating(ascending);
        } else if (sortBy.equals("Review")) {
            sortEvaluationsByReviewText(ascending);
        }
    }
    private void sortEvaluationsByRating(boolean ascending) {
        evaluations.sort((e1, e2) -> {
            if (ascending) {
                return Integer.compare(e1.getNote(), e2.getNote());
            } else {
                return Integer.compare(e2.getNote(), e1.getNote());
            }
        });
        showEvaluations();
    }

    private void sortEvaluationsByReviewText(boolean ascending) {
        evaluations.sort((e1, e2) -> {
            if (ascending) {
                return e1.getReviewText().compareToIgnoreCase(e2.getReviewText());
            } else {
                return e2.getReviewText().compareToIgnoreCase(e1.getReviewText());
            }
        });
        showEvaluations();
    }

}

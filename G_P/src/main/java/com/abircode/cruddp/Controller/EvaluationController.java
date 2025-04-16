package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Evaluation;
import com.abircode.cruddp.utils.DBConnexion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Text;

import java.sql.*;

public class EvaluationController {

    @FXML
    private GridPane cardGrid;

    @FXML
    private TextField searchField;

    @FXML
    private Button nextButton;

    @FXML
    private Button prevButton;

    private Connection con = null;
    private PreparedStatement st = null;
    private ResultSet rs = null;

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
    }

    private void showEvaluations() {
        cardGrid.getChildren().clear();
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, evaluations.size());

        int row = 0;
        int column = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Evaluation evaluation = evaluations.get(i);
            VBox evaluationCard = createEvaluationCard(evaluation);
            cardGrid.add(evaluationCard, column, row);
            column++;
            if (column == 3) {
                column = 0;
                row++;
            }
        }

        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(endIndex == evaluations.size());
    }

    public ObservableList<Evaluation> getEvaluations() {
        ObservableList<Evaluation> evaluations = FXCollections.observableArrayList();
        String query = "SELECT e.id, e.user_id, e.produit_id, e.review_text, e.note, e.date_publication, " +
                "u.name AS user_name, u.email AS user_email, p.nomprod AS produit_name " +
                "FROM evaluation e " +
                "LEFT JOIN produit p ON e.produit_id = p.id " +
                "LEFT JOIN user u ON e.user_id = u.id";

        try (Connection con = DBConnexion.getCon();
             PreparedStatement st = con.prepareStatement(query);
             ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                Evaluation evaluation = new Evaluation();
                evaluation.setId(rs.getInt("id"));
                evaluation.setUserId(rs.getInt("user_id"));
                evaluation.setProduitId(rs.getInt("produit_id"));
                evaluation.setReviewText(rs.getString("review_text"));
                evaluation.setNote(rs.getInt("note"));
                evaluation.setDatePublication(rs.getString("date_publication"));
                evaluation.setUserName(rs.getString("user_name"));
                evaluation.setUserMail(rs.getString("user_email"));

                evaluations.add(evaluation);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching evaluations: " + e.getMessage());
        }
        return evaluations;
    }

    private VBox createEvaluationCard(Evaluation evaluation) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3); -fx-pref-width: 300;");

        Text userName = new Text("Utilisateur: " + evaluation.getUserName());
        userName.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-fill: #b11d85;");

        Text userEmail = new Text("Email: " + evaluation.getUserMail());
        userEmail.setStyle("-fx-font-size: 14; -fx-fill: #333333;");

        Text productName = new Text("Produit: " + evaluation.getProduitId());
        productName.setStyle("-fx-font-size: 14; -fx-fill: #333333;");

        Text reviewText = new Text("Commentaire: " + evaluation.getReviewText());
        reviewText.setStyle("-fx-font-size: 14; -fx-fill: #555555;");
        reviewText.setWrappingWidth(260);

        Text ratingText = new Text("Évaluation: " + getStarsForRating(evaluation.getNote()));
        ratingText.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-fill: #333333;");

        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #b11d85; -fx-text-fill: white; -fx-font-size: 12;");
        deleteButton.setOnAction(event -> deleteEvaluation(evaluation));

        VBox contentBox = new VBox(5);
        contentBox.getChildren().addAll(userName, userEmail, productName, reviewText, ratingText);
        contentBox.setAlignment(Pos.CENTER_LEFT);

        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().add(deleteButton);

        card.getChildren().addAll(contentBox, buttonBox);
        return card;
    }

    private String getStarsForRating(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < rating) {
                stars.append("⭐"); // Emoji étoile pleine
            } else {
                stars.append("☆"); // Emoji étoile vide
            }
        }
        return stars.toString();
    }

    private void deleteEvaluation(Evaluation evaluation) {
        String deleteQuery = "DELETE FROM evaluation WHERE id = ?";
        try (Connection con = DBConnexion.getCon();
             PreparedStatement st = con.prepareStatement(deleteQuery)) {
            st.setInt(1, evaluation.getId());
            st.executeUpdate();
            loadEvaluations();
        } catch (SQLException e) {
            System.err.println("Error deleting evaluation: " + e.getMessage());
        }
    }

    @FXML
    private void searchEvaluations() {
        String searchText = searchField.getText().toLowerCase();
        ObservableList<Evaluation> filteredEvaluations = FXCollections.observableArrayList();

        for (Evaluation evaluation : evaluations) {
            if (evaluation.getReviewText().toLowerCase().contains(searchText) ||
                    evaluation.getUserName().toLowerCase().contains(searchText)) {
                filteredEvaluations.add(evaluation);
            }
        }

        evaluations = filteredEvaluations;
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
}

package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Evaluation;
import com.abircode.cruddp.entities.Produit;
import com.abircode.cruddp.utils.DBConnexion;
import com.abircode.cruddp.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class ReviewFormController {
    @FXML private VBox reviewForm;
    @FXML private TextField userNameField;
    @FXML private TextField userEmailField;
    @FXML private TextField ratingInput;
    @FXML private TextArea reviewTextArea;
    @FXML private TextField reviewDateField;
    @FXML private Button submitReviewButton;
    @FXML private VBox reviewsContainer;
    @FXML private Text ratingError;
    @FXML private Text reviewTextError;
    private Produit currentProduit;
    private Evaluation currentReview;
    private boolean isEditing = false;
    private Connection connection;

    public ReviewFormController() {
        this.connection = DBConnexion.getCon();
    }

    @FXML
    public void initialize() {
        // Get the current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();

        // Define the desired format for date and time (yyyy-MM-dd HH:mm:ss)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Set the formatted date and time in the reviewDateField
        reviewDateField.setText(currentDateTime.format(formatter));

        // Validation of the rating between 1 and 5
        ratingInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[1-5]?")) {
                ratingInput.setText(oldValue);
            }
        });

        // Load current user info
        loadCurrentUserInfo();
    }

    // Charger les informations de l'utilisateur connecté
    private void loadCurrentUserInfo() {
        try {
            // Récupérer l'utilisateur connecté via le SessionManager
            int userId = SessionManager.getCurrentUser().getId();
            String query = "SELECT name, email FROM user WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String currentUserName = rs.getString("name");
                        String currentUserEmail = rs.getString("email");

                        // Mettre à jour les champs de l'interface utilisateur
                        userNameField.setText(currentUserName);
                        userEmailField.setText(currentUserEmail);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les informations de l'utilisateur: " + e.getMessage());
        }
    }

    public void setProduit(Produit produit) {
        this.currentProduit = produit;
        loadReviews();
    }

    public void setReview(Evaluation review) {
        this.currentReview = review;
        this.isEditing = true;
        populateForm(review);
    }

    private void populateForm(Evaluation review) {
        if (review != null) {
            ratingInput.setText(String.valueOf(review.getNote()));
            reviewTextArea.setText(review.getReviewText());
            reviewDateField.setText(review.getDatePublication());
            submitReviewButton.setText("Mettre à jour");
        }
    }

    @FXML
    private void submitReview() {
        if (currentProduit == null) {
            showMessage("Aucun produit sélectionné pour l'évaluation.");
            return;
        }

        if (validateForm()) {
            try {
                String query;
                if (isEditing) {
                    query = "UPDATE evaluation SET user_id = ?, produit_id = ?, review_text = ?, note = ?, date_publication = ?, user_name = ?, user_mail = ? WHERE id = ?";
                } else {
                    query = "INSERT INTO evaluation (user_id, produit_id, review_text, note, date_publication, user_name, user_mail) VALUES (?, ?, ?, ?, ?, ?, ?)";
                }

                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setInt(1, SessionManager.getCurrentUser().getId());
                    stmt.setInt(2, currentProduit.getId());
                    stmt.setString(3, reviewTextArea.getText());
                    stmt.setInt(4, Integer.parseInt(ratingInput.getText()));
                    stmt.setString(5, reviewDateField.getText());
                    stmt.setString(6, SessionManager.getCurrentUser().getName());
                    stmt.setString(7, SessionManager.getCurrentUser().getEmail());

                    if (isEditing) {
                        stmt.setInt(8, currentReview.getId());
                    }

                    stmt.executeUpdate();
                    resetForm();
                    loadReviews();

                }
            } catch (SQLException e) {
                e.printStackTrace();
                showMessage("Échec de l'envoi de l'avis : " + e.getMessage());
            }
        }
    }
    private boolean validateForm() {
        boolean isValid = true;

        // Validation de la note
        if (ratingInput.getText().isEmpty()) {
            ratingError.setText("Veuillez entrer une note entre 1 et 5");
            isValid = false;
        } else {
            try {
                int note = Integer.parseInt(ratingInput.getText());
                if (note < 1 || note > 5) {
                    ratingError.setText("La note doit être comprise entre 1 et 5");
                    isValid = false;
                } else {
                    ratingError.setText("");  // Clear error
                }
            } catch (NumberFormatException e) {
                ratingError.setText("La note doit être un nombre valide (1-5)");
                isValid = false;
            }
        }

        // Validation du texte de l'avis
        if (reviewTextArea.getText().isEmpty() || reviewTextArea.getText().length() < 10) {
            reviewTextError.setText("L'avis doit contenir au moins 10 caractères");
            isValid = false;
        } else {
            reviewTextError.setText("");  // Clear error
        }

        return isValid;
    }
    private void resetForm() {
        ratingInput.clear();
        reviewTextArea.clear();
        reviewDateField.setText(LocalDate.now().toString());
        submitReviewButton.setText("Submit Review");
        isEditing = false;
        currentReview = null;
        ratingError.setText("");
        reviewTextError.setText("");
    }
    private void showMessage(String message) {
        // Afficher un message sous le champ de saisie
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12;");
        reviewForm.getChildren().add(messageLabel); // Ajout du message sous le formulaire
    }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private VBox createReviewCard(Evaluation evaluation) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #962071; -fx-border-radius: 8;");

        HBox userInfo = new HBox(10);
        Text userName = new Text(evaluation.getUserName());
        userName.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        Text userEmail = new Text("(" + evaluation.getUserMail() + ")");
        userEmail.setStyle("-fx-font-size: 12; -fx-fill: #666;");
        userInfo.getChildren().addAll(userName, userEmail);

        HBox ratingBox = new HBox(5);
        Text ratingLabel = new Text("Rating:");
        ratingLabel.setStyle("-fx-font-weight: bold;");
        int note = evaluation.getNote();
        Text rating = new Text(note + " " + "⭐".repeat(note));
        rating.setStyle("-fx-fill: #b11d85;");
        ratingBox.getChildren().addAll(ratingLabel, rating);

        Text reviewText = new Text(evaluation.getReviewText());
        reviewText.setStyle("-fx-font-size: 12; -fx-fill: #333;");
        reviewText.setWrappingWidth(350);

        Text date = new Text(evaluation.getDatePublication());
        date.setStyle("-fx-font-size: 10; -fx-fill: #666;");

        HBox actionButtons = new HBox(10);
        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #962071; -fx-text-fill: white; -fx-font-weight: bold;");
        editBtn.setOnAction(e -> {
            try {
                editReview(evaluation.getId());
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert("Erreur", "Échec de l'édition de l'avis");
            }
        });

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteBtn.setOnAction(e -> {
            try {
                deleteReview(evaluation.getId());
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert("Erreur", "Échec de la suppression de l'avis");
            }
        });

        actionButtons.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().addAll(userInfo, ratingBox, reviewText, date, actionButtons);
        return card;
    }

    private void editReview(int reviewId) throws SQLException {
        String query = "SELECT * FROM evaluation WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, reviewId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                Evaluation review = new Evaluation();
                review.setId(rs.getInt("id"));
                review.setUserId(rs.getInt("user_id"));
                review.setProduitId(rs.getInt("produit_id"));
                review.setReviewText(rs.getString("review_text"));
                review.setNote(rs.getInt("note"));
                review.setDatePublication(rs.getString("date_publication"));
                review.setUserName(rs.getString("user_name"));
                review.setUserMail(rs.getString("user_mail"));

                setReview(review);
            }
        }
    }

    private void deleteReview(int reviewId) throws SQLException {
        String query = "DELETE FROM evaluation WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, reviewId);

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                showAlert("Success", "L'avis a été supprimé avec succès");
                loadReviews();
            }
        }
    }
    public void setCurrentProduct(Produit produit) {
        this.currentProduit = produit;
        loadReviews(); // Recharge les évaluations pour le produit sélectionné
    }


    public void loadReviews() {
        if (reviewsContainer != null && currentProduit != null) {
            reviewsContainer.getChildren().clear();
            try {
                String query = "SELECT * FROM evaluation WHERE produit_id = ? ORDER BY date_publication DESC";
                try (PreparedStatement pst = connection.prepareStatement(query)) {
                    pst.setInt(1, currentProduit.getId());
                    try (ResultSet rs = pst.executeQuery()) {
                        while (rs.next()) {
                            Evaluation evaluation = new Evaluation();
                            evaluation.setId(rs.getInt("id"));
                            evaluation.setUserId(rs.getInt("user_id"));
                            evaluation.setProduitId(rs.getInt("produit_id"));
                            evaluation.setReviewText(rs.getString("review_text"));
                            evaluation.setNote(rs.getInt("note"));
                            evaluation.setDatePublication(rs.getString("date_publication"));
                            evaluation.setUserName(rs.getString("user_name"));
                            evaluation.setUserMail(rs.getString("user_mail"));

                            reviewsContainer.getChildren().add(createReviewCard(evaluation));
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Échec du chargement des avis : " + e.getMessage());
            }
        }
    }
}

package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Evaluation;
import com.abircode.cruddp.entities.Produit;
import com.abircode.cruddp.utils.DBConnexion;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReviewFormController {
    @FXML private VBox reviewForm;
    @FXML private ComboBox<String> userComboBox;
    @FXML private TextField ratingInput;
    @FXML private TextArea reviewTextArea;
    @FXML private TextField reviewDateField;
    @FXML private Button submitReviewButton;
    @FXML private VBox reviewsContainer;

    private Produit currentProduit;
    private Evaluation currentReview;
    private boolean isEditing = false;
    private DetailProduitController detailController;
    private Connection connection;

    public ReviewFormController() {
        this.connection = DBConnexion.getCon();
    }

    @FXML
    public void initialize() {
        if (reviewDateField != null) {
            reviewDateField.setText(LocalDate.now().toString());
        }

        // Ajout des validateurs
        ratingInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[1-5]?")) {
                ratingInput.setText(oldValue);
            }
        });

        loadUsers();
        loadReviews();
    }

    public void setProduit(Produit produit) {
        this.currentProduit = produit;
        loadReviews();
    }

    public void setDetailController(DetailProduitController controller) {
        this.detailController = controller;
    }

    public void setCurrentProduct(Produit product) {
        this.currentProduit = product;
        loadReviews();
    }

    public void setReview(Evaluation review) {
        this.currentReview = review;
        this.isEditing = true;
        populateForm(review);
    }

    public void setReviewData(String userDisplay, int rating, String reviewText) {
        userComboBox.setValue(userDisplay);
        ratingInput.setText(String.valueOf(rating));
        reviewTextArea.setText(reviewText);
    }

    private void populateForm(Evaluation review) {
        if (review != null) {
            userComboBox.setValue(review.getUserName() + " (" + review.getUserId() + ")");
            ratingInput.setText(String.valueOf(review.getNote()));
            reviewTextArea.setText(review.getReviewText());
            reviewDateField.setText(review.getDatePublication());
            submitReviewButton.setText("Update Review");
        }
    }

    public void loadUsers() {
        List<String> users = new ArrayList<>();
        String query = "SELECT id, name, email FROM user";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String userDisplay = rs.getString("name") + " (" + rs.getInt("id") + ")";
                users.add(userDisplay);
            }

            userComboBox.getItems().setAll(users);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load users: " + e.getMessage());
        }
    }

    private void loadCurrentProduct() {
        try {
            String query = "SELECT * FROM produit ORDER BY id DESC LIMIT 1";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    currentProduit = new Produit();
                    currentProduit.setId(rs.getInt("id"));
                    currentProduit.setNom(rs.getString("nomprod"));
                    currentProduit.setDescription(rs.getString("descriptionprod"));
                    currentProduit.setPrix(rs.getDouble("prixprod"));
                    currentProduit.setStock(rs.getInt("nombre_produits_en_stock"));
                    currentProduit.setImage(rs.getString("image_large"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load current product: " + e.getMessage());
        }
    }

    @FXML
    private void submitReview() {
        if (currentProduit == null) {
            loadCurrentProduct();
            if (currentProduit == null) {
                showAlert("Erreur", "Aucun produit sélectionné pour l'évaluation.");
                return;
            }
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
                    String selectedUser = userComboBox.getValue();
                    if (selectedUser == null) {
                        showAlert("Error", "Veuillez sélectionner un utilisateur");
                        return;
                    }

                    String userIdStr = selectedUser.substring(selectedUser.indexOf("(") + 1, selectedUser.indexOf(")"));
                    int userId = Integer.parseInt(userIdStr);
                    String userName = selectedUser.substring(0, selectedUser.indexOf(" ("));
                    String userEmail = getUserEmail(userId);

                    stmt.setInt(1, userId);
                    stmt.setInt(2, currentProduit.getId());
                    stmt.setString(3, reviewTextArea.getText());
                    stmt.setInt(4, Integer.parseInt(ratingInput.getText()));
                    stmt.setString(5, reviewDateField.getText());
                    stmt.setString(6, userName);
                    stmt.setString(7, userEmail);

                    if (isEditing) {
                        stmt.setInt(8, currentReview.getId());
                    }

                    stmt.executeUpdate();
                    resetForm();
                    loadReviews();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Échec de l'envoi de l'avis : " + e.getMessage());
            }
        }
    }

    private String getUserEmail(int userId) throws SQLException {
        String query = "SELECT email FROM user WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        }
        return "";
    }

    private boolean validateForm() {
        // Validation de l'utilisateur
        if (userComboBox.getValue() == null || userComboBox.getValue().isEmpty()) {
            showAlert("Erreur", "Veuillez sélectionner un utilisateur");
            return false;
        }

        // Validation de la note (1-5)
        if (ratingInput.getText().isEmpty()) {
            showAlert("Erreur", "Veuillez entrer une note");
            return false;
        }

        try {
            int note = Integer.parseInt(ratingInput.getText());
            if (note < 1 || note > 5) {
                showAlert("Erreur", "La note doit être comprise entre 1 et 5");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "La note doit être un nombre valide (1-5)");
            return false;
        }

        // Validation du texte de l'avis (min 10 caractères)
        if (reviewTextArea.getText().isEmpty()) {
            showAlert("Erreur", "Veuillez saisir votre avis");
            return false;
        }

        if (reviewTextArea.getText().length() < 10) {
            showAlert("Erreur", "L'avis doit contenir au moins 10 caractères");
            return false;
        }

        return true;
    }

    private void resetForm() {
        userComboBox.setValue(null);
        ratingInput.clear();
        reviewTextArea.clear();
        reviewDateField.setText(LocalDate.now().toString());
        submitReviewButton.setText("Submit Review");
        isEditing = false;
        currentReview = null;
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
        Text userEmail = new Text("(" + evaluation.getUserId() + ")");
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
                showAlert("Error", "Failed to edit review");
            }
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteBtn.setOnAction(e -> {
            try {
                deleteReview(evaluation.getId());
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert("Error", "Failed to delete review");
            }
        });

        actionButtons.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().addAll(userInfo, ratingBox, reviewText, date, actionButtons);
        return card;
    }

    private void editReview(int reviewId) throws SQLException {
        String query = "SELECT * FROM evaluation WHERE id = ?";
        PreparedStatement pst = connection.prepareStatement(query);
        pst.setInt(1, reviewId);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            reviewForm.setVisible(true);
            Evaluation review = new Evaluation();
            review.setId(rs.getInt("id"));
            review.setUserId(rs.getInt("user_id"));
            review.setProduitId(rs.getInt("produit_id"));
            review.setReviewText(rs.getString("review_text"));
            review.setNote(rs.getInt("note"));
            review.setDatePublication(rs.getString("date_publication"));
            review.setUserName(rs.getString("user_name"));

            currentReview = review;
            isEditing = true;

            userComboBox.setValue(review.getUserName() + " (" + review.getUserId() + ")");
            ratingInput.setText(String.valueOf(review.getNote()));
            reviewTextArea.setText(review.getReviewText());
            reviewDateField.setText(review.getDatePublication());
            submitReviewButton.setText("Update Review");
        }
    }

    private void deleteReview(int reviewId) throws SQLException {
        String query = "DELETE FROM evaluation WHERE id = ?";
        PreparedStatement pst = connection.prepareStatement(query);
        pst.setInt(1, reviewId);

        int rowsAffected = pst.executeUpdate();
        if (rowsAffected > 0) {
            showAlert("Success", "Review deleted successfully");
            loadReviews();
        }
    }

    public void loadReviews() {
        if (reviewsContainer != null && currentProduit != null) {
            reviewsContainer.getChildren().clear();
            try {
                String query = "SELECT * FROM evaluation WHERE produit_id = ?";
                PreparedStatement pst = connection.prepareStatement(query);
                pst.setInt(1, currentProduit.getId());
                ResultSet rs = pst.executeQuery();

                while (rs.next()) {
                    Evaluation evaluation = new Evaluation();
                    evaluation.setId(rs.getInt("id"));
                    evaluation.setUserId(rs.getInt("user_id"));
                    evaluation.setProduitId(rs.getInt("produit_id"));
                    evaluation.setReviewText(rs.getString("review_text"));
                    evaluation.setNote(rs.getInt("note"));
                    evaluation.setDatePublication(rs.getString("date_publication"));
                    evaluation.setUserName(rs.getString("user_name"));

                    reviewsContainer.getChildren().add(createReviewCard(evaluation));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to load reviews: " + e.getMessage());
            }
        }
    }
}
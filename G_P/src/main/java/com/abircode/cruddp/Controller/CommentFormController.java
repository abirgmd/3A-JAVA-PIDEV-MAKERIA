package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Commentaire;
import com.abircode.cruddp.entities.Event;
import com.abircode.cruddp.utils.DBConnexion;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentFormController {
    @FXML private VBox commentForm;
    @FXML private ComboBox<String> userComboBox;
    @FXML private TextArea commentTextArea;
    @FXML private TextField commentDateField;
    @FXML private Button submitCommentButton;
    @FXML private VBox commentsContainer;

    private Event currentEvent;
    private Commentaire currentComment;
    private boolean isEditing = false;
    private Connection connection;

    public CommentFormController() {
        this.connection = DBConnexion.getCon();
    }

    @FXML
    public void initialize() {
        if (commentDateField != null) {
            commentDateField.setText(LocalDateTime.now().toString());
        }

        // Ajout d'un écouteur pour limiter la taille minimale du commentaire
        commentTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() < 10 && !newValue.isEmpty()) {
                commentTextArea.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            } else {
                commentTextArea.setStyle("");
            }
        });

        loadUsers();
        if (currentEvent != null) {
            loadComments();
        }
    }

    public void setEvent(Event event) {
        this.currentEvent = event;
        if (currentEvent != null) {
            loadComments();
        }
    }

    public void setCurrentComment(Commentaire comment) {
        this.currentComment = comment;
        this.isEditing = true;
        populateForm(comment);
    }

    private void populateForm(Commentaire comment) {
        if (comment != null) {
            userComboBox.setValue(comment.getNomcomment());
            commentTextArea.setText(comment.getText_commentaire());
            commentDateField.setText(comment.getTimecomment().toString());
            submitCommentButton.setText("Update Comment");
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

    @FXML
    private void submitComment() {
        if (currentEvent == null) {
            showAlert("Error", "No event selected for the comment.");
            return;
        }

        if (validateForm()) {
            try {
                String query;
                if (isEditing) {
                    query = "UPDATE commentaire SET user_id = ?, nomcomment = ?, timecomment = ?, text_commentaire = ? WHERE id = ?";
                } else {
                    query = "INSERT INTO commentaire (event_id, user_id, nomcomment, timecomment, text_commentaire) VALUES (?, ?, ?, ?, ?)";
                }

                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    String selectedUser = userComboBox.getValue();
                    if (selectedUser == null) {
                        showAlert("Error", "Please select a user");
                        return;
                    }

                    // Extraire l'ID utilisateur du format "name (id)"
                    String[] userParts = selectedUser.split(" ");
                    int userId = -1;
                    try {
                        String userIdString = userParts[userParts.length - 1].replace("(", "").replace(")", "");
                        userId = Integer.parseInt(userIdString);
                    } catch (NumberFormatException e) {
                        showAlert("Error", "Invalid user ID format. Please ensure the format is 'name (id)'.");
                        return;
                    }

                    // Préparer la requête avec les données du commentaire
                    if (isEditing) {
                        stmt.setInt(1, userId);
                        stmt.setString(2, selectedUser);
                        stmt.setString(3, commentDateField.getText());
                        stmt.setString(4, commentTextArea.getText());
                        stmt.setInt(5, currentComment.getId());
                    } else {
                        stmt.setInt(1, currentEvent.getId());
                        stmt.setInt(2, userId);
                        stmt.setString(3, selectedUser);
                        stmt.setString(4, commentDateField.getText());
                        stmt.setString(5, commentTextArea.getText());
                    }

                    stmt.executeUpdate();
                    resetForm();
                    loadComments();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to submit the comment: " + e.getMessage());
            }
        }
    }

    private boolean validateForm() {
        // Validation de la sélection de l'utilisateur
        if (userComboBox.getValue() == null || userComboBox.getValue().isEmpty()) {
            showAlert("Error", "Please select a user");
            return false;
        }

        // Validation du texte du commentaire
        String commentText = commentTextArea.getText().trim();
        if (commentText.isEmpty()) {
            showAlert("Error", "Please enter a comment");
            return false;
        }

        if (commentText.length() < 10) {
            showAlert("Error", "The comment must be at least 10 characters long");
            return false;
        }

        return true;
    }

    private void resetForm() {
        userComboBox.setValue(null);
        commentTextArea.clear();
        commentDateField.setText(LocalDateTime.now().toString());
        submitCommentButton.setText("Submit Comment");
        isEditing = false;
        currentComment = null;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void loadComments() {
        if (commentsContainer != null && currentEvent != null) {
            commentsContainer.getChildren().clear();
            try {
                String query = "SELECT * FROM commentaire WHERE event_id = ? ORDER BY timecomment DESC";
                PreparedStatement pst = connection.prepareStatement(query);
                pst.setInt(1, currentEvent.getId());
                ResultSet rs = pst.executeQuery();

                while (rs.next()) {
                    Commentaire commentaire = new Commentaire();
                    commentaire.setId(rs.getInt("id"));
                    commentaire.setEvent(currentEvent);
                    commentaire.setNomcomment(rs.getString("nomcomment"));
                    commentaire.setTimecomment(rs.getTimestamp("timecomment").toLocalDateTime());
                    commentaire.setText_commentaire(rs.getString("text_commentaire"));

                    commentsContainer.getChildren().add(createCommentCard(commentaire));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to load comments: " + e.getMessage());
            }
        }
    }

    private VBox createCommentCard(Commentaire commentaire) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #962071; -fx-border-radius: 8;");

        Text userName = new Text(commentaire.getNomcomment());
        userName.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Text commentText = new Text(commentaire.getText_commentaire());
        commentText.setStyle("-fx-font-size: 12; -fx-fill: #333;");
        commentText.setWrappingWidth(350);

        Text date = new Text(commentaire.getTimecomment().toString());
        date.setStyle("-fx-font-size: 10; -fx-fill: #666;");

        HBox buttonsBox = new HBox(10);
        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: #962071; -fx-text-fill: white;");
        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");

        editButton.setOnAction(e -> setCurrentComment(commentaire));
        deleteButton.setOnAction(e -> deleteComment(commentaire));

        buttonsBox.getChildren().addAll(editButton, deleteButton);
        card.getChildren().addAll(userName, commentText, date, buttonsBox);
        return card;
    }

    private void deleteComment(Commentaire commentaire) {
        try {
            String query = "DELETE FROM commentaire WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, commentaire.getId());
            stmt.executeUpdate();
            loadComments();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to delete the comment: " + e.getMessage());
        }
    }
}
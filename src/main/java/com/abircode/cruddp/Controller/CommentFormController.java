package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Commentaire;
import com.abircode.cruddp.entities.Event;
import com.abircode.cruddp.utils.DBConnexion;
import com.abircode.cruddp.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.sql.*;
import java.time.LocalDateTime;

public class CommentFormController {
    @FXML private VBox commentForm;
    @FXML private TextArea commentTextArea;
    @FXML private TextField commentDateField;
    @FXML private Button submitCommentButton;
    @FXML private VBox commentsContainer;
    @FXML private TextField userNameField;
    @FXML private TextField userEmailField;
    @FXML private VBox reactionsContainer;
@FXML     private HBox reactionContainer; // This will hold the reaction buttons

    private Event currentEvent;
    private Commentaire currentComment;
    private boolean isEditing = false;
    private Connection connection;

    public CommentFormController() {
        this.connection = DBConnexion.getCon();
    }

    @FXML
    public void initialize() {
        // Remplir la date et charger l'utilisateur connecté
        commentDateField.setText(LocalDateTime.now().toString());
        loadCurrentUserInfo();

        // Validation des commentaires
        commentTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() < 10 && !newValue.isEmpty()) {
                commentTextArea.setStyle("-fx-border-color: red;");
            } else {
                commentTextArea.setStyle("");
            }
        });
    }

    private void loadCurrentUserInfo() {
        try {
            var currentUser = SessionManager.getCurrentUser();
            if (currentUser == null) {
                throw new IllegalStateException("Aucun utilisateur connecté.");
            }
            userNameField.setText(currentUser.getName());
            userEmailField.setText(currentUser.getEmail());
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les informations de l'utilisateur connecté.");
        }
    }

    public void setEvent(Event event) {
        this.currentEvent = event;
        if (currentEvent != null) {
            loadComments();
        }
    }

    private void populateForm(Commentaire comment) {
        commentTextArea.setText(comment.getText_commentaire());
        commentDateField.setText(comment.getTimecomment().toString());
        submitCommentButton.setText("Modifier");
    }

    @FXML
    private void submitComment() {
        if (currentEvent == null) {
            showAlert("Erreur", "Aucun événement sélectionné.");
            return;
        }

        if (!validateForm()) {
            return;
        }

        try {
            String query;
            if (isEditing) {
                query = "UPDATE commentaire SET nomcomment = ?, timecomment = ?, text_commentaire = ? WHERE id = ?";
            } else {
                query = "INSERT INTO commentaire (event_id, user_id, nomcomment, timecomment, text_commentaire) VALUES (?, ?, ?, ?, ?)";
            }

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                var user = SessionManager.getCurrentUser();
                if (isEditing) {
                    stmt.setString(1, user.getName());
                    stmt.setString(2, commentDateField.getText());
                    stmt.setString(3, commentTextArea.getText());
                    stmt.setInt(4, currentComment.getId());
                } else {
                    stmt.setInt(1, currentEvent.getId());
                    stmt.setInt(2, user.getId());
                    stmt.setString(3, user.getName());
                    stmt.setString(4, commentDateField.getText());
                    stmt.setString(5, commentTextArea.getText());
                }
                stmt.executeUpdate();
            }

            resetForm();
            loadComments();
        } catch (SQLException e) {
            showAlert("Erreur", "Échec de l'enregistrement du commentaire.");
        }
    }

    private boolean validateForm() {
        String text = commentTextArea.getText().trim();
        if (text.isEmpty()) {
            showAlert("Erreur", "Veuillez saisir un commentaire.");
            return false;
        }
        if (text.length() < 10) {
            showAlert("Erreur", "Le commentaire doit contenir au moins 10 caractères.");
            return false;
        }
        return true;
    }

    private void resetForm() {
        commentTextArea.clear();
        commentDateField.setText(LocalDateTime.now().toString());
        submitCommentButton.setText("Ajouter");
        isEditing = false;
        currentComment = null;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void loadComments() {
        commentsContainer.getChildren().clear();
        if (currentEvent == null) return;

        try {
            String query = "SELECT * FROM commentaire WHERE event_id = ? ORDER BY timecomment DESC";
            try (PreparedStatement pst = connection.prepareStatement(query)) {
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
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Échec du chargement des commentaires.");
        }
    }

    private void editComment(int commentId) {
        String query = "SELECT * FROM commentaire WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, commentId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                Commentaire comment = new Commentaire();
                comment.setId(rs.getInt("id"));
                comment.setNomcomment(rs.getString("nomcomment"));
                comment.setTimecomment(rs.getTimestamp("timecomment").toLocalDateTime());
                comment.setText_commentaire(rs.getString("text_commentaire"));

                setCurrentComment(comment);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de récupérer le commentaire.");
        }
    }

    public void setCurrentComment(Commentaire comment) {
        this.currentComment = comment;
        this.isEditing = true;
        populateForm(comment);
    }

    private void deleteComment(int commentId) {
        try {
            String query = "DELETE FROM commentaire WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, commentId);
                stmt.executeUpdate();
                loadComments();
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de supprimer le commentaire.");
        }
    }
    private void createReactionButton(Commentaire comment) {
        Button reactionButton = new Button("...");
        reactionButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        reactionButton.setOnAction(e -> showReactionOptions(comment));

        VBox card = createCommentCard(comment);
        card.getChildren().add(reactionButton);
        commentsContainer.getChildren().add(card);
    }



    private void loadReactions(Commentaire comment) {
        // Charger les réactions pour un commentaire donné
        String query = "SELECT emoji, COUNT(*) FROM reactions WHERE comment_id = ? GROUP BY emoji";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, comment.getId());
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String emoji = rs.getString("emoji");
                int count = rs.getInt("COUNT(*)");
                updateReactionCard(comment, emoji, count);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Échec du chargement des réactions.");
        }
    }

    private void updateReactionCard(Commentaire comment, String emoji, int count) {
        // Mettre à jour la carte de commentaire pour afficher le nombre de réactions
        // Par exemple, ajouter un texte pour chaque emoji avec son compteur
        Text reactionText = new Text(emoji + " " + count);
        VBox card = findCardByCommentId(comment.getId());
        if (card != null) {
            card.getChildren().add(reactionText);
        }
    }

    private VBox findCardByCommentId(int commentId) {
        // Trouver la carte de commentaire par ID (utiliser cette méthode selon vos besoins)
        for (var child : commentsContainer.getChildren()) {
            if (child instanceof VBox) {
                VBox card = (VBox) child;
                // Ajouter une logique pour vérifier si le commentId correspond à celui de la carte
                // Si oui, retourner la carte
            }
        }
        return null;
    }
    private VBox createCommentCard(Commentaire comment) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #962071; -fx-border-radius: 8;");

        Text name = new Text(comment.getNomcomment());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Text text = new Text(comment.getText_commentaire());
        text.setStyle("-fx-font-size: 12; -fx-fill: #333;");

        Text date = new Text(comment.getTimecomment().toString());

        // Load the reactions for this comment
        loadReactions(comment, card);

        HBox actions = new HBox(10);
        Button editButton = new Button("Modifier");
        Button deleteButton = new Button("Supprimer");
        editButton.setStyle("-fx-background-color: #962071; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold;");

        editButton.setOnAction(e -> editComment(comment.getId()));
        deleteButton.setOnAction(e -> deleteComment(comment.getId()));

        actions.getChildren().addAll(editButton, deleteButton);
        card.getChildren().addAll(name, text, date, actions);
        return card;
    }

    private void loadReactions(Commentaire comment, VBox card) {
        String query = "SELECT emoji, COUNT(*) FROM reactions WHERE comment_id = ? GROUP BY emoji";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, comment.getId());
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String emoji = rs.getString("emoji");
                int count = rs.getInt("COUNT(*)");
                Text reactionText = new Text(emoji + " " + count);
                reactionText.setStyle("-fx-font-size: 16;");
                card.getChildren().add(reactionText);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Échec du chargement des réactions.");
        }
    }
    private void showReactionOptions(Commentaire comment) {
        // Afficher les options d'emojis
        VBox reactionBox = new VBox();
        reactionBox.setStyle("-fx-background-color: transparent; -fx-padding: 10;");

        // Liste des emojis
        String[] emojis = {"😂", "😢", "🐄", "❤️", "👍"};

        for (String emoji : emojis) {
            Button emojiButton = new Button(emoji);
            emojiButton.setStyle("-fx-font-size: 20;");
            emojiButton.setOnAction(e -> handleReaction(comment, emoji));
            reactionBox.getChildren().add(emojiButton);
        }

        // Afficher la liste d'emoji avec un fond transparent
        reactionsContainer.getChildren().setAll(reactionBox);
    }

    private void handleReaction(Commentaire comment, String emoji) {
        try {
            var user = SessionManager.getCurrentUser();
            if (user == null) {
                showAlert("Erreur", "Aucun utilisateur connecté.");
                return;
            }

            // Vérifier si l'utilisateur a déjà réagi
            String checkQuery = "SELECT * FROM reactions WHERE comment_id = ? AND user_id = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, comment.getId());
                checkStmt.setInt(2, user.getId());
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    // L'utilisateur a déjà réagi, supprimer sa réaction précédente
                    String deleteQuery = "DELETE FROM reactions WHERE comment_id = ? AND user_id = ?";
                    try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
                        deleteStmt.setInt(1, comment.getId());
                        deleteStmt.setInt(2, user.getId());
                        deleteStmt.executeUpdate();
                    }
                }

                // Ajouter la nouvelle réaction
                String insertQuery = "INSERT INTO reactions (comment_id, user_id, emoji, count) VALUES (?, ?, ?, 1)";
                try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                    insertStmt.setInt(1, comment.getId());
                    insertStmt.setInt(2, user.getId());
                    insertStmt.setString(3, emoji);
                    insertStmt.executeUpdate();
                }

                loadComments();  // Recharger les commentaires pour afficher les mises à jour
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible d'ajouter la réaction.");
        }
    }
    @FXML
    private void displayReactions() {
        // Clear any previous reactions
        reactionContainer.getChildren().clear();

        // Example: Add some reaction buttons (these can be emojis or other content)
        Button likeButton = new Button("👍");
        Button loveButton = new Button("❤️");
        Button laughButton = new Button("😂");

        // Add these buttons to the reaction container (HBox)
        reactionContainer.getChildren().addAll(likeButton, loveButton, laughButton);

        // Add event handlers for the buttons if necessary
        likeButton.setOnAction(e -> handleReaction("Like"));
        loveButton.setOnAction(e -> handleReaction("Love"));
        laughButton.setOnAction(e -> handleReaction("Laugh"));
    }

    private void handleReaction(String reactionType) {
        // Handle the selected reaction (you can implement logic to store or show it)
        System.out.println("User selected reaction: " + reactionType);
    }
}

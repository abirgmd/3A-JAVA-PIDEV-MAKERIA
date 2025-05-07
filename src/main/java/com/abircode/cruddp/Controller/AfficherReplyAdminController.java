package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Reply;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import com.abircode.cruddp.services.ServiceReply;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class AfficherReplyAdminController {

    @FXML private ListView<Reply> repliesList;
    @FXML private TextField searchField;
    @FXML private Label countLabel;

    private final ServiceReply serviceReply = new ServiceReply();
    private List<Reply> allReplies;

    @FXML
    public void initialize() {
        configureReplyList();
        loadReplies();
        setupSearchFilter();
    }

    private void configureReplyList() {
        repliesList.setCellFactory(param -> new ReplyListCell());
    }

    private void loadReplies() {
        try {
            allReplies = serviceReply.afficher();
            refreshListView(allReplies);
            updateCountLabel(allReplies.size());
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les réponses.", Alert.AlertType.ERROR);
        }
    }

    private void refreshListView(List<Reply> replies) {
        repliesList.getItems().setAll(replies);
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            List<Reply> filtered = allReplies.stream()
                    .filter(reply -> reply.getContenuReply().toLowerCase().contains(newVal.toLowerCase()))
                    .collect(Collectors.toList());
            refreshListView(filtered);
            updateCountLabel(filtered.size());
        });
    }

    private void updateCountLabel(int count) {
        countLabel.setText(count + " réponse(s) trouvée(s)");
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private class ReplyListCell extends ListCell<Reply> {
        private final HBox card = new HBox(10);
        private final VBox replyContent = new VBox(5);
        private final Label contenu = new Label();
        private final Label date = new Label();
        private final Button deleteButton = new Button("Supprimer");

        public ReplyListCell() {
            super();
            card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);");

            contenu.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
            date.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");
            deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");

            replyContent.getChildren().addAll(contenu, date);
            HBox.setHgrow(replyContent, Priority.ALWAYS);
            HBox buttonsBox = new HBox(5, deleteButton);
            card.getChildren().addAll(replyContent, buttonsBox);

            deleteButton.setOnAction(event -> {
                Reply reply = getItem();
                if (reply != null) handleDeleteReply(reply);
            });
        }

        @Override
        protected void updateItem(Reply reply, boolean empty) {
            super.updateItem(reply, empty);
            if (empty || reply == null) {
                setGraphic(null);
            } else {
                contenu.setText(reply.getContenuReply());
                date.setText("Posté le: " + reply.getCreatedAt());
                setGraphic(card);
            }
        }
    }

    private void handleDeleteReply(Reply reply) {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer la réponse");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réponse ?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                serviceReply.supprimer(reply.getId());
                allReplies.remove(reply);
                refreshListView(allReplies);
                updateCountLabel(allReplies.size());
                showAlert("Succès", "Réponse supprimée avec succès.", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Échec de la suppression : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}


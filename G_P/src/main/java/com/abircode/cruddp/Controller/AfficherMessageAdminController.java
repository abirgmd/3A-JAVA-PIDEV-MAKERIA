package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Message;
import com.abircode.cruddp.entities.Reply;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.abircode.cruddp.services.ServiceMessage;
import com.abircode.cruddp.services.ServiceReply;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import java.io.IOException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class AfficherMessageAdminController {
    @FXML private ListView<Message> messagesList;
    @FXML private ListView<Reply> repliesListView;
    @FXML private TextField searchField;
    @FXML private TextArea replyTextArea;
    @FXML private VBox repliesContainer;
    @FXML private Label countLabel;
    @FXML private Label errorLabel;
    @FXML private Button closeRepliesButton;
    private final ServiceMessage serviceMessage = new ServiceMessage();
    private final ServiceReply serviceReply = new ServiceReply();
    private List<Message> allMessages;
    private Message selectedMessage;

    @FXML
    public void initialize() {
        configureMessageList();
        loadMessages();
        setupSearchFilter();
        repliesContainer.setVisible(false);
    }
    @FXML

    private void configureMessageList() {
        messagesList.setCellFactory(param -> new MessageListCell());
    }


    private void loadMessages() {
        try {
            allMessages = serviceMessage.afficher();
            refreshListView(allMessages);
            updateCountLabel(allMessages.size());
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les messages", Alert.AlertType.ERROR);
        }
    }

    private void refreshListView(List<Message> messages) {
        messagesList.getItems().setAll(messages);
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            List<Message> filtered = allMessages.stream()
                    .filter(msg -> msg.getTitreMsg().toLowerCase().contains(newVal.toLowerCase()) ||
                            msg.getDescriptionMsg().toLowerCase().contains(newVal.toLowerCase()))
                    .collect(Collectors.toList());
            refreshListView(filtered);
            updateCountLabel(filtered.size());
        });
    }

    private void updateCountLabel(int count) {
        countLabel.setText(count + " message(s) trouvé(s)");
    }

    private class MessageListCell extends ListCell<Message> {
        private final HBox card = new HBox(10);
        private final VBox messageContent = new VBox(5);
        private final Label title = new Label();
        private final Label description = new Label();
        private final Label contenu = new Label();
        private final Label date = new Label();
        private final Button deleteButton = new Button("Supprimer");

        public MessageListCell() {
            super();

            card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);");

            title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
            description.setStyle("-fx-text-fill: #666;");
            contenu.setStyle("-fx-text-fill: #666;");
            date.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");
            deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");

            messageContent.getChildren().addAll(title, description,contenu, date);
            HBox.setHgrow(messageContent, Priority.ALWAYS);
            HBox buttonsBox = new HBox(5, deleteButton);
            card.getChildren().addAll(messageContent, buttonsBox);

            deleteButton.setOnAction(event -> {
                Message message = getItem();
                if (message != null) handleDeleteMessage(message);
            });
        }

        @Override
        protected void updateItem(Message message, boolean empty) {
            super.updateItem(message, empty);

            if (empty || message == null) {
                setGraphic(null);
            } else {
                title.setText(message.getTitreMsg());
                description.setText(message.getDescriptionMsg());
                contenu.setText(message.getContenu());
                date.setText("Posté le: " + message.getDateMsg());
                setGraphic(card);
            }
        }
    }
    private void handleDeleteMessage(Message message) {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer le message");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer ce message et toutes ses réponses ?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                serviceReply.supprimerToutesReponsesPourMessage(message.getId());
                serviceMessage.supprimer(message.getId());

                allMessages.remove(message);
                refreshListView(allMessages);
                updateCountLabel(allMessages.size());
                repliesContainer.setVisible(false);
                showAlert("Succès", "Message et réponses supprimés avec succès", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Échec de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
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

public class AfficherMessageController {
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
        configureRepliesList();
        loadMessages();
        setupSearchFilter();
        repliesContainer.setVisible(false);
    }

    private void configureMessageList() {
        messagesList.setCellFactory(param -> new MessageListCell());
    }

    private void configureRepliesList() {
        repliesListView.setCellFactory(param -> new ListCell<Reply>() {
            private final HBox container = new HBox(10);
            private final VBox contentBox = new VBox(5);
            private final Label contentLabel = new Label();
            private final Label dateLabel = new Label();
            private final Button deleteButton = new Button("Supprimer");
            private final Button editButton = new Button("Modifier");

            {
                // Style des éléments
                container.setStyle("-fx-padding: 10; -fx-background-color: #f9f9f9; -fx-background-radius: 5;");
                contentBox.setStyle("-fx-padding: 0 10 0 0;");
                contentLabel.setStyle("-fx-font-size: 14px;");
                dateLabel.setStyle("-fx-text-fill: #777; -fx-font-size: 11px;");

                // Style des boutons
                deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-pref-width: 80;");
                editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-pref-width: 80;");

                // Configuration des actions
                deleteButton.setOnAction(e -> {
                    Reply reply = getItem();
                    if (reply != null) {
                        handleDeleteReply(reply);
                    }
                });

                editButton.setOnAction(e -> {
                    Reply reply = getItem();
                    if (reply != null) {
                        handleEditReply(reply);
                    }
                });

                contentBox.getChildren().addAll(contentLabel, dateLabel);
                HBox.setHgrow(contentBox, Priority.ALWAYS);
                container.getChildren().addAll(contentBox, editButton, deleteButton);
            }

            @Override
            protected void updateItem(Reply reply, boolean empty) {
                super.updateItem(reply, empty);
                if (empty || reply == null) {
                    setGraphic(null);
                } else {
                    contentLabel.setText(reply.getContenuReply());
                    dateLabel.setText("Posté le: " + reply.getCreatedAt());
                    setGraphic(container);
                }
            }
        });
    }

    private void handleEditReply(Reply reply) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/ModifierReply.fxml"));
            VBox root = loader.load();

            ModifierReplyController controller = loader.getController();
            controller.setReply(reply);

            Stage stage = new Stage();
            stage.setTitle("Modifier la réponse");
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);

            stage.showAndWait();

            if (controller.isSaved()) {
                showReplies(selectedMessage); // Rafraîchir l'affichage
                showAlert("Succès", "Réponse modifiée avec succès", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur de réponse", Alert.AlertType.ERROR);
        }
    }


    private void handleDeleteReply(Reply reply) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer cette réponse ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réponse ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceReply.supprimer(reply.getId());
                    showReplies(selectedMessage); // Rafraîchir la liste
                    showAlert("Succès", "Réponse supprimée avec succès", Alert.AlertType.INFORMATION);
                } catch (SQLException e) {
                    showAlert("Erreur", "Échec de la suppression de la réponse", Alert.AlertType.ERROR);
                }
            }
        });
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
        private final Button modifierButton = new Button("Modifier");
        private final Button replyButton = new Button("Réponses");

        public MessageListCell() {
            super();

            card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);");

            title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
            description.setStyle("-fx-text-fill: #666;");
            contenu.setStyle("-fx-text-fill: #666;");
            date.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");
            deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
            modifierButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            replyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

            messageContent.getChildren().addAll(title, description,contenu, date);
            HBox.setHgrow(messageContent, Priority.ALWAYS);
            HBox buttonsBox = new HBox(5, modifierButton, deleteButton, replyButton);
            card.getChildren().addAll(messageContent, buttonsBox);

            deleteButton.setOnAction(event -> {
                Message message = getItem();
                if (message != null) handleDeleteMessage(message);
            });

            modifierButton.setOnAction(event -> {
                Message message = getItem();
                if (message != null) openEditWindow(message);
            });

            replyButton.setOnAction(event -> {
                Message message = getItem();
                if (message != null) showReplies(message);
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

    private void showReplies(Message message) {
        try {
            selectedMessage = message;
            List<Reply> replies = serviceReply.trouverParMessageId(message.getId());
            repliesListView.getItems().setAll(replies);
            repliesContainer.setVisible(true);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les réponses", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSendReply() {
        // Réinitialisation du texte d'erreur
        errorLabel.setVisible(false);
        errorLabel.setText("");

        if (selectedMessage == null || replyTextArea.getText().isEmpty()) {
            errorLabel.setText("Le contenu de la réponse ne peut pas être vide.");
            errorLabel.setVisible(true);
            return;
        }

        if (replyTextArea.getText().length() < 2) {
            errorLabel.setText("La réponse doit contenir au moins 2 caractères.");
            errorLabel.setVisible(true);
            return;
        }
        else if (replyTextArea.getText().length() > 100) {
            errorLabel.setText("La réponse doit contenir maximum 100 caractères.");
            errorLabel.setVisible(true);
            return;
        }

        try {
            Reply reply = new Reply();
            reply.setMessageId(selectedMessage.getId());
            reply.setContenuReply(replyTextArea.getText());
            reply.setCreatedAt(LocalDateTime.now().toString());

            serviceReply.ajouter(reply);
            replyTextArea.clear();
            showReplies(selectedMessage);
        } catch (SQLException e) {
            errorLabel.setText("Échec de l'envoi de la réponse.");
            errorLabel.setVisible(true);
        }
    }

    private void openEditWindow(Message message) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/ModifierMessage.fxml"));
            VBox root = loader.load();

            ModifierMessageController controller = loader.getController();
            controller.setMessage(message);

            Stage stage = new Stage();
            stage.setTitle("Modifier le Message");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            messagesList.refresh();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de modification.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCloseReplies() {
        repliesContainer.setVisible(false);  // Masque la section des réponses
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
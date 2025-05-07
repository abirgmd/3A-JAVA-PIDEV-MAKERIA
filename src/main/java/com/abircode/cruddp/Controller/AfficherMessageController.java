package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Message;
import com.abircode.cruddp.entities.Reaction;
import com.abircode.cruddp.entities.User;
import com.abircode.cruddp.services.ServiceMessage;
import com.abircode.cruddp.services.ServiceReaction;
import com.abircode.cruddp.services.ServiceReply;
import com.abircode.cruddp.utils.SessionManager;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.animation.ScaleTransition;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.geometry.Bounds;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import javafx.stage.Popup;
import javafx.scene.Node;
import javafx.stage.PopupWindow;
import javafx.scene.Cursor;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Map;
import javafx.geometry.Pos;

// FreeTTS imports
// Ensure the FreeTTS library is properly added to the project dependencies before importing
import com.sun.speech.freetts.Voice;
// Ensure the FreeTTS library is properly added to the project dependencies before importing
import com.sun.speech.freetts.VoiceManager;

public class AfficherMessageController {

    @FXML
    private ListView<Message> messagesList;
    @FXML
    private TextField searchField;
    @FXML
    private Button clearSearchBtn;
    @FXML
    private Label countLabel;
    @FXML
    private HBox paginationBox;
    @FXML
    private ComboBox<String> sortOptions;
    @FXML
    private VBox usersRankingBox;
    private final ServiceMessage serviceMessage = new ServiceMessage();
    private final ServiceReply serviceReply = new ServiceReply();
    private final ServiceReaction serviceReaction = new ServiceReaction();

    private List<Message> allMessages;
    private List<Message> filteredMessages;
    private int currentPage = 0;
    private final int messagesPerPage = 4;
    private int totalPages;

    // FreeTTS voice
    private Voice voice;

    @FXML
    public void initialize() {
        // Initialisation FreeTTS
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        VoiceManager voiceManager = VoiceManager.getInstance();
        voice = voiceManager.getVoice("kevin16");
        if (voice != null) {
            voice.allocate();
        } else {
            System.err.println("Voix FreeTTS non trouvée !");
        }

        configureMessageList();
        initializeSortOptions();
        loadMessages();
        setupSearchFilter();
        clearSearchBtn.setOnAction(e -> searchField.clear());
        messagesList.requestFocus();
    }

    // Libération de la voix à la fermeture (à appeler dans stop() de l'app)
    public void cleanup() {
        if (voice != null) {
            voice.deallocate();
        }
    }

    private void initializeSortOptions() {
        ObservableList<String> options = FXCollections.observableArrayList(
                "Date (plus ancien - plus récent)",
                "Date (plus récent - plus ancien)",
                "Titre (A - Z)",
                "Titre (Z - A)"
        );
        sortOptions.setItems(options);
        sortOptions.setOnAction(event -> sortMessages());
        sortOptions.setStyle("-fx-background-color: white; -fx-border-color: #b11d85; -fx-border-radius: 5;");
    }

    private void sortMessages() {
        String selectedOption = (String) sortOptions.getValue();
        if (selectedOption != null) {
            switch (selectedOption) {
                case "Date (plus ancien - plus récent)":
                    filteredMessages.sort(Comparator.comparing(Message::getDateMsg));
                    break;
                case "Date (plus récent - plus ancien)":
                    filteredMessages.sort((m1, m2) -> m2.getDateMsg().compareTo(m1.getDateMsg()));
                    break;
                case "Titre (A - Z)":
                    filteredMessages.sort(Comparator.comparing(msg -> {
                        String title = msg.getTitreMsg();
                        return title != null ? title.toLowerCase() : "";
                    }));
                    break;
                case "Titre (Z - A)":
                    filteredMessages.sort((m1, m2) -> {
                        String title1 = m1.getTitreMsg();
                        String title2 = m2.getTitreMsg();
                        return title2.toLowerCase().compareTo(title1.toLowerCase());
                    });
                    break;
            }
            loadPage(currentPage);
        }
    }

    @FXML
    public void ouvrirAjouterMessage(ActionEvent actionEvent) {
        if (SessionManager.getCurrentUser() == null) {
            showLoginAlert();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AjouterMessage.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Message");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadMessages();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la vue: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void configureMessageList() {
        messagesList.setCellFactory(param -> new ListCell<>() {
            private final VBox postContainer = new VBox(8);
            private final HBox header = new HBox(12);
            private final Label titleLabel = new Label();
            private final Label descriptionLabel = new Label();
            private final Label dateLabel = new Label();
            private final Label userLabel = new Label();
            private final ImageView imageView = new ImageView();
            private final HBox actions = new HBox(8);

            private final Button replyButton = new Button();
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();

            // AJOUT bouton LIRE
            private final Button lireButton = new Button("\uD83C\uDFA7");

            // Conteneur pour les réactions
            private final HBox reactionsBox = new HBox(10);

            {
                // Style du conteneur principal
                postContainer.setMaxWidth(900);
                postContainer.setPrefWidth(900);
                postContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5,0,0,1);");
                header.setStyle("-fx-alignment: center-left;");
                titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4a148c;");
                descriptionLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6a1b9a; -fx-wrap-text: true;");
                descriptionLabel.setWrapText(true);
                descriptionLabel.setMaxWidth(800);
                dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9c27b0;");
                userLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #b11d85;");
                imageView.setFitWidth(320);
                imageView.setFitHeight(260);
                imageView.setPreserveRatio(true);
                imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3,0,0,0);");

                FontIcon replyIcon = new FontIcon(FontAwesome.COMMENTS);
                replyIcon.setIconSize(18);
                replyIcon.setIconColor(Color.web("#6a1b9a"));
                replyButton.setGraphic(replyIcon);
                replyButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                FontIcon editIcon = new FontIcon(FontAwesome.PENCIL);
                editIcon.setIconSize(18);
                editIcon.setIconColor(Color.web("#6a1b9a"));
                editButton.setGraphic(editIcon);
                editButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                FontIcon deleteIcon = new FontIcon(FontAwesome.TRASH);
                deleteIcon.setIconSize(18);
                deleteIcon.setIconColor(Color.web("#f44336"));
                deleteButton.setGraphic(deleteIcon);
                deleteButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                // Style bouton Lire
                lireButton.setStyle("-fx-background-color: #b11d85; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 8; -fx-cursor: hand;");

                actions.setStyle("-fx-alignment: center;");
                actions.getChildren().addAll(replyButton, editButton, deleteButton, lireButton);

                header.getChildren().addAll(userLabel, dateLabel);

                postContainer.getChildren().addAll(header, titleLabel, descriptionLabel, imageView, reactionsBox, actions);
                HBox.setHgrow(postContainer, Priority.ALWAYS);

                replyButton.setOnAction(event -> {
                    Message message = getItem();
                    if (message != null) {
                        if (SessionManager.getCurrentUser() == null) {
                            showLoginAlert();
                        } else {
                            showRepliesWindow(message);
                        }
                    }
                });

                editButton.setOnAction(event -> {
                    Message message = getItem();
                    if (message != null) openEditWindow(message);
                });

                deleteButton.setOnAction(event -> {
                    Message message = getItem();
                    if (message != null) handleDeleteMessage(message);
                });

                // Action bouton Lire (FreeTTS)
                lireButton.setOnAction(event -> {
                    Message message = getItem();
                    if (message != null && voice != null) {
                        String toRead = message.getTitreMsg() + ". " + message.getDescriptionMsg() + ". " + message.getContenu();
                        voice.speak(toRead);
                    }
                });
            }

            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setGraphic(null);
                    setStyle(null);
                } else {
                    titleLabel.setText(message.getTitreMsg());
                    descriptionLabel.setText(message.getDescriptionMsg() + "\n" + message.getContenu());
                    dateLabel.setText(message.getDateMsg());
                    if (message.getUser() != null && message.getUser().getName() != null) {
                        userLabel.setText(message.getUser().getName() + " : ");
                    } else {
                        userLabel.setText("Unknown User : ");
                    }
                    String imageUrl = message.getImage();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        try {
                            Image image = new Image("file:" + imageUrl);
                            imageView.setImage(image);
                            imageView.setVisible(true);
                        } catch (Exception e) {
                            System.err.println("Erreur chargement image: " + e.getMessage());
                            imageView.setImage(null);
                            imageView.setVisible(false);
                        }
                    } else {
                        imageView.setImage(null);
                        imageView.setVisible(false);
                    }
                    User currentUser = SessionManager.getCurrentUser();
                    boolean isCurrentUserMessage = currentUser != null && message.getUser() != null && currentUser.getId() == message.getUser().getId();
                    editButton.setVisible(isCurrentUserMessage);
                    deleteButton.setVisible(isCurrentUserMessage);
                    refreshReactions(message.getId(), reactionsBox);
                    setGraphic(postContainer);
                    FadeTransition ft = new FadeTransition(Duration.millis(400), postContainer);
                    ft.setFromValue(0);
                    ft.setToValue(1);
                    ft.play();
                }
            }
        });
    }
    private void loadUsersRanking() {
        try {
            usersRankingBox.getChildren().clear();

            // Récupère le top 5 des utilisateurs les plus actifs
            Map<User, Long> topUsers = serviceMessage.getTopActiveUsers(5);

            int rank = 1;
            for (Map.Entry<User, Long> entry : topUsers.entrySet()) {
                User user = entry.getKey();
                long messageCount = entry.getValue();

                HBox userBox = new HBox(10);
                userBox.setStyle("-fx-background-color: #b11d85; -fx-background-radius: 8; -fx-padding: 10;");

                Label rankLabel = new Label(rank + ".");
                rankLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #b11d85;");

                VBox userInfoBox = new VBox(3);
                Label nameLabel = new Label(user.getName());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                Label countLabel = new Label(messageCount + " message" + (messageCount > 1 ? "s" : ""));
                countLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");

                userInfoBox.getChildren().addAll(nameLabel, countLabel);

                userBox.getChildren().addAll(rankLabel, userInfoBox);
                usersRankingBox.getChildren().add(userBox);

                rank++;
            }

            // Style pour le conteneur du classement
            usersRankingBox.setStyle("-fx-padding: 5;");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le classement des utilisateurs", Alert.AlertType.ERROR);
        }
    }
    private void refreshReactions(int messageId, HBox reactionsBox) {
        reactionsBox.getChildren().clear();
        reactionsBox.setSpacing(5); // Espacement entre les éléments
        reactionsBox.setAlignment(Pos.CENTER_LEFT); // Alignement à gauche

        User currentUser = SessionManager.getCurrentUser();
        int currentUserId = currentUser != null ? currentUser.getId() : -1;

        try {
            Map<String, Integer> reactionsCount = serviceReaction.getReactionsCountParMessage(messageId);
            List<String> userReactions = currentUserId != -1
                    ? serviceReaction.getEmojisParUserEtMessage(currentUserId, messageId)
                    : Collections.emptyList();

            // Afficher les réactions existantes avec tooltip
            for (Map.Entry<String, Integer> entry : reactionsCount.entrySet()) {
                String emoji = entry.getKey();
                int count = entry.getValue();
                boolean isUserReaction = userReactions.contains(emoji);

                // Conteneur pour l'emoji et le compteur (meilleur alignement)
                HBox reactionContainer = new HBox(3); // Petit espace entre emoji et compteur
                reactionContainer.setAlignment(Pos.CENTER);
                reactionContainer.setStyle("-fx-background-radius: 15; -fx-padding: 3 6 3 6;");

                // Style dynamique selon si l'utilisateur a réagi
                String containerStyle = isUserReaction
                        ? "-fx-background-color: #f3d9f3; -fx-border-color: #b11d85; -fx-border-width: 1; -fx-border-radius: 15;"
                        : "-fx-background-color: #f0f0f0; -fx-border-radius: 15;";
                reactionContainer.setStyle(containerStyle);

                Label emojiLabel = new Label(emoji);
                emojiLabel.setStyle("-fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', 'Noto Color Emoji'; -fx-font-size: 16px;");

                Label countLabel = new Label(String.valueOf(count));
                countLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + (isUserReaction ? "#b11d85" : "#555555") + ";");

                reactionContainer.getChildren().addAll(emojiLabel, countLabel);

                // Tooltip avec les utilisateurs
                List<User> reactors = serviceReaction.getUtilisateursParReaction(messageId, emoji);
                if (!reactors.isEmpty()) {
                    String tooltipText = reactors.stream()
                            .map(User::getName)
                            .collect(Collectors.joining("\n"));
                    Tooltip tooltip = new Tooltip(tooltipText);
                    tooltip.setStyle("-fx-font-size: 12px;");
                    Tooltip.install(reactionContainer, tooltip);
                }

                // Animation au survol
                reactionContainer.setOnMouseEntered(e -> {
                    reactionContainer.setCursor(Cursor.HAND);
                    reactionContainer.setStyle(containerStyle + "-fx-scale-x: 1.05; -fx-scale-y: 1.05;");
                });
                reactionContainer.setOnMouseExited(e -> {
                    reactionContainer.setStyle(containerStyle);
                });

                reactionContainer.setOnMouseClicked(e -> handleReactionClick(messageId, currentUserId, emoji, reactionsBox));
                reactionsBox.getChildren().add(reactionContainer);
            }

            // Bouton pour ajouter une réaction (style moderne)
            if (currentUserId != -1) {
                Button addReactionBtn = new Button("\uD83D\uDE42"); // Emoji "🙂"
                addReactionBtn.setStyle(
                        "-fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', 'Noto Color Emoji'; " +
                                "-fx-font-size: 16px; " +
                                "-fx-background-color: linear-gradient(to bottom, #b11d85, #8a1766); " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 15; " +
                                "-fx-border-radius: 15; " +
                                "-fx-padding: 5 10 5 10; " +
                                "-fx-cursor: hand; " +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 1);");

                // Animation du bouton
                addReactionBtn.setOnMouseEntered(e -> {
                    addReactionBtn.setStyle(addReactionBtn.getStyle() + "-fx-scale-x: 1.1; -fx-scale-y: 1.1;");
                });
                addReactionBtn.setOnMouseExited(e -> {
                    addReactionBtn.setStyle(addReactionBtn.getStyle().replace("-fx-scale-x: 1.1; -fx-scale-y: 1.1;", ""));
                });

                addReactionBtn.setOnAction(e -> showReactionPicker(messageId, currentUserId, reactionsBox));
                reactionsBox.getChildren().add(addReactionBtn);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            // Optionnel: Afficher un message d'erreur à l'utilisateur
            Label errorLabel = new Label("Erreur de chargement des réactions");
            errorLabel.setStyle("-fx-text-fill: red;");
            reactionsBox.getChildren().add(errorLabel);
        }
    }

    private void handleReactionClick(int messageId, int userId, String emoji, HBox reactionsBox) {
        if (userId == -1) {
            showLoginAlert();
            return;
        }

        try {
            if (serviceReaction.utilisateurAReact(messageId, userId)) {
                serviceReaction.supprimerReactionsUtilisateur(messageId, userId);
            } else {
                serviceReaction.ajouterReaction(new Reaction(messageId, userId, emoji));
            }
            refreshReactions(messageId, reactionsBox);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void showReactionPicker(int messageId, int userId, HBox reactionsBox) {
        Popup popup = new Popup();
        HBox emojiContainer = new HBox(5);
        emojiContainer.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-radius: 5; -fx-border-color: #b11d85; -fx-spacing: 5;");

        for (Map.Entry<String, String> entry : ServiceReaction.DEFAULT_EMOJIS.entrySet()) {
            String emoji = entry.getKey();
            String description = entry.getValue();

            Label emojiLabel = new Label(emoji);
            emojiLabel.setStyle("-fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', 'Noto Color Emoji', sans-serif; -fx-font-size: 24px; -fx-cursor: hand;");
            Tooltip.install(emojiLabel, new Tooltip(description));

            // Effet hover
            emojiLabel.setOnMouseEntered(e -> emojiLabel.setStyle("-fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', 'Noto Color Emoji', sans-serif; -fx-font-size: 28px; -fx-cursor: hand; -fx-opacity: 0.8;"));
            emojiLabel.setOnMouseExited(e -> emojiLabel.setStyle("-fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', 'Noto Color Emoji', sans-serif; -fx-font-size: 24px; -fx-cursor: hand; -fx-opacity: 1;"));

            emojiLabel.setOnMouseClicked(e -> {
                try {
                    serviceReaction.ajouterReaction(new Reaction(messageId, userId, emoji));
                    refreshReactions(messageId, reactionsBox);
                    popup.hide();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });

            emojiContainer.getChildren().add(emojiLabel);
        }

        popup.getContent().add(emojiContainer);
        popup.setAutoHide(true);
        popup.setAnchorLocation(PopupWindow.AnchorLocation.CONTENT_BOTTOM_LEFT);

        Node source = reactionsBox.getChildren().get(reactionsBox.getChildren().size() - 1);
        Bounds bounds = source.localToScreen(source.getBoundsInLocal());
        popup.show(source, bounds.getMinX(), bounds.getMaxY());
    }

    private void showLoginAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Connexion requise");
        alert.setHeaderText(null);
        alert.setContentText("Vous devez être connecté pour effectuer cette action.");
        alert.showAndWait();
    }

    private void handleDeleteMessage(Message message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le message");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce message et toutes ses réponses ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceReply.supprimerToutesReponsesPourMessage(message.getId());
                    serviceMessage.supprimer(message.getId());
                    allMessages.remove(message);
                    filteredMessages = allMessages;
                    loadPage(currentPage);
                    updateCountLabel(allMessages.size());
                    showAlert("Succès", "Message et réponses supprimés avec succès", Alert.AlertType.INFORMATION);
                } catch (SQLException e) {
                    showAlert("Erreur", "Échec de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void openEditWindow(Message message) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ModifierMessage.fxml"));
            Parent root = loader.load();
            ModifierMessageController controller = loader.getController();
            controller.setMessage(message);
            Stage stage = new Stage();
            stage.setTitle("Modifier le Message");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadMessages();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur", Alert.AlertType.ERROR);
        }
    }

    private void showRepliesWindow(Message message) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RepliesWindow.fxml"));
            Parent root = loader.load();
            RepliesWindowController controller = loader.getController();
            controller.setMessage(message);
            Stage stage = new Stage();
            stage.setTitle("Réponses: " + message.getTitreMsg());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre des réponses", Alert.AlertType.ERROR);
        }
    }

    private void loadMessages() {
        try {
            allMessages = serviceMessage.afficher();
            filteredMessages = allMessages;
            sortMessages();
            totalPages = (int) Math.ceil((double) filteredMessages.size() / messagesPerPage);
            loadPage(0);
            updateCountLabel(allMessages.size());
            setupPaginationButtons();
            loadUsersRanking(); // Ajout de cette ligne
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les messages", Alert.AlertType.ERROR);
        }
    }

    private void loadPage(int page) {
        currentPage = page;
        int startIndex = page * messagesPerPage;
        int endIndex = Math.min(startIndex + messagesPerPage, filteredMessages.size());
        List<Message> pageMessages = filteredMessages.subList(startIndex, endIndex);
        refreshListView(pageMessages);
    }

    private void refreshListView(List<Message> messages) {
        messagesList.getItems().setAll(messages);
    }

    private void setupSearchFilter() {
        searchField.setStyle("-fx-background-color: white; -fx-border-color:#b11d85; -fx-border-radius: 5;");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredMessages = allMessages.stream()
                    .filter(msg -> msg.getTitreMsg().toLowerCase().contains(newVal.toLowerCase()) ||
                            msg.getDescriptionMsg().toLowerCase().contains(newVal.toLowerCase()))
                    .collect(Collectors.toList());
            totalPages = (int) Math.ceil((double) filteredMessages.size() / messagesPerPage);
            currentPage = 0;
            setupPaginationButtons();
            loadPage(0);
            updateCountLabel(allMessages.size());
        });
    }

    private void updateCountLabel(int count) {
        countLabel.setText("Nombre total de messages : " + count);
        countLabel.setStyle("-fx-text-fill: #b11d85; -fx-font-size: 14px;");
    }

    private void setupPaginationButtons() {
        paginationBox.getChildren().clear();
        Button prevButton = new Button("Précédent");
        prevButton.setStyle("-fx-background-color: #b11d85; -fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-padding: 6 14; -fx-background-radius: 10; -fx-cursor: hand;");
        prevButton.setOnAction(e -> {
            if (currentPage > 0) {
                loadPage(currentPage - 1);
            }
        });
        Button nextButton = new Button("Suivant");
        nextButton.setStyle("-fx-background-color: #b11d85; -fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-padding: 6 14; -fx-background-radius: 10; -fx-cursor: hand;");
        nextButton.setOnAction(e -> {
            if (currentPage < totalPages - 1) {
                loadPage(currentPage + 1);
            }
        });
        paginationBox.getChildren().addAll(prevButton, nextButton);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

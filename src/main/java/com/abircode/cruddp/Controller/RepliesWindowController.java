package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Message;
import com.abircode.cruddp.entities.Reply;
import com.abircode.cruddp.entities.User;
import com.abircode.cruddp.services.ServiceReply;
import com.abircode.cruddp.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome.FontAwesome;

public class RepliesWindowController {
    @FXML private ListView<Reply> repliesListView;
    @FXML private TextArea replyTextArea;
    @FXML private Button sendButton;

    // Liste des mots indésirables
    private static final List<String> MOTS_INDESIRABLES = Arrays.asList(
            "merde", "raciste", "mauvais", "noir"
    );

    private Message message;
    private final ServiceReply serviceReply = new ServiceReply();

    @FXML
    public void initialize() {
        repliesListView.setStyle("-fx-background-color: white;");
    }

    public void setMessage(Message message) {
        this.message = message;
        loadReplies();
    }

    private void loadReplies() {
        try {
            List<Reply> replies = serviceReply.trouverParMessageId(message.getId());
            repliesListView.getItems().setAll(replies);
            configureRepliesList();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les réponses", Alert.AlertType.ERROR);
        }
    }

    private void configureRepliesList() {
        repliesListView.setCellFactory(param -> new ListCell<Reply>() {
            private final HBox container = new HBox(10);
            private final VBox contentBox = new VBox(5);
            private final Label userLabel = new Label();
            private final Label content = new Label();
            private final Label date = new Label();
            private final Button deleteButton = new Button();
            private final Button editButton = new Button();

            {
                // Container style
                container.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-padding: 12px; " +
                        "-fx-effect: dropshadow(gaussian, #b11d85, 8, 0, 0, 3);");

                // Labels style
                userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #b11d85; -fx-font-size: 14px;");
                content.setStyle("-fx-font-size: 13px; -fx-text-fill: #4a148c;");
                content.setWrapText(true);
                content.setMaxWidth(300);
                date.setStyle("-fx-text-fill: #7c4dff; -fx-font-size: 11px;");

                // Création des icônes FontIcon pour les boutons
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

                // Ombre légère au survol
                DropShadow shadowEdit = new DropShadow(10, Color.web("#6a1b9a"));
                DropShadow shadowDelete = new DropShadow(10, Color.web("#f44336"));

                addHoverEffect(editButton, shadowEdit);
                addHoverEffect(deleteButton, shadowDelete);

                // Assemble layout
                contentBox.getChildren().addAll(userLabel, content, date);
                HBox.setHgrow(contentBox, Priority.ALWAYS);
                container.getChildren().addAll(contentBox, editButton, deleteButton);

                // Actions boutons
                deleteButton.setOnAction(e -> handleDeleteReply(getItem()));
                editButton.setOnAction(e -> handleEditReply(getItem()));
            }

            private void addHoverEffect(Button btn, DropShadow shadow) {
                btn.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> btn.setEffect(shadow));
                btn.addEventHandler(MouseEvent.MOUSE_EXITED, e -> btn.setEffect(null));
            }

            @Override
            protected void updateItem(Reply reply, boolean empty) {
                super.updateItem(reply, empty);
                if (empty || reply == null) {
                    setGraphic(null);
                } else {
                    userLabel.setText((reply.getUser() != null ? reply.getUser().getName() : "Anonyme") + " :");
                    content.setText(reply.getContenuReply());
                    date.setText("Posté le: " + reply.getCreatedAt());

                    // Afficher boutons uniquement si l'utilisateur est propriétaire
                    boolean isOwner = false;
                    if (reply.getUser() != null && SessionManager.getCurrentUser() != null) {
                        isOwner = reply.getUser().getId() == SessionManager.getCurrentUser().getId();
                    }
                    deleteButton.setVisible(isOwner);
                    editButton.setVisible(isOwner);

                    setGraphic(container);
                }
            }
        });
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) repliesListView.getScene().getWindow();
        stage.close();
    }

    private void handleDeleteReply(Reply reply) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer cette réponse ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réponse ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Utiliser la méthode sécurisée
                    serviceReply.supprimerAvecVerification(
                            reply.getId(),
                            SessionManager.getCurrentUser().getId()
                    );
                    loadReplies();
                    showAlert("Succès", "Réponse supprimée", Alert.AlertType.INFORMATION);
                } catch (SQLException e) {
                    showAlert("Erreur", "Échec de la suppression : " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void handleEditReply(Reply reply) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/ModifierReply.fxml"));
            VBox root = loader.load();

            ModifierReplyController controller = loader.getController();
            controller.setReply(reply);

            Stage stage = new Stage();
            stage.setTitle("Modifier la réponse");
            stage.setScene(new Scene(root));
            controller.setDialogStage(stage);

            stage.showAndWait();

            if (controller.isSaved()) {
                loadReplies();
                showAlert("Succès", "Réponse modifiée avec succès", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur de réponse", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSendReply() {
        String replyText = replyTextArea.getText().trim();

        if (replyText.isEmpty()) {
            showAlert("Erreur", "Veuillez écrire une réponse", Alert.AlertType.ERROR);
            return;
        }

        // Vérification des mots indésirables
        if (contientMotsIndesirables(replyText)) {
            replyTextArea.setText(remplacerMotsIndesirables(replyText));
            showAlert("Attention", "Votre réponse contient des mots inappropriés qui ont été masqués", Alert.AlertType.WARNING);
            return;
        }

        try {
            User currentUser = SessionManager.getCurrentUser();

            Reply reply = new Reply();
            reply.setMessageId(message.getId());
            reply.setContenuReply(replyText);
            reply.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            reply.setUser(currentUser); // Associer l'utilisateur

            serviceReply.ajouter(reply);
            replyTextArea.clear();
            loadReplies();
        } catch (SQLException e) {
            showAlert("Erreur", "Échec de l'envoi de la réponse : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthodes pour la gestion des mots indésirables
    private String masquerMot(String mot) {
        return "*".repeat(mot.length());
    }

    private String remplacerMotsIndesirables(String texte) {
        if (texte == null || texte.isEmpty()) {
            return texte;
        }
        String texteLowerCase = texte.toLowerCase();
        for (String motIndesirable : MOTS_INDESIRABLES) {
            if (texteLowerCase.contains(motIndesirable.toLowerCase())) {
                texte = texte.replaceAll("(?i)" + Pattern.quote(motIndesirable), masquerMot(motIndesirable));
            }
        }
        return texte;
    }

    private boolean contientMotsIndesirables(String texte) {
        if (texte == null || texte.isEmpty()) {
            return false;
        }
        String texteLowerCase = texte.toLowerCase();
        for (String motIndesirable : MOTS_INDESIRABLES) {
            if (texteLowerCase.contains(motIndesirable)) {
                return true;
            }
        }
        return false;
    }
}
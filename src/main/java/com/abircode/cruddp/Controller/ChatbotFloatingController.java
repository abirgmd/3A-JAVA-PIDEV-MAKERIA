package com.abircode.cruddp.Controller;

import com.abircode.cruddp.services.ChatbotService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ChatbotFloatingController {

    @FXML
    private Button openChatButton;

    @FXML
    private VBox chatWindow;

    @FXML
    private TextArea chatArea;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendButton;

    @FXML
    private Button closeButton;

    private final ChatbotService chatbotService;
    private boolean isChatOpen = false;

    public ChatbotFloatingController() {
        this.chatbotService = new ChatbotService();
    }

    @FXML
    public void initialize() {
        chatArea.setWrapText(true);
        chatArea.setEditable(false);
        chatWindow.setVisible(false);
        chatWindow.setManaged(false);

        // Message de bienvenue
        chatArea.appendText("Assistant: Bonjour ! Je suis votre assistant IA. Comment puis-je vous aider aujourd'hui ?\n\n");

        // Bouton d'envoi
        sendButton.setOnAction(event -> handleSendMessage());

        // Appui sur "Entrée" dans le champ de texte
        messageField.setOnAction(event -> handleSendMessage());

        // Bouton de fermeture
        closeButton.setOnAction(event -> toggleChat());
    }

    @FXML
    private void toggleChat() {
        isChatOpen = !isChatOpen;
        chatWindow.setVisible(isChatOpen);
        chatWindow.setManaged(isChatOpen);
    }

    @FXML
    private void handleSendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            // Affichage message utilisateur
            chatArea.appendText("Vous: " + message + "\n\n");

            // Exécution dans un thread séparé pour éviter blocage de l'interface
            new Thread(() -> {
                String response = chatbotService.getResponse(message);
                Platform.runLater(() -> {
                    chatArea.appendText("Assistant: " + response + "\n\n");
                    messageField.clear();
                    chatArea.setScrollTop(Double.MAX_VALUE);
                });
            }).start();
        }
    }
}

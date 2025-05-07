package com.abircode.cruddp.Controller;

import com.abircode.cruddp.services.ChatbotService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ChatbotController {
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField messageField;
    @FXML
    private Button sendButton;
    @FXML
    private Button clearButton;

    private final ChatbotService chatbotService;

    public ChatbotController() {
        this.chatbotService = new ChatbotService();
    }

    @FXML
    public void initialize() {
        // Ajouter un message de bienvenue
        chatArea.appendText("Assistant: Bonjour ! Comment puis-je vous aider aujourd'hui ?\n\n");
        
        // Configurer l'action du bouton Envoyer
        sendButton.setOnAction(event -> handleSendMessage());
        
        // Configurer l'action du bouton Effacer
        clearButton.setOnAction(event -> handleClearChat());
        
        // Permettre d'envoyer un message en appuyant sur Entrée
        messageField.setOnAction(event -> handleSendMessage());
    }

    @FXML
    private void handleSendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            // Afficher le message de l'utilisateur
            chatArea.appendText("Vous: " + message + "\n\n");
            
            // Obtenir la réponse du chatbot
            String response = chatbotService.getResponse(message);
            
            // Afficher la réponse
            chatArea.appendText("Assistant: " + response + "\n\n");
            
            // Effacer le champ de message
            messageField.clear();
            
            // Faire défiler jusqu'au bas
            chatArea.setScrollTop(Double.MAX_VALUE);
        }
    }

    @FXML
    private void handleClearChat() {
        chatArea.clear();
        chatbotService.clearHistory();
        chatArea.appendText("Assistant: Bonjour ! Comment puis-je vous aider aujourd'hui ?\n\n");
    }
} 
package com.abircode.cruddp.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIContentExplainerController {

    @FXML
    private TextField promptInput;

    @FXML
    private Button generateButton;

    @FXML
    private TextArea responseDisplay;

    private static final String API_KEY = "AIzaSyBTLZjfoVot0qWX9SbnvqbbZpnsyEXThqI";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    @FXML
    public void onGenerateButtonClick() {
        String prompt = promptInput.getText().trim();

        // Vérifier si l'entrée est vide
        if (prompt.isEmpty()) {
            responseDisplay.setText("Veuillez entrer une requête.");
            return;
        }

        try {
            // Construction du corps de la requête sans échappement des caractères spéciaux
            String requestBody = String.format("""
            {
                "contents": [
                    {
                        "parts": [
                            {
                                "text": "%s"
                            }
                        ]
                    }
                ]
            }
            """, prompt);

            // Initialiser le client HTTP et envoyer la requête
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Vérifier la réponse de l'API
            if (response.statusCode() == 200) {
                String formattedResponse = formatResponse(response.body());
                responseDisplay.setText(formattedResponse);
            } else {
                responseDisplay.setText("Erreur: " + response.statusCode() + "\n" + response.body());
            }

        } catch (Exception e) {
            responseDisplay.setText("Une erreur est survenue : " + e.getMessage());
        }
    }

    private String formatResponse(String response) {
        try {
            // Utiliser une expression régulière pour extraire le texte généré
            Pattern pattern = Pattern.compile("\"text\"\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(response);

            // Si un texte est trouvé, le formater
            if (matcher.find()) {
                String cleanedText = matcher.group(1);

                // Structure le texte pour ajouter des titres en gras et des listes organisées
                cleanedText = cleanedText
                        .replaceAll("(?<=\\b(Attaquants|Milieux de terrain|Défenseurs|Gardiens de but)\\b)", "\n**$1 :**\n") // Ajouter le titre en gras
                        .replaceAll("(\\*\\s)([^\\*]+)", "\n* $2") // Formatage de la liste sous forme de puces
                        .replaceAll("(\\n\\s*\\n)", "\n"); // Supprimer les retours à la ligne inutiles

                // Retourner la réponse formatée
                return "Réponse générée :\n\n" + cleanedText.trim();
            } else {
                return "Erreur : Impossible d'extraire le texte de la réponse.";
            }

        } catch (Exception e) {
            return "Erreur : Impossible d'analyser la réponse.";
        }
    }

    // Méthode pour formater le texte de manière lisible avec des titres et des listes
    private String formatFormattedText(String rawText) {
        // Remplacer les caractères de retour à la ligne et les espaces excessifs par un format lisible
        String formattedText = rawText.replace("\n", " ")
                .replace("\r", "")
                .replaceAll("\\s{2,}", " ");  // Réduit les espaces multiples en un seul

        // Ajouter des retours à la ligne entre les sections principales
        formattedText = formattedText.replaceAll("(?<=:)(\\s*\\w+)(\\s*)", ": \n\n**$1** :\n\n* $2");

        // Retourner un texte formaté avec une présentation claire
        return formattedText.trim();  // Retirer les espaces superflus avant le retour
    }
}

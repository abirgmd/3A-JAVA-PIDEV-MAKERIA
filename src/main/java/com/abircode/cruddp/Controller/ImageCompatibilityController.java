package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Categorie;
import com.abircode.cruddp.utils.DBConnexion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;

public class ImageCompatibilityController {

    @FXML
    private Button uploadImageButton;

    @FXML
    private Label imagePathLabel;

    @FXML
    private ComboBox<Categorie> categoryComboBox;

    @FXML
    private Button analyzeButton;

    @FXML
    private TextArea responseArea;

    private File selectedImageFile;

    // Clé API Clarifai
    private static final String API_KEY = "9281b188747f49d8b793575aa1951613"; // Remplace par la clé que tu choisis
    private static final String API_URL = "https://api.clarifai.com/v2/models/general-image-recognition/outputs"; // Utilisez un modèle valide

    @FXML
    public void initialize() {
        // Charger les catégories depuis la base de données
        List<Categorie> categories = loadCategoriesFromDatabase();
        if (categories != null) {
            categoryComboBox.getItems().addAll(categories);
        }
    }

    @FXML
    private void onUploadImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        selectedImageFile = fileChooser.showOpenDialog(null);

        if (selectedImageFile != null) {
            imagePathLabel.setText(selectedImageFile.getAbsolutePath());
        } else {
            imagePathLabel.setText("No image selected");
        }
    }

    @FXML
    private void onAnalyzeImageClicked(ActionEvent event) {
        if (selectedImageFile == null) {
            showAlert("Error", "Please upload an image first.");
            return;
        }

        Categorie selectedCategory = categoryComboBox.getValue();
        if (selectedCategory == null) {
            showAlert("Error", "Please select a category.");
            return;
        }

        String categoryName = selectedCategory.getNomcat();

        // Appeler l'API Clarifai
        String response = callClarifaiAPI(selectedImageFile, categoryName);
        responseArea.setText(response);
    }


    private String callClarifaiAPI(File imageFile, String categoryName) {
        try {
            // Convertir l'image en Base64
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Créer la connexion HTTP
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Key " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Construire la charge utile de la requête
            String payload = "{"
                    + "\"inputs\": [{"
                    + "\"data\": {"
                    + "\"image\": {"
                    + "\"base64\": \"" + base64Image + "\""
                    + "}"
                    + "}"
                    + "}]}";

            // Envoyer la requête
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Lire la réponse de l'API
            StringBuilder response = new StringBuilder();
            int statusCode = connection.getResponseCode();

            if (statusCode == HttpURLConnection.HTTP_OK) {
                // Si la réponse est OK, lire la réponse du serveur
                try (InputStream is = connection.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line.trim());
                    }
                }
            } else {
                // Si la réponse n'est pas OK, lire le flux d'erreur
                try (InputStream errorStream = connection.getErrorStream();
                     BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream, "utf-8"))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorResponse.append(errorLine);
                    }
                    return "Error occurred: " + errorResponse.toString();
                }
            }

            String responseString = response.toString();
            if (responseString.contains(categoryName)) {
                return "L'image n'est PAS compatible avec la catégorie sélectionnée.: " + categoryName;
            } else {
                return "L'image n'est PAS compatible avec la catégorie sélectionnée.: " + categoryName;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "Error occurred while calling the API: " + e.getMessage();
        }
    }

    private List<Categorie> loadCategoriesFromDatabase() {
        try (var connection = DBConnexion.getCon();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery("SELECT * FROM categorie")) {

            List<Categorie> categories = new java.util.ArrayList<>();
            while (resultSet.next()) {
                categories.add(new Categorie(
                        resultSet.getInt("id"),
                        resultSet.getString("nomcat"),
                        resultSet.getString("descriptioncat")
                ));
            }
            return categories;

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load categories from the database.");
            return null;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

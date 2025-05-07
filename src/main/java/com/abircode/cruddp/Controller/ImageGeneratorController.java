package com.abircode.cruddp.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Label;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageGeneratorController {

    @FXML
    private TextArea promptInput;
    @FXML
    private Button generateBtn;
    @FXML
    private ImageView generatedImage;
    @FXML
    private ProgressIndicator loader;
    @FXML
    private Label errorMessage;

    private static final String HF_TOKEN = "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";
    private static final String MODEL_ID = "stabilityai/stable-diffusion-xl-base-1.0";

    @FXML
    private void generateImage() {
        String prompt = promptInput.getText().trim();

        if (prompt.isEmpty()) {
            showError("Please enter a description to generate artwork");
            return;
        }

        // Reset UI
        errorMessage.setVisible(false);
        generatedImage.setVisible(false);
        loader.setVisible(true);
        generateBtn.setDisable(true);

        // Create image generation request
        new Thread(() -> {
            try {
                URL url = new URL("https://api-inference.huggingface.co/models/" + MODEL_ID);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + HF_TOKEN);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                String jsonBody = "{\"inputs\":\"" + prompt + "\", \"parameters\":{\"width\":512, \"height\":512, \"num_inference_steps\":50}}";
                connection.getOutputStream().write(jsonBody.getBytes());

                InputStream responseStream = connection.getInputStream();
                Image image = new Image(responseStream);

                // Update UI
                javafx.application.Platform.runLater(() -> {
                    generatedImage.setImage(image);
                    generatedImage.setFitWidth(500); // Set image width to 200px
                    generatedImage.setFitHeight(500); // Set image height to 200px
                    generatedImage.setVisible(true);
                    loader.setVisible(false);
                    generateBtn.setDisable(false);
                });
            } catch (Exception e) {
                showError("Failed to generate image: " + e.getMessage());
            }
        }).start();
    }

    private void showError(String message) {
        javafx.application.Platform.runLater(() -> {
            errorMessage.setText(message);
            errorMessage.setVisible(true);
            loader.setVisible(false);
            generateBtn.setDisable(false);
        });
    }
}

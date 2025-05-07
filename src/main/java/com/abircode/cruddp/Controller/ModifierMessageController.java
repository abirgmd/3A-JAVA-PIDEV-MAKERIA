package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Message;
import com.abircode.cruddp.services.ServiceMessage;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class ModifierMessageController {

    @FXML private TextField titreField;
    @FXML private Label titreError;

    @FXML private TextArea descriptionField;
    @FXML private Label descriptionError;

    @FXML private TextArea contenuField;
    @FXML private Label contenuError;

    @FXML private Label imagePathLabel;
    @FXML private Button chooseImageButton;
    @FXML private ImageView imageViewPreview;

    // Liste des mots indésirables
    private static final List<String> MOTS_INDESIRABLES = Arrays.asList(
            "merde", "raciste", "mauvais", "noir"
    );

    private final ServiceMessage serviceMessage = new ServiceMessage();
    private Message message;
    private String imagePath;

    public void setMessage(Message message) {
        this.message = message;

        titreField.setText(message.getTitreMsg());
        descriptionField.setText(message.getDescriptionMsg());
        contenuField.setText(message.getContenu());

        imagePath = message.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            Image image = new Image("file:" + imagePath);
            imageViewPreview.setImage(image);
            imagePathLabel.setText(imagePath);
        }
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une nouvelle image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(chooseImageButton.getScene().getWindow());
        if (selectedFile != null) {
            imagePath = selectedFile.getAbsolutePath();
            Image image = new Image("file:" + imagePath);
            imageViewPreview.setImage(image);
            imagePathLabel.setText(imagePath);
        }
    }

    @FXML
    private void handleSave() {
        clearErrorLabels();
        boolean isValid = true;

        String titre = titreField.getText().trim();
        String description = descriptionField.getText().trim();
        String contenu = contenuField.getText().trim();

        // Vérification et masquage des mots indésirables
        if (contientMotsIndesirables(titre)) {
            titreError.setText("Le titre contient des mots indésirables.");
            titreField.setText(remplacerMotsIndesirables(titre));
            isValid = false;
        } else if (titre.isEmpty()) {
            titreError.setText("Le titre est obligatoire.");
            isValid = false;
        } else if (titre.length() > 20) {
            titreError.setText("Le titre ne doit pas dépasser 20 caractères.");
            isValid = false;
        }

        if (contientMotsIndesirables(description)) {
            descriptionError.setText("La description contient des mots indésirables.");
            descriptionField.setText(remplacerMotsIndesirables(description));
            isValid = false;
        } else if (description.isEmpty()) {
            descriptionError.setText("La description est obligatoire.");
            isValid = false;
        } else if (description.length() > 50) {
            descriptionError.setText("La description ne doit pas dépasser 50 caractères.");
            isValid = false;
        }

        if (contientMotsIndesirables(contenu)) {
            contenuError.setText("Le contenu contient des mots indésirables.");
            contenuField.setText(remplacerMotsIndesirables(contenu));
            isValid = false;
        } else if (contenu.isEmpty()) {
            contenuError.setText("Le contenu est obligatoire.");
            isValid = false;
        } else if (contenu.length() > 500) {
            contenuError.setText("Le contenu ne doit pas dépasser 500 caractères.");
            isValid = false;
        }

        if (!isValid) return;

        try {
            message.setTitreMsg(titreField.getText());
            message.setDescriptionMsg(descriptionField.getText());
            message.setContenu(contenuField.getText());
            message.setImage(imagePath);

            serviceMessage.modifier(message);
            closeWindow();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de modifier le message : " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void clearErrorLabels() {
        titreError.setText("");
        descriptionError.setText("");
        contenuError.setText("");
    }

    private void closeWindow() {
        Stage stage = (Stage) titreField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

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
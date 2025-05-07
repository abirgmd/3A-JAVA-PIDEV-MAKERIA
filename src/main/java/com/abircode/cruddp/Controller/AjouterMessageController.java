package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Message;
import com.abircode.cruddp.entities.User;
import com.abircode.cruddp.services.ServiceMessage;
import com.abircode.cruddp.utils.SessionManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.vosk.Model;
import java.util.regex.Pattern;
import org.vosk.Recognizer;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class AjouterMessageController {
    @FXML
    private TextField tf_titre;
    @FXML
    private TextField tf_description;
    @FXML
    private TextArea ta_contenu;
    @FXML
    private Label error_titre;
    @FXML
    private Label error_description;
    @FXML
    private Label error_contenu;
    @FXML
    private Button btn_choisirImage;
    @FXML
    private Label labelImagePath;
    @FXML
    private Button btn_micro_titre;
    @FXML
    private Button btn_micro_description;
    @FXML
    private Button btn_micro_contenu;

    private String imagePath;

    // Liste des mots indésirables
    private static final List<String> MOTS_INDESIRABLES = Arrays.asList(
            "merde", "raciste", "mauvais", "noir"
    );

    @FXML
    public void initialize() {
        labelImagePath.setText("");
        applyStyles();
    }

    private void applyStyles() {
        // Couleurs principales
        String violetFonce = "#4a148c";
        String violetMoyen = "#b11d85";
        String violetClair = "#7c4dff";
        String rougeErreur = "#f44336";

        // Style des TextFields et TextArea
        String inputStyle = "-fx-border-color: " + violetMoyen + ";" +
                "-fx-border-radius: 12px; " +
                "-fx-background-radius: 12px; " +
                "-fx-padding: 8 12 8 12; " +
                "-fx-font-size: 14px;";
        tf_titre.setStyle(inputStyle);
        tf_description.setStyle(inputStyle);
        ta_contenu.setStyle(inputStyle + "-fx-font-size: 13px;");

        // Style des labels d'erreur
        error_titre.setTextFill(Color.web(rougeErreur));
        error_description.setTextFill(Color.web(rougeErreur));
        error_contenu.setTextFill(Color.web(rougeErreur));
        error_titre.setStyle("-fx-font-weight: bold;");
        error_description.setStyle("-fx-font-weight: bold;");
        error_contenu.setStyle("-fx-font-weight: bold;");

        // Style label image path
        labelImagePath.setStyle("-fx-text-fill: " + violetMoyen + "; -fx-font-style: italic;");

        // Style boutons
        String btnStyle = "-fx-background-color: transparent; " +
                "-fx-text-fill: " + violetClair + "; " +
                "-fx-font-weight: bold; " +
                "-fx-cursor: hand;";
        btn_choisirImage.setStyle(btnStyle);
        btn_micro_titre.setStyle(btnStyle);
        btn_micro_description.setStyle(btnStyle);
        btn_micro_contenu.setStyle(btnStyle);

        // Ajout d'ombre légère sur boutons au survol
        DropShadow shadow = new DropShadow(10, Color.web(violetClair));
        addHoverEffect(btn_choisirImage, shadow);
        addHoverEffect(btn_micro_titre, shadow);
        addHoverEffect(btn_micro_description, shadow);
        addHoverEffect(btn_micro_contenu, shadow);
    }

    private void addHoverEffect(Button btn, DropShadow shadow) {
        btn.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> btn.setEffect(shadow));
        btn.addEventHandler(MouseEvent.MOUSE_EXITED, e -> btn.setEffect(null));
    }
    private String masquerMot(String mot) {
        return "*".repeat(mot.length());
    }
    private String remplacerMotsIndesirables(String texte) {
        if (texte == null || texte.isEmpty()) {
            return texte;
        }
        String texteLowerCase = texte.toLowerCase(); // Pour rendre la vérification insensible à la casse
        for (String motIndesirable : MOTS_INDESIRABLES) {
            if (texteLowerCase.contains(motIndesirable.toLowerCase())) {
                texte = texte.replaceAll("(?i)" + Pattern.quote(motIndesirable), masquerMot(motIndesirable));
            }
        }
        return texte;
    }
    @FXML
    public void choisirImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(btn_choisirImage.getScene().getWindow());
        if (selectedFile != null) {
            imagePath = selectedFile.getAbsolutePath();
            labelImagePath.setText(imagePath);
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        Stage stage = (Stage) btn_choisirImage.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void AfficherMessage(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/AfficherMessage.fxml"));
            tf_titre.getScene().setRoot(root);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la vue: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void AjouterMessage(ActionEvent actionEvent) {
        if (!validerChamps()) {
            return;
        }
        if (imagePath != null) {
            File imgFile = new File(imagePath);
            if (!imgFile.exists() || !imgFile.isFile()) {
                imagePath = null;
                labelImagePath.setText("");
            }
        }
        String dateNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        // Récupération de l'utilisateur connecté
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showAlert("Erreur", "Aucun utilisateur connecté !", Alert.AlertType.ERROR);
            return;
        }
        ServiceMessage serviceMessage = new ServiceMessage();
        Message message = new Message(
                tf_titre.getText().trim(),
                tf_description.getText().trim(),
                ta_contenu.getText().trim(),
                dateNow,
                imagePath,
                currentUser // On lie le message à l'utilisateur connecté
        );
        try {
            serviceMessage.ajouter(message);
            showAlert("Succès", "Message ajouté avec succès", Alert.AlertType.INFORMATION);
            clearFields();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout du message: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validerChamps() {
        boolean isValid = true;
        error_titre.setText("");
        error_description.setText("");
        error_contenu.setText("");

        String titre = tf_titre.getText().trim();
        String description = tf_description.getText().trim();
        String contenu = ta_contenu.getText().trim();

        // Vérification et masquage des mots indésirables
        if (contientMotsIndesirables(titre)) {
            error_titre.setText("Le titre contient des mots indésirables.");
            tf_titre.setText(remplacerMotsIndesirables(titre)); // Remplace les mots indésirables par des étoiles
            isValid = false;
        } else if (titre.isEmpty()) {
            error_titre.setText("Le titre est obligatoire.");
            isValid = false;
        } else if (titre.length() > 20) {
            error_titre.setText("Le titre ne doit pas dépasser 20 caractères.");
            isValid = false;
        }

        if (contientMotsIndesirables(description)) {
            error_description.setText("La description contient des mots indésirables.");
            tf_description.setText(remplacerMotsIndesirables(description)); // Remplace les mots indésirables par des étoiles
            isValid = false;
        } else if (description.isEmpty()) {
            error_description.setText("La description est obligatoire.");
            isValid = false;
        } else if (description.length() > 50) {
            error_description.setText("La description ne doit pas dépasser 50 caractères.");
            isValid = false;
        }

        if (contientMotsIndesirables(contenu)) {
            error_contenu.setText("Le contenu contient des mots indésirables.");
            ta_contenu.setText(remplacerMotsIndesirables(contenu)); // Remplace les mots indésirables par des étoiles
            isValid = false;
        } else if (contenu.isEmpty()) {
            error_contenu.setText("Le contenu est obligatoire.");
            isValid = false;
        } else if (contenu.length() > 1000) {
            error_contenu.setText("Le contenu ne doit pas dépasser 1000 caractères.");
            isValid = false;
        }

        return isValid;
    }

    private boolean contientMotsIndesirables(String texte) {
        if (texte == null || texte.isEmpty()) {
            return false;
        }
        String texteLowerCase = texte.toLowerCase(); // Pour rendre la vérification insensible à la casse
        for (String motIndesirable : MOTS_INDESIRABLES) {
            if (texteLowerCase.contains(motIndesirable)) {
                return true;
            }
        }
        return false;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        // Fix taille minimale pour éviter le resize sur certains messages
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    private void clearFields() {
        tf_titre.clear();
        tf_description.clear();
        ta_contenu.clear();
        labelImagePath.setText("");
        error_titre.setText("");
        error_description.setText("");
        error_contenu.setText("");
        imagePath = null;
    }

    // --- Speech-to-Text avec Vosk ---
    @FXML
    public void speechToTextTitre(ActionEvent actionEvent) {
        speechToText(tf_titre);
    }

    @FXML
    public void speechToTextDescription(ActionEvent actionEvent) {
        speechToText(tf_description);
    }

    @FXML
    public void speechToTextContenu(ActionEvent actionEvent) {
        speechToText(ta_contenu);
    }

    private void speechToText(TextInputControl targetField) {
        new Thread(() -> {
            try (Model model = new Model("version_finale/models/fr")) {
                AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
                microphone.open(format);
                microphone.start();
                Recognizer recognizer = new Recognizer(model, 16000.0f);
                byte[] buffer = new byte[4096];
                StringBuilder resultText = new StringBuilder();
                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < 7000) {
                    int bytesRead = microphone.read(buffer, 0, buffer.length);
                    if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                        resultText.append(parseTextFromResult(recognizer.getResult()));
                    }
                }
                resultText.append(parseTextFromResult(recognizer.getFinalResult()));
                microphone.stop();
                microphone.close();
                Platform.runLater(() -> targetField.setText(resultText.toString()));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Erreur", "Erreur reconnaissance vocale: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    private String parseTextFromResult(String jsonResult) {
        if (jsonResult == null || jsonResult.isEmpty()) return "";
        int index = jsonResult.indexOf("\"text\" : ");
        if (index == -1) return "";
        int start = jsonResult.indexOf("\"", index + 8) + 1;
        int end = jsonResult.indexOf("\"", start);
        if (start == 0 || end == -1) return "";
        return jsonResult.substring(start, end) + " ";
    }
}
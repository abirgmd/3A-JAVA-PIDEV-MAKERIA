package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Reply;
import com.abircode.cruddp.services.ServiceReply;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class ModifierReplyController {

    @FXML
    private TextArea contenuReplyField;

    @FXML
    private Text contenuReplyError;

    // Liste des mots indésirables
    private static final List<String> MOTS_INDESIRABLES = Arrays.asList(
            "merde", "raciste", "mauvais", "noir"
    );

    private Stage dialogStage;
    private Reply reply;
    private boolean saved = false;
    private final ServiceReply serviceReply = new ServiceReply();

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setReply(Reply reply) {
        this.reply = reply;
        contenuReplyField.setText(reply.getContenuReply());
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSave() {
        String contenu = contenuReplyField.getText().trim();

        if (contenu.isEmpty()) {
            contenuReplyError.setText("Le contenu de la réponse ne peut pas être vide");
            return;
        }
        if (contenu.length() < 2) {
            contenuReplyError.setText("Le contenu doit avoir au moins 2 caractères.");
            return;
        }
        if (contenu.length() > 100) {
            contenuReplyError.setText("Le contenu doit avoir maximum 100 caractères.");
            return;
        }

        // Vérification des mots indésirables
        if (contientMotsIndesirables(contenu)) {
            contenuReplyField.setText(remplacerMotsIndesirables(contenu));
            contenuReplyError.setText("Le contenu contient des mots inappropriés qui ont été masqués");
            return;
        }

        contenuReplyError.setText("");

        try {
            reply.setContenuReply(contenu);
            reply.setCreatedAt(LocalDateTime.now().toString());
            serviceReply.modifier(reply);
            saved = true;
            dialogStage.close();
        } catch (SQLException e) {
            showAlert("Erreur", "Échec de la modification de la réponse: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
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
package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Reply;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;  // Assurez-vous que cette ligne est présente
import javafx.scene.text.Text;
import javafx.stage.Stage;
import com.abircode.cruddp.services.ServiceReply;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class ModifierReplyController {
    @FXML private TextArea contenuReplyField;  // Vérifiez que c'est un TextArea
    @FXML private Text contenuReplyError;

    private Stage dialogStage;
    private Reply reply;
    private boolean saved = false;
    private final ServiceReply serviceReply = new ServiceReply();

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setReply(Reply reply) {
        this.reply = reply;
        contenuReplyField.setText(reply.getContenuReply());  // Assurez-vous que c'est un TextArea
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSave() {
        if (contenuReplyField.getText().isEmpty()) {
            contenuReplyError.setText("Le contenu de la réponse ne peut pas être vide");
            contenuReplyError.setStyle("-fx-fill: red;");
            return;
        } else if (contenuReplyField.getText().length() < 2) {
            contenuReplyError.setText("Le contenu doit avoir au moins 2 caractères.");
            contenuReplyError.setStyle("-fx-fill: red;");
            return;
        }
        else if (contenuReplyField.getText().length() >100) {
            contenuReplyError.setText("Le contenu doit avoir maximum 100 caractères.");
            contenuReplyError.setStyle("-fx-fill: red;");
            return;
        }

        contenuReplyError.setText("");

        try {
            reply.setContenuReply(contenuReplyField.getText());
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
}

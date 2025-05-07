package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Reclamation;
import com.abircode.cruddp.entities.Reponses;
import com.abircode.cruddp.services.ServiceReponses;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Date;

public class AjouterModifierReponseController {

    @FXML
    private Button btnsubmit;

    @FXML
    private Label ltitre;

    @FXML
    private TextArea tareponse;

    @FXML
    private Label lblError; // <-- Ajoute ce Label dans ton FXML pour afficher les messages

    private Reclamation reclamationContext;
    private Reponses reponseContext;
    private Runnable refreshCallback;
    ServiceReponses serviceReponses = new ServiceReponses();

    public void setContext(String title, String buttonText) {
        ltitre.setText(title);
        btnsubmit.setText(buttonText);
    }

    public void setRefreshCallback(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
    }

    public void setReclamation(Reclamation reclamation) {
        this.reclamationContext = reclamation;
    }

    public void setReponse(Reponses reponse) {
        this.reponseContext = reponse;
        if (reponse != null) {
            tareponse.setText(reponse.getContenuRep());
        }
    }

    @FXML
    void submit(ActionEvent event) {
        if (!controleSaisie()) {
            return;
        }

        String contenu = tareponse.getText().trim();

        try {
            if (reponseContext == null) {
                if (reclamationContext != null) {
                    Reponses newReponse = new Reponses(new Date(), contenu, reclamationContext, 1); // 1 = id_moderateur ?
                    serviceReponses.ajouter(newReponse);
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Réponse ajoutée avec succès.");
                }
            } else {
                reponseContext.setDateRep(new Date());
                reponseContext.setContenuRep(contenu);
                serviceReponses.modifier(reponseContext);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Réponse mise à jour avec succès.");
            }

            if (refreshCallback != null) {
                refreshCallback.run();
            }

            Stage stage = (Stage) btnsubmit.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'enregistrement.");
        }
    }
    private boolean controleSaisie() {
        String contenu = tareponse.getText().trim();
        tareponse.setStyle(""); // reset
        lblError.setText("");

        if (contenu.isEmpty()) {
            tareponse.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            lblError.setText("⚠ Veuillez saisir une réponse.");
            showAlert(Alert.AlertType.WARNING, "Champ manquant", "Le champ de réponse est vide.");
            return false;
        }

        if (contenu.length() < 10) {
            tareponse.setStyle("-fx-border-color: orange; -fx-border-width: 2px;");
            lblError.setText("⚠ La réponse doit contenir au moins 10 caractères.");
            showAlert(Alert.AlertType.WARNING, "Contenu trop court", "La réponse doit contenir au moins 10 caractères.");
            return false;
        }

        tareponse.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
        lblError.setText("");
        return true;
    }
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

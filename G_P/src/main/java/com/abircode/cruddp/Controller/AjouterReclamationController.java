package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Reclamation;
import com.abircode.cruddp.entities.StatutReclamation;
import com.abircode.cruddp.services.ServiceReclamation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class AjouterReclamationController {

    @FXML
    private ComboBox<String> cbtype;

    @FXML
    private TextArea tadesc;

    @FXML
    private Label errorLabel;

    ServiceReclamation serviceReclamation = new ServiceReclamation();
    private Reclamation reclamationContext;
    private Runnable refreshCallback;

    public void setRefreshCallback(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
    }

    public void initialize() {
        List<String> types = List.of("Produit", "Service", "Command");
        cbtype.getItems().addAll(types);

        if (reclamationContext != null) {
            cbtype.setValue(reclamationContext.getType());
            tadesc.setText(reclamationContext.getDescriptionRec());
        }
    }

    public void setReclamation(Reclamation reclamation) {
        this.reclamationContext = reclamation;
        if (reclamation != null) {
            cbtype.setValue(reclamation.getType());
            tadesc.setText(reclamation.getDescriptionRec());
        }
    }

    @FXML
    void ajouterReclamation(ActionEvent event) {
        if (!controleSaisie()) {
            return;
        }



        try {
            if (reclamationContext == null) {
                Reclamation r = new Reclamation();
                r.setType(cbtype.getValue());
                r.setDescriptionRec(tadesc.getText());
                r.setDateRec(new Date());
                r.setId_utilisateur(6);
                r.setStatRec(StatutReclamation.EN_COURS);
                serviceReclamation.ajouter(r);
                showAlert(Alert.AlertType.INFORMATION,"Succès","Réclamation ajoutée avec succès !");
            } else {
                reclamationContext.setType(cbtype.getValue());
                reclamationContext.setDescriptionRec(tadesc.getText());
                reclamationContext.setDateRec(new Date());
                serviceReclamation.modifier(reclamationContext);
                showAlert(Alert.AlertType.INFORMATION,"Mise à jour","Réclamation mise à jour avec succès !");
                if (refreshCallback != null) refreshCallback.run();
                Stage stage = (Stage) tadesc.getScene().getWindow();
                stage.close();
            }



        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,"Erreur","Une erreure est survenue lors de l'enregistrement: "+e.getMessage());
        }
    }
    private boolean controleSaisie(){
        String erros="";
        cbtype.getStyleClass().removeAll("invalid", "valid");
        tadesc.getStyleClass().removeAll("invalid", "valid");

        if (cbtype.getValue() == null || cbtype.getValue().isEmpty()) {
            cbtype.getStyleClass().add("invalid");
            erros+="⚠ Type de réclamation est requis. \n ";

        } else {
            cbtype.getStyleClass().add("valid");
        }

        if (tadesc.getText().trim().isEmpty()) {
            tadesc.getStyleClass().add("invalid");
            erros+=("⚠ La description est vide.\\n");

        }else if(tadesc.getText().length()<10){
            tadesc.getStyleClass().add("invalid");
            erros+=("⚠ La description doit contenir au moins 10 caractères.\n");
        }else {
            tadesc.getStyleClass().add("valid");
        }

        if(erros.length()>0){
            errorLabel.setText("Erreur de saisie");
            showAlert(Alert.AlertType.ERROR,"Erreur de saisie",erros);
            return false;
        }
        errorLabel.setText("");
        return true;

    }
    private void showAlert(Alert.AlertType type,String title,String message){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

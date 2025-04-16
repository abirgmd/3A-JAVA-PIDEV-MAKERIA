package com.abircode.cruddp.Controller;
import com.abircode.cruddp.entities.Reclamation;
import com.abircode.cruddp.entities.Reponses;
import com.abircode.cruddp.entities.StatutReclamation;
import com.abircode.cruddp.services.ServiceReponses;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.abircode.cruddp.services.ServiceReclamation;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

public class AfficherReclamationController {

    @FXML
    private ListView<Reclamation> lvreclamation;

    @FXML
    private Button btnrejete;

    @FXML
    private Button btnrepondre;
    @FXML
    void reclamationselected(MouseEvent event) {
        Reclamation reclamation = lvreclamation.getSelectionModel().getSelectedItem();
        if(reclamation != null && reclamation.getStatRec().equals(StatutReclamation.EN_COURS)) {
            btnrepondre.setDisable(false);
            btnrejete.setDisable(false);
        }else{
            btnrepondre.setDisable(true);
            btnrejete.setDisable(true);
        }
    }

    ServiceReclamation serviceReclamation = new ServiceReclamation();
    ServiceReponses serviceReponses = new ServiceReponses();
    ObservableList<Reclamation> list;

    {
        try {
            list = FXCollections.observableList(serviceReclamation.afficher());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void initialize() {

        lvreclamation.setItems(list);
    }
    @FXML
    void rejeter(ActionEvent event) {
        Reclamation reclamation = lvreclamation.getSelectionModel().getSelectedItem();
        if(reclamation!=null){
            reclamation.setStatRec(StatutReclamation.REJETE);
            try {
                serviceReclamation.modifier(reclamation);
                lvreclamation.refresh();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @FXML
    void repondre(ActionEvent event) {
        Reclamation reclamation = lvreclamation.getSelectionModel().getSelectedItem();
        if (reclamation != null) {
            reclamation.setStatRec(StatutReclamation.RESOLU);
            try {
                serviceReclamation.modifier(reclamation);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/ajouter-modifier-reponse.fxml"));
                Parent root = loader.load();
                AjouterModifierReponseController controller = loader.getController();
                controller.setContext("Répondre à la Réclamation", "Répondre");
                controller.setReclamation(reclamation);
                controller.setRefreshCallback(()->{
                    lvreclamation.refresh();
                });
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Ajouter une Réponse");
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }



}
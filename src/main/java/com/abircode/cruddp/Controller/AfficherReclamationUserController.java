package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Reclamation;
import com.abircode.cruddp.entities.StatutReclamation;
import com.abircode.cruddp.services.ServiceReclamation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class AfficherReclamationUserController {

    @FXML
    private ListView<Reclamation> lvreclamation;

    @FXML
    private Button btnsupp;

    @FXML
    private Button btnmodifier;

    ServiceReclamation serviceReclamation = new ServiceReclamation();
    ObservableList<Reclamation> list;

    public void initialize() {
        try {
            list = FXCollections.observableList(serviceReclamation.afficher());
            lvreclamation.setItems(list);
            btnsupp.setDisable(true);
            btnmodifier.setDisable(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void reclamationselected(MouseEvent event) {
        Reclamation reclamation = lvreclamation.getSelectionModel().getSelectedItem();
        if (reclamation != null && reclamation.getStatRec().equals(StatutReclamation.RESOLU)) {
            btnsupp.setDisable(true);
            btnmodifier.setDisable(true);
        }else if(reclamation != null && reclamation.getStatRec().equals(StatutReclamation.REJETE)){
            btnsupp.setDisable(false);
            btnmodifier.setDisable(true);
        }
        else {
            btnsupp.setDisable(false);
            btnmodifier.setDisable(false);
        }
    }

    @FXML
    void supprimer(ActionEvent event) {
        Reclamation reclamation = lvreclamation.getSelectionModel().getSelectedItem();
        if (reclamation != null) {
            try {
                serviceReclamation.supprimer(reclamation.getId());
                list.remove(reclamation);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void modifier(ActionEvent event) {
        Reclamation reclamation = lvreclamation.getSelectionModel().getSelectedItem();
        if (reclamation != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/ajouter-reclamation.fxml"));
                Parent root = loader.load();
                AjouterReclamationController controller = loader.getController();

                controller.setReclamation(reclamation);
                controller.setRefreshCallback(()->{
                    lvreclamation.refresh();
                });
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Modifier la Réclamation");
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}

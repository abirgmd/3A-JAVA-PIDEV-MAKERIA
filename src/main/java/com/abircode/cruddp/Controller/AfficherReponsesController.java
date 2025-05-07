package com.abircode.cruddp.Controller;
import com.abircode.cruddp.entities.Reponses;
import com.abircode.cruddp.services.ServiceReponses;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class AfficherReponsesController {

    @FXML
    private ListView<Reponses> lvreponse;
    ServiceReponses serviceReponses = new ServiceReponses();
    ObservableList<Reponses> list;

    public void initialize() {
        try {
            list = FXCollections.observableList(serviceReponses.afficher());
            lvreponse.setItems(list);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    void modifier(ActionEvent event) {
        Reponses selected = lvreponse.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/ajouter-modifier-reponse.fxml"));
                Parent root = loader.load();
                AjouterModifierReponseController controller = loader.getController();
                controller.setContext("Modifier la Réponse", "Modifier");
                controller.setReponse(selected);
                controller.setRefreshCallback(()->{
                    lvreponse.refresh();
                });
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Modifier la Réponse");
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println( "Please select a response to modify.");
        }
    }

    @FXML
    void supprimer(ActionEvent event) {
        Reponses selected = lvreponse.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                serviceReponses.supprimer(selected.getId());
                lvreponse.refresh();
            } catch (SQLException e) {
                System.out.println( e.getMessage());
            }
        } else {
            System.out.println( "Please select a response to delete.");
        }
    }
    @FXML
    void stat(ActionEvent event) {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/reclamation-stats.fxml"));
                Parent root = loader.load();

                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Stat");
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

    }

}
package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Reclamation;
import com.abircode.cruddp.entities.StatutReclamation;
import com.abircode.cruddp.services.ServiceReclamation;
import com.abircode.cruddp.utils.ReclamationClassifier;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AfficherReclamationController implements Initializable {
    @FXML private FlowPane reclamationCardsContainer;
    @FXML private Button btnrejete;
    @FXML private Button btnrepondre;
    @FXML private TextField tfrecherche;
    @FXML private ComboBox<String> cbtri;

    private final ServiceReclamation serviceReclamation = new ServiceReclamation();
    private final ReclamationClassifier classifier = new ReclamationClassifier();
    private List<Reclamation> originalList;
    private Reclamation selectedReclamation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            originalList = serviceReclamation.afficher();
        } catch (SQLException e) {
            originalList = List.of();
        }
        btnrejete.setDisable(true);
        btnrepondre.setDisable(true);
        cbtri.setItems(FXCollections.observableArrayList(
                "Date ↑","Date ↓","Type A→Z","Type Z→A",
                "Statut A→Z","Statut Z→A","Urgence H→L","Urgence L→H"
        ));
        tfrecherche.textProperty().addListener((obs,old,val)->updateView());
        cbtri.valueProperty().addListener((obs,old,val)->updateView());
        updateView();
    }

    private void updateView() {
        String filter = tfrecherche.getText()==null?"":tfrecherche.getText().toLowerCase();
        List<Reclamation> filtered = originalList.stream().filter(rec->{
            if(filter.isEmpty())return true;
            if(rec.getType()!=null&&rec.getType().toLowerCase().contains(filter))return true;
            if(rec.getDescriptionRec()!=null&&rec.getDescriptionRec().toLowerCase().contains(filter))return true;
            if(rec.getUser()!=null&&rec.getUser().getName()!=null&&rec.getUser().getName().toLowerCase().contains(filter))return true;
            if(rec.getStatRec()!=null&&rec.getStatRec().name().toLowerCase().contains(filter))return true;
            if(rec.getDateRec()!=null&&rec.getDateRec().toString().toLowerCase().contains(filter))return true;
            String urg=classifier.classifyUrgency(rec.getDescriptionRec());
            if(urg.toLowerCase().contains(filter))return true;
            return false;
        }).collect(Collectors.toList());
        String sortKey=cbtri.getValue();
        if(sortKey!=null){
            switch(sortKey){
                case "Date ↑"->filtered.sort(Comparator.comparing(Reclamation::getDateRec));
                case "Date ↓"->filtered.sort(Comparator.comparing(Reclamation::getDateRec).reversed());
                case "Type A→Z"->filtered.sort(Comparator.comparing(Reclamation::getType,String.CASE_INSENSITIVE_ORDER));
                case "Type Z→A"->filtered.sort(Comparator.comparing(Reclamation::getType,String.CASE_INSENSITIVE_ORDER).reversed());
                case "Statut A→Z"->filtered.sort(Comparator.comparing(r->r.getStatRec().name(),String.CASE_INSENSITIVE_ORDER));
                case "Statut Z→A"->filtered.sort(Comparator.comparing((Reclamation r)->r.getStatRec().name(),String.CASE_INSENSITIVE_ORDER).reversed());
                case "Urgence H→L"->filtered.sort(Comparator.comparing(r->classifier.classifyUrgency(r.getDescriptionRec()),Comparator.reverseOrder()));
                case "Urgence L→H"->filtered.sort(Comparator.comparing(r->classifier.classifyUrgency(r.getDescriptionRec())));
            }
        }
        reclamationCardsContainer.getChildren().clear();
        selectedReclamation=null;
        btnrejete.setDisable(true);
        btnrepondre.setDisable(true);
        for (Reclamation rec : filtered) {
            VBox card = new VBox(5);
            card.getStyleClass().add("reclamation-card");

            Label titre = new Label("Type: " + rec.getType());
            Label desc  = new Label("Desc: " + rec.getDescriptionRec());

            String urg = classifier.classifyUrgency(rec.getDescriptionRec());
            Label urgence = new Label("Urgence: " + urg);


            String color;
            switch (urg) {
                case "HIGH"   -> color = "red";
                case "MEDIUM" -> color = "orange";
                case "LOW"    -> color = "green";
                default       -> color = "black";
            }
            urgence.setStyle("-fx-text-fill: " + color + ";");

            card.getChildren().addAll(titre, desc, urgence);

            card.setOnMouseClicked(evt -> {
                reclamationCardsContainer.getChildren().forEach(n -> n.getStyleClass().remove("selected-card"));
                card.getStyleClass().add("selected-card");
                selectedReclamation = rec;
                boolean inProgress = rec.getStatRec() == StatutReclamation.EN_COURS;
                btnrejete .setDisable(!inProgress);
                btnrepondre.setDisable(!inProgress);
            });

            reclamationCardsContainer.getChildren().add(card);
        }

    }

    @FXML
    private void rejeter(ActionEvent event) {
        if(selectedReclamation!=null){
            selectedReclamation.setStatRec(StatutReclamation.REJETE);
            try{serviceReclamation.modifier(selectedReclamation);}catch(SQLException ignored){}
            updateView();
        }
    }

    @FXML
    private void repondre(ActionEvent event) {
        if(selectedReclamation!=null){
            selectedReclamation.setStatRec(StatutReclamation.RESOLU);
            try{serviceReclamation.modifier(selectedReclamation);}catch(SQLException ignored){}
            try{
                FXMLLoader loader=new FXMLLoader(getClass().getResource("/Fxml/ajouter-modifier-reponse.fxml"));
                Parent root=loader.load();
                com.abircode.cruddp.Controller.AjouterModifierReponseController ctrl=loader.getController();
                ctrl.setContext("Répondre à la Réclamation","Répondre");
                ctrl.setReclamation(selectedReclamation);
                ctrl.setRefreshCallback(this::updateView);
                Stage stage=new Stage();
                stage.setScene(new Scene(root));
                stage.getScene().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                stage.show();
            }catch(IOException ignored){}
        }
    }
}

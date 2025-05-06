package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Event;
import com.abircode.cruddp.services.ServiceEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class CalendrierController {

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Label monthLabel;

    @FXML
    private VBox eventsVBox;  // Un VBox pour afficher les événements cliqués

    private ServiceEvent serviceEvent;
    private YearMonth currentMonth;

    public CalendrierController() {
        serviceEvent = new ServiceEvent();
        currentMonth = YearMonth.now();
    }

    @FXML
    public void initialize() {
        afficherCalendrier(currentMonth);
    }

    // Méthode pour afficher le calendrier pour le mois donné
    private void afficherCalendrier(YearMonth month) {
        calendarGrid.getChildren().clear(); // Effacer le mois précédent
        monthLabel.setText(month.getMonth() + " " + month.getYear());

        // Récupérer les événements pour ce mois
        List<Event> events = getEventsForMonth(month);

        // Affichage des jours du mois
        int firstDayOfMonth = month.atDay(1).getDayOfWeek().getValue();
        int daysInMonth = month.lengthOfMonth();

        // Placer chaque jour dans le GridPane
        for (int day = 1; day <= daysInMonth; day++) {
            int col = (firstDayOfMonth + day - 1) % 7;
            int row = (firstDayOfMonth + day - 1) / 7;

            Button dayButton = new Button(String.valueOf(day));
            dayButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

            // Ajouter des événements au jour si nécessaire
            for (Event event : events) {
                if (event.getDate_debut().toLocalDate().equals(month.atDay(day))) {
                    // Changer la couleur du bouton si un événement est présent
                    dayButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #0984e3;");
                    dayButton.setOnAction(e -> showEventDetails(event));  // Afficher les événements associés
                }
            }

            calendarGrid.add(dayButton, col, row);
        }
    }

    // Méthode pour récupérer les événements pour un mois donné
    private List<Event> getEventsForMonth(YearMonth month) {
        try {
            List<Event> events = serviceEvent.afficher();
            events.removeIf(event -> event.getDate_debut().getMonth() != month.getMonth());
            return events;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Méthode pour afficher les détails des événements lorsqu'un jour est cliqué
    private void showEventDetails(Event event) {
        // Effacer les anciens événements affichés
        eventsVBox.getChildren().clear();

        // Créer un label avec le titre de l'événement et ajouter à VBox
        Label eventTitle = new Label("Titre de l'événement : " + event.getTitreevents());
        eventTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #b11d85;");
        eventsVBox.getChildren().add(eventTitle);

        // Ajouter d'autres informations si nécessaire
        Label eventDescription = new Label("Description : " + event.getDescriptionevents());
        eventDescription.setStyle("-fx-font-size: 14px; -fx-text-fill: #b11d85;");
        eventsVBox.getChildren().add(eventDescription);

        // Ajouter d'autres informations si nécessaire
        Label eventDate1 = new Label("Date début : " + event.getDate_debut());
        eventDate1.setStyle("-fx-font-size: 14px; -fx-text-fill: #b11d85;");
        eventsVBox.getChildren().add(eventDate1);
        // Ajouter d'autres informations si nécessaire
        Label eventDate2 = new Label("Date fin : " + event.getDate_fin());
        eventDate2.setStyle("-fx-font-size: 14px; -fx-text-fill: #b11d85;");
        eventsVBox.getChildren().add(eventDate2);

    }

    // Méthode pour passer au mois suivant
    public void nextMonth(ActionEvent actionEvent) {
        currentMonth = currentMonth.plusMonths(1);  // Passer au mois suivant
        afficherCalendrier(currentMonth);  // Mettre à jour le calendrier
    }

    // Méthode pour revenir au mois précédent
    public void previousMonth(ActionEvent actionEvent) {
        currentMonth = currentMonth.minusMonths(1);  // Passer au mois précédent
        afficherCalendrier(currentMonth);  // Mettre à jour le calendrier
    }

    // Méthode pour gérer le clic sur le bouton de retour
    public void handleReturnButtonClick(ActionEvent actionEvent) {
        Stage stage = (Stage) calendarGrid.getScene().getWindow();
        stage.close();  // Fermer la fenêtre actuelle
    }
}

package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Event;
import com.abircode.cruddp.utils.DBConnexion;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.ColumnConstraints;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class EventMController implements Initializable {

    @FXML
    private GridPane eventGrid;
    @FXML
    private HBox paginationContainer;
    @FXML
    private TextField searchField;

    private List<Event> allEvents = new ArrayList<>();
    private List<Event> filteredEvents = new ArrayList<>();
    private static final int MAX_EVENTS_PAR_LIGNE = 3;
    private int currentPage = 1;
    private int itemsPerPage = 6;
    private MainController mainController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configuration du GridPane
        eventGrid.setPrefWidth(900);

        // Configuration des colonnes
        for (int i = 0; i < MAX_EVENTS_PAR_LIGNE; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / MAX_EVENTS_PAR_LIGNE);
            eventGrid.getColumnConstraints().add(column);
        }

        loadEvents();
        setupPagination();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void loadEvents() {
        allEvents = getAllEventsFromDB();
        filteredEvents = new ArrayList<>(allEvents);
        displayEvents();
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        filteredEvents = allEvents.stream()
                .filter(event -> event.getTitreevents().toLowerCase().contains(searchText) ||
                        event.getDescriptionevents().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
        currentPage = 1;
        displayEvents();
        setupPagination();
    }

    private void displayEvents() {
        eventGrid.getChildren().clear();
        int col = 0, row = 0;

        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredEvents.size());

        for (int i = startIndex; i < endIndex; i++) {
            Event event = filteredEvents.get(i);
            VBox eventCard = createEventCard(event);

            eventGrid.add(eventCard, col, row);
            col++;
            if (col == MAX_EVENTS_PAR_LIGNE) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createEventCard(Event event) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-radius: 8; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefWidth(270);
        card.setMaxWidth(270);

        // Image de l'événement
        ImageView imageView = new ImageView();
        try {
            Image image = new Image(event.getImage1events());
            imageView.setImage(image);
            imageView.setFitWidth(220);
            imageView.setFitHeight(200);
            imageView.setPreserveRatio(true);
        } catch (Exception e) {
            imageView.setFitWidth(220);
            imageView.setFitHeight(200);
        }

        // Informations de l'événement
        VBox infoBox = new VBox(8);
        infoBox.setStyle("-fx-padding: 10 0 0 0;");

        Label titreLabel = new Label(event.getTitreevents());
        titreLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: black;");

        Label dateDebutLabel = new Label("Début: " + formatDate(event.getDate_debut()));
        dateDebutLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #b11d85; -fx-font-weight: bold;");

        Label dateFinLabel = new Label("Fin: " + formatDate(event.getDate_fin()));
        dateFinLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #b11d85; -fx-font-weight: bold;");

        Label descriptionLabel = new Label(event.getDescriptionevents());
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: black;");
        descriptionLabel.setWrapText(true);

        // Bouton More
        Button moreButton = new Button("More Details");
        moreButton.setStyle("-fx-background-color: #b11d85; -fx-text-fill: white; -fx-font-size: 12px; " +
                "-fx-background-radius: 4; -fx-padding: 8 16;");
        moreButton.setOnAction(e -> showEventDetails(event));

        infoBox.getChildren().addAll(titreLabel, dateDebutLabel, dateFinLabel, descriptionLabel, moreButton);
        card.getChildren().addAll(imageView, infoBox);
        card.setPadding(new Insets(15));

        return card;
    }

    @FXML
    private void showEventDetails(Event event) {
        try {
            // Charger l'FXML du formulaire de commentaires
            FXMLLoader commentLoader = new FXMLLoader(getClass().getResource("/Fxml/commentForm.fxml"));
            Parent commentRoot = commentLoader.load();

            // Obtenez le contrôleur du formulaire de commentaires et passez l'événement
            CommentFormController commentController = commentLoader.getController();
            commentController.setEvent(event); // Passez l'événement au contrôleur du formulaire de commentaires

            // Créez une nouvelle scène pour le formulaire de commentaires
            Scene commentScene = new Scene(commentRoot);
            Stage commentStage = new Stage();
            commentStage.setTitle("Event Details - Comment Section");

            // Définir la scène et afficher la fenêtre
            commentStage.setScene(commentScene);
            commentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load event details: " + e.getMessage());
        }
    }

    private String formatDate(LocalDateTime date) {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private void setupPagination() {
        paginationContainer.getChildren().clear();
        int totalPages = (int) Math.ceil(filteredEvents.size() / (double) itemsPerPage);

        if (currentPage > 1) {
            Button prevBtn = new Button("Précédent");
            prevBtn.setStyle("-fx-background-color: #b11d85; -fx-text-fill: white; -fx-background-radius: 4; " +
                    "-fx-padding: 8 16; -fx-font-size: 12px;");
            prevBtn.setOnAction(e -> {
                currentPage--;
                displayEvents();
                setupPagination();
            });
            paginationContainer.getChildren().add(prevBtn);
        }

        for (int i = 1; i <= totalPages; i++) {
            Button pageBtn = new Button(String.valueOf(i));
            if (i == currentPage) {
                pageBtn.setStyle("-fx-background-color: #b11d85; -fx-text-fill: white; -fx-background-radius: 4; " +
                        "-fx-padding: 8 16; -fx-font-size: 12px;");
            } else {
                pageBtn.setStyle("-fx-background-color: white; -fx-text-fill: #b11d85; -fx-background-radius: 4; " +
                        "-fx-padding: 8 16; -fx-font-size: 12px; -fx-border-color: #b11d85;");
            }
            final int page = i;
            pageBtn.setOnAction(e -> {
                currentPage = page;
                displayEvents();
                setupPagination();
            });
            paginationContainer.getChildren().add(pageBtn);
        }

        if (currentPage < totalPages) {
            Button nextBtn = new Button("Suivant");
            nextBtn.setStyle("-fx-background-color: #b11d85; -fx-text-fill: white; -fx-background-radius: 4; " +
                    "-fx-padding: 8 16; -fx-font-size: 12px;");
            nextBtn.setOnAction(e -> {
                currentPage++;
                displayEvents();
                setupPagination();
            });
            paginationContainer.getChildren().add(nextBtn);
        }
    }

    private List<Event> getAllEventsFromDB() {
        List<Event> events = new ArrayList<>();
        String query = "SELECT * FROM event";

        try (Connection con = DBConnexion.getCon();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Event event = new Event();
                event.setId(rs.getInt("id"));
                event.setTitreevents(rs.getString("titreevents"));
                event.setDate_debut(rs.getTimestamp("date_debut").toLocalDateTime());
                event.setDate_fin(rs.getTimestamp("date_fin").toLocalDateTime());
                event.setDescriptionevents(rs.getString("descriptionevents"));
                event.setImage1events(rs.getString("image1events"));
                event.setImage2events(rs.getString("image2events"));
                events.add(event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return events;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);  // You can add a header text if needed
        alert.setContentText(message);
        alert.showAndWait();
    }
}

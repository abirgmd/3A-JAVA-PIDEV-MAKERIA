package com.abircode.cruddp.Controller;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
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
    @FXML
    private VBox participationVBox;  // Conteneur pour afficher les participations

    private List<Event> allEvents = new ArrayList<>();
    private List<Event> filteredEvents = new ArrayList<>();
    private List<Event> participatedEvents = new ArrayList<>();  // Liste pour stocker les participations
    private static final int MAX_EVENTS_PAR_LIGNE = 3;
    private int currentPage = 1;
    private int itemsPerPage = 6;
    private MainController mainController;
    public static final String ACCOUNT_SID = "AC2fe478193421515271b24e3a336c5e46";
    public static final String AUTH_TOKEN = "ffbbe3c42a968c6c0d6df913af6cb998";
    public static final String TWILIO_PHONE_NUMBER = "+15414398064";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        eventGrid.setPrefWidth(900);

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



    private void displayEvents() {
        // Efface les événements actuellement affichés
        eventGrid.getChildren().clear();

        if (filteredEvents.isEmpty()) {
            // Affiche un message si aucun événement n'est trouvé
            Label noResultsLabel = new Label("Aucun événement trouvé pour votre recherche.");
            noResultsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: gray; -fx-alignment: center;");
            eventGrid.add(noResultsLabel, 0, 0);
            GridPane.setColumnSpan(noResultsLabel, MAX_EVENTS_PAR_LIGNE); // Étendre sur toutes les colonnes
            return;
        }

        int col = 0, row = 0;

        // Calcul des index pour la pagination
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredEvents.size());

        // Boucle pour afficher les événements dans la plage définie
        for (int i = startIndex; i < endIndex; i++) {
            Event event = filteredEvents.get(i);

            // Création de la carte d'événement
            VBox eventCard = createEventCard(event);

            // Ajoute la carte au GridPane
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
        card.setPrefWidth(430);
        card.setMaxWidth(430);

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

        Button moreButton = new Button("Ajouter commantaire");
        moreButton.setStyle("-fx-background-color: #b11d85; -fx-text-fill: white; -fx-font-size: 12px; " +
                "-fx-background-radius: 4; -fx-padding: 8 16;");
        moreButton.setOnAction(e -> showEventDetails(event));
        Button participateButton = new Button("Participer");
        participateButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px; " +
                "-fx-background-radius: 4; -fx-padding: 8 16;");
        participateButton.setOnAction(e -> addToParticipation(event));

        infoBox.getChildren().addAll(titreLabel, dateDebutLabel, dateFinLabel, descriptionLabel, moreButton, participateButton);

        card.getChildren().addAll(imageView, infoBox);
        card.setPadding(new Insets(15));

        return card;
    }

    private void addToParticipation(Event event) {
        if (!participatedEvents.contains(event)) {
            participatedEvents.add(event);
            sendParticipationSMS(event);

            displayParticipations();  // Mettre à jour la liste des participations affichées
        } else {
        }
    }
    private void sendParticipationSMS(Event event) {
        try {
            // Initialisation de Twilio avec votre SID et Auth Token
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

            // Le numéro de téléphone de l'utilisateur doit être récupéré depuis la base de données
            String userPhoneNumber = getPhoneNumberForCurrentUser();  // Implémentez cette méthode pour récupérer le numéro du participant

            // Message à envoyer
            String messageBody = String.format("Vous participez à l'événement '%s'.\nDébut: %s\nFin: %s",
                    event.getTitreevents(), formatDate(event.getDate_debut()), formatDate(event.getDate_fin()));

            // Envoi du SMS via Twilio
            Message message = Message.creator(
                    new PhoneNumber(userPhoneNumber),  // Numéro de téléphone du participant
                    new PhoneNumber(TWILIO_PHONE_NUMBER),  // Votre numéro Twilio
                    messageBody  // Contenu du message
            ).create();

            System.out.println("Message envoyé avec succès à " + userPhoneNumber);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur s'est produite lors de l'envoi du SMS.");
        }
    }

    // Méthode pour récupérer le numéro de téléphone de l'utilisateur
    private String getPhoneNumberForCurrentUser() {
        // Assurez-vous de récupérer le numéro du participant de manière appropriée (ici, on suppose que c'est le numéro de l'utilisateur courant)
        String phoneNumber = "+21654649999"; // Remplacez cette ligne par la logique pour récupérer le numéro depuis la base de données
        return phoneNumber;
    }

    private void showEventDetails(Event event) {
        try {
            FXMLLoader commentLoader = new FXMLLoader(getClass().getResource("/Fxml/commentForm.fxml"));
            Parent commentRoot = commentLoader.load();

            CommentFormController commentController = commentLoader.getController();
            commentController.setEvent(event);

            Scene commentScene = new Scene(commentRoot);
            Stage commentStage = new Stage();
            commentStage.setTitle("Event Details - Comment Section");

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
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void displayParticipations() {
        participationVBox.getChildren().clear();

        for (Event event : participatedEvents) {
            VBox participationCard = createParticipationCard(event);
            participationVBox.getChildren().add(participationCard);
        }
    }

    private VBox createParticipationCard(Event event) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #e381dc; -fx-padding: 15; -fx-border-radius: 8; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefWidth(20);

        Label titreLabel = new Label(event.getTitreevents());
        titreLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: black;");

        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 8px; " +
                "-fx-background-radius: 4; -fx-padding: 8 16;");
        deleteButton.setOnAction(e -> removeFromParticipation(event));

        card.getChildren().addAll(titreLabel, deleteButton);
        return card;
    }

    private void removeFromParticipation(Event event) {
        participatedEvents.remove(event);
        displayParticipations();  // Mettre à jour l'affichage
    }
    @FXML
    private void handleSearch() {
        // Récupérer le texte de recherche et le convertir en minuscules
        String searchText = searchField.getText().trim().toLowerCase();

        if (searchText.isEmpty()) {
            // Si le champ de recherche est vide, afficher tous les événements
            filteredEvents = new ArrayList<>(allEvents);
        } else {
            // Filtrer les événements en fonction du texte saisi
            filteredEvents = allEvents.stream()
                    .filter(event -> event.getTitreevents().toLowerCase().contains(searchText) ||
                            event.getDescriptionevents().toLowerCase().contains(searchText))
                    .collect(Collectors.toList());
        }

        // Réinitialiser à la première page
        currentPage = 1;

        // Mettre à jour l'affichage des événements et la pagination
        displayEvents();
        setupPagination();
    }


}

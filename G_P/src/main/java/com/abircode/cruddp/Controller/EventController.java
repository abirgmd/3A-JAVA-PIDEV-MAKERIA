package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Event;
import com.abircode.cruddp.utils.DBConnexion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;
import java.util.Optional;


public class EventController implements Initializable {

    private Connection con = null;
    private PreparedStatement st = null;
    private ResultSet rs = null;

    @FXML private TextField searchField;
    @FXML private TableView<Event> tablep;
    @FXML private FlowPane eventCardsContainer;
    @FXML private StackPane formContainer;
    @FXML private VBox addEventForm;
    @FXML private VBox updateEventForm;

    // Champs pour l'ajout
    @FXML private TextField titreEvent, descriptionEvent, image1Event, image2Event;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;



    // Champs pour la mise à jour
    @FXML private TextField updateTitreEvent, updateDescriptionEvent, updateImage1Event, updateImage2Event;
    @FXML private DatePicker updateDateDebutPicker;
    @FXML private DatePicker updateDateFinPicker;
    private Event selectedEvent;

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean validateInputs(String titre, String description, String image1, String image2, String dateDebutStr, String dateFinStr) {
        if (titre == null || titre.trim().length() < 3) {
            showAlert("Erreur de saisie", "Le titre doit contenir au moins 3 caractères");
            return false;
        }

        if (description == null || description.trim().length() < 10) {
            showAlert("Erreur de saisie", "La description doit contenir au moins 10 caractères");
            return false;
        }

        if (image1 == null || image1.trim().isEmpty()) {
            showAlert("Erreur de saisie", "L'image 1 est obligatoire");
            return false;
        }

        if (image2 == null || image2.trim().isEmpty()) {
            showAlert("Erreur de saisie", "L'image 2 est obligatoire");
            return false;
        }

        if (dateDebutStr == null || dateDebutStr.trim().isEmpty()) {
            showAlert("Erreur de saisie", "La date de début est obligatoire");
            return false;
        }

        if (dateFinStr == null || dateFinStr.trim().isEmpty()) {
            showAlert("Erreur de saisie", "La date de fin est obligatoire");
            return false;
        }

        // Vérification du format et de la cohérence des dates
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime debut = LocalDateTime.parse(dateDebutStr, formatter);
            LocalDateTime fin = LocalDateTime.parse(dateFinStr, formatter);

            if (debut.isAfter(fin)) {
                showAlert("Erreur de date", "La date de début doit être avant la date de fin");
                return false;
            }

            if (fin.isBefore(debut)) {
                showAlert("Erreur de date", "La date de fin doit être après la date de début");
                return false;
            }

        } catch (DateTimeParseException e) {
            showAlert("Format de date invalide", "Le format des dates doit être yyyy-MM-dd HH:mm");
            return false;
        }

        return true;
    }

    @FXML
    void createEvent(ActionEvent event) {
        String titre = titreEvent.getText();
        String description = descriptionEvent.getText();
        String image1 = image1Event.getText();
        String image2 = image2Event.getText();

        String debut = dateDebutPicker.getValue() != null ? dateDebutPicker.getValue().atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
        String fin = dateFinPicker.getValue() != null ? dateFinPicker.getValue().atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";


        if (!validateInputs(titre, description, image1, image2, debut, fin)) {
            return;
        }

        String insert = "INSERT INTO event (titreevents, descriptionevents, image1events, image2events, date_debut, date_fin) VALUES (?, ?, ?, ?, ?, ?)";
        con = DBConnexion.getCon();
        try {
            st = con.prepareStatement(insert);
            st.setString(1, titre);
            st.setString(2, description);
            st.setString(3, image1);
            st.setString(4, image2);
            st.setString(5, debut);
            st.setString(6, fin);
            st.executeUpdate();
            showEvents();
            clearFields();
            hideForms();

            // Afficher un message de succès
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Succès");
            successAlert.setHeaderText(null);
            successAlert.setContentText("Événement ajouté avec succès!");
            successAlert.showAndWait();
        } catch (SQLException e) {
            System.err.println("Error while adding the event: " + e.getMessage());
            showAlert("Erreur", "Une erreur est survenue lors de l'ajout de l'événement");
        }
    }

    @FXML
    void showUpdateForm(Event event) {
        selectedEvent = event;
        updateTitreEvent.setText(event.getTitreevents());
        updateDescriptionEvent.setText(event.getDescriptionevents());
        updateImage1Event.setText(event.getImage1events());
        updateImage2Event.setText(event.getImage2events());
        updateDateDebutPicker.setValue(event.getDate_debut().toLocalDate());
        updateDateFinPicker.setValue(event.getDate_fin().toLocalDate());


        addEventForm.setVisible(false);
        updateEventForm.setVisible(true);
        formContainer.setVisible(true);
    }

    @FXML
    void updateEvent(ActionEvent event) {
        if (selectedEvent != null) {
            String titre = updateTitreEvent.getText();
            String description = updateDescriptionEvent.getText();
            String image1 = updateImage1Event.getText();
            String image2 = updateImage2Event.getText();
            String debut = updateDateDebutPicker.getValue() != null ? updateDateDebutPicker.getValue().atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
            String fin = updateDateFinPicker.getValue() != null ? updateDateFinPicker.getValue().atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";


            if (!validateInputs(titre, description, image1, image2, debut, fin)) {
                return;
            }

            String update = "UPDATE event SET titreevents = ?, descriptionevents = ?, image1events = ?, image2events = ?, date_debut = ?, date_fin = ? WHERE id = ?";
            con = DBConnexion.getCon();
            try {
                st = con.prepareStatement(update);
                st.setString(1, titre);
                st.setString(2, description);
                st.setString(3, image1);
                st.setString(4, image2);
                st.setString(5, debut);
                st.setString(6, fin);
                st.setInt(7, selectedEvent.getId());
                st.executeUpdate();
                showEvents();
                hideForms();

                // Afficher un message de succès
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Succès");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Événement mis à jour avec succès!");
                successAlert.showAndWait();
            } catch (SQLException e) {
                System.err.println("Error while updating the event: " + e.getMessage());
                showAlert("Erreur", "Une erreur est survenue lors de la mise à jour de l'événement");
            }
        }
    }



// ...

    @FXML
    void deleteEvent(Event event) {
        if (event != null) {
            // Confirmation avant suppression
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmation");
            confirmAlert.setHeaderText("Supprimer l'événement");
            confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cet événement ?");

            // Version corrigée
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                String delete = "DELETE FROM event WHERE id = ?";
                con = DBConnexion.getCon();
                try {
                    st = con.prepareStatement(delete);
                    st.setInt(1, event.getId());
                    st.executeUpdate();
                    showEvents();

                    // Afficher un message de succès
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Succès");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Événement supprimé avec succès!");
                    successAlert.showAndWait();
                } catch (SQLException e) {
                    System.err.println("Error while deleting the event: " + e.getMessage());
                    showAlert("Erreur", "Une erreur est survenue lors de la suppression de l'événement");
                }
            }
        }
    }

    void showEvents() {
        ObservableList<Event> events = getEvents();
        displayEventCards(events);
    }

    private void displayEventCards(ObservableList<Event> events) {
        eventCardsContainer.getChildren().clear();
        eventCardsContainer.setHgap(10);
        eventCardsContainer.setVgap(10);
        eventCardsContainer.setPrefWrapLength(1000);

        int count = 0;
        HBox rowContainer = new HBox(10);
        rowContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        for (Event event : events) {
            BorderPane card = createEventCard(event);
            rowContainer.getChildren().add(card);
            count++;

            if (count % 2 == 0) {
                eventCardsContainer.getChildren().add(rowContainer);
                rowContainer = new HBox(10);
            }
        }

        if (!rowContainer.getChildren().isEmpty()) {
            eventCardsContainer.getChildren().add(rowContainer);
        }
    }

    private BorderPane createEventCard(Event event) {
        BorderPane card = new BorderPane();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3); -fx-padding: 10;");

        VBox imageContainer = new VBox();
        imageContainer.setPrefWidth(100);
        imageContainer.setPrefHeight(180);
        imageContainer.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 5;");
        imageContainer.setAlignment(javafx.geometry.Pos.CENTER);
        ImageView imageView = new ImageView();
        imageView.setFitWidth(90);
        imageView.setFitHeight(90);
        imageView.setPreserveRatio(true);

        try {
            if (event.getImage1events() != null && !event.getImage1events().isEmpty()) {
                Image image = new Image(event.getImage1events());
                imageView.setImage(image);
            } else {
                Image defaultImage = new Image(getClass().getResourceAsStream("/images/default-event.png"));
                imageView.setImage(defaultImage);
            }
        } catch (Exception e) {
            imageView.setStyle("-fx-background-color: #e0e0e0;");
            Label placeholderLabel = new Label("No Image");
            placeholderLabel.setStyle("-fx-text-fill: #888888;");
            imageContainer.getChildren().add(placeholderLabel);
        }

        imageContainer.getChildren().add(imageView);

        VBox detailsBox = new VBox(5);
        detailsBox.setPadding(new Insets(10));
        detailsBox.setSpacing(5);

        Label titleLabel = new Label(event.getTitreevents());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-wrap-text: true; -fx-max-width: 200px; -fx-text-fill: #b11d85;");
        titleLabel.setWrapText(true);

        Label descriptionLabel = new Label(event.getDescriptionevents());
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(200);

        Label dateDebutLabel = new Label("Start: " + event.getDate_debut().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        dateDebutLabel.setStyle("-fx-font-weight: bold;");

        Label dateFinLabel = new Label("End: " + event.getDate_fin().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        dateFinLabel.setStyle("-fx-font-weight: bold;");

        detailsBox.getChildren().addAll(titleLabel, descriptionLabel, dateDebutLabel, dateFinLabel);

        VBox buttonsBox = new VBox(10);
        buttonsBox.setPadding(new Insets(10));
        buttonsBox.setAlignment(javafx.geometry.Pos.CENTER);

        Button updateButton = new Button("Update");
        updateButton.setStyle("-fx-background-color: #b11d85; -fx-text-fill: white;");
        updateButton.setOnAction(e -> showUpdateForm(event));

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #0A2647; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> deleteEvent(event));

        buttonsBox.getChildren().addAll(updateButton, deleteButton);

        card.setLeft(imageContainer);
        card.setCenter(detailsBox);
        card.setRight(buttonsBox);

        BorderPane.setAlignment(detailsBox, javafx.geometry.Pos.TOP_CENTER);
        BorderPane.setAlignment(buttonsBox, javafx.geometry.Pos.CENTER);

        return card;
    }

    private ObservableList<Event> getEvents() {
        ObservableList<Event> events = FXCollections.observableArrayList();
        String query = "SELECT * FROM event";
        con = DBConnexion.getCon();
        try {
            st = con.prepareStatement(query);
            rs = st.executeQuery();
            while (rs.next()) {
                Event event = new Event();
                event.setId(rs.getInt("id"));
                event.setTitreevents(rs.getString("titreevents"));
                event.setDescriptionevents(rs.getString("descriptionevents"));
                event.setImage1events(rs.getString("image1events"));
                event.setImage2events(rs.getString("image2events"));
                event.setDate_debut(rs.getTimestamp("date_debut").toLocalDateTime());
                event.setDate_fin(rs.getTimestamp("date_fin").toLocalDateTime());
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("Error while retrieving events: " + e.getMessage());
            showAlert("Erreur", "Une erreur est survenue lors de la récupération des événements");
        }
        return events;
    }

    void clearFields() {
        if (titreEvent != null) titreEvent.clear();
        if (descriptionEvent != null) descriptionEvent.clear();
        if (image1Event != null) image1Event.clear();
        if (image2Event != null) image2Event.clear();
        if (dateDebutPicker != null) dateDebutPicker.setValue(null);
        if (dateFinPicker != null) dateFinPicker.setValue(null);


        if (updateTitreEvent != null) updateTitreEvent.clear();
        if (updateDescriptionEvent != null) updateDescriptionEvent.clear();
        if (updateImage1Event != null) updateImage1Event.clear();
        if (updateImage2Event != null) updateImage2Event.clear();
        if (updateDateDebutPicker != null) updateDateDebutPicker.setValue(null);
        if (updateDateFinPicker != null) updateDateFinPicker.setValue(null);

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        con = DBConnexion.getCon();
        showEvents();
    }

    @FXML
    void showAddForm() {
        addEventForm.setVisible(true);
        updateEventForm.setVisible(false);
        formContainer.setVisible(true);
        clearFields();
    }

    public void hideForms() {
        formContainer.setVisible(false);
    }
}
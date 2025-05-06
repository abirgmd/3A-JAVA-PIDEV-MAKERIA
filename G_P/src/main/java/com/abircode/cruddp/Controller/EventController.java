package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Event;
import com.abircode.cruddp.services.ServiceEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.Optional;
import java.io.File;

public class EventController implements Initializable {

    private ServiceEvent serviceEvent = new ServiceEvent();


    @FXML
    private TextField searchEventField;
    @FXML private TableView<Event> tablep;
    @FXML private FlowPane eventCardsContainer;
    @FXML private StackPane formContainer;
    @FXML private VBox addEventForm;
    @FXML private VBox updateEventForm;
    @FXML
    private ComboBox<String> sortByComboBox;
    @FXML private Text titreError, descriptionError, imageError, dateDebutError, dateFinError;

    @FXML
    private ComboBox<String> sortOrderComboBox;
    // Champs pour l'ajout
    @FXML private TextField titreEvent, descriptionEvent, image1Event;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;

    private ObservableList<Event> events = FXCollections.observableArrayList();
    @FXML private Button selectImage1Button;
    // Champs pour la mise à jour
    @FXML private TextField updateTitreEvent, updateDescriptionEvent, updateImage1Event;
    @FXML private DatePicker updateDateDebutPicker;
    @FXML private DatePicker updateDateFinPicker;
    @FXML private ComboBox<String> image1TypeComboBox;
    @FXML private ComboBox<String> updateImage1TypeComboBox;
    @FXML private ImageView image1Preview;
    @FXML private Button updateSelectImage1Button;
    @FXML private ImageView updateImage1Preview;
    @FXML private Text updateTitreError, updateDescriptionError, updateImageError, updateDateDebutError, updateDateFinError;

    private Event selectedEvent;


    private void showAlert(Alert.AlertType error, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private boolean validateInputs(String titre, String description, String image1, String dateDebutStr, String dateFinStr, boolean isUpdate) {
        boolean isValid = true;

        // Reset error messages
        if (!isUpdate) {
            titreError.setText("");
            descriptionError.setText("");
            imageError.setText("");
            dateDebutError.setText("");
            dateFinError.setText("");
        } else {
            updateTitreError.setText("");
            updateDescriptionError.setText("");
            updateImageError.setText("");
            updateDateDebutError.setText("");
            updateDateFinError.setText("");
        }

        // Validation du titre
        if (titre == null || titre.trim().length() < 3) {
            if (!isUpdate) {
                titreError.setText("Le titre doit contenir au moins 3 caractères");
                titreError.setFill(Color.RED);
            } else {
                updateTitreError.setText("Le titre doit contenir au moins 3 caractères");
                updateTitreError.setFill(Color.RED);
            }
            isValid = false;
        }

        // Validation de la description
        if (description == null || description.trim().length() < 10) {
            if (!isUpdate) {
                descriptionError.setText("La description doit contenir au moins 10 caractères");
                descriptionError.setFill(Color.RED);
            } else {
                updateDescriptionError.setText("La description doit contenir au moins 10 caractères");
                updateDescriptionError.setFill(Color.RED);
            }
            isValid = false;
        }

        // Validation de l'image
        if (image1 == null || image1.trim().isEmpty()) {
            if (!isUpdate) {
                imageError.setText("L'image est obligatoire");
                imageError.setFill(Color.RED);
            } else {
                updateImageError.setText("L'image est obligatoire");
                updateImageError.setFill(Color.RED);
            }
            isValid = false;
        }

        // Validation des dates
        if (dateDebutStr == null || dateDebutStr.trim().isEmpty()) {
            if (!isUpdate) {
                dateDebutError.setText("La date de début est obligatoire");
                dateDebutError.setFill(Color.RED);
            } else {
                updateDateDebutError.setText("La date de début est obligatoire");
                updateDateDebutError.setFill(Color.RED);
            }
            isValid = false;
        }

        if (dateFinStr == null || dateFinStr.trim().isEmpty()) {
            if (!isUpdate) {
                dateFinError.setText("La date de fin est obligatoire");
                dateFinError.setFill(Color.RED);
            } else {
                updateDateFinError.setText("La date de fin est obligatoire");
                updateDateFinError.setFill(Color.RED);
            }
            isValid = false;
        }

        // Vérification du format et de la cohérence des dates
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime debut = LocalDateTime.parse(dateDebutStr, formatter);
            LocalDateTime fin = LocalDateTime.parse(dateFinStr, formatter);

            // Vérification que la date de début n'est pas dans le passé
            if (debut.isBefore(now)) {
                if (!isUpdate) {
                    dateDebutError.setText("La date de début ne peut pas être dans le passé");
                    dateDebutError.setFill(Color.RED);
                } else {
                    updateDateDebutError.setText("La date de début ne peut pas être dans le passé");
                    updateDateDebutError.setFill(Color.RED);
                }
                isValid = false;
            }

            // Vérification que la date de fin n'est pas dans le passé
            if (fin.isBefore(now)) {
                if (!isUpdate) {
                    dateFinError.setText("La date de fin ne peut pas être dans le passé");
                    dateFinError.setFill(Color.RED);
                } else {
                    updateDateFinError.setText("La date de fin ne peut pas être dans le passé");
                    updateDateFinError.setFill(Color.RED);
                }
                isValid = false;
            }

            // Vérification que la date de début est avant la date de fin
            if (debut.isAfter(fin)) {
                if (!isUpdate) {
                    dateDebutError.setText("La date de début doit être avant la date de fin");
                    dateDebutError.setFill(Color.RED);
                } else {
                    updateDateDebutError.setText("La date de début doit être avant la date de fin");
                    updateDateDebutError.setFill(Color.RED);
                }
                isValid = false;
            }

        } catch (DateTimeParseException e) {
            if (!isUpdate) {
                dateDebutError.setText("Format de date invalide (yyyy-MM-dd HH:mm)");
                dateDebutError.setFill(Color.RED);
                dateFinError.setText("Format de date invalide (yyyy-MM-dd HH:mm)");
                dateFinError.setFill(Color.RED);
            } else {
                updateDateDebutError.setText("Format de date invalide (yyyy-MM-dd HH:mm)");
                updateDateDebutError.setFill(Color.RED);
                updateDateFinError.setText("Format de date invalide (yyyy-MM-dd HH:mm)");
                updateDateFinError.setFill(Color.RED);
            }
            isValid = false;
        }

        return isValid;
    }


    @FXML
    void createEvent(ActionEvent event) {
        String titre = titreEvent.getText();
        String description = descriptionEvent.getText();
        String image1 = image1Event.getText();

        String debut = dateDebutPicker.getValue() != null ?
                dateDebutPicker.getValue().atTime(0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
        String fin = dateFinPicker.getValue() != null ?
                dateFinPicker.getValue().atTime(0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";

        if (!validateInputs(titre, description, image1, debut, fin, false)) {
            return;
        }

        try {
            Event newEvent = new Event();
            newEvent.setTitreevents(titre);
            newEvent.setDescriptionevents(description);
            newEvent.setImage1events(image1);
            newEvent.setDate_debut(LocalDateTime.parse(debut, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            newEvent.setDate_fin(LocalDateTime.parse(fin, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

            serviceEvent.ajouter(newEvent);
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'ajout de l'événement");
        }
    }
    @FXML
    void showUpdateForm(Event event) {
        selectedEvent = event;
        updateTitreEvent.setText(event.getTitreevents());
        updateDescriptionEvent.setText(event.getDescriptionevents());
        updateImage1Event.setText(event.getImage1events());
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
            String debut = updateDateDebutPicker.getValue() != null ?
                    updateDateDebutPicker.getValue().atTime(0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
            String fin = updateDateFinPicker.getValue() != null ?
                    updateDateFinPicker.getValue().atTime(0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";

            if (!validateInputs(titre, description, image1, debut, fin, true)) {
                return;
            }

            try {
                selectedEvent.setTitreevents(titre);
                selectedEvent.setDescriptionevents(description);
                selectedEvent.setImage1events(image1);
                selectedEvent.setDate_debut(LocalDateTime.parse(debut, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                selectedEvent.setDate_fin(LocalDateTime.parse(fin, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

                serviceEvent.modifier(selectedEvent);
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
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la mise à jour de l'événement");
            }
        }
    }
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
                try {
                    serviceEvent.supprimer(event.getId());
                    showEvents();

                    // Afficher un message de succès
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Succès");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Événement supprimé avec succès!");
                    successAlert.showAndWait();
                } catch (SQLException e) {
                    System.err.println("Error while deleting the event: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la suppression de l'événement");
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
        HBox rowContainer = new HBox(20);
        rowContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        for (Event event : events) {
            BorderPane card = createEventCard(event);
            rowContainer.getChildren().add(card);
            count++;

            if (count % 2 == 0) {
                eventCardsContainer.getChildren().add(rowContainer);
                rowContainer = new HBox(20);
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

        Button updateButton = new Button("Modifier");
        updateButton.setStyle("-fx-background-color: #b11d85; -fx-text-fill: white;");
        updateButton.setOnAction(e -> showUpdateForm(event));

        Button deleteButton = new Button("Supprimer");
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
        try {
            events.addAll(serviceEvent.afficher());
        } catch (SQLException e) {
            System.err.println("Error while retrieving events: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la récupération des événements");
        }
        return events;
    }

    void clearFields() {
        if (titreEvent != null) titreEvent.clear();
        if (descriptionEvent != null) descriptionEvent.clear();
        if (image1Event != null) image1Event.clear();
        if (dateDebutPicker != null) dateDebutPicker.setValue(null);
        if (dateFinPicker != null) dateFinPicker.setValue(null);

        titreError.setText("");
        descriptionError.setText("");
        imageError.setText("");
        dateDebutError.setText("");
        dateFinError.setText("");

        if (updateTitreEvent != null) updateTitreEvent.clear();
        if (updateDescriptionEvent != null) updateDescriptionEvent.clear();
        if (updateImage1Event != null) updateImage1Event.clear();
        if (updateDateDebutPicker != null) updateDateDebutPicker.setValue(null);
        if (updateDateFinPicker != null) updateDateFinPicker.setValue(null);

        updateTitreError.setText("");
        updateDescriptionError.setText("");
        updateImageError.setText("");
        updateDateDebutError.setText("");
        updateDateFinError.setText("");
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showEvents();
        loadEvents();
        // Dans la méthode initialize()
        dateDebutPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        dateFinPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

// Faire de même pour les DatePicker de mise à jour
        updateDateDebutPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        updateDateFinPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
        // Initialize image type ComboBoxes
        image1TypeComboBox.getItems().addAll("Png", "URL");
        updateImage1TypeComboBox.getItems().addAll("png", "URL");
        sortByComboBox.getItems().addAll("Titre", "Description");

        // Remplir le ComboBox pour l'ordre
        sortOrderComboBox.getItems().addAll("Croissant", "Décroissant");

        // Sélectionner par défaut les premiers éléments
        sortByComboBox.getSelectionModel().selectFirst();
        sortOrderComboBox.getSelectionModel().selectFirst();
        // Ajouter un écouteur pour chaque ComboBox
        sortByComboBox.valueProperty().addListener((observable, oldValue, newValue) -> sortEvents());
        sortOrderComboBox.valueProperty().addListener((observable, oldValue, newValue) -> sortEvents());

        // Add listeners for image type changes
        image1TypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (newVal.equals("Local")) {
                    image1Event.setEditable(false);
                    selectImage1Button.setVisible(true);
                } else {
                    image1Event.setEditable(true);
                    selectImage1Button.setVisible(false);
                }
            }
        });

        updateImage1TypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (newVal.equals("Local")) {
                    updateImage1Event.setEditable(false);
                    updateSelectImage1Button.setVisible(true);
                } else {
                    updateImage1Event.setEditable(true);
                    updateSelectImage1Button.setVisible(false);
                }
            }
        });

        // Add listeners for URL changes
        image1Event.textProperty().addListener((obs, oldVal, newVal) -> {
            if (image1TypeComboBox.getValue() != null && image1TypeComboBox.getValue().equals("URL")) {
            }
        });

        updateImage1Event.textProperty().addListener((obs, oldVal, newVal) -> {
            if (updateImage1TypeComboBox.getValue() != null && updateImage1TypeComboBox.getValue().equals("URL")) {
            }
        });
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

    @FXML
    private void selectImage1File() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            String imagePath = file.toURI().toString();
            image1Event.setText(imagePath);
            image1TypeComboBox.setValue("Local");
        }
    }
    private void loadEvents() {
        try {
            events.clear();
            events.addAll(serviceEvent.getAllEvents()); // Récupérer les événements depuis la base de données
            displayEvents(events); // Afficher les événements
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des événements : " + e.getMessage());
        }
    }
    @FXML
    private void searchEvents() {
        String searchText = searchEventField.getText().toLowerCase(); // Texte de recherche
        ObservableList<Event> filteredEvents = FXCollections.observableArrayList();

        // Filtrer les événements par titre ou description
        for (Event event : events) {
            if (event.getTitreevents().toLowerCase().contains(searchText) ||
                    event.getDescriptionevents().toLowerCase().contains(searchText)) {
                filteredEvents.add(event);
            }
        }

        displayEvents(filteredEvents); // Afficher les événements filtrés
    }
    private void displayEvents(ObservableList<Event> eventsToDisplay) {
        eventCardsContainer.getChildren().clear(); // Effacer les anciennes cartes

        for (Event event : eventsToDisplay) {
            BorderPane card = createEventCard(event); // Créer une carte pour chaque événement
            eventCardsContainer.getChildren().add(card); // Ajouter la carte au conteneur
        }
    }
    @FXML
    private void sortEvents() {
        String sortBy = sortByComboBox.getValue();  // Obtenez le critère de tri sélectionné
        String sortOrder = sortOrderComboBox.getValue();  // Obtenez l'ordre de tri (croissant/décroissant)

        Comparator<Event> comparator = null;

        if (sortBy.equals("Titre")) {
            comparator = Comparator.comparing(Event::getTitreevents);
        }
        else if (sortBy.equals("Description")) {
            comparator = Comparator.comparing(Event::getDescriptionevents);
        }

        // Si l'ordre de tri est décroissant, inversez le comparateur
        if (sortOrder.equals("Décroissant") && comparator != null) {
            comparator = comparator.reversed();
        }

        // Appliquez le tri à la liste des événements
        if (comparator != null) {
            FXCollections.sort(events, comparator);
        }

        displayEvents(events);  // Affichez les événements triés
    }

    private void sortEventsByTitle(boolean ascending) {
        events.sort((e1, e2) -> ascending
                ? e1.getTitreevents().compareToIgnoreCase(e2.getTitreevents())
                : e2.getTitreevents().compareToIgnoreCase(e1.getTitreevents()));
        showEvents();
    }


    @FXML
    private void updateSelectImage1File() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            String imagePath = file.toURI().toString();
            updateImage1Event.setText(imagePath);
            updateImage1TypeComboBox.setValue("Local");
        }
    }}
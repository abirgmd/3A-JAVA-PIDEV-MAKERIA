package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Categorie;
import com.abircode.cruddp.services.ServiceCategorie;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import java.util.Comparator;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class CategoryController implements Initializable {

    private ServiceCategorie serviceCategorie = new ServiceCategorie();

    // Composants FXML
    @FXML private TextField searchField;
    @FXML private VBox addCategoryForm, updateCategoryForm;
    @FXML private TextField nomcat, descriptioncat, updateNomcat, updateDescriptioncat;
    @FXML private Button BcreateCategory, BupdateCategory, Bcancel;
    @FXML private Label validationLabel;
    @FXML private ComboBox<String> sortByComboBox;
    @FXML private ComboBox<String> sortOrderComboBox;
    private Categorie selectedCategory = null;
    @FXML private GridPane cardGrid; // À ajouter
    private static final int PAGE_SIZE = 6; // Nombre d'éléments par page
    private int currentPage = 1; // Page actuelle

    @FXML private Button prevButton, nextButton;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupFieldValidation();
        showCategories();
        cardGrid.setHgap(10); // Espacement horizontal entre les cartes
        cardGrid.setVgap(15); // Espacement vertical entre les cartes
        // Recherche en temps réel
        searchField.textProperty().addListener((observable, oldValue, newValue) -> searchCategories());
        sortByComboBox.getItems().addAll("Nom", "Description");
        sortOrderComboBox.getItems().addAll("Ascendant", "Descendant");
        prevButton.setDisable(true); // Le bouton Précédent est désactivé au début
        nextButton.setDisable(false);
    }

    private void setupFieldValidation() {
        // Validation pour le formulaire d'ajout
        nomcat.textProperty().addListener((obs, oldVal, newVal) -> validateNameField(newVal, nomcat));
        descriptioncat.textProperty().addListener((obs, oldVal, newVal) -> validateDescField(newVal, descriptioncat));

        // Validation pour le formulaire de modification
        updateNomcat.textProperty().addListener((obs, oldVal, newVal) -> validateNameField(newVal, updateNomcat));
        updateDescriptioncat.textProperty().addListener((obs, oldVal, newVal) -> validateDescField(newVal, updateDescriptioncat));
    }

    private void validateNameField(String value, TextField field) {
        if (value == null || value.trim().isEmpty()) {
            field.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            showValidationText(field, "Le nom est requis");
        } else if (value.trim().length() < 3) {
            field.setStyle("-fx-border-color: orange; -fx-border-width: 1px;");
            showValidationText(field, "Le nom doit comporter au moins 3 caractères");
        } else {
            field.setStyle("-fx-border-color: green; -fx-border-width: 1px;");
            hideValidationText(field);
        }
    }

    private void validateDescField(String value, TextField field) {
        if (value == null || value.trim().isEmpty()) {
            field.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            showValidationText(field, "La description est requise");
        } else if (value.trim().length() < 10) {
            field.setStyle("-fx-border-color: orange; -fx-border-width: 1px;");
            showValidationText(field, "La description doit comporter au moins 10 caractères");
        } else {
            field.setStyle("-fx-border-color: green; -fx-border-width: 1px;");
            hideValidationText(field);
        }
    }

    private void showValidationText(TextField field, String message) {
        VBox parent = (VBox) field.getParent();
        Text validationText = new Text(message);
        validationText.setStyle("-fx-fill: red; -fx-font-size: 12px;");
        parent.getChildren().removeIf(node -> node instanceof Text && ((Text) node).getStyle().contains("-fx-fill: red"));
        parent.getChildren().add(validationText);
    }

    private void hideValidationText(TextField field) {
        VBox parent = (VBox) field.getParent();
        parent.getChildren().removeIf(node -> node instanceof Text && ((Text) node).getStyle().contains("-fx-fill: red"));
    }

    private boolean validateForm(TextField nameField, TextField descField) {
        boolean isValid = true;
        String name = nameField.getText().trim();
        String desc = descField.getText().trim();

        if (name.isEmpty()) {
            nameField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            showValidationText(nameField, "Le nom est requis");
            isValid = false;
        }

        if (desc.isEmpty()) {
            descField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            showValidationText(descField, "La description est requise");
            isValid = false;
        }

        return isValid;
    }

    @FXML
    void createCategory(ActionEvent event) {
        if (!validateForm(nomcat, descriptioncat)) {
            return;
        }

        try {
            Categorie newCategory = new Categorie();
            newCategory.setNomcat(nomcat.getText().trim());
            newCategory.setDescriptioncat(descriptioncat.getText().trim());

            serviceCategorie.ajouter(newCategory);
            hideForms();
            clearFields();
            showCategories();
        } catch (SQLException e) {
            validationLabel.setText("Erreur lors de l'ajout de la catégorie: " + e.getMessage());
            validationLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    void updateCategory(ActionEvent event) {
        if (!validateForm(updateNomcat, updateDescriptioncat)) {
            return;
        }

        try {
            selectedCategory.setNomcat(updateNomcat.getText().trim());
            selectedCategory.setDescriptioncat(updateDescriptioncat.getText().trim());

            serviceCategorie.modifier(selectedCategory);
            hideForms();
            clearFields();
            showCategories();
        } catch (SQLException e) {
            validationLabel.setText("Erreur lors de la mise à jour de la catégorie: " + e.getMessage());
            validationLabel.setStyle("-fx-text-fill: red;");
        }
    }



    @FXML
    void hideForms() {
        addCategoryForm.setVisible(false);
        updateCategoryForm.setVisible(false);
    }

    @FXML
    void clearFields() {
        nomcat.clear();
        descriptioncat.clear();
        updateNomcat.clear();
        updateDescriptioncat.clear();
        if (validationLabel != null) {
            validationLabel.setText("");
        }
    }



    @FXML
    void searchCategories() {
        // Passer la liste des catégories filtrées à updateCategoryCards
        updateCategoryCards(getCategories(searchField.getText()));
    }

    private ObservableList<Categorie> getCategories(String filter) {
        ObservableList<Categorie> categories = FXCollections.observableArrayList();
        try {
            // Récupération de toutes les catégories
            categories.addAll(serviceCategorie.afficher());

            // Application du filtre
            if (filter != null && !filter.isEmpty()) {
                categories.removeIf(category ->
                        !category.getNomcat().toLowerCase().contains(filter.toLowerCase()) &&
                                !category.getDescriptioncat().toLowerCase().contains(filter.toLowerCase()));
            }
        } catch (SQLException e) {
            validationLabel.setText("Erreur lors de la récupération des catégories: " + e.getMessage());
            validationLabel.setStyle("-fx-text-fill: red;");
        }
        return categories;
    }

    private void updateCategoryCards(ObservableList<Categorie> categories) {
        cardGrid.getChildren().clear(); // Vider la grille avant d'ajouter les nouvelles catégories
        int column = 0;
        int row = 0;
        final int MAX_COLUMNS = 3; // Nombre maximal de colonnes

        for (Categorie category : categories) {
            cardGrid.add(createCategoryCard(category), column, row);
            column++;
            if (column >= MAX_COLUMNS) {
                column = 0;
                row++;
            }
        }

    }private void updatePaginationControls(ObservableList<Categorie> categories) {
        int totalCategories = categories.size();
        int totalPages = (int) Math.ceil((double) totalCategories / PAGE_SIZE);


    }





    private VBox createCategoryCard(Categorie category) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);");

        // Nom de la catégorie
        Text nameText = new Text(category.getNomcat());
        nameText.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-fill: #b11d85;");

        // Description
        Text descriptionText = new Text(category.getDescriptioncat());
        descriptionText.setWrappingWidth(230);
        descriptionText.setStyle("-fx-font-size: 14; -fx-fill: black;");

        // Boutons d'action
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        Button editButton = new Button("Modifier");
        editButton.setStyle("-fx-background-color: #0A2647; -fx-text-fill: white; -fx-background-radius: 5;");
        editButton.setOnAction(e -> {
            showUpdateForm(e, category); // Passage correct de l'événement et de la catégorie
        });

        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #b11d85; -fx-text-fill: white; -fx-background-radius: 5;");
        deleteButton.setOnAction(e -> deleteCategory(category));

        buttonsBox.getChildren().addAll(editButton, deleteButton);

        card.getChildren().addAll(nameText, descriptionText, buttonsBox);
        return card;
    }
    @FXML
    void deleteCategory(Categorie category) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette catégorie?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceCategorie.supprimer(category.getId());
                showCategories();
            } catch (SQLException e) {
                validationLabel.setText("Erreur lors de la suppression de la catégorie: " + e.getMessage());
                validationLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }
    @FXML
    void showCreateForm(ActionEvent event) {
        hideForms();
        addCategoryForm.setVisible(true);
        clearFields();
    }

    @FXML
    void showUpdateForm(ActionEvent event, Categorie category) {
        hideForms();
        updateCategoryForm.setVisible(true);
        selectedCategory = category;
        updateNomcat.setText(category.getNomcat());
        updateDescriptioncat.setText(category.getDescriptioncat());
    }
    @FXML
    private void sortCategories(ActionEvent event) {
        try {
            ObservableList<Categorie> categories = FXCollections.observableArrayList(serviceCategorie.afficher());

            String sortBy = sortByComboBox.getValue();  // Nom ou Description
            String sortOrder = sortOrderComboBox.getValue();  // Ascendant ou Descendant

            Comparator<Categorie> comparator = null;

            // Choisir le critère de tri
            if ("Nom".equals(sortBy)) {
                comparator = Comparator.comparing(Categorie::getNomcat);
            } else if ("Description".equals(sortBy)) {
                comparator = Comparator.comparing(Categorie::getDescriptioncat);
            }

            // Appliquer l'ordre
            if ("Descendant".equals(sortOrder)) {
                comparator = comparator.reversed();
            }

            // Trier les catégories
            if (comparator != null) {
                categories.sort(comparator);
            }

            // Afficher les catégories triées
            updateCategoryCards(categories);
        } catch (SQLException e) {
            validationLabel.setText("Erreur lors du tri des catégories: " + e.getMessage());
            validationLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void updatePaginationControls(int totalCategories) {
        int totalPages = (int) Math.ceil((double) totalCategories / PAGE_SIZE);

        prevButton.setDisable(currentPage <= 1); // Désactiver si on est sur la première page
        nextButton.setDisable(currentPage >= totalPages); // Désactiver si on est sur la dernière page
    }

    @FXML
    void goToPreviousPage(ActionEvent event) {
        if (currentPage > 1) {
            currentPage--;
            showCategories();
        }
    }

    @FXML
    void goToNextPage(ActionEvent event) {
        int totalCategories = 0;
        try {
            totalCategories = serviceCategorie.afficher().size();
        } catch (SQLException e) {
            validationLabel.setText("Erreur lors de la récupération des catégories: " + e.getMessage());
            validationLabel.setStyle("-fx-text-fill: red;");
        }

        if (currentPage < Math.ceil((double) totalCategories / PAGE_SIZE)) {
            currentPage++;
            showCategories();
        }
    }

    private void showCategories() {
        try {
            ObservableList<Categorie> categories = getCategories(currentPage, searchField.getText());
            updateCategoryCards(categories);

            // Mettre à jour les contrôles de pagination
            int totalCategories = serviceCategorie.afficher().size();
            updatePaginationControls(totalCategories);
        } catch (SQLException e) {
            validationLabel.setText("Erreur lors de la récupération des catégories: " + e.getMessage());
            validationLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private ObservableList<Categorie> getCategories(int page, String filter) {
        ObservableList<Categorie> allCategories = FXCollections.observableArrayList();
        try {
            // Récupérer toutes les catégories
            allCategories.addAll(serviceCategorie.afficher());

            // Appliquer le filtre de recherche
            if (filter != null && !filter.isEmpty()) {
                allCategories.removeIf(category ->
                        !category.getNomcat().toLowerCase().contains(filter.toLowerCase()) &&
                                !category.getDescriptioncat().toLowerCase().contains(filter.toLowerCase()));
            }

            // Paginer les catégories
            int startIndex = (page - 1) * PAGE_SIZE;
            int endIndex = Math.min(startIndex + PAGE_SIZE, allCategories.size());
            return FXCollections.observableArrayList(allCategories.subList(startIndex, endIndex));
        } catch (SQLException e) {
            validationLabel.setText("Erreur lors de la récupération des catégories: " + e.getMessage());
            validationLabel.setStyle("-fx-text-fill: red;");
            return FXCollections.emptyObservableList();
        }
    }


}

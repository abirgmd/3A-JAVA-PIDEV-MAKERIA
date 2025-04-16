package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Categorie;
import com.abircode.cruddp.utils.DBConnexion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.Optional;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class CategoryController implements Initializable {

    private Connection con = null;
    private PreparedStatement st = null;
    private ResultSet rs = null;

    @FXML private FlowPane categoryFlowPane;
    @FXML private TextField searchField;
    @FXML private VBox addCategoryForm, updateCategoryForm;
    @FXML private TextField nomcat, descriptioncat, updateNomcat, updateDescriptioncat;
    @FXML private Button BcreateCategory, BupdateCategory, Bcancel;
    @FXML private Label validationLabel;

    private Categorie selectedCategory = null;



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
        } else if (value.trim().length() < 3) {
            field.setStyle("-fx-border-color: orange; -fx-border-width: 1px;");
        } else {
            field.setStyle("-fx-border-color: green; -fx-border-width: 1px;");
        }
    }

    private void validateDescField(String value, TextField field) {
        if (value == null || value.trim().isEmpty()) {
            field.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
        } else if (value.trim().length() < 10) {
            field.setStyle("-fx-border-color: orange; -fx-border-width: 1px;");
        } else {
            field.setStyle("-fx-border-color: green; -fx-border-width: 1px;");
        }
    }

    private boolean validateForm(TextField nameField, TextField descField) {
        boolean isValid = true;

        // Validation du nom (min 3 caractères)
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            nameField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom est obligatoire");
            isValid = false;
        } else if (nameField.getText().trim().length() < 3) {
            nameField.setStyle("-fx-border-color: orange; -fx-border-width: 2px;");
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom doit contenir au moins 3 caractères");
            isValid = false;
        }

        // Validation de la description (min 10 caractères)
        if (descField.getText() == null || descField.getText().trim().isEmpty()) {
            descField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            showAlert(Alert.AlertType.ERROR, "Erreur", "La description est obligatoire");
            isValid = false;
        } else if (descField.getText().trim().length() < 10) {
            descField.setStyle("-fx-border-color: orange; -fx-border-width: 2px;");
            showAlert(Alert.AlertType.ERROR, "Erreur", "La description doit contenir au moins 10 caractères");
            isValid = false;
        }

        return isValid;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void showCreateForm(ActionEvent event) {
        addCategoryForm.setVisible(true);
        updateCategoryForm.setVisible(false);
        clearFields();
    }

    @FXML
    void showUpdateForm(ActionEvent event, Categorie category) {
        selectedCategory = category;
        updateNomcat.setText(category.getNomcat());
        updateDescriptioncat.setText(category.getDescriptioncat());
        updateCategoryForm.setVisible(true);
        addCategoryForm.setVisible(false);
    }

    @FXML
    void createCategory(ActionEvent event) {
        if (!validateForm(nomcat, descriptioncat)) {
            return;
        }

        String insert = "INSERT INTO categorie (nomcat, descriptioncat) VALUES (?, ?)";
        con = DBConnexion.getCon();
        try {
            st = con.prepareStatement(insert);
            st.setString(1, nomcat.getText().trim());
            st.setString(2, descriptioncat.getText().trim());
            int affectedRows = st.executeUpdate();

            if (affectedRows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie créée avec succès");
                showCategories();
                clearFields();
                hideForms();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la création: " + e.getMessage());
        }
    }

    @FXML
    void updateCategory(ActionEvent event) {
        if (selectedCategory == null) return;
        if (!validateForm(updateNomcat, updateDescriptioncat)) {
            return;
        }

        String update = "UPDATE categorie SET nomcat = ?, descriptioncat = ? WHERE id = ?";
        con = DBConnexion.getCon();
        try {
            st = con.prepareStatement(update);
            st.setString(1, updateNomcat.getText().trim());
            st.setString(2, updateDescriptioncat.getText().trim());
            st.setInt(3, selectedCategory.getId());
            int affectedRows = st.executeUpdate();

            if (affectedRows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie mise à jour avec succès");
                showCategories();
                clearFields();
                hideForms();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    @FXML
    void deleteCategory(Categorie category) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer la catégorie");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette catégorie ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String delete = "DELETE FROM categorie WHERE id = ?";
            con = DBConnexion.getCon();
            try {
                st = con.prepareStatement(delete);
                st.setInt(1, category.getId());
                int affectedRows = st.executeUpdate();

                if (affectedRows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie supprimée avec succès");
                    showCategories();
                    clearFields();
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    @FXML
    void cancelAction(ActionEvent event) {
        hideForms();
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
        selectedCategory = null;

        // Réinitialiser les styles
        nomcat.setStyle("");
        descriptioncat.setStyle("");
        updateNomcat.setStyle("");
        updateDescriptioncat.setStyle("");
    }

    private void showCategories() {
        updateCategoryCards(null);
    }

    private void searchCategories() {
        updateCategoryCards(searchField.getText());
    }


    private VBox createCategoryCard(Categorie category) {
        VBox card = new VBox(10);
        card.setPadding(new javafx.geometry.Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3); -fx-padding: 10;");
        card.setPrefWidth(250);

        // Nom de la catégorie
        Label nameLabel = new Label(category.getNomcat());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: #b11d85;");
        nameLabel.setWrapText(true);

        // Description de la catégorie
        Label descriptionLabel = new Label(category.getDescriptioncat());
        descriptionLabel.setStyle("-fx-font-size: 14; -fx-text-fill: black;");
        descriptionLabel.setWrapText(true);

        // Boîte pour les boutons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #b11d85; -fx-text-fill: white; -fx-background-radius: 5;");
        editBtn.setOnAction(e -> showUpdateForm(e, category));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #0A2647; -fx-text-fill: white; -fx-background-radius: 5;");
        deleteBtn.setOnAction(e -> deleteCategory(category));

        buttonBox.getChildren().addAll(editBtn, deleteBtn);

        // Ajout des éléments au card
        card.getChildren().addAll(nameLabel, descriptionLabel, buttonBox);

        return card;
    }

    private ObservableList<Categorie> getCategories(String filter) {
        ObservableList<Categorie> categories = FXCollections.observableArrayList();
        String query = "SELECT * FROM categorie";

        if (filter != null && !filter.trim().isEmpty()) {
            query += " WHERE nomcat LIKE ? OR descriptioncat LIKE ?";
        }

        try (Connection con = DBConnexion.getCon();
             PreparedStatement st = con.prepareStatement(query)) {

            if (filter != null && !filter.trim().isEmpty()) {
                String search = "%" + filter + "%";
                st.setString(1, search);
                st.setString(2, search);
            }

            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    Categorie category = new Categorie();
                    category.setId(rs.getInt("id"));
                    category.setNomcat(rs.getString("nomcat"));
                    category.setDescriptioncat(rs.getString("descriptioncat"));
                    categories.add(category);
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des catégories: " + e.getMessage());
        }
        return categories;
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addCategoryForm.setVisible(false);
        updateCategoryForm.setVisible(false);
        showCategories();
        searchField.setOnKeyReleased(event -> searchCategories());

        setupFieldValidation();

        // Configurer le FlowPane pour afficher 4 cartes par ligne
        categoryFlowPane.setHgap(10); // Espacement horizontal entre les cartes
        categoryFlowPane.setVgap(10); // Espacement vertical entre les cartes

        // Calculer la largeur nécessaire pour 4 cartes avec un petit espace supplémentaire
        double cardWidth = 250; // Largeur de la carte
        double margin = 10; // Espacement supplémentaire
        double preferredWidth = 4 * cardWidth + 3 * margin; // 4 cartes + 3 espaces entre elles
        categoryFlowPane.setPrefWrapLength(preferredWidth);
    }

    private void updateCategoryCards(String filter) {
        categoryFlowPane.getChildren().clear();
        ObservableList<Categorie> categories = getCategories(filter);

        for (Categorie category : categories) {
            VBox card = createCategoryCard(category);
            categoryFlowPane.getChildren().add(card);
        }
    }

}
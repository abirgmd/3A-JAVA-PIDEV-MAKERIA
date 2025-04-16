package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Produit;
import com.abircode.cruddp.entities.Categorie;
import com.abircode.cruddp.utils.DBConnexion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert.AlertType;
import java.util.Optional;
import java.util.regex.Pattern;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class ProductController implements Initializable {

    private Connection con = null;
    private PreparedStatement st = null;
    private ResultSet rs = null;

    // Constantes de validation
    private static final int MIN_NOM = 3;
    private static final int MIN_DESCRIPTION = 10;
    private static final int MIN_AVANTAGES = 10;
    private static final Pattern FLOAT_PATTERN = Pattern.compile("^[0-9]+(\\.[0-9]+)?$");
    private static final Pattern INT_PATTERN = Pattern.compile("^[0-9]+$");
    private static final String[] VALID_SIZES = {"S", "M", "L", "XL"};

    @FXML private TextField searchField;
    @FXML private ComboBox<Categorie> categorieComboBox;
    @FXML private FlowPane productCardsContainer;
    @FXML private StackPane formContainer;
    @FXML private VBox addProductForm;
    @FXML private VBox updateProductForm;

    // Champs d'ajout
    @FXML private TextField nomprod, desprod, avantages, image_large, nombre_produits_en_stock, prixprod, sizeprod;

    // Champs de mise à jour
    @FXML private TextField updateNomprod, updateDesprod, updateAvantages, updateImageLarge,
            updateNombreProduitsEnStock, updatePrixprod, updateSizeprod;
    @FXML private ComboBox<Categorie> updateCategorieComboBox;

    private Produit selectedProduct;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        con = DBConnexion.getCon();
        ObservableList<Categorie> categories = getCategories();
        categorieComboBox.setItems(categories);
        updateCategorieComboBox.setItems(categories);
        showProduits();

        // Configuration des validateurs
        setupFieldValidators();
    }

    private void setupFieldValidators() {
        // Validation en temps réel pour le formulaire d'ajout
        setupTextFieldValidator(nomprod, this::validateNom);
        setupTextFieldValidator(desprod, this::validateDescription);
        setupTextFieldValidator(avantages, this::validateAvantages);
        setupTextFieldValidator(nombre_produits_en_stock, this::validateStock);
        setupTextFieldValidator(prixprod, this::validatePrix);
        setupTextFieldValidator(sizeprod, this::validateSize);

        // Validation en temps réel pour le formulaire de mise à jour
        setupTextFieldValidator(updateNomprod, this::validateNom);
        setupTextFieldValidator(updateDesprod, this::validateDescription);
        setupTextFieldValidator(updateAvantages, this::validateAvantages);
        setupTextFieldValidator(updateNombreProduitsEnStock, this::validateStock);
        setupTextFieldValidator(updatePrixprod, this::validatePrix);
        setupTextFieldValidator(updateSizeprod, this::validateSize);
    }

    private void setupTextFieldValidator(TextField field, Validator validator) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (validator.validate(newVal)) {
                field.setStyle("-fx-border-color: green;");
            } else {
                field.setStyle("-fx-border-color: red;");
            }
        });
    }

    // Interfaces de validation
    private interface Validator {
        boolean validate(String value);
    }

    // Méthodes de validation
    private boolean validateNom(String nom) {
        return nom != null && nom.trim().length() >= MIN_NOM;
    }

    private boolean validateDescription(String desc) {
        return desc != null && desc.trim().length() >= MIN_DESCRIPTION;
    }

    private boolean validateAvantages(String avantages) {
        return avantages != null && avantages.trim().length() >= MIN_AVANTAGES;
    }

    private boolean validateStock(String stock) {
        return stock != null && INT_PATTERN.matcher(stock).matches();
    }

    private boolean validatePrix(String prix) {
        return prix != null && FLOAT_PATTERN.matcher(prix).matches();
    }

    private boolean validateSize(String size) {
        if (size == null) return false;
        for (String validSize : VALID_SIZES) {
            if (validSize.equalsIgnoreCase(size.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean validateCategorie(Categorie categorie) {
        return categorie != null;
    }

    private boolean validateAddForm() {
        boolean isValid = true;

        if (!validateNom(nomprod.getText())) {
            showAlert(AlertType.ERROR, "Nom invalide", "Le nom doit contenir au moins " + MIN_NOM + " caractères");
            isValid = false;
        }

        if (!validateDescription(desprod.getText())) {
            showAlert(AlertType.ERROR, "Description invalide", "La description doit contenir au moins " + MIN_DESCRIPTION + " caractères");
            isValid = false;
        }

        if (!validateAvantages(avantages.getText())) {
            showAlert(AlertType.ERROR, "Avantages invalides", "Les avantages doivent contenir au moins " + MIN_AVANTAGES + " caractères");
            isValid = false;
        }

        if (!validateStock(nombre_produits_en_stock.getText())) {
            showAlert(AlertType.ERROR, "Stock invalide", "Le stock doit être un nombre entier positif");
            isValid = false;
        }

        if (!validatePrix(prixprod.getText())) {
            showAlert(AlertType.ERROR, "Prix invalide", "Le prix doit être un nombre (ex: 12.99)");
            isValid = false;
        }

        if (!validateSize(sizeprod.getText())) {
            showAlert(AlertType.ERROR, "Taille invalide", "La taille doit être S, M, L ou XL");
            isValid = false;
        }

        if (!validateCategorie(categorieComboBox.getValue())) {
            showAlert(AlertType.ERROR, "Catégorie requise", "Veuillez sélectionner une catégorie");
            isValid = false;
        }

        return isValid;
    }

    private boolean validateUpdateForm() {
        boolean isValid = true;

        if (!validateNom(updateNomprod.getText())) {
            showAlert(AlertType.ERROR, "Nom invalide", "Le nom doit contenir au moins " + MIN_NOM + " caractères");
            isValid = false;
        }

        if (!validateDescription(updateDesprod.getText())) {
            showAlert(AlertType.ERROR, "Description invalide", "La description doit contenir au moins " + MIN_DESCRIPTION + " caractères");
            isValid = false;
        }

        if (!validateAvantages(updateAvantages.getText())) {
            showAlert(AlertType.ERROR, "Avantages invalides", "Les avantages doivent contenir au moins " + MIN_AVANTAGES + " caractères");
            isValid = false;
        }

        if (!validateStock(updateNombreProduitsEnStock.getText())) {
            showAlert(AlertType.ERROR, "Stock invalide", "Le stock doit être un nombre entier positif");
            isValid = false;
        }

        if (!validatePrix(updatePrixprod.getText())) {
            showAlert(AlertType.ERROR, "Prix invalide", "Le prix doit être un nombre (ex: 12.99)");
            isValid = false;
        }

        if (!validateSize(updateSizeprod.getText())) {
            showAlert(AlertType.ERROR, "Taille invalide", "La taille doit être S, M, L ou XL");
            isValid = false;
        }

        if (!validateCategorie(updateCategorieComboBox.getValue())) {
            showAlert(AlertType.ERROR, "Catégorie requise", "Veuillez sélectionner une catégorie");
            isValid = false;
        }

        return isValid;
    }

    @FXML
    void creatProduit(ActionEvent event) {
        if (!validateAddForm()) {
            return;
        }

        String insert = "INSERT INTO produit (nomprod, descriptionprod, avantages, sizeprod, image_large, nombre_produits_en_stock, prixprod, categorie_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        con = DBConnexion.getCon();
        try {
            st = con.prepareStatement(insert);
            st.setString(1, nomprod.getText().trim());
            st.setString(2, desprod.getText().trim());
            st.setString(3, avantages.getText().trim());
            st.setString(4, sizeprod.getText().trim().toUpperCase());
            st.setString(5, image_large.getText().trim()); // Pas de validation pour l'image
            st.setInt(6, Integer.parseInt(nombre_produits_en_stock.getText().trim()));
            st.setDouble(7, Double.parseDouble(prixprod.getText().trim()));
            st.setInt(8, categorieComboBox.getValue().getId());

            int affectedRows = st.executeUpdate();
            if (affectedRows > 0) {
                showAlert(AlertType.INFORMATION, "Succès", "Produit créé avec succès");
                showProduits();
                viderChamps();
                hideForms();
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la création du produit: " + e.getMessage());
        }
    }

    @FXML
    void updateProduit(ActionEvent event) {
        if (selectedProduct == null || !validateUpdateForm()) {
            return;
        }

        String update = "UPDATE produit SET nomprod=?, descriptionprod=?, avantages=?, sizeprod=?, image_large=?, nombre_produits_en_stock=?, prixprod=?, categorie_id=? WHERE id=?";
        con = DBConnexion.getCon();
        try {
            st = con.prepareStatement(update);
            st.setString(1, updateNomprod.getText().trim());
            st.setString(2, updateDesprod.getText().trim());
            st.setString(3, updateAvantages.getText().trim());
            st.setString(4, updateSizeprod.getText().trim().toUpperCase());
            st.setString(5, updateImageLarge.getText().trim()); // Pas de validation pour l'image
            st.setInt(6, Integer.parseInt(updateNombreProduitsEnStock.getText().trim()));
            st.setDouble(7, Double.parseDouble(updatePrixprod.getText().trim()));
            st.setInt(8, updateCategorieComboBox.getValue().getId());
            st.setInt(9, selectedProduct.getId());

            int affectedRows = st.executeUpdate();
            if (affectedRows > 0) {
                showAlert(AlertType.INFORMATION, "Succès", "Produit mis à jour avec succès");
                showProduits();
                hideForms();
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    @FXML
    void showUpdateForm(Produit produit) {
        selectedProduct = produit;

        updateNomprod.setText(produit.getNom());
        updateDesprod.setText(produit.getDescription());
        updateAvantages.setText(produit.getAvantages());
        updateSizeprod.setText(produit.getSize());
        updateImageLarge.setText(produit.getImage());
        updateNombreProduitsEnStock.setText(String.valueOf(produit.getStock()));
        updatePrixprod.setText(String.valueOf(produit.getPrix()));

        if (produit.getCategorie() != null) {
            for (Categorie categorie : updateCategorieComboBox.getItems()) {
                if (categorie.getId() == produit.getCategorie().getId()) {
                    updateCategorieComboBox.setValue(categorie);
                    break;
                }
            }
        }

        addProductForm.setVisible(false);
        updateProductForm.setVisible(true);
        formContainer.setVisible(true);
    }

    @FXML
    void deleteProduit(Produit produit) {
        if (produit == null) return;

        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer le produit");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer ce produit ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String delete = "DELETE FROM produit WHERE id=?";
            con = DBConnexion.getCon();
            try {
                st = con.prepareStatement(delete);
                st.setInt(1, produit.getId());
                int affectedRows = st.executeUpdate();

                if (affectedRows > 0) {
                    showAlert(AlertType.INFORMATION, "Succès", "Produit supprimé avec succès");
                    showProduits();
                }
            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showProduits() {
        ObservableList<Produit> produits = getProduits();
        displayProductCards(produits);
    }

    private void displayProductCards(ObservableList<Produit> produits) {
        productCardsContainer.getChildren().clear();
        productCardsContainer.setHgap(10);
        productCardsContainer.setVgap(10);
        productCardsContainer.setPrefWrapLength(1000);

        int count = 0;
        HBox rowContainer = new HBox(10);
        rowContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        for (Produit produit : produits) {
            BorderPane card = createProductCard(produit);
            rowContainer.getChildren().add(card);
            count++;

            if (count % 2 == 0) {
                productCardsContainer.getChildren().add(rowContainer);
                rowContainer = new HBox(10);
            }
        }

        if (!rowContainer.getChildren().isEmpty()) {
            productCardsContainer.getChildren().add(rowContainer);
        }
    }

    private BorderPane createProductCard(Produit produit) {
        BorderPane card = new BorderPane();
        card.setStyle("-fx-pref-width: 550; -fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3); -fx-padding: 10;");

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
            if (produit.getImage() != null && !produit.getImage().isEmpty()) {
                Image image = new Image(produit.getImage());
                imageView.setImage(image);
            } else {
                Image defaultImage = new Image(getClass().getResourceAsStream("/images/default-product.png"));
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

        Label nameLabel = new Label(produit.getNom());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-wrap-text: true; -fx-max-width: 200px; -fx-text-fill: #b11d85;");
        nameLabel.setWrapText(true);

        Label descriptionLabel = new Label(produit.getDescription());
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(200);

        Label sizeLabel = new Label("Taille: " + produit.getSize());
        sizeLabel.setStyle("-fx-font-weight: bold;");

        Label priceLabel = new Label("Prix: " + produit.getPrix() + " €");
        priceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #b11d85;");

        Label stockLabel = new Label("Stock: " + produit.getStock());
        stockLabel.setStyle("-fx-text-fill: black;");

        Label categoryLabel = new Label("Catégorie: " + (produit.getCategorie() != null ? produit.getCategorie().getNomcat() : "Non catégorisé"));
        categoryLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #b11d85;");

        Label advantagesLabel = new Label("Avantages: " + produit.getAvantages());
        advantagesLabel.setStyle("-fx-text-fill: black;");

        detailsBox.getChildren().addAll(nameLabel, descriptionLabel, sizeLabel, priceLabel, stockLabel, categoryLabel, advantagesLabel);

        VBox buttonsBox = new VBox(10);
        buttonsBox.setPadding(new Insets(10));
        buttonsBox.setAlignment(javafx.geometry.Pos.CENTER);

        Button updateButton = new Button("Modifier");
        updateButton.setStyle("-fx-background-color: #b11d85; -fx-text-fill: white;");
        updateButton.setOnAction(e -> showUpdateForm(produit));

        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #0A2647; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> deleteProduit(produit));

        buttonsBox.getChildren().addAll(updateButton, deleteButton);

        card.setLeft(imageContainer);
        card.setCenter(detailsBox);
        card.setRight(buttonsBox);

        BorderPane.setAlignment(detailsBox, javafx.geometry.Pos.TOP_CENTER);
        BorderPane.setAlignment(buttonsBox, javafx.geometry.Pos.CENTER);

        return card;
    }

    private ObservableList<Produit> getProduits() {
        ObservableList<Produit> produits = FXCollections.observableArrayList();
        String query = "SELECT p.*, c.nomcat, c.descriptioncat FROM produit p LEFT JOIN categorie c ON p.categorie_id = c.id";
        con = DBConnexion.getCon();

        try {
            st = con.prepareStatement(query);
            rs = st.executeQuery();
            while (rs.next()) {
                Produit produit = new Produit();
                produit.setId(rs.getInt("id"));
                produit.setNom(rs.getString("nomprod"));
                produit.setDescription(rs.getString("descriptionprod"));
                produit.setSize(rs.getString("sizeprod"));
                produit.setPrix(rs.getDouble("prixprod"));
                produit.setStock(rs.getInt("nombre_produits_en_stock"));
                produit.setImage(rs.getString("image_large"));

                int categorieId = rs.getInt("categorie_id");
                String categorieNom = rs.getString("nomcat");
                String categorieDescription = rs.getString("descriptioncat");
                Categorie categorie = new Categorie(categorieId, categorieNom, categorieDescription);
                produit.setCategorie(categorie);

                produit.setAvantages(rs.getString("avantages"));
                produits.add(produit);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des produits : " + e.getMessage());
        }
        return produits;
    }

    private ObservableList<Categorie> getCategories() {
        ObservableList<Categorie> categories = FXCollections.observableArrayList();
        String query = "SELECT * FROM categorie";
        con = DBConnexion.getCon();
        try {
            st = con.prepareStatement(query);
            rs = st.executeQuery();
            while (rs.next()) {
                categories.add(new Categorie(rs.getInt("id"), rs.getString("nomcat"), rs.getString("descriptioncat")));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des catégories : " + e.getMessage());
        }
        return categories;
    }

    @FXML
    void showAddForm() {
        addProductForm.setVisible(true);
        updateProductForm.setVisible(false);
        formContainer.setVisible(true);
        viderChamps();
    }

    @FXML
    void viderChamps() {
        nomprod.clear();
        desprod.clear();
        avantages.clear();
        sizeprod.clear();
        image_large.clear();
        nombre_produits_en_stock.clear();
        prixprod.clear();
        categorieComboBox.setValue(null);
    }

    @FXML
    void hideForms() {
        formContainer.setVisible(false);
    }

    @FXML
    void cancelAction(ActionEvent event) {
        hideForms();
    }
}
package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Produit;
import com.abircode.cruddp.entities.Categorie;
import com.abircode.cruddp.services.ServiceProduit;
import com.abircode.cruddp.services.ServiceCategorie;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import java.util.Comparator;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Pattern;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import javafx.scene.chart.*;
import javafx.scene.layout.VBox;
import javafx.scene.control.ComboBox;

public class ProductController implements Initializable {

    private ServiceProduit serviceProduit = new ServiceProduit();
    private ServiceCategorie serviceCategorie = new ServiceCategorie();

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
    @FXML private ComboBox<String> imageTypeComboBox;
    @FXML private Button selectImageButton;
    @FXML private ComboBox<String> updateImageTypeComboBox;
    @FXML private Button updateSelectImageButton;
    @FXML
    private ComboBox<String> sortByComboBox;
    @FXML
    private ComboBox<String> sortOrderComboBox;
    @FXML private ImageView imagePreview;
    @FXML private ImageView updateImagePreview;

    private Produit selectedProduct;
    private String selectedImagePath;
    private String updateSelectedImagePath;
    // Ajoutez ces Labels pour les messages d'erreur
    @FXML private Label nomprodError, desprodError, avantagesError, image_largeError,
            nombre_produits_en_stockError, prixprodError, sizeprodError;
    @FXML private Label updateNomprodError, updateDesprodError, updateAvantagesError,
            updateImageLargeError, updateNombreProduitsEnStockError,
            updatePrixprodError, updateSizeprodError;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            ObservableList<Categorie> categories = FXCollections.observableArrayList(serviceCategorie.afficher());
            categorieComboBox.setItems(categories);
            updateCategorieComboBox.setItems(categories);
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterProduits(newValue);  // Appeler la méthode de filtrage
            });
            // Initialiser les ComboBox pour le type d'image
            ObservableList<String> imageTypes = FXCollections.observableArrayList("URL", "PNG");
            imageTypeComboBox.setItems(imageTypes);
            updateImageTypeComboBox.setItems(imageTypes);
            // Valeur par défaut
            sortByComboBox.getItems().addAll("Nom", "Description");
            sortOrderComboBox.getItems().addAll("Ascendant", "Descendant");

            // Sélectionner un élément par défaut
            sortByComboBox.getSelectionModel().selectFirst();
            sortOrderComboBox.getSelectionModel().selectFirst();
            // Initialiser le ComboBox pour l'ordre de tri
            sortOrderComboBox.setItems(FXCollections.observableArrayList("Ascendant", "Descendant"));
            sortOrderComboBox.getSelectionModel().select("Ascendant");
            // Ajouter les listeners pour les ComboBox
            imageTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    image_large.setDisable(newVal.equals("PNG"));
                    selectImageButton.setDisable(newVal.equals("URL"));
                    if (newVal.equals("URL") && imagePreview != null) {
                        imagePreview.setImage(null);
                    }
                }
            });

            updateImageTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    updateImageLarge.setDisable(newVal.equals("PNG"));
                    updateSelectImageButton.setDisable(newVal.equals("URL"));
                    if (newVal.equals("URL") && updateImagePreview != null) {
                        updateImagePreview.setImage(null);
                    }
                }
            });

            // Ajouter un listener pour l'URL de l'image
            image_large.textProperty().addListener((obs, oldVal, newVal) -> {
                if (imageTypeComboBox.getValue() != null && imageTypeComboBox.getValue().equals("URL") && newVal != null && !newVal.isEmpty() && imagePreview != null) {
                    try {
                        Image image = new Image(newVal);
                        imagePreview.setImage(image);
                    } catch (Exception e) {
                        imagePreview.setImage(null);
                    }
                }
            });

            updateImageLarge.textProperty().addListener((obs, oldVal, newVal) -> {
                if (updateImageTypeComboBox.getValue() != null && updateImageTypeComboBox.getValue().equals("URL") && newVal != null && !newVal.isEmpty() && updateImagePreview != null) {
                    try {
                        Image image = new Image(newVal);
                        updateImagePreview.setImage(image);
                    } catch (Exception e) {
                        updateImagePreview.setImage(null);
                    }
                }
            });

            showProduits();
            setupFieldValidators();
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de l'initialisation: " + e.getMessage());
        }
    }
    private void setupFieldValidators() {
        // Validation en temps réel pour le formulaire d'ajout
        setupTextFieldValidator(nomprod, nomprodError, this::validateNom,
                "Le nom doit contenir au moins " + MIN_NOM + " caractères");
        setupTextFieldValidator(desprod, desprodError, this::validateDescription,
                "La description doit contenir au moins " + MIN_DESCRIPTION + " caractères");
        setupTextFieldValidator(avantages, avantagesError, this::validateAvantages,
                "Les avantages doivent contenir au moins " + MIN_AVANTAGES + " caractères");
        setupTextFieldValidator(nombre_produits_en_stock, nombre_produits_en_stockError, this::validateStock,
                "Le stock doit être un nombre entier positif");
        setupTextFieldValidator(prixprod, prixprodError, this::validatePrix,
                "Le prix doit être un nombre (ex: 12.99)");
        setupTextFieldValidator(sizeprod, sizeprodError, this::validateSize,
                "La taille doit être S, M, L ou XL");

        // Validation en temps réel pour le formulaire de mise à jour
        setupTextFieldValidator(updateNomprod, updateNomprodError, this::validateNom,
                "Le nom doit contenir au moins " + MIN_NOM + " caractères");
        setupTextFieldValidator(updateDesprod, updateDesprodError, this::validateDescription,
                "La description doit contenir au moins " + MIN_DESCRIPTION + " caractères");
        setupTextFieldValidator(updateAvantages, updateAvantagesError, this::validateAvantages,
                "Les avantages doivent contenir au moins " + MIN_AVANTAGES + " caractères");
        setupTextFieldValidator(updateNombreProduitsEnStock, updateNombreProduitsEnStockError, this::validateStock,
                "Le stock doit être un nombre entier positif");
        setupTextFieldValidator(updatePrixprod, updatePrixprodError, this::validatePrix,
                "Le prix doit être un nombre (ex: 12.99)");
        setupTextFieldValidator(updateSizeprod, updateSizeprodError, this::validateSize,
                "La taille doit être S, M, L ou XL");
    }

    private void setupTextFieldValidator(TextField field, Label errorLabel, Validator validator, String errorMessage) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (validator.validate(newVal)) {
                field.setStyle("-fx-border-color: green;");
                errorLabel.setText("");
                errorLabel.setStyle("-fx-text-fill: transparent;");
            } else {
                field.setStyle("-fx-border-color: red;");
                errorLabel.setText(errorMessage);
                errorLabel.setStyle("-fx-text-fill: red;");
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
            nomprodError.setText("Le nom doit contenir au moins " + MIN_NOM + " caractères");
            nomprodError.setStyle("-fx-text-fill: red;");
            isValid = false;
        }

        if (!validateDescription(desprod.getText())) {
            desprodError.setText("La description doit contenir au moins " + MIN_DESCRIPTION + " caractères");
            desprodError.setStyle("-fx-text-fill: red;");
            isValid = false;
        }

        if (!validateAvantages(avantages.getText())) {
            avantagesError.setText("Les avantages doivent contenir au moins " + MIN_AVANTAGES + " caractères");
            avantagesError.setStyle("-fx-text-fill: red;");
            isValid = false;
        }

        if (!validateStock(nombre_produits_en_stock.getText())) {
            nombre_produits_en_stockError.setText("Le stock doit être un nombre entier positif");
            nombre_produits_en_stockError.setStyle("-fx-text-fill: red;");
            isValid = false;
        }

        if (!validatePrix(prixprod.getText())) {
            prixprodError.setText("Le prix doit être un nombre (ex: 12.99)");
            prixprodError.setStyle("-fx-text-fill: red;");
            isValid = false;
        }

        if (!validateSize(sizeprod.getText())) {
            sizeprodError.setText("La taille doit être S, M, L ou XL");
            sizeprodError.setStyle("-fx-text-fill: red;");
            isValid = false;
        }

        if (!validateCategorie(categorieComboBox.getValue())) {
            // Vous pouvez ajouter un Label pour l'erreur de catégorie si nécessaire
            isValid = false;
        }

        return isValid;
    }

    private boolean validateUpdateForm() {
        boolean isValid = true;

        if (!validateNom(updateNomprod.getText())) {
            updateNomprodError.setText("Le nom doit contenir au moins " + MIN_NOM + " caractères");
            updateNomprodError.setStyle("-fx-text-fill: red;");
            isValid = false;
        }

        if (!validateDescription(updateDesprod.getText())) {
            updateDesprodError.setText("La description doit contenir au moins " + MIN_DESCRIPTION + " caractères");
            updateDesprodError.setStyle("-fx-text-fill: red;");
            isValid = false;
        }

        if (!validateAvantages(updateAvantages.getText())) {
            updateAvantagesError.setText("Les avantages doivent contenir au moins " + MIN_AVANTAGES + " caractères");
            updateAvantagesError.setStyle("-fx-text-fill: red;");
            isValid = false;
        }

        if (!validateStock(updateNombreProduitsEnStock.getText())) {
            updateNombreProduitsEnStockError.setText("Le stock doit être un nombre entier positif");
            updateNombreProduitsEnStockError.setStyle("-fx-text-fill: red;");
            isValid = false;
        }

        if (!validatePrix(updatePrixprod.getText())) {
            updatePrixprodError.setText("Le prix doit être un nombre (ex: 12.99)");
            updatePrixprodError.setStyle("-fx-text-fill: red;");
            isValid = false;
        }

        if (!validateSize(updateSizeprod.getText())) {
            updateSizeprodError.setText("La taille doit être S, M, L ou XL");
            updateSizeprodError.setStyle("-fx-text-fill: red;");
            isValid = false;
        }

        if (!validateCategorie(updateCategorieComboBox.getValue())) {
            // Vous pouvez ajouter un Label pour l'erreur de catégorie si nécessaire
            isValid = false;
        }

        return isValid;
    }

    @FXML
    private void selectImageFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image PNG");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images PNG", "*.png")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                // Utiliser directement le chemin du fichier sélectionné
                selectedImagePath = selectedFile.toURI().toString();
                image_large.setText(selectedImagePath);

                // Afficher l'aperçu de l'image
                if (imagePreview != null) {
                    Image image = new Image(selectedImagePath);
                    imagePreview.setImage(image);
                }
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Erreur", "Erreur lors du chargement de l'image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void updateSelectImageFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image PNG");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images PNG", "*.png")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                // Utiliser directement le chemin du fichier sélectionné
                updateSelectedImagePath = selectedFile.toURI().toString();
                updateImageLarge.setText(updateSelectedImagePath);

                // Afficher l'aperçu de l'image
                if (updateImagePreview != null) {
                    Image image = new Image(updateSelectedImagePath);
                    updateImagePreview.setImage(image);
                }
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Erreur", "Erreur lors du chargement de l'image: " + e.getMessage());
            }
        }
    }

    @FXML
    void creatProduit(ActionEvent event) {
        if (!validateAddForm()) {
            return;
        }

        try {
            Produit newProduct = new Produit();
            newProduct.setNom(nomprod.getText().trim());
            newProduct.setDescription(desprod.getText().trim());
            newProduct.setAvantages(avantages.getText().trim());
            newProduct.setSize(sizeprod.getText().trim().toUpperCase());

            // Gérer l'image selon le type sélectionné
            String imageType = imageTypeComboBox.getValue();
            if (imageType != null) {
                if (imageType.equals("PNG") && selectedImagePath != null) {
                    newProduct.setImage(selectedImagePath);
                } else {
                    newProduct.setImage(image_large.getText().trim());
                }
            }

            newProduct.setStock(Integer.parseInt(nombre_produits_en_stock.getText().trim()));
            newProduct.setPrix(Double.parseDouble(prixprod.getText().trim()));
            newProduct.setCategorie(categorieComboBox.getValue());

            serviceProduit.ajouter(newProduct);
            showAlert(AlertType.INFORMATION, "Succès", "Produit créé avec succès");
            showProduits();
            hideForms();
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la création du produit: " + e.getMessage());
        }
    }

    @FXML
    void updateProduit(ActionEvent event) {
        if (selectedProduct == null || !validateUpdateForm()) {
            return;
        }

        try {
            selectedProduct.setNom(updateNomprod.getText().trim());
            selectedProduct.setDescription(updateDesprod.getText().trim());
            selectedProduct.setAvantages(updateAvantages.getText().trim());
            selectedProduct.setSize(updateSizeprod.getText().trim().toUpperCase());

            // Gérer l'image selon le type sélectionné
            String imageType = updateImageTypeComboBox.getValue();
            if (imageType != null) {
                if (imageType.equals("PNG") && updateSelectedImagePath != null) {
                    selectedProduct.setImage(updateSelectedImagePath);
                } else {
                    selectedProduct.setImage(updateImageLarge.getText().trim());
                }
            }

            selectedProduct.setStock(Integer.parseInt(updateNombreProduitsEnStock.getText().trim()));
            selectedProduct.setPrix(Double.parseDouble(updatePrixprod.getText().trim()));
            selectedProduct.setCategorie(updateCategorieComboBox.getValue());

            serviceProduit.modifier(selectedProduct);
            showAlert(AlertType.INFORMATION, "Succès", "Produit mis à jour avec succès");
            showProduits();
            hideForms();
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

        try {
            if (produit.getImage() != null && !produit.getImage().isEmpty() && updateImagePreview != null) {
                Image image = new Image(produit.getImage());
                updateImagePreview.setImage(image);
            }
        } catch (Exception e) {
            if (updateImagePreview != null) {
                updateImagePreview.setImage(null);
            }
        }

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
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer ce produit ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceProduit.supprimer(produit.getId());
                showAlert(AlertType.INFORMATION, "Succès", "Produit supprimé avec succès");
                showProduits();
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
        try {
            ObservableList<Produit> produits = FXCollections.observableArrayList(serviceProduit.afficher());
            displayProductCards(produits);
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la récupération des produits: " + e.getMessage());
        }
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
        card.setStyle("-fx-pref-width: 610; -fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3); -fx-padding: 10;");

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

        // Affichage du prix avec promotion
        VBox priceBox = new VBox(2);
        Label priceLabel = new Label(String.format("Prix: %.2f DT", produit.getPrix()));
        priceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #b11d85;");

        if (produit.getPrixPromo() > 0 && produit.getPromoExpireAt() != null &&
                produit.getPromoExpireAt().isAfter(LocalDateTime.now())) {
            Label promoPriceLabel = new Label(String.format("Prix promo: %.2f DT (-%.0f%%)",
                    produit.getPrixPromo(), produit.getReduction()));
            promoPriceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: orange;");
            Label expireLabel = new Label("Expire le: " + produit.getPromoExpireAt().toLocalDate());
            expireLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: orange;");
            priceBox.getChildren().addAll(priceLabel, promoPriceLabel, expireLabel);
        } else {
            priceBox.getChildren().add(priceLabel);
        }

        Label stockLabel = new Label("Stock: " + produit.getStock());
        stockLabel.setStyle("-fx-text-fill: black;");

        Label categoryLabel = new Label("Catégorie: " + (produit.getCategorie() != null ? produit.getCategorie().getNomcat() : "Non catégorisé"));
        categoryLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #b11d85;");

        Label advantagesLabel = new Label("Avantages: " + produit.getAvantages());
        advantagesLabel.setStyle("-fx-text-fill: black;");

        detailsBox.getChildren().addAll(nameLabel, descriptionLabel, sizeLabel, priceBox, stockLabel, categoryLabel, advantagesLabel);

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

    @FXML
    void showAddForm() {
        addProductForm.setVisible(true);
        updateProductForm.setVisible(false);

        formContainer.setVisible(true);
    }




    @FXML
    void hideForms() {
        formContainer.setVisible(false);
    }


    @FXML
    private void showStats() {
        try {
            List<Produit> produits = serviceProduit.afficher();

            // Filtrer les produits avec un stock entre 1 et 5
            List<Produit> produitsFaibleStock = produits.stream()
                    .filter(p -> p.getStock() >= 1 && p.getStock() <= 5)
                    .collect(Collectors.toList());

            // Créer une boîte de dialogue pour le graphique
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Statistiques des Produits");
            dialog.setHeaderText("Visualisation des stocks");

            VBox content = new VBox(10);
            content.setPadding(new Insets(20));

            // Créer le graphique en camembert
            PieChart pieChart = new PieChart();
            pieChart.setTitle("Répartition des Stocks par Produit");

            // Ajouter les données au graphique
            int totalStock = produits.stream().mapToInt(Produit::getStock).sum();
            for (Produit produit : produits) {
                double percentage = produit.getStock() * 100.0 / totalStock;
                pieChart.getData().add(new PieChart.Data(produit.getNom() + " (" + String.format("%.2f", percentage) + "%)", produit.getStock()));
            }
            content.getChildren().add(pieChart);

            // Afficher les produits avec faible stock
            if (!produitsFaibleStock.isEmpty()) {
                Label faibleStockLabel = new Label("Produits avec stock entre 1 et 5 (éligibles pour promotion) :");
                faibleStockLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: red;");

                VBox faibleStockList = new VBox(5);
                for (Produit p : produitsFaibleStock) {
                    Button produitButton = new Button(p.getNom() + " - Stock: " + p.getStock());
                    produitButton.setOnAction(event -> showPromotionForm(p)); // Lier chaque produit au formulaire
                    faibleStockList.getChildren().add(produitButton);
                }

                content.getChildren().addAll(faibleStockLabel, faibleStockList);
            }

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.showAndWait();

        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors du chargement des statistiques : " + e.getMessage());
        }
    }

    private void showPromotionForm(Produit produit) {
        // Créer un formulaire de promotion
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Créer une Promotion");
        dialog.setHeaderText("Promotion pour " + produit.getNom());

        // Créer les champs du formulaire
        DatePicker dateDebut = new DatePicker();
        DatePicker dateFin = new DatePicker();
        TextField pourcentageField = new TextField();
        pourcentageField.setPromptText("Pourcentage de réduction");

        // Créer la mise en page
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Date de début:"), 0, 0);
        grid.add(dateDebut, 1, 0);
        grid.add(new Label("Date de fin:"), 0, 1);
        grid.add(dateFin, 1, 1);
        grid.add(new Label("Pourcentage de réduction:"), 0, 2);
        grid.add(pourcentageField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Afficher le dialogue et gérer la réponse
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Valider les entrées
                if (dateDebut.getValue() == null || dateFin.getValue() == null ||
                        pourcentageField.getText().isEmpty()) {
                    showAlert(AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs");
                    return;
                }

                double pourcentage = Double.parseDouble(pourcentageField.getText());
                if (pourcentage <= 0 || pourcentage >= 100) {
                    showAlert(AlertType.ERROR, "Erreur", "Le pourcentage doit être entre 0 et 100");
                    return;
                }

                // Calculer le nouveau prix
                double nouveauPrix = produit.getPrix() * (1 - pourcentage / 100);

                // Appliquer la promotion
                produit.setPrixPromo(nouveauPrix);
                produit.setDate_promotion(LocalDateTime.of(dateDebut.getValue(), LocalDateTime.now().toLocalTime()));
                produit.setPromoExpireAt(LocalDateTime.of(dateFin.getValue(), LocalDateTime.now().toLocalTime()));
                produit.setReduction(pourcentage);

                serviceProduit.modifier(produit);

                // Afficher une confirmation
                Alert confirmation = new Alert(AlertType.INFORMATION);
                confirmation.setTitle("Promotion créée");
                confirmation.setHeaderText(null);
                confirmation.setContentText(String.format(
                        "Promotion appliquée avec succès!\n" +
                                "Prix après promotion: %.2f €\n" +
                                "Date d'expiration: %s",
                        nouveauPrix,
                        dateFin.getValue().toString()
                ));
                confirmation.showAndWait();

                // Rafraîchir l'affichage
                showProduits();

            } catch (NumberFormatException e) {
                showAlert(AlertType.ERROR, "Erreur", "Le pourcentage doit être un nombre valide");
            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Erreur", "Erreur lors de l'application de la promotion: " + e.getMessage());
            }
        }
    }
    private void filterProduits(String query) {
        try {
            // Récupérer tous les produits
            ObservableList<Produit> produits = FXCollections.observableArrayList(serviceProduit.afficher());

            // Filtrer les produits en fonction du nom ou de la description
            ObservableList<Produit> filteredProduits = produits.filtered(produit ->
                    produit.getNom().toLowerCase().contains(query.toLowerCase()) ||
                            produit.getDescription().toLowerCase().contains(query.toLowerCase())
            );

            // Afficher les produits filtrés
            displayProductCards(filteredProduits);
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors du filtrage des produits: " + e.getMessage());
        }
    }
    @FXML
    private void sortByNameAndDescription() {
        try {
            ObservableList<Produit> produits = FXCollections.observableArrayList(serviceProduit.afficher());

            String sortBy = sortByComboBox.getValue();  // Nom ou Description
            String sortOrder = sortOrderComboBox.getValue();  // Ascendant ou Descendant

            Comparator<Produit> comparator = null;

            // Choisir le critère de tri
            if ("Nom".equals(sortBy)) {
                comparator = Comparator.comparing(Produit::getNom);
            } else if ("Description".equals(sortBy)) {
                comparator = Comparator.comparing(Produit::getDescription);
            }

            // Appliquer l'ordre
            if ("Descendant".equals(sortOrder)) {
                comparator = comparator.reversed();
            }

            // Trier les produits
            if (comparator != null) {
                produits.sort(comparator);
            }

            // Afficher les produits triés
            displayProductCards(produits);
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors du tri par nom et description: " + e.getMessage());
        }
    }
}

package com.abircode.cruddp.Controller;
import com.abircode.cruddp.entities.Produit;
import com.abircode.cruddp.entities.Categorie;
import com.abircode.cruddp.utils.DBConnexion;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import java.time.LocalDateTime;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import java.net.URL;
import java.util.stream.Collectors;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;

public class MenuProduit implements Initializable {

    @FXML
    private VBox rootVBox;
    @FXML
    private GridPane productGrid;
    @FXML
    private VBox categoriesContainer;
    @FXML
    private VBox favoritesContainer;
    @FXML
    private HBox paginationContainer;

    @FXML
    private TextField searchField;

    private MainController mainController; // Reference to the main controller
    private List<Produit> filteredProduits = new ArrayList<>();

    private List<Produit> produits = new ArrayList<>();
    private List<Produit> favorites = new ArrayList<>();
    private static final int MAX_PRODUITS_PAR_LIGNE = 3;
    private int currentPage = 1;
    private int itemsPerPage = 6;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        produits = getAllProduitsFromDB();
        rootVBox.setPrefWidth(900);
        productGrid.setPrefWidth(900);

        for (int i = 0; i < MAX_PRODUITS_PAR_LIGNE; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / MAX_PRODUITS_PAR_LIGNE);
            productGrid.getColumnConstraints().add(column);
        }
        setupSearchField();
        loadCategories();
        loadProducts();
        setupPagination();
        loadFavorites();
    }

    private void loadProducts() {
        productGrid.getChildren().clear();
        int col = 0, row = 0;

        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, produits.size());

        for (int i = startIndex; i < endIndex; i++) {
            Produit produit = produits.get(i);
            VBox productCard = createProductCard(produit);

            productGrid.add(productCard, col, row);
            col++;
            if (col == MAX_PRODUITS_PAR_LIGNE) {
                col = 0;
                row++;
            }
        }
    }
    private VBox createProductCard(Produit produit) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-radius: 8; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefWidth(420);
        card.setMaxWidth(420);
        card.setAlignment(Pos.TOP_CENTER); // Centrer le contenu de la carte

        // Conteneur pour l'image
        StackPane imageContainer = new StackPane();
        imageContainer.setAlignment(Pos.TOP_CENTER);

        ImageView imageView = new ImageView();
        try {
            Image image = new Image(produit.getImage());
            imageView.setImage(image);
            imageView.setFitWidth(300);
            imageView.setFitHeight(200);
            imageView.setPreserveRatio(true);
        } catch (Exception e) {
            imageView.setFitWidth(300);
            imageView.setFitHeight(200);
        }

        // Bouton favori positionné en haut
        Button favButton = new Button("♥");
        favButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef0b33; -fx-font-size: 20px; " +
                "-fx-border-color: #df0c31; -fx-border-radius: 50%; -fx-background-radius: 50%; " +
                "-fx-padding: 5; -fx-min-width: 30; -fx-min-height: 30;");

        StackPane.setAlignment(favButton, Pos.TOP_CENTER);
        StackPane.setMargin(favButton, new Insets(10, 0, 0, 0)); // Marge seulement en haut

        // Style différent si déjà dans les favoris
        if (favorites.contains(produit)) {
            favButton.setStyle("-fx-background-color: #d80b2e; -fx-text-fill: white; -fx-font-size: 20px; " +
                    "-fx-border-color: #e60d33; -fx-border-radius: 50%; -fx-background-radius: 50%; " +
                    "-fx-padding: 5; -fx-min-width: 30; -fx-min-height: 30;");
        }

        favButton.setOnAction(e -> toggleFavorite(produit, favButton));

        imageContainer.getChildren().addAll(imageView, favButton);

        // Conteneur des informations
        VBox infoBox = new VBox(8);
        infoBox.setStyle("-fx-padding: 10 0 0 0;");
        infoBox.setAlignment(Pos.CENTER); // Centrer le contenu

        Label nomLabel = new Label(produit.getNom());
        nomLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: black;");

        // Affichage du prix avec promotion
        Label prixLabel;
        if (produit.getPrixPromo() > 0 && produit.getPromoExpireAt() != null &&
                produit.getPromoExpireAt().isAfter(LocalDateTime.now())) {
            // Affichage du prix barré
            prixLabel = new Label(String.format("%.2f DT", produit.getPrix()));
            prixLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: grey; -fx-strikethrough: true;");

            // Affichage du prix promo
            Label promoPriceLabel = new Label(String.format("Promo: %.2f DT (-%.0f%%)", produit.getPrixPromo(), produit.getReduction()));
            promoPriceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: orange; -fx-font-weight: bold;");

            infoBox.getChildren().addAll(nomLabel, prixLabel, promoPriceLabel);
        } else {
            // Affichage du prix normal
            prixLabel = new Label(String.format("%.2f DT", produit.getPrix()));
            prixLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #b11d85; -fx-font-weight: bold;");
            infoBox.getChildren().addAll(nomLabel, prixLabel);
        }

        Label stockLabel = new Label("En stock: " + produit.getStock());
        stockLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: black;");

        Button moreButton = new Button("plus de détails");
        moreButton.setStyle("-fx-background-color: #b11d85; -fx-text-fill: white; -fx-font-size: 12px; " +
                "-fx-background-radius: 4; -fx-padding: 8 16;");
        moreButton.setOnAction(e -> showProductDetails(produit));

        infoBox.getChildren().addAll(stockLabel, moreButton);
        card.getChildren().addAll(imageContainer, infoBox);

        return card;
    }

    private void toggleFavorite(Produit produit, Button favButton) {
        if (favorites.contains(produit)) {
            removeFromFavorites(produit);
            favButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #ec0b32; -fx-font-size: 20px; " +
                    "-fx-border-color: #ec082f; -fx-border-radius: 50%; -fx-background-radius: 50%; " +
                    "-fx-padding: 5; -fx-min-width: 30; -fx-min-height: 30;");
        } else {
            addToFavorites(produit);
            favButton.setStyle("-fx-background-color: #e60a31; -fx-text-fill: white; -fx-font-size: 20px; " +
                    "-fx-border-color: #ec0b33; -fx-border-radius: 50%; -fx-background-radius: 50%; " +
                    "-fx-padding: 5; -fx-min-width: 30; -fx-min-height: 30;");
        }
    }
    private void addToFavorites(Produit produit) {
        if (!favorites.contains(produit)) {
            favorites.add(produit);
            loadFavorites();
        }
    }

    private void removeFromFavorites(Produit produit) {
        favorites.remove(produit);
        loadFavorites();
    }

    private void loadFavorites() {
        favoritesContainer.getChildren().clear();
        favoritesContainer.setSpacing(10);  // Space between each favorite item

        // Iterate through the favorites list
        for (Produit produit : favorites) {
            // Create a VBox for each favorite product with only the name and a delete button
            VBox productCard = new VBox(5);
            productCard.setStyle("-fx-background-color: #e381dc; -fx-padding: 15; -fx-border-radius: 8; -fx-background-radius: 8; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
            productCard.setPrefWidth(270);
            productCard.setMaxWidth(270);

            // Display only the product name
            Label nomLabel = new Label(produit.getNom());
            nomLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: black;");
            productCard.getChildren().add(nomLabel);

            // Create the delete button and style it
            Button deleteButton = new Button("♥");
            deleteButton.setStyle(" -fx-text-fill: e74c3c; -fx-font-size: 18px; " +
                    "-fx-background-radius: 4; ");
            deleteButton.setOnAction(e -> removeFromFavorites(produit));

            // Add the delete button to the product card
            productCard.getChildren().add(deleteButton);

            // Add the product card to the container
            favoritesContainer.getChildren().add(productCard);
        }
    }


    private void showProductDetails(Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/DetailProduit.fxml"));
            Parent root = loader.load();
            DetailProduitController controller = loader.getController();
            controller.setProduit(produit);
            mainController.getContentArea().getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load product details: " + e.getMessage());
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void loadCategories() {
        List<Categorie> categories = getAllCategoriesFromDB();

        for (Categorie category : categories) {
            VBox categoryBox = createCategoryBox(category);
            categoriesContainer.getChildren().add(categoryBox);
        }
    }

    private List<Categorie> getAllCategoriesFromDB() {
        List<Categorie> categories = new ArrayList<>();
        String query = "SELECT * FROM categorie";

        try (var con = DBConnexion.getCon();
             var stmt = con.createStatement();
             var rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Categorie category = new Categorie();
                category.setId(rs.getInt("id"));
                category.setNomcat(rs.getString("nomcat"));
                categories.add(category);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return categories;
    }

    private VBox createCategoryBox(Categorie category) {
        VBox box = new VBox(5);
        Button categoryBtn = new Button(category.getNomcat());
        categoryBtn.setStyle("-fx-background-color: #b11d85; -fx-text-fill: white; -fx-background-radius: 4; " +
                "-fx-padding: 8 16; -fx-font-size: 12px;");
        categoryBtn.setPrefWidth(260);
        categoryBtn.setOnAction(e -> filterByCategory(category));

        box.getChildren().add(categoryBtn);
        return box;
    }

    private void filterByCategory(Categorie category) {
        List<Produit> filteredProducts = produits.stream()
                .filter(p -> p.getCategorie().getId() == category.getId())
                .toList();

        displayFilteredProducts(filteredProducts);
    }

    private void displayFilteredProducts(List<Produit> filteredProducts) {
        productGrid.getChildren().clear();
        int col = 0, row = 0;

        for (Produit product : filteredProducts) {
            VBox productCard = createProductCard(product);
            productGrid.add(productCard, col, row);
            col++;
            if (col == MAX_PRODUITS_PAR_LIGNE) {
                col = 0;
                row++;
            }
        }
    }

    private void setupPagination() {
        updatePagination();
    }

    private void updatePagination() {
        paginationContainer.getChildren().clear();
        int totalPages = (int) Math.ceil(produits.size() / (double) itemsPerPage);

        if (currentPage > 1) {
            Button prevBtn = new Button("Précédent");
            prevBtn.setStyle("-fx-background-color: #b11d85; -fx-text-fill: white; -fx-background-radius: 4; " +
                    "-fx-padding: 8 16; -fx-font-size: 12px;");
            prevBtn.setOnAction(e -> {
                currentPage--;
                loadProducts();
                updatePagination();
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
                loadProducts();
                updatePagination();
            });
            paginationContainer.getChildren().add(pageBtn);
        }

        if (currentPage < totalPages) {
            Button nextBtn = new Button("Suivant");
            nextBtn.setStyle("-fx-background-color: #b11d85; -fx-text-fill: white; -fx-background-radius: 4; " +
                    "-fx-padding: 8 16; -fx-font-size: 12px;");
            nextBtn.setOnAction(e -> {
                currentPage++;
                loadProducts();
                updatePagination();
            });
            paginationContainer.getChildren().add(nextBtn);
        }
    }

    private List<Produit> getAllProduitsFromDB() {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT p.*, c.nomcat, c.descriptioncat FROM produit p LEFT JOIN categorie c ON p.categorie_id = c.id";

        try (var con = DBConnexion.getCon();
             var stmt = con.createStatement();
             var rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Produit produit = new Produit();
                produit.setId(rs.getInt("id"));
                produit.setNom(rs.getString("nomprod"));
                produit.setDescription(rs.getString("descriptionprod"));
                produit.setPrix(rs.getDouble("prixprod"));
                produit.setStock(rs.getInt("nombre_produits_en_stock"));
                produit.setImage(rs.getString("image_large"));

                Categorie categorie = new Categorie();
                categorie.setId(rs.getInt("categorie_id"));
                categorie.setNomcat(rs.getString("nomcat"));
                produit.setCategorie(categorie);

                produits.add(produit);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return produits;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void setupSearchField() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String query = newValue.toLowerCase().trim();

            // Filtrer les produits en fonction du nom et du prix
            filteredProduits = produits.stream()
                    .filter(p -> p.getNom().toLowerCase().contains(query) ||
                            String.valueOf(p.getPrix()).contains(query))
                    .collect(Collectors.toList());

            // Réinitialiser la page actuelle à 1 après chaque recherche
            currentPage = 1;

            // Afficher les produits filtrés
            loadProducts(filteredProduits);

            // Mettre à jour la pagination
            updatePagination();
        });
    }

    private void loadProducts(List<Produit> produitsList) {
        // Si des produits sont filtrés, les utiliser ; sinon afficher tous les produits
        List<Produit> productsToDisplay = produitsList.isEmpty() ? produits : produitsList;

        productGrid.getChildren().clear();
        int col = 0, row = 0;

        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, productsToDisplay.size());

        for (int i = startIndex; i < endIndex; i++) {
            Produit produit = productsToDisplay.get(i);
            VBox productCard = createProductCard(produit);

            productGrid.add(productCard, col, row);
            col++;
            if (col == MAX_PRODUITS_PAR_LIGNE) {
                col = 0;
                row++;
            }
        }
    }

}

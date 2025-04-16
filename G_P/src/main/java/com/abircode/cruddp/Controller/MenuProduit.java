package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Produit;
import com.abircode.cruddp.entities.Categorie;
import com.abircode.cruddp.utils.DBConnexion;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;

public class MenuProduit implements Initializable {

    @FXML
    private VBox rootVBox; // Changement de ScrollPane à VBox
    @FXML
    private GridPane productGrid;
    @FXML
    private VBox categoriesContainer;
    @FXML
    private VBox favoritesContainer;
    @FXML
    private HBox paginationContainer;

    private List<Produit> produits = new ArrayList<>();
    private static final int MAX_PRODUITS_PAR_LIGNE = 3;
    private int currentPage = 1;
    private int itemsPerPage = 6; // Affichage de 6 produits par page
    private List<Produit> favorites = new ArrayList<>();
    private MainController mainController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        produits = getAllProduitsFromDB();

        // Configuration du VBox pour une largeur fixe
        rootVBox.setPrefWidth(900);

        // Configuration du GridPane
        productGrid.setPrefWidth(900);

        // Configuration des colonnes
        for (int i = 0; i < MAX_PRODUITS_PAR_LIGNE; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / MAX_PRODUITS_PAR_LIGNE);
            productGrid.getColumnConstraints().add(column);
        }

        loadCategories();
        loadProducts();
        setupPagination();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
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
        card.setPrefWidth(270);
        card.setMaxWidth(270);

        // Image du produit
        ImageView imageView = new ImageView();
        try {
            Image image = new Image(produit.getImage());
            imageView.setImage(image);
            imageView.setFitWidth(220);
            imageView.setFitHeight(200);
            imageView.setPreserveRatio(true);
        } catch (Exception e) {
            imageView.setFitWidth(220);
            imageView.setFitHeight(200);
        }

        // Informations du produit
        VBox infoBox = new VBox(8);
        infoBox.setStyle("-fx-padding: 10 0 0 0;");

        Label nomLabel = new Label(produit.getNom());
        nomLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: black;");

        Label prixLabel = new Label(String.format("%.2f €", produit.getPrix()));
        prixLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #b11d85; -fx-font-weight: bold;");

        Label stockLabel = new Label("En stock: " + produit.getStock());
        stockLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: black;");

        // Bouton More
        Button moreButton = new Button("More Details");
        moreButton.setStyle("-fx-background-color: #b11d85; -fx-text-fill: white; -fx-font-size: 12px; " +
                "-fx-background-radius: 4; -fx-padding: 8 16;");
        moreButton.setOnAction(e -> showProductDetails(produit));

        infoBox.getChildren().addAll(nomLabel, prixLabel, stockLabel, moreButton);
        card.getChildren().addAll(imageView, infoBox);
        card.setPadding(new Insets(15));

        return card;
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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

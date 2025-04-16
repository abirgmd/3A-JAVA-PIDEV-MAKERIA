package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Produit;
import com.abircode.cruddp.utils.DBConnexion;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DetailProduitController {
    @FXML private Hyperlink backToProductsLink;
    @FXML private Text productNameBreadcrumb;
    @FXML private ImageView mainImageView;
    @FXML private HBox imageContainer;
    @FXML private Text productNameLabel;
    @FXML private Text productDescriptionLabel;
    @FXML private Text productPriceLabel;
    @FXML private Text productStockLabel;
    @FXML private Text productSizeLabel;
    @FXML private Text productColorLabel;
    @FXML private Text productCategoryLabel;
    @FXML private TextField quantityField;

    private Produit currentProduit;
    private Connection connection;
    private MainController mainController;
    private int currentQuantity = 1;

    public DetailProduitController() {
        this.connection = DBConnexion.getCon();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setProduit(Produit produit) {
        this.currentProduit = produit;
        loadProductData();
    }

    private void loadProductData() {
        if (currentProduit != null) {
            productNameLabel.setText(currentProduit.getNom());
            productDescriptionLabel.setText(currentProduit.getDescription());
            productPriceLabel.setText(String.format("%.2f", currentProduit.getPrix()));
            productStockLabel.setText(String.valueOf(currentProduit.getStock()));
            productNameBreadcrumb.setText(currentProduit.getNom());
            loadProductImages();
        }
    }

    private void loadProductImages() {
        if (currentProduit != null && imageContainer != null) {
            imageContainer.getChildren().clear();
            try {
                String query = "SELECT image_large FROM produit WHERE id = ?";
                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setInt(1, currentProduit.getId());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            String imagePath = rs.getString("image_large");
                            if (imagePath != null && !imagePath.isEmpty()) {
                                Image image = new Image(imagePath);
                                mainImageView.setImage(image);
                                mainImageView.setFitWidth(400);
                                mainImageView.setFitHeight(400);
                                mainImageView.setPreserveRatio(true);
                                
                                // Créer une miniature de la même image
                                ImageView thumbnailView = new ImageView(image);
                                thumbnailView.setFitWidth(100);
                                thumbnailView.setFitHeight(100);
                                thumbnailView.setPreserveRatio(true);
                                thumbnailView.setOnMouseClicked(event -> {
                                    mainImageView.setImage(image);
                                });
                                imageContainer.getChildren().add(thumbnailView);
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void navigateToProducts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/MenuProduit.fxml"));
            Parent root = loader.load();
            MenuProduit controller = loader.getController();
            controller.setMainController(mainController);
            mainController.getContentArea().getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to navigate back to products: " + e.getMessage());
        }
    }

    @FXML
    private void initialize() {
        quantityField.setText("1");
        quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                quantityField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            try {
                currentQuantity = Integer.parseInt(quantityField.getText());
                if (currentQuantity < 1) {
                    currentQuantity = 1;
                    quantityField.setText("1");
                }
            } catch (NumberFormatException e) {
                quantityField.setText("1");
                currentQuantity = 1;
            }
        });
    }

    @FXML
    private void decreaseQuantity() {
        if (currentQuantity > 1) {
            currentQuantity--;
            quantityField.setText(String.valueOf(currentQuantity));
        }
    }

    @FXML
    private void increaseQuantity() {
        currentQuantity++;
        quantityField.setText(String.valueOf(currentQuantity));
    }

    @FXML
    private void addToCart() {
        if (currentProduit != null) {
            try {
                if (currentProduit.getStock() >= currentQuantity) {
                    showAlert("Success", "Added " + currentQuantity + " " + currentProduit.getNom() + " to cart");
                } else {
                    showAlert("Error", "Not enough stock available");
                }
            } catch (Exception e) {
                showAlert("Error", "Failed to add to cart: " + e.getMessage());
            }
        }
    }

    @FXML
    private void navigateToReviewForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/review_form.fxml"));
            Parent root = loader.load();
            ReviewFormController controller = loader.getController();
            controller.setCurrentProduct(currentProduit);

            Stage stage = new Stage();
            stage.setTitle("Add Review");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load review form: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 
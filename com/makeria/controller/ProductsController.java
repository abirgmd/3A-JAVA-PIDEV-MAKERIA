package com.makeria.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.ArrayList;

public class ProductsController {
    @FXML
    private GridPane productsGrid;
    @FXML
    private ComboBox<String> categoryFilter;
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private VBox productDetails;

    private ObservableList<Product> products = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize category filter
        categoryFilter.setItems(FXCollections.observableArrayList(
            "All Categories",
            "Electronics",
            "Clothing",
            "Books",
            "Home & Garden"
        ));
        categoryFilter.setValue("All Categories");

        // Initialize search functionality
        searchButton.setOnAction(e -> handleSearch());

        // Load initial products
        loadProducts();

        // Setup product grid
        setupProductGrid();
    }

    private void loadProducts() {
        // TODO: Replace with actual database call
        products.addAll(
            new Product("Product 1", 99.99, "Electronics", "product1.jpg"),
            new Product("Product 2", 149.99, "Clothing", "product2.jpg"),
            new Product("Product 3", 29.99, "Books", "product3.jpg")
        );
    }

    private void setupProductGrid() {
        int column = 0;
        int row = 0;
        int maxColumns = 3;

        for (Product product : products) {
            VBox productCard = createProductCard(product);
            productsGrid.add(productCard, column, row);

            column++;
            if (column >= maxColumns) {
                column = 0;
                row++;
            }
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        // TODO: Load actual product image
        // imageView.setImage(new Image(getClass().getResourceAsStream("/images/" + product.getImagePath())));

        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label priceLabel = new Label(String.format("$%.2f", product.getPrice()));
        priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #731000;");

        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.setStyle("-fx-background-color: #FF2400; -fx-text-fill: white;");
        addToCartButton.setOnAction(e -> handleAddToCart(product));

        card.getChildren().addAll(imageView, nameLabel, priceLabel, addToCartButton);
        return card;
    }

    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        String selectedCategory = categoryFilter.getValue();

        ObservableList<Product> filteredProducts = FXCollections.observableArrayList();

        for (Product product : products) {
            boolean matchesSearch = product.getName().toLowerCase().contains(searchText);
            boolean matchesCategory = selectedCategory.equals("All Categories") || 
                                    product.getCategory().equals(selectedCategory);

            if (matchesSearch && matchesCategory) {
                filteredProducts.add(product);
            }
        }

        updateProductGrid(filteredProducts);
    }

    private void updateProductGrid(ObservableList<Product> filteredProducts) {
        productsGrid.getChildren().clear();
        int column = 0;
        int row = 0;
        int maxColumns = 3;

        for (Product product : filteredProducts) {
            VBox productCard = createProductCard(product);
            productsGrid.add(productCard, column, row);

            column++;
            if (column >= maxColumns) {
                column = 0;
                row++;
            }
        }
    }

    private void handleAddToCart(Product product) {
        // TODO: Implement add to cart functionality
        System.out.println("Adding to cart: " + product.getName());
    }

    // Product class to hold product data
    public static class Product {
        private String name;
        private double price;
        private String category;
        private String imagePath;

        public Product(String name, double price, String category, String imagePath) {
            this.name = name;
            this.price = price;
            this.category = category;
            this.imagePath = imagePath;
        }

        public String getName() { return name; }
        public double getPrice() { return price; }
        public String getCategory() { return category; }
        public String getImagePath() { return imagePath; }
    }
} 
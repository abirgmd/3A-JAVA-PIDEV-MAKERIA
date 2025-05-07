package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.OrdersDetails;
import com.abircode.cruddp.entities.Produit;
import com.abircode.cruddp.services.CartService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;

public class CartItemController {
    @FXML
    private ImageView productImage;

    @FXML
    private Label productName;

    @FXML
    private Label productSize;

    @FXML
    private Label productColor;

    @FXML
    private Label productPrice;

    @FXML
    private TextField productQuantity;

    @FXML
    private Button plusButton;

    @FXML
    private Button minusButton;

    @FXML
    private Button removeButton;

    private OrdersDetails ordersDetails;
    private CartService cartService;
    private CartController cartController;

    public void setData(OrdersDetails ordersDetails, CartService cartService, CartController cartController) {
        this.ordersDetails = ordersDetails;
        this.cartService = cartService;
        this.cartController = cartController;

        Produit produit = ordersDetails.getProduit();

        // Affichage du nom et du prix du produit
        productName.setText(produit.getNom());
        productPrice.setText(produit.getPrix() + " DNT");

        // Affichage de la taille
        productSize.setText(produit.getSize());

        // Puisque vous n'avez pas mentionné de champ pour la couleur dans l'entité Produit,
        // on pourrait le cacher ou le laisser vide
        productColor.setText("");

        // Chargement de l'image
        try {
            String imagePath = produit.getImage(); // Utilise image_large
            if (imagePath != null && !imagePath.isEmpty()) {
                File file = new File(imagePath);
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    productImage.setImage(image);
                } else {
                    // Si le chemin n'est pas un fichier local, essayez de le charger directement
                    Image image = new Image(imagePath);
                    productImage.setImage(image);
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors du chargement de l'image: " + e.getMessage());
            // Vous pourriez charger une image par défaut ici
        }

        // Affichage de la quantité
        int quantity = ordersDetails.getQuantity();
        productQuantity.setText(String.valueOf(quantity));

        // Supprimer du panier
        removeButton.setOnAction(event -> {
            cartService.removeProductFromCart(produit);
            cartController.refreshCart();
        });

        // Augmenter la quantité
        plusButton.setOnAction(event -> {
            cartService.addProductToCart(produit); // Incrémente automatiquement
            cartController.refreshCart();
        });

        // Diminuer la quantité
        minusButton.setOnAction(event -> {
            cartService.decreaseProductQuantity(produit);
            cartController.refreshCart();
        });
    }
}
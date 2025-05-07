package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.OrdersDetails;
import com.abircode.cruddp.services.CartService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CartController {

    @FXML private ListView<HBox> cartListView;
    @FXML private Label totalPriceLabel;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Button proceedToPayBtn;

    private final CartService cartService = CartService.getInstance();
    private MainController mainController;

    @FXML
    public void initialize() {
        sortComboBox.setValue("Price");
        sortComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> refreshCart());

        proceedToPayBtn.setOnAction(e -> handleProceedToPayment());

        refreshCart();
    }

    public void setMainController(MainController mc) {
        this.mainController = mc;
    }

    public void refreshCart() {
        cartListView.getItems().clear();

        List<OrdersDetails> items = cartService.getCartItems();
        String crit = sortComboBox.getValue();
        if ("Price".equals(crit)) {
            items = items.stream()
                    .sorted(Comparator.comparing(od -> od.getProduit().getPrix()))
                    .collect(Collectors.toList());
        } else if ("Quantity".equals(crit)) {
            items = items.stream()
                    .sorted(Comparator.comparing(OrdersDetails::getQuantity))
                    .collect(Collectors.toList());
        }

        double total = 0;
        for (OrdersDetails od : items) {
            total += od.getProduit().getPrix() * od.getQuantity();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/CartItem.fxml"));
                HBox row = loader.load();
                CartItemController c = loader.getController();
                c.setData(od, cartService, this);
                cartListView.getItems().add(row);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        totalPriceLabel.setText(String.format("%.2f DNT", total));
    }

    private double calculateTotalPrice() {
        return cartService.getCartItems()
                .stream()
                .mapToDouble(od -> od.getProduit().getPrix() * od.getQuantity())
                .sum();
    }

    @FXML
    private void handleProceedToPayment() {
        if (cartService.getCartItems().isEmpty()) {
            new Alert(Alert.AlertType.WARNING,
                    "Votre panier est vide. Veuillez ajouter des produits avant de procéder au paiement.")
                    .showAndWait();
            return;
        }

        try {
            double montantTotal = calculateTotalPrice();

            // Appel Stripe Checkout
            com.stripe.model.checkout.Session session =
                    com.abircode.cruddp.services.PaiementService
                            .getInstance()
                            .createCheckoutSession(montantTotal);

            // Ouvre la session Stripe Checkout dans le navigateur
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(session.getUrl()));
            } else {
                new Alert(Alert.AlertType.ERROR, "Impossible d’ouvrir le navigateur pour effectuer le paiement.")
                        .showAndWait();
            }

            // ⚠️ BONUS : ici tu peux gérer le retour du paiement via webhook ou redirection
            // Pour l’instant, on suppose que le paiement est fait manuellement.

        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Une erreur est survenue pendant le paiement :\n" + ex.getMessage())
                    .showAndWait();
        }
    }
}

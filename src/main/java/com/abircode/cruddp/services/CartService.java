package com.abircode.cruddp.services;

import com.abircode.cruddp.entities.OrdersDetails;
import com.abircode.cruddp.entities.Produit;
import com.abircode.cruddp.Controller.MainController;

import java.util.ArrayList;
import java.util.List;

public class CartService {
    private static CartService instance;
    private final List<OrdersDetails> cartItems;
    private MainController mainController;

    private CartService() {
        cartItems = new ArrayList<>();
    }

    public static CartService getInstance() {
        if (instance == null) {
            instance = new CartService();
        }
        return instance;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void addProductToCart(Produit produit) {
        for (OrdersDetails item : cartItems) {
            if (item.getProduit().getId() == produit.getId()) {
                item.setQuantity(item.getQuantity() + 1);
                notifyCartChanged();
                return;
            }
        }

        OrdersDetails newItem = new OrdersDetails();
        newItem.setProduit(produit);
        newItem.setQuantity(1);
        cartItems.add(newItem);
        notifyCartChanged();
    }

    public void removeProductFromCart(Produit produit) {
        cartItems.removeIf(item -> item.getProduit().getId() == produit.getId());
        notifyCartChanged();
    }

    public void decreaseProductQuantity(Produit produit) {
        for (int i = 0; i < cartItems.size(); i++) {
            OrdersDetails item = cartItems.get(i);
            if (item.getProduit().getId() == produit.getId()) {
                int qty = item.getQuantity();
                if (qty > 1) {
                    item.setQuantity(qty - 1);
                } else {
                    cartItems.remove(i);
                }
                break;
            }
        }
        notifyCartChanged();
    }

    public List<OrdersDetails> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public int getCartSize() {
        return cartItems.size();
    }

    public void clearCart() {
        cartItems.clear();
        notifyCartChanged();
    }

    private void notifyCartChanged() {
        if (mainController != null) {
            mainController.updateCartBadge(cartItems.size());
        }
    }
}

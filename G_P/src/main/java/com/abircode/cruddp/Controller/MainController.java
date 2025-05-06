package com.abircode.cruddp.Controller;
import com.abircode.cruddp.Controller.user.UserProfileController;
import com.abircode.cruddp.entities.Produit;

import com.abircode.cruddp.entities.User;
import com.abircode.cruddp.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.geometry.Pos;
import java.io.IOException;

public class MainController {
    @FXML private VBox mainContainer;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Button homeButton;
    @FXML private MenuButton productsMenu;
    @FXML private Button forumButton;
    @FXML private MenuButton eventsMenu;
    @FXML private MenuButton reclamationMenu;
    @FXML private MenuButton forumMenu;
    @FXML private Button cartButton;
    @FXML private StackPane contentPane;
    User currentUser = SessionManager.getCurrentUser();

    @FXML
    public void initialize() {

        setupEventHandlers();
        setupMenuItems();
        // Charger la page d'accueil par défaut
        loadContent("/Fxml/HomeF.fxml");
        if (currentUser != null) {

            loginButton.setText("Logout");
            loginButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");

            registerButton.setText("Mon Profile");
            //transforamtion registre button to my profile
            registerButton.setOnAction(event -> {
                // Implement register functionality



                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/profile.fxml"));
                        Node content = loader.load();
                        UserProfileController controller = loader.getController();
                        controller.initializeUserData(currentUser.getId());
                        if (contentPane != null) {
                            contentPane.getChildren().clear();
                            contentPane.getChildren().add(content);
                            StackPane.setAlignment(content, Pos.CENTER);
                        }
                    } catch (IOException e) {
                        System.err.println("Error loading " + "/user/profile.fxml" + ": " + e.getMessage());
                        e.printStackTrace();
                    }











             });



            loginButton.setOnAction(event -> {
                loginButton.setText("Login");
                registerButton.setText("Register");
                loginButton.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-text-fill: #000000;" +
                                "-fx-border-color: #000036;" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 5;" +
                                "-fx-background-radius: 5;"
                );
                registerButton.setDisable(false);

                SessionManager.clearSession();

                loadContent("/login.fxml");
            });
        }
    }

    private void setupEventHandlers() {
        // Search functionality
        if (searchButton != null && searchField != null) {
            searchButton.setOnAction(event -> {
                String searchText = searchField.getText();
                // Implement search functionality
                System.out.println("Searching for: " + searchText);
            });
        }

        // Auth buttons
        if (loginButton != null) {
            loginButton.setOnAction(event -> {
                // Implement login functionality
                loadContent("/login.fxml");
            });
        }

        if (registerButton != null) {
            registerButton.setOnAction(event -> {
                // Implement register functionality
                loadContent("/register.fxml");
            });
        }

        // Forum button
        if (forumButton != null) {
            forumButton.setOnAction(event -> {
                loadContent("/Fxml/Forum.fxml");
            });
        }

        // Cart button
        if (cartButton != null) {
            cartButton.setOnAction(event -> {
                loadContent("/Fxml/Cart.fxml");
            });
        }
    }

    private void setupMenuItems() {
        // Products menu
        if (productsMenu != null) {
            MenuItem allProducts = productsMenu.getItems().get(0);
            allProducts.setOnAction(event -> {
                loadProductsView();
            });
        }

        // Events menu
        if (eventsMenu != null) {
            MenuItem goToEvents = eventsMenu.getItems().get(0);

            goToEvents.setOnAction(event -> {
                loadContent("/Fxml/eventM.fxml");
            });
            

        }

        // Reclamation menu
        if (reclamationMenu != null) {
            MenuItem addReclamation = reclamationMenu.getItems().get(0);
            MenuItem myReclamations = reclamationMenu.getItems().get(1);

            addReclamation.setOnAction(event -> {
                loadContent("/Fxml/ajouter-reclamation.fxml");
            });

            myReclamations.setOnAction(event -> {
                loadContent("/Fxml/afficher-reclamation-user.fxml");
            });
        }
        if (forumMenu != null) {
            MenuItem myForum = forumMenu.getItems().get(0);
            MenuItem myReplys = forumMenu.getItems().get(1);

            myForum.setOnAction(event -> {
                loadContent("/Fxml/AfficherMessageAdmin.fxml");
            });

            myReplys.setOnAction(event -> {
                loadContent("/Fxml/AfficherReplyAdmin.fxml");
            });
        }
    }

    @FXML
    public void onHomeClicked() {
        loadContent("/Fxml/HomeF.fxml");
    }

    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();

            if (contentPane != null) {
                contentPane.getChildren().clear();
                contentPane.getChildren().add(content);
                StackPane.setAlignment(content, Pos.CENTER);
            }
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadProductsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/MenuProduit.fxml"));
            Node content = loader.load();

           MenuProduit controller = loader.getController();
            controller.setMainController(this);

            if (contentPane != null) {
                contentPane.getChildren().clear();
                contentPane.getChildren().add(content);
                StackPane.setAlignment(content, Pos.CENTER);
            }
        } catch (IOException e) {
            System.err.println("Error loading products view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadProductDetails(Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/DetailProduit.fxml"));
            Node content = loader.load();

            // Get the DetailProduit controller and set the product data
            DetailProduitController controller = loader.getController();
            controller.setMainController(this);
            controller.setProduit(produit);

            if (contentPane != null) {
                contentPane.getChildren().clear();
                contentPane.getChildren().add(content);
                StackPane.setAlignment(content, Pos.CENTER);
            }
        } catch (IOException e) {
            System.err.println("Error loading product details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public StackPane getContentArea() {
        return contentPane;
    }



} 
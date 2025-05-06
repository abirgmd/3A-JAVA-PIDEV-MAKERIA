package com.abircode.cruddp.Controller;

import com.abircode.cruddp.Controller.user.UserProfileController;
import com.abircode.cruddp.entities.User;
import com.abircode.cruddp.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class ProduitController implements Initializable {
    @FXML
    private VBox sidebar;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (SessionManager.isArtist()) {
            // Disable all menu buttons except Products
            disableAllMenusExceptProducts();
            // Load products view by default for artists
            loadUI("ProduitForm.fxml");
        }
    }

    private void disableAllMenusExceptProducts() {
        // Get all children of the sidebar (which are the MenuButtons)
        for (Node node : sidebar.getChildren()) {
            if (node instanceof MenuButton) {
                MenuButton menuButton = (MenuButton) node;
                // Only enable the "Produits" menu button
                if (!"Produits".equals(menuButton.getText())) {
                    menuButton.setDisable(true);
                }
            }
        }
    }



    @FXML
    private StackPane contentPane;

    @FXML

    void showCommentPane(ActionEvent event) {
        loadUI("commentaire.fxml"); // Charger le formulaire ici
    }

    @FXML
    void showCategoriesPane(ActionEvent event) {
        loadUI("CategorielForm.fxml");
    }

    @FXML

    void showCalenderPane(ActionEvent event) {
        loadUI("Calendrier.fxml");
    }
    @FXML
    void showeventPane(ActionEvent event) {
        loadUI("event.fxml");
    }

    @FXML
    void showEvaluationsPane(ActionEvent event) {
        loadUI("EvaluationForm.fxml");
    }

    @FXML
    void showProduitsPane(ActionEvent event) {
        loadUI("ProduitForm.fxml");
    }
    @FXML
    void showForumPane(ActionEvent event) {
        loadUI("AfficherMessage.fxml");
    }
    @FXML
    void addMessage(ActionEvent event) {
        loadUI("AjouterMessage.fxml");
    }
    @FXML
    void showReclamationPane(ActionEvent event) {
        loadUI("afficher-reclamation.fxml"); // Charger le formulaire ici
    }
    @FXML
    void showReclamationUserPane(ActionEvent event) {
        loadUI("afficher-reclamation.fxml");
    }
    @FXML
    void showReponsesPane(ActionEvent event) {
        loadUI("afficher-reponses.fxml"); // Charger le formulaire ici
    }
    @FXML
    void addReclamation(ActionEvent event) {
        loadUI("ajouter-reclamation.fxml"); // Charger le formulaire ici
    }


    @FXML
    void addImg(ActionEvent event) {
        loadUI("generateImage.fxml"); // Charger le formulaire ici
    }
    @FXML
    void addImgCom(ActionEvent event) {
        loadUI("imagecompatible.fxml"); // Charger le formulaire ici
    }
    @FXML
    void addS(ActionEvent event) {
        loadUI("AIContentExplainer.fxml"); // Charger le formulaire ici
    }



    private void loadUI(String fxml) {
        try {
            // Charger le fichier FXML avec le chemin absolu
            URL fxmlURL = getClass().getResource("/Fxml/" + fxml);

            // Vérifier que le fichier FXML existe
            if (fxmlURL != null) {
                FXMLLoader loader = new FXMLLoader(fxmlURL);
                Node node = loader.load(); // Utilisation de la méthode load() de FXMLLoader
                contentPane.getChildren().setAll(node);
            } else {
                System.err.println("Fichier FXML non trouvé : " + fxml);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du fichier FXML : " + fxml);
            e.printStackTrace();
        }
    }
    @FXML
    public void showmenuprofile(ActionEvent event) {

    }
    User currentUser = SessionManager.getCurrentUser();

    @FXML
    public void showprofile(ActionEvent event) {
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

    }
    @FXML
    public void logout(ActionEvent event) throws IOException {
        SessionManager.clearSession();

        // Get reference to the current stage through the menu button
        MenuItem menuItem = (MenuItem) event.getSource();
        ContextMenu menu = menuItem.getParentPopup();
        Node node = menu.getOwnerNode();
        Stage currentStage = (Stage) node.getScene().getWindow();

        // Load the login screen
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/MakeriaF.fxml")));

        // Create new scene with styles
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());

        // Configure the stage
        currentStage.setScene(scene);
        currentStage.setFullScreen(true);
        currentStage.setTitle("Login");
        currentStage.show();
    }
    @FXML
    public void showusers(ActionEvent event) {


        try {
            // Charger le fichier FXML avec le chemin absolu
            URL fxmlURL = getClass().getResource("/admin/userManagement.fxml");

            // Vérifier que le fichier FXML existe
            if (fxmlURL != null) {
                FXMLLoader loader = new FXMLLoader(fxmlURL);
                Node node = loader.load(); // Utilisation de la méthode load() de FXMLLoader
                contentPane.getChildren().setAll(node);
            } else {
                System.err.println("Fichier FXML non trouvé : " );
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du fichier FXML : " );
            e.printStackTrace();
        }

    }

    @FXML
    public void stats(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/analytics.fxml"));
            Node content = loader.load();

            if (contentPane != null) {
                contentPane.getChildren().clear();
                contentPane.getChildren().add(content);
                StackPane.setAlignment(content, Pos.CENTER);
            }
        } catch (IOException e) {
            System.err.println("Error loading " + "/user/profile.fxml" + ": " + e.getMessage());
            e.printStackTrace();
        }    }


}
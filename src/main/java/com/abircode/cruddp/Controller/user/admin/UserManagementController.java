package com.abircode.cruddp.Controller.user.admin;

import com.abircode.cruddp.entities.User;
import com.abircode.cruddp.services.user.UserService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;

public class UserManagementController {
    @FXML private FlowPane usersContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Pagination pagination;

    private final UserService userService = new UserService();
    private ObservableList<User> usersList = FXCollections.observableArrayList();
    private FilteredList<User> filteredUsers;
    private SortedList<User> sortedUsers;

    private static final int ITEMS_PER_PAGE = 10;

    @FXML
    public void initialize() {
        setupSortComboBox();
        loadUsers();
        setupSearch();
        setupPagination();

        // Lier le SortedList à la pagination
        sortedUsers.addListener((ListChangeListener.Change<? extends User> c) -> {
            updatePagination();
            displayCurrentPage();
        });
    }

    private void setupSortComboBox() {
        sortComboBox.getItems().addAll(
                "Name (A-Z)",
                "Name (Z-A)",
                "Email (A-Z)",
                "Email (Z-A)",
                "Status (Active first)",
                "Status (Blocked first)"
        );
        sortComboBox.getSelectionModel().selectFirst();

        sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            applySorting();
            pagination.setCurrentPageIndex(0);
        });
    }

    private void applySorting() {
        String sortOption = sortComboBox.getValue();
        if (sortOption == null) return;

        Comparator<User> comparator = switch (sortOption) {
            case "Name (A-Z)" -> Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER);
            case "Name (Z-A)" -> Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER).reversed();
            case "Email (A-Z)" -> Comparator.comparing(User::getEmail, String.CASE_INSENSITIVE_ORDER);
            case "Email (Z-A)" -> Comparator.comparing(User::getEmail, String.CASE_INSENSITIVE_ORDER).reversed();
            case "Status (Active first)" -> Comparator.comparing(User::isBlok);
            case "Status (Blocked first)" -> Comparator.comparing(User::isBlok).reversed();
            default -> null;
        };

        sortedUsers.setComparator(comparator);
    }
    private void loadUsers() {
        try {
            usersList.setAll(userService.getAllUsers());

            filteredUsers = new FilteredList<>(usersList, p -> true);
            sortedUsers = new SortedList<>(filteredUsers);

            // Appliquer le tri initial
            applySorting();

            updatePagination();
            displayCurrentPage();
        } catch (SQLException e) {
            showAlert("Error", "Failed to load users: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    private void updatePagination() {
        Platform.runLater(() -> {
            int pageCount = (int) Math.ceil((double) sortedUsers.size() / ITEMS_PER_PAGE);
            pagination.setPageCount(pageCount == 0 ? 1 : pageCount);
        });
    }
    private void setupPagination() {
        pagination.setPageFactory(this::createPage);
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            displayCurrentPage();
        });
    }

    private Node createPage(int pageIndex) {
        return new Pane(); // Just a placeholder, actual content is handled in displayCurrentPage()
    }

    private void displayCurrentPage() {
        usersContainer.getChildren().clear();

        int fromIndex = pagination.getCurrentPageIndex() * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, sortedUsers.size());

        for (int i = fromIndex; i < toIndex; i++) {
            User user = sortedUsers.get(i);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/UserCard.fxml"));
                Parent card = loader.load();

                UserCardController controller = loader.getController();
                controller.setUser(user);
                controller.setParentController(this);

                usersContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredUsers.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                return user.getName().toLowerCase().contains(lowerCaseFilter) ||
                        user.getEmail().toLowerCase().contains(lowerCaseFilter) ||
                        user.getRoles().toLowerCase().contains(lowerCaseFilter);
            });

            // Pas besoin d'appeler updatePagination() ici car le listener sur sortedUsers le fera
        });
    }
    // This method will be called from UserCardController
    public void handleEditUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/editUser.fxml"));
            Parent root = loader.load();

            EditUserController controller = loader.getController();
            controller.setUser(user);


            controller.setOnUserUpdated(this::loadUsers); // callback to refresh


            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Edit User");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadUsers(); // Refresh after editing
        } catch (IOException e) {
            showAlert("Error", "Failed to open editor: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }



    // This method will be called from UserCardController
    public void handleDeleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete User");
        confirm.setContentText("Are you sure you want to delete " + user.getName() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userService.deleteUser(user.getId());
                    loadUsers();
                } catch (SQLException e) {
                    showAlert("Error", "Failed to delete user: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    // This method will be called from UserCardController
    public void handleBlockToggle(User user) {
        try {
            boolean newBlockStatus = !user.isBlok();
            userService.updateBlockStatus(user.getId(), newBlockStatus);
            user.setBlok(newBlockStatus);
            loadUsers(); // Refresh the view

            showAlert("Success", user.getName() + " has been " +
                    (newBlockStatus ? "blocked" : "unblocked"), Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Error", "Failed to update user status: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
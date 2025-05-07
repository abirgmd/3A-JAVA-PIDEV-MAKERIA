package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Message;
import com.abircode.cruddp.entities.User;
import com.abircode.cruddp.services.ServiceMessage;
import com.abircode.cruddp.services.ServiceReply;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AfficherMessageAdminController {

    // Composants UI
    @FXML private ListView<Message> messagesList;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCombo;
    @FXML private ComboBox<String> sortOptions;
    @FXML private HBox paginationBox;
    @FXML private Label countLabel;

    // Statistiques
    @FXML private Label totalMessagesLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label avgMessagesLabel;
    @FXML private BarChart<String, Number> messagesByUserChart;
    @FXML private LineChart<String, Number> activityTrendChart;
    @FXML private PieChart messagesPieChart;

    // Services
    private final ServiceMessage serviceMessage = new ServiceMessage();
    private final ServiceReply serviceReply = new ServiceReply();

    // Données
    private List<Message> allMessages;
    private List<Message> filteredMessages;
    private int currentPage = 0;
    private final int messagesPerPage = 5;
    private int totalPages;

    @FXML
    public void initialize() {
        setupUI();
        loadData();
    }

    private void setupUI() {
        configureMessageList();
        setupFilters();
        setupSortOptions();
        setupSearchFilter();
    }

    private void configureMessageList() {
        messagesList.setCellFactory(param -> new ListCell<>() {
            private final VBox container = new VBox(8);
            private final HBox header = new HBox(10);
            private final Label titleLabel = new Label();
            private final Label userLabel = new Label();
            private final Label dateLabel = new Label();
            private final Label contentLabel = new Label();
            private final ImageView imageView = new ImageView();
            private final Button deleteButton = new Button();

            {
                container.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 12;");
                titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a148c;");
                userLabel.setStyle("-fx-text-fill: #7c4dff;");
                dateLabel.setStyle("-fx-text-fill: #9e9e9e; -fx-font-size: 11;");
                contentLabel.setWrapText(true);
                contentLabel.setMaxWidth(680);

                FontIcon deleteIcon = new FontIcon(FontAwesome.TRASH);
                deleteIcon.setIconColor(Color.web("#f44336"));
                deleteButton.setGraphic(deleteIcon);
                deleteButton.setStyle("-fx-background-color: transparent;");
                deleteButton.setOnAction(e -> handleDeleteMessage(getItem()));

                header.getChildren().addAll(userLabel, dateLabel);
                container.getChildren().addAll(titleLabel, contentLabel, imageView, header, deleteButton);
            }

            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setGraphic(null);
                } else {
                    titleLabel.setText(message.getTitreMsg());
                    contentLabel.setText(message.getContenu());
                    userLabel.setText("Par: " + message.getUser().getName());
                    dateLabel.setText("Le: " + message.getDateMsg());
                    if (message.getImage() != null && !message.getImage().isEmpty()) {
                        try {
                            imageView.setImage(new Image("file:" + message.getImage()));
                            imageView.setFitWidth(200);
                            imageView.setPreserveRatio(true);
                            imageView.setVisible(true);
                        } catch (Exception e) {
                            imageView.setVisible(false);
                        }
                    } else {
                        imageView.setVisible(false);
                    }
                    setGraphic(container);
                    FadeTransition ft = new FadeTransition(Duration.millis(300), container);
                    ft.setFromValue(0);
                    ft.setToValue(1);
                    ft.play();
                }
            }
        });
    }

    private void setupFilters() {
        filterCombo.getItems().addAll(
                "Tous les messages",
                "Avec images",
                "Sans images",
                "Ce mois-ci"
        );
        filterCombo.getSelectionModel().selectFirst();
        filterCombo.setOnAction(e -> applyFiltersAndSort());
    }

    private void setupSortOptions() {
        sortOptions.getItems().addAll(
                "Plus récent",
                "Plus ancien",
                "A-Z (Titre)",
                "Z-A (Titre)"
        );
        sortOptions.getSelectionModel().selectFirst();
        sortOptions.setOnAction(e -> applyFiltersAndSort());
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
    }

    private void loadData() {
        try {
            allMessages = serviceMessage.afficher();
            applyFiltersAndSort();
            loadStatistics();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de chargement des données: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void applyFiltersAndSort() {
        filteredMessages = allMessages.stream()
                .filter(this::applyContentFilter)
                .filter(this::applySearchFilter)
                .collect(Collectors.toList());
        sortMessages();
        updatePagination();
        loadPage(currentPage);
    }

    private boolean applyContentFilter(Message message) {
        int filterIndex = filterCombo.getSelectionModel().getSelectedIndex();
        return switch (filterIndex) {
            case 1 -> message.getImage() != null && !message.getImage().isEmpty();
            case 2 -> message.getImage() == null || message.getImage().isEmpty();
            case 3 -> isFromThisMonth(message);
            default -> true;
        };
    }

    private boolean isFromThisMonth(Message message) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate messageDate = LocalDate.parse(message.getDateMsg(), formatter);
            return messageDate.getMonth() == LocalDate.now().getMonth();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean applySearchFilter(Message message) {
        String searchText = searchField.getText().toLowerCase();
        return searchText.isEmpty() ||
                message.getTitreMsg().toLowerCase().contains(searchText) ||
                message.getContenu().toLowerCase().contains(searchText);
    }

    private void sortMessages() {
        int sortIndex = sortOptions.getSelectionModel().getSelectedIndex();
        filteredMessages.sort((m1, m2) -> switch (sortIndex) {
            case 1 -> m1.getDateMsg().compareTo(m2.getDateMsg()); // Plus ancien
            case 2 -> m1.getTitreMsg().compareToIgnoreCase(m2.getTitreMsg()); // A-Z
            case 3 -> m2.getTitreMsg().compareToIgnoreCase(m1.getTitreMsg()); // Z-A
            default -> m2.getDateMsg().compareTo(m1.getDateMsg()); // Plus récent (défaut)
        });
    }

    private void updatePagination() {
        totalPages = (int) Math.ceil((double) filteredMessages.size() / messagesPerPage);
        paginationBox.getChildren().clear();

        if (totalPages > 1) {
            for (int i = 0; i < totalPages; i++) {
                Button pageBtn = new Button(String.valueOf(i + 1));

                // Appliquer le style au bouton actif
                if (i == currentPage) {
                    pageBtn.setStyle("-fx-background-color: #7c4dff; -fx-text-fill: white;");
                } else {
                    pageBtn.setStyle("-fx-background-color: #7c4dff;");
                }

                int pageIndex = i;
                pageBtn.setOnAction(e -> {
                    currentPage = pageIndex;
                    loadPage(currentPage);
                    updatePagination(); // Re-mettre à jour tous les styles
                });

                paginationBox.getChildren().add(pageBtn);
            }
        }
    }

    private void loadPage(int page) {
        int fromIndex = page * messagesPerPage;
        int toIndex = Math.min(fromIndex + messagesPerPage, filteredMessages.size());
        messagesList.setItems(FXCollections.observableArrayList(
                filteredMessages.subList(fromIndex, toIndex)
        ));
        countLabel.setText(String.format("Affichage %d-%d sur %d",
                fromIndex + 1, toIndex, filteredMessages.size()));
    }

    private void loadStatistics() throws SQLException {
        totalMessagesLabel.setText(String.valueOf(serviceMessage.getTotalMessagesCount()));
        activeUsersLabel.setText(String.valueOf(serviceMessage.getActiveUsersCount()));
        avgMessagesLabel.setText(String.format("%.1f", serviceMessage.getDailyAverageMessages()));
        setupMessagesByUserChart();
        setupActivityTrendChart();
        setupMessagesPieChart();
    }

    private void setupMessagesByUserChart() throws SQLException {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Messages par utilisateur");
        Map<User, Long> userStats = serviceMessage.getTopActiveUsers(5);
        userStats.forEach((user, count) -> {
            series.getData().add(new XYChart.Data<>(user.getName(), count));
        });
        messagesByUserChart.getData().clear();
        messagesByUserChart.getData().add(series);
        for (XYChart.Data<String, Number> data : series.getData()) {
            Node node = data.getNode();
            if (node != null) node.setStyle("-fx-bar-fill: #7c4dff;");
        }
    }

    private void setupActivityTrendChart() throws SQLException {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Activité");
        Map<String, Long> monthlyStats = serviceMessage.getMessagesByPeriod("mois");
        monthlyStats.forEach((month, count) -> {
            series.getData().add(new XYChart.Data<>(month, count));
        });
        activityTrendChart.getData().clear();
        activityTrendChart.getData().add(series);
        if (series.getNode() != null)
            series.getNode().setStyle("-fx-stroke: #7c4dff; -fx-stroke-width: 2px;");
    }

    private void setupMessagesPieChart() throws SQLException {
        messagesPieChart.getData().clear();
        Map<String, Long> categoryStats = serviceMessage.getMessagesByCategory();
        categoryStats.forEach((category, count) -> {
            PieChart.Data slice = new PieChart.Data(category, count);
            messagesPieChart.getData().add(slice);
        });
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    private void handleDeleteMessage(Message message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le message");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce message et toutes ses réponses ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceReply.supprimerToutesReponsesPourMessage(message.getId());
                    serviceMessage.supprimer(message.getId());
                    allMessages.remove(message);
                    filteredMessages = allMessages;
                    loadPage(currentPage);
                    updateCountLabel(allMessages.size());
                    showAlert("Succès", "Message et réponses supprimés avec succès", Alert.AlertType.INFORMATION);
                } catch (SQLException e) {
                    showAlert("Erreur", "Échec de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }
    private void updateCountLabel(int count) {
        countLabel.setText("Nombre total de messages : " + count);
        countLabel.setStyle("-fx-text-fill: #b11d85; -fx-font-size: 14px;");
    }
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

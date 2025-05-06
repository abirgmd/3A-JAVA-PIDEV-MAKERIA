package com.abircode.cruddp.Controller.user.admin;

import com.abircode.cruddp.services.user.UserService;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.Map;

public class AnalyticsController {
    @FXML private BarChart<String, Number> userChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private VBox statsContainer;
    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label blockedUsersLabel;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        setupChart();
        loadStatistics();
    }

    private void setupChart() {
        xAxis.setLabel("User Status");
        yAxis.setLabel("Count");
        userChart.setTitle("User Statistics");
        userChart.setLegendVisible(false);
    }

    private void loadStatistics() {
        try {
            // Get data from service
            Map<String, Integer> stats = userService.getUserStatistics();

            // Update labels
            totalUsersLabel.setText(String.valueOf(stats.getOrDefault("total", 0)));
            activeUsersLabel.setText(String.valueOf(stats.getOrDefault("active", 0)));
            blockedUsersLabel.setText(String.valueOf(stats.getOrDefault("blocked", 0)));

            // Update chart
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.getData().add(new XYChart.Data<>("Active", stats.getOrDefault("active", 0)));
            series.getData().add(new XYChart.Data<>("Blocked", stats.getOrDefault("blocked", 0)));
            userChart.getData().add(series);

        } catch (SQLException e) {
            showError("Failed to load analytics data: " + e.getMessage());
        }
    }

    private void showError(String message) {
        statsContainer.getChildren().clear();
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        statsContainer.getChildren().add(errorLabel);
    }
}
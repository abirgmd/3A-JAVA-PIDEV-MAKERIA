package com.abircode.cruddp.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import com.abircode.cruddp.utils.DBConnexion;

public class StatsController {

    public enum ChartType {
        BAR_CHART, PIE_CHART, LINE_CHART, AREA_CHART, SCATTER_CHART
    }

    @FXML
    private ComboBox<ChartType> chartTypeComboBoxProduct;

    @FXML
    private ComboBox<ChartType> chartTypeComboBoxEvaluation;

    @FXML
    private StackPane chartContainerProduct;

    @FXML
    private StackPane chartContainerEvaluation;

    @FXML
    public void initialize() {
        chartTypeComboBoxProduct.setItems(FXCollections.observableArrayList(ChartType.values()));
        chartTypeComboBoxEvaluation.setItems(FXCollections.observableArrayList(ChartType.values()));

        chartTypeComboBoxProduct.setOnAction(event -> updateProductChart(chartTypeComboBoxProduct.getValue()));
        chartTypeComboBoxEvaluation.setOnAction(event -> updateEvaluationChart(chartTypeComboBoxEvaluation.getValue()));

        updateProductChart(ChartType.BAR_CHART); // Par défaut, Bar Chart pour les produits
        updateEvaluationChart(ChartType.BAR_CHART); // Par défaut, Bar Chart pour les évaluations
    }

    private void updateProductChart(ChartType chartType) {
        Map<String, Integer> data = getProductCountByCategory();
        int total = data.values().stream().mapToInt(Integer::intValue).sum();

        // Calculer les pourcentages
        Map<String, Double> percentageData = new HashMap<>();
        data.forEach((key, value) -> percentageData.put(key, value * 100.0 / total));

        chartContainerProduct.getChildren().clear();

        switch (chartType) {
            case BAR_CHART -> chartContainerProduct.getChildren().add(createBarChartWithPercentage(data, percentageData, total, "Catégories", "Nombre "));
            case PIE_CHART -> chartContainerProduct.getChildren().add(createPieChartWithPercentage(data, total));
            case LINE_CHART -> chartContainerProduct.getChildren().add(createLineChart(data, "Catégories", "Nombre "));
            case AREA_CHART -> chartContainerProduct.getChildren().add(createAreaChart(data, "Catégories", "Nombre "));
            case SCATTER_CHART -> chartContainerProduct.getChildren().add(createScatterChart(data, "Catégories", "Nombre "));
        }
    }

    private void updateEvaluationChart(ChartType chartType) {
        Map<Integer, String> idToNameMap = new HashMap<>();
        Map<Integer, Integer> data = getEvaluationCountByProduct(idToNameMap);

        // Convertir les clés d'ID en noms de produit
        Map<String, Integer> convertedData = new HashMap<>();
        data.forEach((key, value) -> convertedData.put(idToNameMap.getOrDefault(key, "ID " + key), value));

        chartContainerEvaluation.getChildren().clear();

        switch (chartType) {
            case BAR_CHART -> chartContainerEvaluation.getChildren().add(createBarChart(convertedData, 0, "Produit", "Nombre "));
            case PIE_CHART -> chartContainerEvaluation.getChildren().add(createEvaluationPieChart(convertedData, 0));
            case LINE_CHART -> chartContainerEvaluation.getChildren().add(createLineChart(convertedData, "Produit", "Nombre "));
            case AREA_CHART -> chartContainerEvaluation.getChildren().add(createAreaChart(convertedData, "Produit", "Nombre "));
            case SCATTER_CHART -> chartContainerEvaluation.getChildren().add(createScatterChart(convertedData, "Produit", "Nombre "));
        }
    }
    private PieChart createPieChartWithPercentage(Map<String, Integer> data, int total) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        data.forEach((key, value) -> {
            double percentage = value * 100.0 / total;
            pieData.add(new PieChart.Data(key + " (" + String.format("%.2f", percentage) + "%)", value));
        });
        PieChart pieChart = new PieChart(pieData);
        pieChart.setTitle("Répartition des Produits");
        return pieChart;
    }

    private BarChart<String, Number> createBarChart(Map<String, Integer> data, int total, String xLabel, String yLabel) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xLabel);
        yAxis.setLabel(yLabel);

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        data.forEach((key, value) -> {
            XYChart.Data<String, Number> barData = new XYChart.Data<>(key, value);
            Tooltip tooltip = new Tooltip(key + ": " + value);
            Tooltip.install(barData.getNode(), tooltip);
            series.getData().add(barData);
        });

        barChart.getData().add(series);
        return barChart;
    }

    private PieChart createEvaluationPieChart(Map<String, Integer> data, int total) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        data.forEach((key, value) -> {
            double percentage = value * 100.0 / total;
            pieData.add(new PieChart.Data(key + " (" + String.format("%.2f", percentage) + "%)", value));
        });

        PieChart pieChart = new PieChart(pieData);
        pieChart.setLabelsVisible(true); // Affiche les étiquettes des parts de graphique
        return pieChart;
    }

    private BarChart<String, Number> createBarChartWithPercentage(Map<String, Integer> data, Map<String, Double> percentageData, int total, String xLabel, String yLabel) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xLabel);
        yAxis.setLabel(yLabel);

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        data.forEach((key, value) -> {
            XYChart.Data<String, Number> barData = new XYChart.Data<>(key, value);
            Tooltip tooltip = new Tooltip(key + ": " + value + " (" + String.format("%.2f", percentageData.get(key)) + "%)");
            Tooltip.install(barData.getNode(), tooltip);
            series.getData().add(barData);
        });

        barChart.getData().add(series);
        return barChart;
    }

    private LineChart<String, Number> createLineChart(Map<String, Integer> data, String xLabel, String yLabel) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xLabel);
        yAxis.setLabel(yLabel);

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        data.forEach((key, value) -> series.getData().add(new XYChart.Data<>(key, value)));

        lineChart.getData().add(series);
        return lineChart;
    }

    private AreaChart<String, Number> createAreaChart(Map<String, Integer> data, String xLabel, String yLabel) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xLabel);
        yAxis.setLabel(yLabel);

        AreaChart<String, Number> areaChart = new AreaChart<>(xAxis, yAxis);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        data.forEach((key, value) -> series.getData().add(new XYChart.Data<>(key, value)));

        areaChart.getData().add(series);
        return areaChart;
    }

    private ScatterChart<String, Number> createScatterChart(Map<String, Integer> data, String xLabel, String yLabel) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xLabel);
        yAxis.setLabel(yLabel);

        ScatterChart<String, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        data.forEach((key, value) -> series.getData().add(new XYChart.Data<>(key, value)));

        scatterChart.getData().add(series);
        return scatterChart;
    }

    private Map<String, Integer> getProductCountByCategory() {
        Map<String, Integer> data = new HashMap<>();
        String query = "SELECT c.nomcat, COUNT(p.id) AS count FROM categorie c LEFT JOIN produit p ON c.id = p.categorie_id GROUP BY c.nomcat";

        try (Connection con = DBConnexion.getCon(); PreparedStatement st = con.prepareStatement(query); ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                data.put(rs.getString("nomcat"), rs.getInt("count"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    private Map<Integer, Integer> getEvaluationCountByProduct(Map<Integer, String> idToNameMap) {
        Map<Integer, Integer> data = new HashMap<>();
        String query = "SELECT p.id, p.nomprod, COUNT(e.id) AS count FROM produit p LEFT JOIN evaluation e ON p.id = e.produit_id GROUP BY p.id";

        try (Connection con = DBConnexion.getCon(); PreparedStatement st = con.prepareStatement(query); ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                int count = rs.getInt("count");

                data.put(id, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}

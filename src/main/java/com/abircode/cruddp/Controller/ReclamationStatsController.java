package com.abircode.cruddp.Controller;
import com.abircode.cruddp.services.ServiceReclamation;
import com.abircode.cruddp.entities.StatutReclamation;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;

import java.sql.SQLException;
import java.util.Map;
public class ReclamationStatsController {
    @FXML
    private PieChart pieChart;

    private ServiceReclamation serviceReclamation;
    public void initialize() {
        serviceReclamation = new ServiceReclamation();
        try{
            Map<StatutReclamation, Long> statusCount = serviceReclamation.getStatutReclamationCount();
            long encoursCount = statusCount.getOrDefault(StatutReclamation.EN_COURS, 0L);
            long resoluCount = statusCount.getOrDefault(StatutReclamation.RESOLU, 0L);
            long rejeteCount = statusCount.getOrDefault(StatutReclamation.REJETE, 0L);
            PieChart.Data encoursData = new PieChart.Data("EN_COURS", encoursCount);
            PieChart.Data resoluData = new PieChart.Data("RESOLU", resoluCount);
            PieChart.Data rejeteData = new PieChart.Data("REJETE", rejeteCount);
            pieChart.getData().addAll(encoursData, resoluData, rejeteData);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

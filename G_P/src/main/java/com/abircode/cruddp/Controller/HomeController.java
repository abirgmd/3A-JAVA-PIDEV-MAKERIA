package com.abircode.cruddp.Controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class HomeController implements Initializable {

    @FXML private StackPane carouselPane;
    @FXML private ImageView image1;
    @FXML private ImageView image2;
    @FXML private ImageView image3;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button indicator1;
    @FXML private Button indicator2;
    @FXML private Button indicator3;
    @FXML private HBox indicators;

    private List<ImageView> images;
    private List<Button> indicatorButtons;
    private int currentImageIndex = 0;
    private Timer autoSlideTimer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupImages();
        setupControls();
        startAutoSlide();
    }

    private void setupImages() {
        images = new ArrayList<>();
        images.add(image1);
        images.add(image2);
        images.add(image3);

        indicatorButtons = new ArrayList<>();
        indicatorButtons.add(indicator1);
        indicatorButtons.add(indicator2);
        indicatorButtons.add(indicator3);

        // Hide all images except the first one
        for (int i = 1; i < images.size(); i++) {
            images.get(i).setOpacity(0);
        }

        updateIndicators();
    }

    private void setupControls() {
        prevButton.setOnAction(e -> showPreviousImage());
        nextButton.setOnAction(e -> showNextImage());

        // Add hover effects to buttons
        prevButton.setOnMouseEntered(e -> prevButton.setStyle(prevButton.getStyle() + "-fx-opacity: 1;"));
        prevButton.setOnMouseExited(e -> prevButton.setStyle(prevButton.getStyle() + "-fx-opacity: 0.6;"));
        nextButton.setOnMouseEntered(e -> nextButton.setStyle(nextButton.getStyle() + "-fx-opacity: 1;"));
        nextButton.setOnMouseExited(e -> nextButton.setStyle(nextButton.getStyle() + "-fx-opacity: 0.6;"));

        // Setup indicator buttons
        for (int i = 0; i < indicatorButtons.size(); i++) {
            final int index = i;
            indicatorButtons.get(i).setOnAction(e -> {
                stopAutoSlide();
                showImage(index);
                startAutoSlide();
            });
        }
    }

    private void showPreviousImage() {
        stopAutoSlide();
        int nextIndex = (currentImageIndex - 1 + images.size()) % images.size();
        showImage(nextIndex);
        startAutoSlide();
    }

    private void showNextImage() {
        stopAutoSlide();
        int nextIndex = (currentImageIndex + 1) % images.size();
        showImage(nextIndex);
        startAutoSlide();
    }

    private void showImage(int index) {
        if (index == currentImageIndex) return;

        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), images.get(currentImageIndex));
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), images.get(index));
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        fadeOut.play();
        fadeIn.play();

        currentImageIndex = index;
        updateIndicators();
    }

    private void updateIndicators() {
        for (int i = 0; i < indicatorButtons.size(); i++) {
            indicatorButtons.get(i).setText(i == currentImageIndex ? "●" : "○");
        }
    }

    private void startAutoSlide() {
        stopAutoSlide();
        autoSlideTimer = new Timer(true);
        autoSlideTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> showNextImage());
            }
        }, 5000, 5000);
    }

    private void stopAutoSlide() {
        if (autoSlideTimer != null) {
            autoSlideTimer.cancel();
            autoSlideTimer = null;
        }
    }
} 
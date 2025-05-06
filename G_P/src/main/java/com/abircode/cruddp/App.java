package com.abircode.cruddp;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/MakeriaF.fxml")));
        Scene scene = new Scene(parent);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        stage.setFullScreen(true);

        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch();
    }
}
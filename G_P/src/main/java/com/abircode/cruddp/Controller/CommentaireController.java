package com.abircode.cruddp.Controller;

import com.abircode.cruddp.entities.Commentaire;
import com.abircode.cruddp.entities.Event;
import com.abircode.cruddp.utils.DBConnexion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.net.URL;
import java.sql.*;
import javafx.geometry.Insets;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class CommentaireController implements Initializable {

    @FXML private FlowPane flowPane;

    private ObservableList<Commentaire> commentaireList;
    private Connection con;
    private PreparedStatement st;
    private ResultSet rs;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadCommentaires();
    }

    private void loadCommentaires() {
        commentaireList = FXCollections.observableArrayList();
        String query = "SELECT c.id, c.nomcomment, c.timecomment, c.text_commentaire, c.event_id, e.titreevents FROM commentaire c JOIN event e ON c.event_id = e.id";
        try {
            st = DBConnexion.getCon().prepareStatement(query);
            rs = st.executeQuery();
            while (rs.next()) {
                Commentaire comment = new Commentaire();
                comment.setId(rs.getInt("id"));
                comment.setNomcomment(rs.getString("nomcomment"));
                comment.setTimecomment(rs.getTimestamp("timecomment").toLocalDateTime());
                comment.setText_commentaire(rs.getString("text_commentaire"));
                
                Event event = new Event();
                event.setId(rs.getInt("event_id"));
                event.setTitreevents(rs.getString("titreevents"));
                comment.setEvent(event);
                
                commentaireList.add(comment);
                createCommentCard(comment);
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des commentaires", e.getMessage());
        }
    }
    private void createCommentCard(Commentaire comment) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3); -fx-pref-width: 300;");

        // En-tête de la carte
        HBox header = new HBox(10);
        header.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 14; -fx-background-radius: 8;");

        Text eventText = new Text(comment.getEvent().getTitreevents());
        eventText.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-fill: #b11d85;");
        header.getChildren().add(eventText);

        // Corps de la carte
        VBox body = new VBox(8);

        Text nameText = new Text("Commenté par: " + comment.getNomcomment());
        nameText.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-fill: #333333;");

        Text dateText = new Text(comment.getTimecomment().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        dateText.setStyle("-fx-fill: #333333; -fx-font-size: 14px;");

        TextFlow commentText = new TextFlow();
        Text commentContent = new Text(comment.getText_commentaire());
        commentContent.setStyle("-fx-fill: #333333; -fx-font-size: 14px;");
        commentText.getChildren().add(commentContent);

        // Bouton de suppression
        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #b11d85; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
        deleteButton.setOnAction(event -> deleteCommentaire(comment));

        body.getChildren().addAll(nameText, dateText, commentText, deleteButton);

        // Ajouter les éléments à la carte
        card.getChildren().addAll(header, body);

        flowPane.getChildren().add(card);
    }


    private void deleteCommentaire(Commentaire comment) {
        try {
            st = DBConnexion.getCon().prepareStatement("DELETE FROM commentaire WHERE id=?");
            st.setInt(1, comment.getId());
            st.executeUpdate();
            flowPane.getChildren().clear();
            loadCommentaires();
            showSuccess("Commentaire supprimé avec succès");
        } catch (SQLException e) {
            showError("Erreur lors de la suppression du commentaire", e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

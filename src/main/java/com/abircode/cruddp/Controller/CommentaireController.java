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
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortByComboBox;
    @FXML private ComboBox<String> sortOrderComboBox;
    @FXML private Button nextButton;
    @FXML private Button prevButton;

    private ObservableList<Commentaire> commentaireList;
    private Connection con;
    private PreparedStatement st;
    private ResultSet rs;

    private int currentPage = 0;
    private int itemsPerPage = 8;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadCommentaires();
        sortByComboBox.getItems().addAll("Nom", "Commentaire");
        sortOrderComboBox.getItems().addAll("Croissant", "Décroissant");
        sortByComboBox.getSelectionModel().selectFirst();
        sortOrderComboBox.getSelectionModel().selectFirst();
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
            }
            showCommentaires();
        } catch (SQLException e) {
            showError("Erreur lors du chargement des commentaires", e.getMessage());
        }
    }

    private void showCommentaires() {
        flowPane.getChildren().clear();

        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, commentaireList.size());

        for (int i = startIndex; i < endIndex; i++) {
            Commentaire comment = commentaireList.get(i);
            createCommentCard(comment);
        }

        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(endIndex >= commentaireList.size());
    }

    private void createCommentCard(Commentaire comment) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3); -fx-pref-width: 300;");

        HBox header = new HBox(10);
        header.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 14; -fx-background-radius: 8;");

        Text eventText = new Text(comment.getEvent().getTitreevents());
        eventText.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-fill: #b11d85;");
        header.getChildren().add(eventText);

        VBox body = new VBox(8);

        Text nameText = new Text("Commenté par: " + comment.getNomcomment());
        nameText.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-fill: #333333;");

        Text dateText = new Text(comment.getTimecomment().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        dateText.setStyle("-fx-fill: #333333; -fx-font-size: 14px;");

        TextFlow commentText = new TextFlow();
        Text commentContent = new Text(comment.getText_commentaire());
        commentContent.setStyle("-fx-fill: #333333; -fx-font-size: 14px;");
        commentText.getChildren().add(commentContent);

        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #b11d85; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
        deleteButton.setOnAction(event -> deleteCommentaire(comment));

        body.getChildren().addAll(nameText, dateText, commentText, deleteButton);

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

    @FXML
    private void searchCommentaires() {
        String searchText = searchField.getText().toLowerCase();
        commentaireList.clear();

        try {
            String query = "SELECT c.id, c.nomcomment, c.timecomment, c.text_commentaire, c.event_id, e.titreevents FROM commentaire c JOIN event e ON c.event_id = e.id";
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

                if (comment.getNomcomment().toLowerCase().contains(searchText) ||
                        comment.getText_commentaire().toLowerCase().contains(searchText)) {
                    commentaireList.add(comment);
                }
            }
            showCommentaires();
        } catch (SQLException e) {
            showError("Erreur lors de la recherche des commentaires", e.getMessage());
        }
    }

    @FXML
    private void nextPage() {
        currentPage++;
        showCommentaires();
    }

    @FXML
    private void prevPage() {
        currentPage--;
        showCommentaires();
    }

    @FXML
    private void sortCommentaires() {
        String sortBy = sortByComboBox.getValue();
        String sortOrder = sortOrderComboBox.getValue();

        boolean ascending = sortOrder.equals("Croissant");

        if (sortBy.equals("Nom")) {
            sortCommentairesByName(ascending);
        } else if (sortBy.equals("Date")) {
            sortCommentairesByDate(ascending);
        } else if (sortBy.equals("Commentaire")) {
            sortCommentairesByText(ascending);
        }
    }

    private void sortCommentairesByName(boolean ascending) {
        commentaireList.sort((c1, c2) -> {
            if (ascending) {
                return c1.getNomcomment().compareToIgnoreCase(c2.getNomcomment());
            } else {
                return c2.getNomcomment().compareToIgnoreCase(c1.getNomcomment());
            }
        });
        showCommentaires();
    }

    private void sortCommentairesByDate(boolean ascending) {
        commentaireList.sort((c1, c2) -> {
            if (ascending) {
                return c1.getTimecomment().compareTo(c2.getTimecomment());
            } else {
                return c2.getTimecomment().compareTo(c1.getTimecomment());
            }
        });
        showCommentaires();
    }

    private void sortCommentairesByText(boolean ascending) {
        commentaireList.sort((c1, c2) -> {
            if (ascending) {
                return c1.getText_commentaire().compareToIgnoreCase(c2.getText_commentaire());
            } else {
                return c2.getText_commentaire().compareToIgnoreCase(c1.getText_commentaire());
            }
        });
        showCommentaires();
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

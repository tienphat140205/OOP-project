package sourceCode.UserControllers;

import java.sql.PreparedStatement;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sourceCode.AdminControllers.Function.ShowBook;
import sourceCode.Models.Feedback;
import sourceCode.Services.DatabaseConnection;
import sourceCode.Services.DatabaseOperation;
import sourceCode.Services.SwitchScene;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import sourceCode.UserControllers.Function.UpdateFeedback;

public class FeedbackController extends SwitchScene implements Initializable {

    private static final ObservableList<Feedback> feedbackList = FXCollections.observableArrayList();
    @FXML
    private TableColumn<FeedbackController, Integer> feedbackidColumn;
    @FXML
    private TableColumn<FeedbackController, String> isbnColumn;
    @FXML
    private TableColumn<FeedbackController, Integer> ratingColumn;
    @FXML
    private TableColumn<FeedbackController, String> dateColumn;
    @FXML
    private TableColumn<FeedbackController, String> commentColumn;
    @FXML
    private TableView<sourceCode.Models.Feedback> feedbackTableView;
    @FXML
    private ChoiceBox<String> choiceBox;
    @FXML
    private TextField searchBar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        searchBar.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().toString().equals("ENTER")) {
                searchFeedback();
            }
        });
        String[] searchBy = {"All", "ISBN", "Rating", "Date", "Comment"};
        choiceBox.getItems().addAll(searchBy);
        choiceBox.setValue(searchBy[0]);
        feedbackTableView.setItems(feedbackList);
        feedbackidColumn.setCellValueFactory(new PropertyValueFactory<>("feedbackID"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("ISBN"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        refreshList();
    }

    public void refreshList() {
        String query = "SELECT * FROM library.Feedback WHERE userId = " + "'"
                + sourceCode.LoginController.currentUserId + "'";
        DatabaseOperation.loadFeedbackfromDatabase(query, feedbackList);
    }

    public void searchFeedback() {
        if (searchBar.getText().isEmpty()) {
            refreshList();
            return;
        }
        String query = "SELECT * FROM library.Feedback WHERE userId = " + "'"
                + sourceCode.LoginController.currentUserId + "' AND ";
        switch (choiceBox.getValue()) {
            case "ISBN":
                query += "ISBN = '" + searchBar.getText() + "'";
                break;
            case "Rating":
                query += "rating = " + searchBar.getText();
                break;
            case "Date":
                query += "date = '" + searchBar.getText() + "'";
                break;
            case "Comment":
                query += "comment LIKE '%" + searchBar.getText() + "%'";
                break;
            default:
                query = "SELECT * FROM library.Feedback WHERE userId = " + "'"
                        + sourceCode.LoginController.currentUserId + "'";
        }
        DatabaseOperation.loadFeedbackfromDatabase(query, feedbackList);
    }

    public void showBook() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/sourceCode/AdminFXML/ShowBook.fxml"));
        Parent root = loader.load();
        ShowBook showBook = loader.getController();
        sourceCode.Models.Feedback selectedFeedback = feedbackTableView.getSelectionModel()
                .getSelectedItem();
        if (selectedFeedback == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Feedback Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a feedback to view the book details.");
            alert.showAndWait();
            return;
        }
        String query =
                "SELECT * FROM library.Book WHERE ISBN = '" + selectedFeedback.getISBN() + "';";
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            assert conn != null;
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    sourceCode.Models.Book book = new sourceCode.Models.Book(
                            rs.getString("ISBN"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("genre"),
                            rs.getString("publisher"),
                            rs.getString("publicationDate"),
                            rs.getString("language"),
                            rs.getInt("pageNumber"),
                            rs.getString("imageUrl"),
                            rs.getString("description"),
                            rs.getInt("quantity")
                    );
                    showBook.setBook(book);
                }
            }
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Book Information");
            stage.initStyle(StageStyle.UNDECORATED);
            stage.centerOnScreen();
            stage.show();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateFeedback() {
        try {
            Feedback feedback = feedbackTableView.getSelectionModel().getSelectedItem();
            if (feedback == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Feedback Selected");
                alert.setHeaderText(null);
                alert.setContentText("Please select a feedback to update.");
                alert.showAndWait();
                System.out.println("No feedback selected");
                return;
            }
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/sourceCode/UserFXML/UpdateFeedback.fxml"));
            Parent root = loader.load();
            UpdateFeedback updateFeedback = loader.getController();
            updateFeedback.setFeedbackController(this);
            updateFeedback.setFeedbackID(feedback.getFeedbackID());
            updateFeedback.setComment(feedback.getComment());
            updateFeedback.setRating(feedback.getRating());
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Update Feedback");
            stage.initStyle(StageStyle.UNDECORATED);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeFeedback() {
        Feedback feedback = feedbackTableView.getSelectionModel()
                .getSelectedItem();
        if (feedback == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Feedback Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a feedback to remove.");
            alert.showAndWait();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Feedback");
        alert.setHeaderText("Can't restore your feedback after removing");
        alert.setContentText("Do you want to remove this feedback?");
        Optional<ButtonType> a = alert.showAndWait();
        if (a.isEmpty() || a.get() != ButtonType.OK) {
            return;
        }
        String query = "DELETE FROM library.Feedback WHERE feedbackId = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            assert connection != null;
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, feedback.getFeedbackID());
                stmt.executeUpdate();
                feedbackList.remove(feedback);
                Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                alert1.setTitle("Feedback Removed");
                alert1.setHeaderText(null);
                alert1.setContentText("Feedback removed successfully");
                alert1.showAndWait();
                System.out.println("Feedback removed successfully");
            }
        } catch (SQLException e) {
            System.out.println("Feedback removing failed");
            e.printStackTrace();
        }
    }
}
package sourceCode.AdminControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sourceCode.AdminControllers.Function.ShowBook;
import sourceCode.AdminControllers.Function.ShowUser;
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

public class FeedbackController extends SwitchScene implements Initializable {

    private static final ObservableList<Feedback> feedbackList = FXCollections.observableArrayList();
    @FXML
    private ChoiceBox<String> choiceBox;
    @FXML
    private TextField searchBar;
    @FXML
    private TableView<Feedback> feedbackTableView;
    @FXML
    private TableColumn<Feedback, Integer> feedbackidColumn;
    @FXML
    private TableColumn<Feedback, String> uidColumn;
    @FXML
    private TableColumn<Feedback, String> isbnColumn;
    @FXML
    private TableColumn<Feedback, Integer> ratingColumn;
    @FXML
    private TableColumn<Feedback, String> dateColumn;
    @FXML
    private TableColumn<Feedback, String> commentColumn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        searchBar.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().toString().equals("ENTER")) {
                searchFeedback();
            }
        });
        String[] searchBy = {"All", "User ID", "ISBN", "Rating", "Date"};
        choiceBox.getItems().addAll(searchBy);
        choiceBox.setValue(searchBy[0]);
        feedbackTableView.setItems(feedbackList);
        feedbackidColumn.setCellValueFactory(new PropertyValueFactory<>("feedbackID"));
        uidColumn.setCellValueFactory(new PropertyValueFactory<>("userID"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("ISBN"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        refreshList();
    }

    public void refreshList() {
        String query = "Select * from library.Feedback";
        DatabaseOperation.loadFeedbackfromDatabase(query, feedbackList);
    }

    public void searchFeedback() {
        String search = searchBar.getText();
        if (!search.isEmpty()) {
            String query;
            String filter = choiceBox.getValue();
            switch (filter) {
                case "All":
                    query = "Select * from library.Feedback WHERE feedbackId LIKE '%" + search
                            + "%' OR userId LIKE '%" + search + "%' OR ISBN LIKE '%" + search
                            + "%' OR rating LIKE '%" + search + "%' OR date LIKE '%" + search
                            + "%' OR comment LIKE '%" + search + "%'";
                    break;
                case "User ID":
                    query = "Select * from library.Feedback WHERE userId LIKE '%" + search + "%'";
                    break;
                case "ISBN":
                    query = "Select * from library.Feedback WHERE ISBN LIKE '%" + search + "%'";
                    break;
                case "Rating":
                    query = "Select * from library.Feedback WHERE rating LIKE '%" + search + "%'";
                    break;
                default:
                    query = "Select * from library.Feedback WHERE date LIKE '%" + search + "%'";
                    break;
            }
            DatabaseOperation.loadFeedbackfromDatabase(query, feedbackList);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Input");
            alert.setHeaderText(null);
            alert.setContentText("Please input something to search.");
            alert.showAndWait();
        }
    }

    public void showUser() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/sourceCode/AdminFXML/ShowUser.fxml"));
        Parent root = loader.load();
        ShowUser showUser = loader.getController();
        sourceCode.Models.Feedback selectedFeedback = feedbackTableView.getSelectionModel()
                .getSelectedItem();
        if (selectedFeedback == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Feedback Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a feedback to show user information.");
            alert.showAndWait();
            return;
        }
        String query =
                "SELECT * FROM library.User WHERE userId = '" + selectedFeedback.getUserID() + "';";
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            assert conn != null;
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    sourceCode.Models.User user = new sourceCode.Models.User(
                            rs.getString("userId"),
                            rs.getString("name"),
                            rs.getString("identityNumber"),
                            rs.getDate("birth").toLocalDate(),
                            rs.getString("gender"),
                            rs.getString("phoneNumber"),
                            rs.getString("email"),
                            rs.getString("address"),
                            rs.getString("password"),
                            rs.getString("role")
                    );
                    showUser.setUser(user);
                }
            }
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("User Information");
            stage.initStyle(StageStyle.UNDECORATED);
            stage.centerOnScreen();
            stage.show();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
            alert.setContentText("Please select a feedback to show book information.");
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
}
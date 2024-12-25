package sourceCode.AdminControllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sourceCode.AdminControllers.Function.ShowBook;
import sourceCode.AdminControllers.Function.ShowUser;
import sourceCode.Models.Ticket;
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

public class TicketController extends SwitchScene implements Initializable {
    private static final ObservableList<Ticket> ticketList = FXCollections.observableArrayList();
    @FXML
    private ChoiceBox<String> choiceBox;
    @FXML
    private TextField searchBar;
    @FXML
    private TableView<Ticket> ticketTableView;
    @FXML
    private TableColumn<Ticket, Integer> ticketIDColumn;
    @FXML
    private TableColumn<Ticket, String> uidColumn;
    @FXML
    private TableColumn<Ticket, String> isbnColumn;
    @FXML
    private TableColumn<Ticket, Integer> quantityColumn;
    @FXML
    private TableColumn<Ticket, LocalDate> borrowedDateColumn;
    @FXML
    private TableColumn<Ticket, LocalDate> returnedDateColumn;
    @FXML
    private TableColumn<Ticket, String> statusColumn;

    public void initialize(URL url, ResourceBundle resourceBundle) {
        searchBar.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().toString().equals("ENTER")) {
                searchTicket();
            }
        });
        String[] searchBy = {"All", "User ID", "ISBN", "Borrowed Date", "Returned Date",
                "Status"};

        choiceBox.getItems().addAll(searchBy);
        choiceBox.setValue(searchBy[0]);
        ticketTableView.setItems(ticketList);
        ticketIDColumn.setCellValueFactory(new PropertyValueFactory<>("ticketID"));
        uidColumn.setCellValueFactory(new PropertyValueFactory<>("userID"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("ISBN"));
        borrowedDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowedDate"));
        returnedDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnedDate"));
        returnedDateColumn.setCellFactory(
                column -> new TableCell<Ticket, LocalDate>() {
                    @Override
                    protected void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        Ticket ticket = getTableRow().getItem();
                        if (empty || ticket == null) {
                            setText(null);
                        } else if (item == null && ticket.getTicketID() != 0) {
                            setText("-");
                        } else if (item != null) {
                            setText(item.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                        } else {
                            setText(null);
                        }
                    }
                });
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        refreshList();
    }

    public void refreshList() {
        String query = """
            SELECT *,CASE\
                    WHEN returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) <= 30 THEN 'On time'
                    WHEN returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) > 30 THEN 'Late'
                    WHEN returnedDate IS NULL AND DATEDIFF(CURDATE(), borrowedDate) <= 30 THEN 'Borrowing'
                    WHEN returnedDate IS NULL AND DATEDIFF(CURDATE(), borrowedDate) > 30 THEN 'Overdue'
                    ELSE 'Unknown'
                END AS status FROM library.Ticket""";
        DatabaseOperation.loadTicketfromDatabase(query, ticketTableView.getItems());
    }

    public void searchTicket() {
        String searchBy = choiceBox.getValue();
        String searchValue = searchBar.getText();
        String query = "";
        switch (searchBy) {
            case "All" -> query = """
                    SELECT *,CASE\
                            WHEN returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) <= 30 THEN 'On time'
                            WHEN returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) > 30 THEN 'Late'
                            WHEN returnedDate IS NULL AND DATEDIFF(CURDATE(), borrowedDate) <= 30 THEN 'Borrowing'
                            WHEN returnedDate IS NULL AND DATEDIFF(CURDATE(), borrowedDate) > 30 THEN 'Overdue'
                            ELSE 'Unknown'
                        END AS status FROM library.Ticket
                    WHERE ticketId LIKE '%%%s%%' OR userId LIKE '%%%s%%' OR ISBN LIKE '%%%s%%'
                    OR borrowedDate LIKE '%%%s%%' OR returnedDate LIKE '%%%s%%' OR status LIKE '%%%s%%'"""
                    .formatted(searchValue, searchValue, searchValue, searchValue, searchValue,
                            searchValue);
            case "User ID" -> query = """
                    SELECT *,CASE\
                            WHEN returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) <= 30 THEN 'On time'
                            WHEN returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) > 30 THEN 'Late'
                            WHEN returnedDate IS NULL AND DATEDIFF(CURDATE(), borrowedDate) <= 30 THEN 'Borrowing'
                            WHEN returnedDate IS NULL AND DATEDIFF(CURDATE(), borrowedDate) > 30 THEN 'Overdue'
                            ELSE 'Unknown'
                        END AS status FROM library.Ticket
                    WHERE userId LIKE '%%%s%%'""".formatted(searchValue);
            case "ISBN" -> query = """
                    SELECT *,CASE\
                            WHEN returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) <= 30 THEN 'On time'
                            WHEN returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) > 30 THEN 'Late'
                            WHEN returnedDate IS NULL AND DATEDIFF(CURDATE(), borrowedDate) <= 30 THEN 'Borrowing'
                            WHEN returnedDate IS NULL AND DATEDIFF(CURDATE(), borrowedDate) > 30 THEN 'Overdue'
                            ELSE 'Unknown'
                        END AS status FROM library.Ticket
                    WHERE ISBN LIKE '%%%s%%'""".formatted(searchValue);
            case "Borrowed Date" -> query = """
                    SELECT *,CASE\
                            WHEN returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) <= 30 THEN 'On time'
                            WHEN returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) > 30 THEN 'Late'
                            WHEN returnedDate IS NULL AND DATEDIFF(CURDATE(), borrowedDate) <= 30 THEN 'Borrowing'
                            WHEN returnedDate IS NULL AND DATEDIFF(CURDATE(), borrowedDate) > 30 THEN 'Overdue'
                            ELSE 'Unknown'
                        END AS status FROM library.Ticket
                    WHERE borrowedDate LIKE '%%%s%%'""".formatted(searchValue);
            case "Returned Date" -> query = """
                    SELECT *,CASE\
                            WHEN returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) <= 30 THEN 'On time'
                            WHEN returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) > 30 THEN 'Late'
                            WHEN returnedDate IS NULL AND DATEDIFF(CURDATE(), borrowedDate) <= 30 THEN 'Borrowing'
                            WHEN returnedDate IS NULL AND DATEDIFF(CURDATE(), borrowedDate) > 30 THEN 'Overdue'
                            ELSE 'Unknown'
                        END AS status FROM library.Ticket
                    WHERE returnedDate LIKE '%%%s%%'""".formatted(searchValue);
            case "Status" -> query = """
                    SELECT *,CASE\
                            WHEN returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) <= 30 THEN 'On time'
                            WHEN returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) > 30 THEN 'Late'
                            WHEN returnedDate IS NULL AND DATEDIFF(CURDATE(), borrowedDate) <= 30 THEN 'Borrowing'
                            WHEN returnedDate IS NULL AND DATEDIFF(CURDATE(), borrowedDate) > 30 THEN 'Overdue'
                            ELSE 'Unknown'
                        END AS status FROM library.Ticket
                    WHERE status LIKE '%%%s%%'""".formatted(searchValue);
            default -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Invalid Search By");
                alert.setHeaderText(null);
                alert.setContentText("Please select a valid search by option.");
                alert.showAndWait();
            }
        }
        DatabaseOperation.loadTicketfromDatabase(query, ticketTableView.getItems());
    }

    public void showUser() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/sourceCode/AdminFXML/ShowUser.fxml"));
        Parent root = loader.load();
        ShowUser showUser = loader.getController();
        Ticket selectedTicket = ticketTableView.getSelectionModel()
                .getSelectedItem();
        if (selectedTicket == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Ticket Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a ticket to show user information.");
            alert.showAndWait();
            return;
        }
        String userID = selectedTicket.getUserID();
        String query = "SELECT * FROM library.User WHERE userId = '" + userID + "';";
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
        Ticket selectedTicket = ticketTableView.getSelectionModel()
                .getSelectedItem();
        if (selectedTicket == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Ticket Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a ticket to show book information.");
            alert.showAndWait();
            return;
        }
        String query =
                "SELECT * FROM library.Book WHERE ISBN = '" + selectedTicket.getISBN() + "';";
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
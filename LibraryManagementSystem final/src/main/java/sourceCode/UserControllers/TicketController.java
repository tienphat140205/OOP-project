package sourceCode.UserControllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sourceCode.AdminControllers.Function.ShowBook;
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

    private static final ObservableList<sourceCode.Models.Ticket> ticketList = FXCollections.observableArrayList();
    @FXML
    private ChoiceBox<String> choiceBox;
    @FXML
    private TextField searchBar;
    @FXML
    private TableView<Ticket> ticketTableView;
    @FXML
    private TableColumn<Ticket, Integer> ticketIDColumn;
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
        String[] searchBy = {"All", "ISBN", "Borrowed Date", "Returned Date",
                "Status"};
        choiceBox.getItems().addAll(searchBy);
        choiceBox.setValue(searchBy[0]);
        ticketTableView.setItems(ticketList);
        ticketIDColumn.setCellValueFactory(new PropertyValueFactory<>("ticketID"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("ISBN"));
        borrowedDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowedDate"));
        returnedDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnedDate"));
        returnedDateColumn.setCellFactory(
                column -> new TableCell<sourceCode.Models.Ticket, LocalDate>() {
                    @Override
                    protected void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        sourceCode.Models.Ticket ticket = getTableRow().getItem();
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
                SELECT *,
                CASE
                    WHEN returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) <= 30 THEN 'On time'
                    WHEN returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) > 30 THEN 'Late'
                    WHEN returnedDate IS NULL AND DATEDIFF(CURDATE(), borrowedDate) <= 30 THEN 'Borrowing'
                    WHEN returnedDate IS NULL AND DATEDIFF(CURDATE(), borrowedDate) > 30 THEN 'Overdue'
                    ELSE 'Không xác định'
                END AS status FROM library.Ticket WHERE userId = '"""
                + sourceCode.LoginController.currentUserId + "'";
        DatabaseOperation.loadTicketfromDatabase(query, ticketList);
    }

    public void searchTicket() {
        if (searchBar.getText().isEmpty()) {
            refreshList();
            return;
        }
        String query = "SELECT *, CASE ";
        switch (choiceBox.getValue()) {
            case "ISBN":
                query += "WHEN ISBN LIKE '%" + searchBar.getText() + "%' THEN ";
                break;
            case "Borrowed Date":
                query += "WHEN borrowedDate LIKE '%" + searchBar.getText() + "%' THEN ";
                break;
            case "Returned Date":
                query += "WHEN returnedDate LIKE '%" + searchBar.getText() + "%' THEN ";
                break;
            case "Status":
                query += "WHEN status LIKE '%" + searchBar.getText() + "%' THEN ";
                break;
            default:
                query += "WHEN ISBN LIKE '%" + searchBar.getText() + "%' OR "
                        + "borrowedDate LIKE '%" + searchBar.getText() + "%' OR "
                        + "returnedDate LIKE '%" + searchBar.getText() + "%' OR "
                        + "status LIKE '%" + searchBar.getText() + "%' THEN ";
                break;
        }
        query += """
                CASE
                    WHEN returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) <= 30 THEN 'On time'
                    WHEN returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) > 30 THEN 'Late'
                    WHEN returnedDate IS NULL AND DATEDIFF(CURDATE(), borrowedDate) <= 30 THEN 'Borrowing'
                    WHEN returnedDate IS NULL AND DATEDIFF(CURDATE(), borrowedDate) > 30 THEN 'Overdue'
                    ELSE 'Không xác định'
                END AS status
                FROM library.Ticket WHERE userId = '"""
                + sourceCode.LoginController.currentUserId + "'";
        DatabaseOperation.loadTicketfromDatabase(query, ticketList);
    }

    public void showBook() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/sourceCode/AdminFXML/ShowBook.fxml"));
        Parent root = loader.load();
        ShowBook showBook = loader.getController();
        Ticket selectedTicket = ticketTableView.getSelectionModel()
                .getSelectedItem();
        if (selectedTicket == null) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select a ticket from the table.");
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
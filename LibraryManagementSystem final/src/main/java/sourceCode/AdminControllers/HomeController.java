package sourceCode.AdminControllers;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import sourceCode.Models.Ticket;
import sourceCode.Services.DatabaseConnection;
import sourceCode.Services.DatabaseOperation;
import sourceCode.Services.SwitchScene;

public class HomeController extends SwitchScene implements Initializable {

    @FXML
    private Label bookCount;
    @FXML
    private Label userCount;
    @FXML
    private Label ticketCount;
    @FXML
    private Label feedbackCount;
    @FXML
    private PieChart myPieChart;
    @FXML
    private BarChart<String, Integer> myBarChart;
    @FXML
    private TableView<Ticket> myTableView;
    @FXML
    private TableColumn<Ticket, String> uidColumn;
    @FXML
    private TableColumn<Ticket, String> isbnColumn;
    @FXML
    private TableColumn<Ticket, Integer> quantityColumn;
    @FXML
    private TableColumn<Ticket, LocalDate> borrowedDateColumn;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initSummaryPane();
        initTableView();
        initPieChart();
        initBarChart();
    }

    private void initSummaryPane() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            assert conn != null;
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM library.book")) {
                while (rs.next()) {
                    bookCount.setText(rs.getString(1));
                }
            }
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM library.user")) {
                while (rs.next()) {
                    userCount.setText(rs.getString(1));
                }
            }
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM library.feedback")) {
                while (rs.next()) {
                    feedbackCount.setText(rs.getString(1));
                }
            }
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM library.ticket")) {
                while (rs.next()) {
                    ticketCount.setText(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Number of Borrowed Books");
        XYChart.Series<String, Integer> series = new XYChart.Series<>();

        String query = "SELECT borrowedDate, COUNT(*) AS borrowedCount FROM library.ticket GROUP BY borrowedDate ORDER BY borrowedDate DESC LIMIT 7";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            assert conn != null;
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    series.getData().add(new XYChart.Data<>(rs.getDate("borrowedDate").toString(),
                            rs.getInt("borrowedCount")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        myBarChart.getData().add(series);
    }

    private void initPieChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        String query = """
                SELECT CASE
                    WHEN returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) <= 30 THEN 'On time'
                    WHEN returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) > 30 THEN 'Late'
                    WHEN returnedDate IS NULL AND DATEDIFF(CURDATE(), borrowedDate) <= 30 THEN 'Borrowing'
                    WHEN returnedDate IS NULL AND DATEDIFF(CURDATE(), borrowedDate) > 30 THEN 'Overdue'
                    ELSE 'Unknown'
                END AS status, COUNT(*) AS statusCount FROM library.ticket GROUP BY status""";
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            assert conn != null;
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    PieChart.Data data = new PieChart.Data(rs.getString("status"),
                            rs.getInt("statusCount"));
                    data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                        if (newNode != null) {
                            newNode.setOnMouseEntered(e -> {
                                ScaleTransition scaleTransition = new ScaleTransition(
                                        Duration.millis(300),
                                        newNode);
                                scaleTransition.setToY(1.1);
                                scaleTransition.setToX(1.1);
                                scaleTransition.play();
                            });
                            newNode.setOnMouseExited(e -> {
                                ScaleTransition scaleTransition = new ScaleTransition(
                                        Duration.millis(300),
                                        newNode);
                                scaleTransition.setToY(1);
                                scaleTransition.setToX(1);
                                scaleTransition.play();
                            });
                        }
                    });
                    pieChartData.add(data);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        myPieChart.setData(pieChartData);
    }

    private void initTableView() {
        ObservableList<Ticket> ticketList = FXCollections.observableArrayList();
        String query = """
                SELECT *,CASE\
                        WHEN returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) <= 30 THEN 'On time'
                        WHEN returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) > 30 THEN 'Late'
                        WHEN returnedDate IS NULL AND DATEDIFF(CURDATE(), borrowedDate) <= 30 THEN 'Borrowing'
                        WHEN returnedDate IS NULL AND DATEDIFF(CURDATE(), borrowedDate) > 30 THEN 'Overdue'
                        ELSE 'Unknown'
                    END AS status FROM library.Ticket""";
        myTableView.setItems(ticketList);
        uidColumn.setCellValueFactory(new PropertyValueFactory<>("userID"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("ISBN"));
        borrowedDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowedDate"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        DatabaseOperation.loadTicketfromDatabase(query, ticketList);
    }
}
package sourceCode.UserControllers;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sourceCode.AdminControllers.Function.EditUser;
import sourceCode.Models.User;
import sourceCode.Services.DatabaseConnection;
import sourceCode.Services.SwitchScene;

public class ProfileController extends SwitchScene implements Initializable {

    private final ObservableList<String[]> bookList = FXCollections.observableArrayList();
    @FXML
    private TableView<String[]> favouriteBooks;
    @FXML
    private TableColumn<String[], String> titleColumn;
    @FXML
    private TableColumn<String[], String> authorColumn;
    @FXML
    private TableColumn<String[], String> genreColumn;
    @FXML
    private TableColumn<String[], String> ratingColumn;
    @FXML
    private Label borrowingCount;
    @FXML
    private Label ontimeCount;
    @FXML
    private Label lateCount;
    @FXML
    private Label overdueCount;
    @FXML
    private Label feedbackCount;
    @FXML
    private Label userId;
    @FXML
    private Label name;
    @FXML
    private Label identityNumber;
    @FXML
    private Label birth;
    @FXML
    private Label gender;
    @FXML
    private Label phoneNumber;
    @FXML
    private Label email;
    @FXML
    private Label address;
    @FXML
    private Label password;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initSummaryPane();
        initProfilePane();
        initFavouriteBooks();
    }

    private void initSummaryPane() {
        String borrowing = "SELECT * FROM library.ticket WHERE userId = ? AND returnedDate IS NULL AND DATEDIFF(CURRENT_DATE, borrowedDate) <= 30";
        String ontime = "SELECT * FROM library.ticket WHERE userId = ? AND returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) <= 30";
        String late = "SELECT * FROM library.ticket WHERE userId = ? AND returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) > 30";
        String overdue = "SELECT * FROM library.ticket WHERE userId = ? AND returnedDate IS NULL AND DATEDIFF(CURRENT_DATE, borrowedDate) > 30";
        String feedback = "SELECT COUNT(*) FROM library.feedback WHERE userId = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            assert conn != null;
            try (PreparedStatement stmt = conn.prepareStatement(borrowing);
                    PreparedStatement stmt1 = conn.prepareStatement(ontime);
                    PreparedStatement stmt2 = conn.prepareStatement(late);
                    PreparedStatement stmt3 = conn.prepareStatement(overdue);
                    PreparedStatement stmt4 = conn.prepareStatement(feedback)) {
                stmt.setString(1, sourceCode.LoginController.currentUserId);
                stmt1.setString(1, sourceCode.LoginController.currentUserId);
                stmt2.setString(1, sourceCode.LoginController.currentUserId);
                stmt3.setString(1, sourceCode.LoginController.currentUserId);
                stmt4.setString(1, sourceCode.LoginController.currentUserId);
                try (ResultSet rs = stmt.executeQuery();
                        ResultSet rs1 = stmt1.executeQuery();
                        ResultSet rs2 = stmt2.executeQuery();
                        ResultSet rs3 = stmt3.executeQuery();
                        ResultSet rs4 = stmt4.executeQuery()) {
                    int borrowingCount = 0;
                    int ontimeCount = 0;
                    int lateCount = 0;
                    int overdueCount = 0;
                    int feedbackCount = 0;
                    while (rs.next()) {
                        borrowingCount++;
                    }
                    while (rs1.next()) {
                        ontimeCount++;
                    }
                    while (rs2.next()) {
                        lateCount++;
                    }
                    while (rs3.next()) {
                        overdueCount++;
                    }
                    while (rs4.next()) {
                        feedbackCount = rs4.getInt(1);
                    }
                    this.borrowingCount.setText(String.valueOf(borrowingCount));
                    this.ontimeCount.setText(String.valueOf(ontimeCount));
                    this.lateCount.setText(String.valueOf(lateCount));
                    this.overdueCount.setText(String.valueOf(overdueCount));
                    this.feedbackCount.setText(String.valueOf(feedbackCount));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void initProfilePane() {
        String query = "SELECT * FROM library.user WHERE userId = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            assert connection != null;
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, sourceCode.LoginController.currentUserId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        userId.setText(rs.getString("userId"));
                        name.setText(rs.getString("name"));
                        identityNumber.setText(rs.getString("identityNumber"));
                        birth.setText(rs.getDate("birth").toString());
                        gender.setText(rs.getString("gender"));
                        phoneNumber.setText(rs.getString("phoneNumber"));
                        email.setText(rs.getString("email"));
                        address.setText(rs.getString("address"));
                        password.setText("*************");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initFavouriteBooks() {
        titleColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue()[0]));
        authorColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue()[1]));
        genreColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue()[2]));
        ratingColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue()[3]));

        favouriteBooks.setItems(bookList);
        String query =
                "SELECT DISTINCT b.title, b.author, b.genre, MAX(f.rating) AS max_rating, f.userID "
                        +
                        "FROM library.book b " +
                        "JOIN library.feedback f ON b.ISBN = f.ISBN " +
                        "GROUP BY b.ISBN, b.title, b.author, b.genre, f.userID " +
                        "HAVING f.userID = ? " +
                        "ORDER BY max_rating DESC " +
                        "LIMIT 10";
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            assert connection != null;
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, sourceCode.LoginController.currentUserId);
                stmt.executeQuery();
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String[] bookData = new String[]{
                                rs.getString("title"),
                                rs.getString("author"),
                                rs.getString("genre"),
                                rs.getString("max_rating")
                        };
                        bookList.add(bookData);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Can't load favourite books");
            e.printStackTrace();
        }
    }

    public void deleteAccount(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Account");
        alert.setHeaderText("Can't restore this account after removing");
        alert.setContentText(
                "Do you want to delete this account ?\nAll your data will be lost");
        Optional<ButtonType> a = alert.showAndWait();
        if (a.isEmpty() || a.get() != ButtonType.OK) {
            return;
        }
        String query = "UPDATE library.user SET password = NULL WHERE userId = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            assert connection != null;
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, sourceCode.LoginController.currentUserId);
                stmt.executeUpdate();
                System.out.println("User removed successfully");
            }
        } catch (SQLException e) {
            System.out.println("User removing failed");
            e.printStackTrace();
        }
        switchToLogin(event);
    }

    public void updateProfile() {
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            String query = "SELECT * FROM library.user WHERE userId = '"
                    + sourceCode.LoginController.currentUserId + "'";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/sourceCode/AdminFXML/EditUser.fxml"));
            Parent root = loader.load();
            EditUser editUser = loader.getController();
            assert connection != null;
            try (Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    User user = new User(
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
                    editUser.setUser(user);
                    editUser.setProfileController(this);
                }
            }
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Edit User");
            stage.initStyle(StageStyle.UNDECORATED);
            stage.centerOnScreen();
            stage.show();
        } catch (SQLException | IOException e) {
            System.out.println("Edit user failed");
            e.printStackTrace();
        }
    }
}

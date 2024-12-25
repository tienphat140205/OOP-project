package sourceCode.UserControllers.Function;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sourceCode.Services.DatabaseConnection;

public class AddFeedback {

    @FXML
    private TextField commentText;
    @FXML
    private Slider ratingSlider;
    private String isbn;

    public String getISBN() {
        return this.isbn;
    }

    public void setISBN(String ISBN) {
        this.isbn = ISBN.substring(6);
    }

    public void confirmButtonOnAction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Add Feedback");
        alert.setHeaderText("Confirm Feedback Addition");
        alert.setContentText("Are you sure you want to add this feedback?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        String query = "INSERT INTO library.Feedback (ISBN, userID, comment, rating, date) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            assert connection != null;
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, getISBN());
                stmt.setString(2, sourceCode.LoginController.currentUserId);
                stmt.setString(3, commentText.getText());
                stmt.setInt(4, ratingSlider.valueProperty().intValue());
                stmt.setDate(5, new Date(System.currentTimeMillis()));
                stmt.executeUpdate();
                System.out.println("Feedback added successfully");
            }
        } catch (SQLException e) {
            System.out.println("Feedback adding failed");
            e.printStackTrace();
        }
        cancelButtonOnAction(event);
    }

    public void cancelButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }
}

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
import sourceCode.UserControllers.FeedbackController;

public class UpdateFeedback {

    @FXML
    private TextField commentText;
    @FXML
    private Slider ratingSlider;
    private FeedbackController feedbackController;
    private int feedbackId;

    public void setFeedbackController(FeedbackController feedbackController) {
        this.feedbackController = feedbackController;
    }

    public int getFeedbackId() {
        return this.feedbackId;
    }

    public void setFeedbackID(int feedbackId) {
        this.feedbackId = feedbackId;
    }

    public String getComment() {
        return commentText.getText();
    }

    public void setComment(String comment) {
        commentText.setText(comment);
    }

    public int getRating() {
        return (int) ratingSlider.getValue();
    }

    public void setRating(int rating) {
        ratingSlider.setValue(rating);
    }

    public void confirmButtonOnAction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Update Feedback");
        alert.setHeaderText("Confirm Feedback Update");
        alert.setContentText("Are you sure you want to update this feedback?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        String query = "UPDATE library.Feedback SET comment = ?, rating = ?, date = ? WHERE feedbackId = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            assert connection != null;
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, getComment());
                stmt.setInt(2, getRating());
                stmt.setDate(3, new Date(System.currentTimeMillis()));
                stmt.setInt(4, getFeedbackId());
                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("Feedback updated successfully");
                } else {
                    System.out.println("No feedback found with the given ID");
                }
            }
        } catch (SQLException e) {
            System.out.println("Feedback updating failed");
            e.printStackTrace();
            cancelButtonOnAction(event);
        }
        feedbackController.initialize(null, null);
        cancelButtonOnAction(event);
    }

    public void cancelButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }
}

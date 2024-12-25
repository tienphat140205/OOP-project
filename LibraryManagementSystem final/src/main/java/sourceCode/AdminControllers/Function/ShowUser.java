package sourceCode.AdminControllers.Function;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import sourceCode.Models.User;

public class ShowUser {

    // FXML labels to display user information
    @FXML
    private Label name; // Label for the user's name
    @FXML
    private Label userID; // Label for the user's ID
    @FXML
    private Label identityNumber; // Label for the user's identity number
    @FXML
    private Label birth; // Label for the user's birth date
    @FXML
    private Label gender; // Label for the user's gender
    @FXML
    private Label phoneNumber; // Label for the user's phone number
    @FXML
    private Label email; // Label for the user's email address
    @FXML
    private Label address; // Label for the user's physical address

    // Method to populate the labels with user information
    public void setUser(User user) {
        // Set the text for each label using the corresponding user data
        name.setText(user.getName());
        userID.setText("USERID: " + user.getUserId()); // Add "USERID:" prefix for better clarity
        identityNumber.setText(user.getIdentityNumber());
        birth.setText(user.getBirth().toString()); // Convert the birth date to a string
        gender.setText(user.getGender());
        phoneNumber.setText(user.getPhoneNumber());
        email.setText(user.getEmail());
        address.setText(user.getAddress());
    }

    // Action handler for the confirm button to close the window
    public void confirmButtonOnAction(ActionEvent event) {
        // Close the current stage when the confirm button is clicked
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
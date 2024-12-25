package sourceCode.AdminControllers.Function;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sourceCode.AdminControllers.UserController;
import sourceCode.Services.DatabaseConnection;
import sourceCode.UserControllers.ProfileController;

public class EditUser {

    // FXML components for user input fields
    @FXML
    private TextField userID; // Field for user ID
    @FXML
    private TextField name; // Field for user name
    @FXML
    private TextField identityNumber; // Field for user's identity number
    @FXML
    private ChoiceBox<String> gender; // Dropdown for gender selection
    @FXML
    private TextField mail; // Field for user email
    @FXML
    private TextField phoneNumber; // Field for user phone number
    @FXML
    private TextField address; // Field for user address
    @FXML
    private DatePicker birth; // Date picker for user's birth date
    @FXML
    private PasswordField password; // Field for user's password

    // Controllers for user and profile management
    private UserController userController;
    private ProfileController profileController;

    // Method to set the user's data in the form fields
    public void setUser(sourceCode.Models.User user) {
        name.setText(user.getName());
        userID.setText(user.getUserId());
        identityNumber.setText(user.getIdentityNumber());
        birth.setValue(user.getBirth());
        mail.setText(user.getEmail());
        address.setText(user.getAddress());
        phoneNumber.setText(user.getPhoneNumber());
        gender.setValue(user.getGender());
        password.setText(user.getPassword());
    }

    // Method to set the UserController instance
    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    // Method to set the ProfileController instance
    public void setProfileController(ProfileController profileController) {
        this.profileController = profileController;
    }

    // Action handler for the confirm button
    public void confirmButtonOnAction(ActionEvent event) {
        // Show confirmation dialog
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Edit");
        confirmationAlert.setHeaderText("Are you sure you want to save the changes?");
        confirmationAlert.setContentText("Please confirm your action.");
        Optional<ButtonType> result = confirmationAlert.showAndWait();

        // If the user cancels the action, exit the method
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        // SQL query to update the user information in the database
        String query = "UPDATE library.user SET name = ?, identityNumber = ?, birth = ?, gender = ?, phoneNumber = ?, email = ?, address = ?, password = ? WHERE userId = ?";

        // Establish a database connection and execute the update
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            assert connection != null; // Ensure the connection is not null
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                // Set the parameters for the query
                stmt.setString(1, name.getText());
                stmt.setString(2, identityNumber.getText());
                stmt.setDate(3, Date.valueOf(birth.getValue()));
                stmt.setString(4, gender.getValue());
                stmt.setString(5, phoneNumber.getText());
                stmt.setString(6, mail.getText());
                stmt.setString(7, address.getText());
                stmt.setString(8, password.getText());
                stmt.setString(9, userID.getText());

                // Execute the update
                stmt.executeUpdate();

                // Show success alert
                Alert alrt = new Alert(Alert.AlertType.INFORMATION);
                alrt.setTitle("User Edited");
                alrt.setHeaderText(null);
                alrt.setContentText("User edited successfully");
                alrt.showAndWait();
                System.out.println("User edited successfully");
            }
        } catch (SQLException e) {
            // Handle SQL exceptions
            System.out.println("Can't edit this user");
            e.printStackTrace();
        }

        // Refresh the user or profile view based on the controller
        if (userController != null) {
            userController.refreshList();
        } else {
            profileController.initProfilePane();
        }

        // Close the current window
        cancelButtonOnAction(event);
    }

    // Action handler for the cancel button
    public void cancelButtonOnAction(ActionEvent event) {
        // Close the current stage
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
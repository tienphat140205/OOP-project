package sourceCode.AdminControllers.Function;

import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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

public class AddUser implements Initializable {

    // UI components from the FXML file
    @FXML
    private TextField address; // Address of the user
    @FXML
    private TextField phoneNumber; // Phone number of the user
    @FXML
    private ChoiceBox<String> gender; // Gender choice box ("Nam" or "Nữ")
    @FXML
    private TextField identityNumber; // Identity number of the user
    @FXML
    private TextField name; // Full name of the user
    @FXML
    private TextField userID; // User ID
    @FXML
    private TextField mail; // Email address of the user
    @FXML
    private DatePicker birth; // Date of birth of the user
    @FXML
    private PasswordField password; // Password field for the user's account

    // Reference to the UserController for refreshing the user list
    private UserController userController;

    // Setter for the UserController reference
    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    // Initializes the controller and sets up the ChoiceBox items
    @Override
    public void initialize(URL url, ResourceBundle resource) {
        gender.getItems().addAll("Nam", "Nữ"); // Add gender options to the ChoiceBox
    }

    // Handles the "Confirm" button click event
    public void confirmButtonOnAction(ActionEvent event) {
        // Show confirmation dialog
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Add");
        confirmationAlert.setHeaderText("Are you sure you want to save the changes?");
        confirmationAlert.setContentText("Please confirm your action.");
        Optional<ButtonType> result = confirmationAlert.showAndWait();

        // Exit if the user cancels the action
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        // SQL query to insert a new user into the database
        String query = "INSERT INTO library.user (name, userId, identityNumber, birth, gender, phoneNumber, email, address, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            assert connection != null; // Ensure the connection is valid

            // Prepare the SQL statement
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, name.getText()); // Set the user's name
                stmt.setString(2, userID.getText()); // Set the user's ID
                stmt.setString(3, identityNumber.getText()); // Set the identity number
                stmt.setDate(4, Date.valueOf(birth.getValue())); // Set the birth date
                stmt.setString(5, gender.getValue()); // Set the gender
                stmt.setString(6, phoneNumber.getText()); // Set the phone number
                stmt.setString(7, mail.getText()); // Set the email address
                stmt.setString(8, address.getText()); // Set the address
                stmt.setString(9, password.getText()); // Set the password
                stmt.executeUpdate(); // Execute the SQL query
                System.out.println("User added successfully");
            }
        } catch (SQLException e) {
            // Handle SQL exceptions
            System.out.println("User adding failed");
            e.printStackTrace();
        }

        // Refresh the user list in the UserController
        userController.refreshList();

        // Close the current window
        cancelButtonOnAction(event);
    }

    // Handles the "Cancel" button click event
    public void cancelButtonOnAction(ActionEvent event) {
        // Close the current window
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }
}

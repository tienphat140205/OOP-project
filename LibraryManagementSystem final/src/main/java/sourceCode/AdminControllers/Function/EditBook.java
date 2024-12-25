package sourceCode.AdminControllers.Function;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sourceCode.AdminControllers.BookController;
import sourceCode.Models.Book;
import sourceCode.Services.DatabaseConnection;

public class EditBook {

    // FXML UI components linked from the corresponding FXML file
    @FXML
    private TextField ISBN; // Field to display and edit the book's ISBN
    @FXML
    private TextField author; // Field to display and edit the book's author
    @FXML
    private TextField genre; // Field to display and edit the book's genre
    @FXML
    private TextField publisher; // Field to display and edit the book's publisher
    @FXML
    private TextField imageUrl; // Field to display and edit the book's image URL
    @FXML
    private TextField publicationDate; // Field to display and edit the book's publication date
    @FXML
    private TextArea description; // Field to display and edit the book's description
    @FXML
    private TextField pageNumber; // Field to display and edit the number of pages in the book
    @FXML
    private TextField language; // Field to display and edit the book's language
    @FXML
    private TextField quantity; // Field to display and edit the book's quantity in stock
    @FXML
    private TextField title; // Field to display and edit the book's title

    // Reference to the BookController for refreshing the book list after editing
    private BookController bookController;

    // Setter method to inject the BookController instance
    public void setBookController(BookController bookController) {
        this.bookController = bookController;
    }

    // Method to populate the form with data from the selected book
    public void setBook(Book book) {
        ISBN.setText(book.getISBN()); // Set ISBN
        title.setText(book.getTitle()); // Set title
        author.setText(book.getAuthor()); // Set author
        genre.setText(book.getGenre()); // Set genre
        publisher.setText(book.getPublisher()); // Set publisher
        imageUrl.setText(book.getImageUrl()); // Set image URL
        publicationDate.setText(book.getPublicationDate()); // Set publication date
        description.setText(book.getDescription()); // Set description
        pageNumber.setText(String.valueOf(book.getPageNumber())); // Set page number
        language.setText(book.getLanguage()); // Set language
        quantity.setText(String.valueOf(book.getQuantity())); // Set quantity
    }

    // Handles the "Confirm" button click event to save the changes
    public void confirmButtonOnAction(ActionEvent event) {
        // Show confirmation dialog
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Edit");
        confirmationAlert.setHeaderText("Are you sure you want to save the changes?");
        confirmationAlert.setContentText("Please confirm your action.");
        Optional<ButtonType> result = confirmationAlert.showAndWait();

        // Exit if the user cancels the action
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        // SQL query to update the book's details in the database
        String query =
                "UPDATE library.book SET title = ?, author = ?, genre = ?, publisher = ?, publicationDate = ?, "
                        + "language = ?, pageNumber = ?, imageUrl = ?, description = ?, quantity = ? WHERE ISBN = ?";

        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            assert connection != null; // Ensure the database connection is not null

            // Prepare the SQL statement
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, title.getText()); // Set updated title
                stmt.setString(2, author.getText()); // Set updated author
                stmt.setString(3, genre.getText()); // Set updated genre
                stmt.setString(4, publisher.getText()); // Set updated publisher
                stmt.setString(5, publicationDate.getText()); // Set updated publication date
                stmt.setString(6, language.getText()); // Set updated language
                stmt.setInt(7, Integer.parseInt(pageNumber.getText())); // Set updated page number
                stmt.setString(8, imageUrl.getText()); // Set updated image URL
                stmt.setString(9, description.getText()); // Set updated description
                stmt.setInt(10, Integer.parseInt(quantity.getText())); // Set updated quantity
                stmt.setString(11, ISBN.getText()); // Set ISBN for WHERE clause
                stmt.executeUpdate(); // Execute the SQL update query

                // Display success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Book Edited");
                alert.setHeaderText(null);
                alert.setContentText("Book edited successfully");
                alert.showAndWait();
                System.out.println("Book edited successfully");
            }
        } catch (SQLException e) {
            // Handle any SQL errors
            System.out.println("Can't edit this book");
            e.printStackTrace();
        }

        // Refresh the book list in the BookController
        bookController.refreshList();

        // Close the edit book window
        cancelButtonOnAction(event);
    }

    // Handles the "Cancel" button click event to close the window
    public void cancelButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow(); // Get the current stage
        stage.close(); // Close the stage
    }
}

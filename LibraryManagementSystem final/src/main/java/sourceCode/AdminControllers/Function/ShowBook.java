package sourceCode.AdminControllers.Function;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import sourceCode.Models.Book;

import static sourceCode.LoginController.imageCache;
import static sourceCode.LoginController.imagedefault;

public class ShowBook {

    // FXML components for displaying book information
    @FXML
    private ImageView image; // Image view for displaying the book's cover
    @FXML
    private TextArea title; // Text area for displaying the book's title
    @FXML
    private Label ISBN; // Label for displaying the book's ISBN
    @FXML
    private Label author; // Label for displaying the author's name
    @FXML
    private Label publisher; // Label for displaying the publisher's name
    @FXML
    private Label date; // Label for displaying the publication date
    @FXML
    private Label genre; // Label for displaying the genre
    @FXML
    private Label language; // Label for displaying the language
    @FXML
    private Label pageNumber; // Label for displaying the number of pages
    @FXML
    private TextArea description; // Text area for displaying the book's description

    // Method to set book details in the corresponding UI components
    public void setBook(Book book) {
        // Set each field with the corresponding value from the Book object
        title.setText(book.getTitle());
        ISBN.setText(book.getISBN());
        author.setText(book.getAuthor());
        genre.setText(book.getGenre());
        publisher.setText(book.getPublisher());
        date.setText(book.getPublicationDate());
        language.setText(book.getLanguage());
        pageNumber.setText(String.valueOf(book.getPageNumber()));
        description.setText(book.getDescription());

        // Check if the book has an image URL and load it
        if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
            loadImageWithCache(book.getImageUrl());
        }
    }

    // Method to load the book's image using a cache to optimize performance
    private void loadImageWithCache(String imageUrl) {
        if (imageCache.containsKey(imageUrl)) {
            // If the image is already in the cache, use the cached image
            image.setImage(imageCache.get(imageUrl));
        } else {
            try {
                // Attempt to load the image directly
                Image img = new Image(imageUrl);
                if (!img.isError()) {
                    // If the image loads successfully, add it to the cache and display it
                    imageCache.put(imageUrl, img);
                    image.setImage(img);
                    System.out.println("Image loaded and cached: " + imageUrl);
                } else {
                    // If there is an error loading the image, use the default image
                    System.out.println("Image error, using default for URL: " + imageUrl);
                    image.setImage(imagedefault);
                }
            } catch (Exception e) {
                // Handle exceptions and use the default image
                System.out.println("Exception while loading image: " + imageUrl);
                e.printStackTrace();
                image.setImage(imagedefault);
            }
        }
    }

    // Action handler for the confirm button to close the window
    public void confirmButtonOnAction(ActionEvent event) {
        // Close the current stage when the confirm button is clicked
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
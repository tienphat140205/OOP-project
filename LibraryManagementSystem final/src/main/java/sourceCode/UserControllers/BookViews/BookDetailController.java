package sourceCode.UserControllers.BookViews;

import static sourceCode.LoginController.imageCache;
import static sourceCode.LoginController.imagedefault;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import sourceCode.Models.Book;
import sourceCode.Services.DatabaseConnection;

public class BookDetailController {

    @FXML
    private ImageView image;
    @FXML
    private Label title;
    @FXML
    private Label ISBN;
    @FXML
    private Label author;
    @FXML
    private Label publisher;
    @FXML
    private Label date;
    @FXML
    private Label genre;
    @FXML
    private Label language;
    @FXML
    private Label pageNumber;
    @FXML
    private TextArea description;
    private Book book;

    public void setBook(Book book) {
        this.book = book;
        title.setText(book.getTitle());
        ISBN.setText(book.getISBN());
        author.setText(book.getAuthor());
        genre.setText(book.getGenre());
        publisher.setText(book.getPublisher());
        date.setText(book.getPublicationDate());
        language.setText(book.getLanguage());
        pageNumber.setText(String.valueOf(book.getPageNumber()));
        if (book.getImageUrl() != null) {
            if (imageCache.containsKey(book.getImageUrl())) {
                // Nếu ảnh đã có trong cache, sử dụng ảnh đó
                image.setImage(imageCache.get(book.getImageUrl()));
            } else {
                try {
                    // Tải ảnh trực tiếp trong luồng chính
                    Image img = new Image(book.getImageUrl());
                    if (!img.isError()) {
                        // Nếu tải thành công, lưu vào cache và hiển thị ảnh
                        imageCache.put(book.getImageUrl(), img);
                        image.setImage(img);
                        System.out.println("Image loaded and cached: " + book.getImageUrl());
                    } else {
                        // Nếu ảnh có lỗi, sử dụng ảnh mặc định
                        System.out.println(
                                "Image error, using default for URL: " + book.getImageUrl());
                        image.setImage(imagedefault);
                    }
                } catch (Exception e) {
                    // Xử lý ngoại lệ và sử dụng ảnh mặc định
                    System.out.println("Exception while loading image: " + book.getImageUrl());
                    e.printStackTrace();
                    image.setImage(imagedefault);
                }
            }
        }
        description.setText(book.getDescription());
    }

    public void borrowBook() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Borrow Book");
        alert.setHeaderText("Confirm Book Borrowing");
        alert.setContentText("Are you sure you want to borrow this book?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        String checkQuery = "SELECT quantity FROM library.book WHERE ISBN = ?";
        String returnLateQuery = "SELECT COUNT(*) FROM library.ticket t WHERE userId = ? AND returnedDate IS NOT NULL AND DATEDIFF(returnedDate, borrowedDate) > 30";
        String lateQuery = "SELECT COUNT(*) FROM library.ticket t WHERE userId = ? AND returnedDate IS NULL AND DATEDIFF(CURDATE(), borrowedDate) > 30";
        String borrowQuery = "SELECT COUNT(*) FROM library.ticket t WHERE userId = ? AND returnedDate IS NULL AND DATEDIFF(CURDATE(), borrowedDate) <= 30";
        String insertQuery = "INSERT INTO library.ticket (ISBN, userID, borrowedDate, returnedDate, quantity) VALUES (?, ?, CURDATE(), null, 1)";
        String updateBookQuery = "UPDATE library.book SET quantity = quantity - 1 WHERE ISBN = ?";

        ExecutorService executor = Executors.newFixedThreadPool(4);
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            assert conn != null;

            // Future để kiểm tra số lượng sách
            Future<Integer> checkFuture = executor.submit(() -> {
                try (PreparedStatement stmt = conn.prepareStatement(checkQuery)) {
                    stmt.setString(1, book.getISBN());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt("quantity");
                        }
                    }
                }
                return 0;
            });

            Future<Integer> returnLateFuture = executor.submit(() -> {
                try (PreparedStatement stmt = conn.prepareStatement(returnLateQuery)) {
                    stmt.setString(1, sourceCode.LoginController.currentUserId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt(1);
                        }
                    }
                }
                return 0;
            });

            Future<Integer> lateFuture = executor.submit(() -> {
                try (PreparedStatement stmt = conn.prepareStatement(lateQuery)) {
                    stmt.setString(1, sourceCode.LoginController.currentUserId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt(1);
                        }
                    }
                }
                return 0;
            });

            Future<Integer> borrowFuture = executor.submit(() -> {
                try (PreparedStatement stmt = conn.prepareStatement(borrowQuery)) {
                    stmt.setString(1, sourceCode.LoginController.currentUserId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt(1);
                        }
                    }
                }
                return 0;
            });
            int bookQuantity = checkFuture.get();
            int returnLateCount = returnLateFuture.get();
            int lateCount = lateFuture.get();
            int borrowCount = borrowFuture.get();
            if (lateCount >= 1) {
                showAlert(Alert.AlertType.ERROR, "Borrowing Error",
                        "You have too many overdue books.");
                return;
            }
            if (returnLateCount >= 3) {
                showAlert(Alert.AlertType.ERROR, "Borrowing Error",
                        "You have too many late returns.");
                return;
            }
            if (borrowCount >= 5) {
                showAlert(Alert.AlertType.ERROR, "Borrowing Error",
                        "You have borrowed the maximum number of books allowed.");
                return;
            }
            if (bookQuantity <= 0) {
                showAlert(Alert.AlertType.ERROR, "Borrowing Error",
                        "The book is not available for borrowing.");
                return;
            }
            System.out.println(insertQuery);
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setString(1, book.getISBN());
                insertStmt.setString(2, sourceCode.LoginController.currentUserId);
                insertStmt.executeUpdate();
            }
            try (PreparedStatement updateBookStmt = conn.prepareStatement(updateBookQuery)) {
                updateBookStmt.setString(1, book.getISBN());
                updateBookStmt.executeUpdate();
                System.out.println("Book quantity updated successfully");
            }

        } catch (SQLException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Borrowing Error", "Book borrowing failed.");
        } finally {
            executor.shutdown();
        }
    }

    public void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void confirmButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }
}

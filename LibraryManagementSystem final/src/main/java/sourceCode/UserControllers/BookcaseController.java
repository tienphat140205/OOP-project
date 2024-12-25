package sourceCode.UserControllers;

import static sourceCode.LoginController.imagedefault;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sourceCode.Models.Book;
import sourceCode.Services.DatabaseConnection;
import sourceCode.Services.DatabaseOperation;
import sourceCode.Services.SwitchScene;
import sourceCode.UserControllers.BookViews.BookCellController;
import sourceCode.UserControllers.Function.AddFeedback;

public class BookcaseController extends SwitchScene implements Initializable {

    private static final String defaultQuery =
            "SELECT * FROM library.ticket t JOIN library.book b ON t.ISBN = b.ISBN "
                    + "WHERE t.returnedDate IS NULL AND t.userID = '" + sourceCode.LoginController.currentUserId + "'";
    private static final ObservableList<Book> bookList = FXCollections.observableArrayList();
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    @FXML
    public AnchorPane bookDetail;
    @FXML
    public Pane myPane;
    @FXML
    private TextField searchBar;
    @FXML
    private ChoiceBox<String> choiceBox;
    @FXML
    private SplitPane splitPane;
    @FXML
    private ListView<Book> bookListView;
    @FXML
    private ImageView bookImage;
    @FXML
    private Label bookTitle;
    @FXML
    private Label bookISBN;
    @FXML
    private Label bookAuthor;
    @FXML
    private Label bookPublisher;
    @FXML
    private Label bookPublicationDate;
    @FXML
    private Label bookGenre;
    @FXML
    private Label bookLanguage;
    @FXML
    private Label bookPageNumber;
    @FXML
    private Label bookDescription;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchBar.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().toString().equals("ENTER")) {
                searchBook();
            }
        });
        splitPane.setDividerPositions(1);
        String[] searchBy = {"All", "ISBN", "Title", "Author", "Genre"};
        choiceBox.getItems().addAll(searchBy);
        choiceBox.setValue(searchBy[0]);
        bookListView.setItems(bookList);
        bookListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Book book, boolean empty) {
                super.updateItem(book, empty);
                if (empty || book == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Task<Parent> loadCellTask = new Task<>() {
                        @Override
                        protected Parent call() throws Exception {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                                    "/sourceCode/UserFXML/BookCell.fxml"));
                            Parent cell = loader.load();
                            BookCellController controller = loader.getController();
                            controller.setBook(book);
                            return cell;
                        }
                    };
                    loadCellTask.setOnSucceeded(event -> setGraphic(loadCellTask.getValue()));
                    executor.submit(loadCellTask);
                }
            }
        });
        bookListView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        if (newValue.getImageUrl() != null) {
                            try {
                                Image image = new Image(newValue.getImageUrl());
                                if (image.isError()) {
                                    bookImage.setImage(imagedefault); // Gán ảnh mặc định
                                } else {
                                    bookImage.setImage(image); // Gán ảnh nếu tải thành công
                                }
                            } catch (Exception e) {
                                bookImage.setImage(imagedefault);
                            }
                        } else {
                            bookImage.setImage(imagedefault);
                        }
                        bookTitle.setText(newValue.getTitle());
                        bookISBN.setText("ISBN: " + newValue.getISBN());
                        bookAuthor.setText("Title: " + newValue.getAuthor());
                        bookPublisher.setText("Publisher: " + newValue.getPublisher());
                        bookPublicationDate.setText(
                                "Date: " + newValue.getPublicationDate());
                        bookGenre.setText("Genre: " + newValue.getGenre());
                        bookLanguage.setText("Language: " + newValue.getLanguage());
                        bookPageNumber.setText("Page number: " + newValue.getPageNumber());
                        bookDescription.setText("Description: " + newValue.getDescription());
                        splitPane.setDividerPositions(0.6);
                    }
                });
        DatabaseOperation.loadBookfromDatabase(defaultQuery, bookList);
    }

    public void searchBook() {
        String query = defaultQuery;
        if (!searchBar.getText().isEmpty()) {
            switch (choiceBox.getValue()) {
                case "ISBN":
                    query += " AND b.ISBN LIKE ?";
                    break;
                case "Title":
                    query += " AND b.title LIKE ?";
                    break;
                case "Author":
                    query += " AND b.author LIKE ?";
                    break;
                case "Genre":
                    query += " AND b.genre LIKE ?";
                    break;
                default:
                    query += " AND b.ISBN LIKE ? OR b.title LIKE ? OR b.author LIKE ? OR b.genre LIKE ?";
                    break;
            }
        }
        load(query);
    }

    public void returnBook() {

        sourceCode.Models.Book selectedBook = bookListView.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            System.out.println("Please select a book to return.");
            return;
        }
        Alert alert = new Alert(AlertType.CONFIRMATION,
                "Are you sure you want to return this book?",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm Book Return");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                String currentUserID = sourceCode.LoginController.currentUserId;
                String returnticketQuery = """
                            UPDATE library.ticket
                            SET returnedDate = CURRENT_DATE
                            WHERE userId = ? AND ISBN = ? AND returnedDate IS NULL
                            LIMIT 1;
                        """;
                String updatequantityQuery = """
                            UPDATE library.book
                            SET quantity = quantity + 1
                            WHERE ISBN = ?;
                        """;

                try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
                    try (PreparedStatement pstmt = conn.prepareStatement(returnticketQuery)) {
                        pstmt.setString(1, currentUserID);
                        pstmt.setString(2, selectedBook.getISBN());
                        int rowsAffected = pstmt.executeUpdate();
                        if (rowsAffected > 0) {
                            try (PreparedStatement pstmt1 = conn.prepareStatement(
                                    updatequantityQuery)) {
                                pstmt1.setString(1, selectedBook.getISBN());
                                rowsAffected = pstmt1.executeUpdate();
                                if (rowsAffected > 0) {
                                    DatabaseOperation.loadBookfromDatabase(defaultQuery, bookList);
                                    System.out.println(
                                            "Return book successfully" + selectedBook.getTitle());
                                } else {
                                    System.out.println("");
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                                System.out.println("Database connection error.");
                            }
                        } else {
                            System.out.println("Can't return book, please try again.");
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println("Database connection error.");
                }
                initialize(null, null);
            }
        });
    }

    public void sendFeedback() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/sourceCode/UserFXML/AddFeedback.fxml"));
            Parent root = loader.load();
            AddFeedback controller = loader.getController();
            controller.setISBN(bookISBN.getText());
            System.out.println(controller.getISBN());
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Add Feedback");
            stage.initStyle(StageStyle.UNDECORATED);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load(String query) {
        Task<ObservableList<Book>> loadBooksTask = new Task<>() {
            @Override
            protected ObservableList<Book> call() throws Exception {
                ObservableList<Book> books = FXCollections.observableArrayList();
                try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
                    assert conn != null;
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setString(1, sourceCode.LoginController.currentUserId);
                        try (ResultSet rs = stmt.executeQuery()) {
                            while (rs.next()) {
                                Book book = new Book(
                                        rs.getString("ISBN"),
                                        rs.getString("title"),
                                        rs.getString("author"),
                                        rs.getString("genre"),
                                        rs.getString("publisher"),
                                        rs.getString("publicationDate"),
                                        rs.getString("language"),
                                        rs.getInt("pageNumber"),
                                        rs.getString("imageUrl"),
                                        rs.getString("description"),
                                        rs.getInt("quantity")
                                );
                                books.add(book);
                            }
                        }
                    }
                }
                return books;
            }
        };
        loadBooksTask.setOnSucceeded(event -> {
            bookList.setAll(loadBooksTask.getValue());
        });
        loadBooksTask.setOnFailed(event -> {
            System.err.println("Lỗi khi tải dữ liệu từ database: " + loadBooksTask.getException());
        });
        executor.submit(loadBooksTask);
    }
}
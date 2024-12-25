package sourceCode.UserControllers;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import javafx.util.Duration;
import sourceCode.Models.Book;
import sourceCode.Services.DatabaseOperation;
import sourceCode.Services.SwitchScene;
import sourceCode.UserControllers.BookViews.BookGridController;
import sourceCode.UserControllers.Function.Chatbot;

public class LibraryController extends SwitchScene implements Initializable {

    private static final ObservableList<Book> bookList = FXCollections.observableArrayList();
    private static final ObservableList<Book> recommendBookList = FXCollections.observableArrayList();
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    @FXML
    private SplitPane mySplitPane;
    @FXML
    private ScrollPane myScrollPane;
    @FXML
    private TilePane myTilePane;
    @FXML
    private ListView<Book> myListView;
    @FXML
    private TextField searchBar;
    @FXML
    private ChoiceBox<String> choiceBox;
    @FXML
    private Button showRecommendedBook;
    @FXML
    private ImageView chatbotIcon;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        searchBar.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().toString().equals("ENTER")) {
                searchBook();
            }
        });
        chatbotIcon.setLayoutY(353);
        mySplitPane.setDividerPositions(0.465);
        myTilePane.setVisible(false);
        myScrollPane.setVisible(false);
        String[] searchBy = {"All", "ISBN", "Title", "Author", "Genre"};
        choiceBox.getItems().addAll(searchBy);
        choiceBox.setValue(searchBy[0]);
        showRecommendedBook.setText("Hide Recommend");
        initRecommendBookList();
    }

    public void initRecommendBookList() {
        String query = "SELECT * FROM library.Book LIMIT 20";
        recommendBookList.clear();
        DatabaseOperation.loadBookfromDatabase(query, recommendBookList);
        myListView.setItems(recommendBookList);
        myListView.setCellFactory(lv -> new ListCell<>() {
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
                                    "/sourceCode/UserFXML/BookGrid.fxml"));
                            Parent cell = loader.load();
                            BookGridController controller = loader.getController();
                            controller.setBook(book);
                            return cell;
                        }
                    };
                    loadCellTask.setOnSucceeded(event -> {
                        Parent graphic = loadCellTask.getValue();
                        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), graphic);
                        fadeIn.setFromValue(0);
                        fadeIn.setToValue(1);
                        fadeIn.play();

                        setGraphic(graphic);
                    });
                    executor.submit(loadCellTask);
                }
            }
        });

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    int currentIndex = myListView.getFocusModel().getFocusedIndex();
                    int nextIndex = (currentIndex + 1) % recommendBookList.size();
                    ListCell<Book> currentCell = (ListCell<Book>) myListView.lookup(
                            ".list-cell:nth-child(" + (currentIndex + 1) + ")");
                    ListCell<Book> nextCell = (ListCell<Book>) myListView.lookup(
                            ".list-cell:nth-child(" + (nextIndex + 1) + ")");
                    if (currentCell != null && nextCell != null) {
                        TranslateTransition transition = new TranslateTransition(
                                Duration.millis(500), nextCell);
                        transition.setFromX(-50);
                        transition.setToX(0);
                        transition.play();
                    }
                    myListView.scrollTo(nextIndex);
                    myListView.getFocusModel().focus(nextIndex);
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        Platform.runLater(timeline::play);
    }

    public void populateTilePane() {
        myTilePane.getChildren().clear();
        int batchSize = 15;

        for (int i = 0; i < bookList.size(); i += batchSize) {
            int start = i;
            int end = Math.min(i + batchSize, bookList.size());

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    for (int j = start; j < end; j++) {
                        Book book = bookList.get(j);
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/sourceCode/UserFXML/BookGrid.fxml"));
                        Pane bookPane = loader.load();
                        BookGridController controller = loader.getController();
                        controller.setBook(book);
                        Platform.runLater(() -> {
                            myTilePane.getChildren().add(bookPane);
                            FadeTransition fadeTransition = new FadeTransition(Duration.millis(500),
                                    bookPane);
                            fadeTransition.setFromValue(0);
                            fadeTransition.setToValue(1);
                            fadeTransition.play();
                        });
                        Thread.sleep(50);
                    }
                    return null;
                }
            };
            new Thread(task).start();
        }
    }

    public void searchBook() {
        if (choiceBox.getValue().equals("All")) {
            DatabaseOperation.loadBookfromDatabase(
                    "SELECT * FROM library.Book WHERE ISBN LIKE '%" + searchBar.getText()
                            + "%' OR title LIKE '%" + searchBar.getText() + "%' OR author LIKE '%"
                            + searchBar.getText() + "%' OR genre LIKE '%" + searchBar.getText()
                            + "%'", bookList);
        } else {
            DatabaseOperation.loadBookfromDatabase(
                    "SELECT * FROM library.Book WHERE " + choiceBox.getValue() + " LIKE '%"
                            + searchBar.getText() + "%'", bookList);
        }
        populateTilePane();
        myTilePane.setVisible(true);
        myScrollPane.setVisible(true);
        mySplitPane.setDividerPositions(0.9);
        myListView.setVisible(false);
        showRecommendedBook.setText("Show Recommend");
    }

    public void showRecommendedBook() {
        if (myListView.isVisible()) {
            myListView.setVisible(false);
            mySplitPane.setDividerPositions(0.9);
            showRecommendedBook.setText("Show Recommend");
            chatbotIcon.setLayoutY(634);
        } else {
            myListView.setVisible(true);
            mySplitPane.setDividerPositions(0.465);
            showRecommendedBook.setText("Hide Recommend");
            chatbotIcon.setLayoutY(353);
        }
    }

    public void showChatbot() {
        Stage stage = new Stage();
        Image icon = new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream("/sourceCode/Image/chatbotIcon.jpg")));
        stage.getIcons().add(icon);
        stage.setTitle("Chatbot");
        stage.centerOnScreen();
        Chatbot chatbot = new Chatbot();
        chatbot.start(stage);
    }
}
package sourceCode.Services;

import java.util.Optional;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SwitchScene {

    private final Map<String, Parent> fxmlCache = new HashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public void preloadFXML(String... fxmlFiles) {
        for (String fxml : fxmlFiles) {
            String path = "/sourceCode/" + fxml + ".fxml";
            if (!fxmlCache.containsKey(fxml)) {
                Task<Parent> loadTask = new Task<>() {
                    @Override
                    protected Parent call() throws Exception {
                        return FXMLLoader.load(
                                Objects.requireNonNull(getClass().getResource(path)));
                    }
                };

                loadTask.setOnSucceeded(event -> fxmlCache.put(fxml, loadTask.getValue()));
                loadTask.setOnFailed(event -> {
                    System.err.println("Failed to load FXML: " + fxml);
                    loadTask.getException().printStackTrace();
                });

                executor.submit(loadTask);
            }
        }
    }

    public void switchTo(ActionEvent event, String fxml) {
        try {
            Parent root = fxmlCache.getOrDefault(fxml, loadFXML(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Parent loadFXML(String fxml) throws IOException {
        String path = "/sourceCode/" + fxml + ".fxml";
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(path)));
        fxmlCache.put(fxml, root);
        return root;
    }

    public void switchToHome(ActionEvent event) {
        switchTo(event, "AdminFXML/Home");
    }

    public void switchToUser(ActionEvent event) {
        switchTo(event, "AdminFXML/User");
    }

    public void switchToBook(ActionEvent event) {
        switchTo(event, "AdminFXML/Book");
    }

    public void switchToTicket(ActionEvent event) {
        switchTo(event, "AdminFXML/Ticket");
    }

    public void switchToFeedback(ActionEvent event) {
        switchTo(event, "AdminFXML/Feedback");
    }

    public void switchToLogin(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log out");
        alert.setContentText("Do you want to log out?");
        Optional<ButtonType> a = alert.showAndWait();
        if (a.isEmpty() || a.get() != ButtonType.OK) {
            return;
        }
        if (alert.getResult().getText().equals("OK")) {
            switchTo(event, "AdminFXML/Login");
        } else {
            alert.close();
        }
    }

    public void switchToLibrary(ActionEvent event) {
        switchTo(event, "UserFXML/Library");
    }

    public void switchToBookcase(ActionEvent event) {
        switchTo(event, "UserFXML/Bookcase");
    }

    public void switchToMyTicket(ActionEvent event) {
        switchTo(event, "UserFXML/Ticket");
    }

    public void switchToMyFeedback(ActionEvent event) {
        switchTo(event, "UserFXML/Feedback");
    }

    public void switchToProfile(ActionEvent event) {
        switchTo(event, "UserFXML/Profile");
    }

    public void shutdown() {
        executor.shutdown();
    }
}
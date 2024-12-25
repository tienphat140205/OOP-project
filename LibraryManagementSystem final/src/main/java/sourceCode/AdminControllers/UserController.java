package sourceCode.AdminControllers;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sourceCode.AdminControllers.Function.AddUser;
import sourceCode.AdminControllers.Function.EditUser;
import sourceCode.AdminControllers.Function.ShowUser;
import sourceCode.Models.User;
import sourceCode.Services.DatabaseConnection;
import sourceCode.Services.DatabaseOperation;
import sourceCode.Services.SwitchScene;

public class UserController extends SwitchScene implements Initializable {

    private static final ObservableList<User> userList = FXCollections.observableArrayList();
    @FXML
    private TableView<User> userTableView;
    @FXML
    private TableColumn<User, String> useridColumn;
    @FXML
    private TableColumn<User, String> fullnameColumn;
    @FXML
    private TableColumn<User, String> identitynumberColumn;
    @FXML
    private TableColumn<User, String> birthColumn;
    @FXML
    private ChoiceBox<String> choiceBox;
    @FXML
    private TextField searchBar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        searchBar.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().toString().equals("ENTER")) {
                searchUser();
            }
        });
        String[] searchBy = {"All", "User ID", "Full Name", "Identity Number",
                "Birth"};
        choiceBox.getItems().addAll(searchBy);
        choiceBox.setValue(searchBy[0]);
        userTableView.setItems(userList);
        useridColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        fullnameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        identitynumberColumn.setCellValueFactory(new PropertyValueFactory<>("identityNumber"));
        birthColumn.setCellValueFactory(new PropertyValueFactory<>("birth"));
        refreshList();
    }

    public void refreshList() {
        DatabaseOperation.loadUserfromDatabase("SELECT * FROM library.user", userList);
    }

    public void searchUser() {
        if (searchBar.getText().isEmpty()) {
            refreshList();
            return;
        }
        String query = "SELECT * FROM library.user WHERE ";
        switch (choiceBox.getValue()) {
            case "User ID":
                query += "userId = '" + searchBar.getText() + "'";
                break;
            case "Full Name":
                query += "name LIKE '%" + searchBar.getText() + "%'";
                break;
            case "Identity Number":
                query += "identityNumber = '" + searchBar.getText() + "'";
                break;
            case "Birth":
                query += "birth = '" + searchBar.getText() + "'";
                break;
            default:
                query += "userId LIKE '%" + searchBar.getText() + "%' OR "
                        + "name LIKE '%" + searchBar.getText() + "%' OR "
                        + "identityNumber LIKE '%" + searchBar.getText() + "%' OR "
                        + "birth LIKE '%" + searchBar.getText() + "%'";
                break;
        }
        DatabaseOperation.loadUserfromDatabase(query, userList);
    }

    public void showUser() {
        User user = userTableView.getSelectionModel().getSelectedItem();
        if (user == null) {
            Alert alrt = new Alert(Alert.AlertType.WARNING);
            alrt.setTitle("No User Selected");
            alrt.setHeaderText(null);
            alrt.setContentText("Please select a user to show information.");
            System.out.println("No user selected");
            alrt.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/sourceCode/AdminFXML/ShowUser.fxml"));
            Parent root = loader.load();
            ShowUser showUser = loader.getController();
            showUser.setUser(user);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("User Information");
            stage.initStyle(StageStyle.UNDECORATED);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeUser() {
        User user = userTableView.getSelectionModel().getSelectedItem();
        if (user == null) {
            Alert alrt = new Alert(Alert.AlertType.WARNING);
            alrt.setTitle("No User Selected");
            alrt.setHeaderText(null);
            alrt.setContentText("Please select a user to remove.");
            System.out.println("No user selected");
            alrt.showAndWait();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove User");
        alert.setHeaderText("Can't restore this user after removing");
        alert.setContentText("Do you want to remove this user ?");
        Optional<ButtonType> a = alert.showAndWait();
        if (a.isEmpty() || a.get() != ButtonType.OK) {
            return;
        }

        String query = "DELETE FROM library.user WHERE userId = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            assert connection != null;
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, user.getUserId());
                stmt.executeUpdate();
                userList.remove(user);
                Alert alrt = new Alert(Alert.AlertType.INFORMATION);
                alrt.setTitle("User Removed");
                alrt.setHeaderText(null);
                alrt.setContentText("User removed successfully");
                alrt.showAndWait();
                System.out.println("User removed successfully");
            }
        } catch (SQLException e) {
            System.out.println("User removing failed");
            e.printStackTrace();
        }
    }

    public void editUser() {
        User user = userTableView.getSelectionModel().getSelectedItem();
        if (user == null) {
            Alert alrt = new Alert(Alert.AlertType.WARNING);
            alrt.setTitle("No User Selected");
            alrt.setHeaderText(null);
            alrt.setContentText("Please select a user to edit.");
            System.out.println("No user selected");
            alrt.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/sourceCode/AdminFXML/EditUser.fxml"));
            Parent root = loader.load();
            EditUser editUser = loader.getController();
            editUser.setUser(user);
            editUser.setUserController(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Edit User");
            stage.initStyle(StageStyle.UNDECORATED);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/sourceCode/AdminFXML/AddUser.fxml"));
            Parent root = loader.load();
            AddUser addUser = loader.getController();
            addUser.setUserController(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Add User");
            stage.initStyle(StageStyle.UNDECORATED);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

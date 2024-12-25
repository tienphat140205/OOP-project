package sourceCode.Services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.collections.ObservableList;
import sourceCode.Models.Book;
import sourceCode.Models.Feedback;
import sourceCode.Models.Ticket;
import sourceCode.Models.User;

public class DatabaseOperation {
    public static void loadTicketfromDatabase(String query, ObservableList<Ticket> ticketList) {
        ticketList.clear();
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            assert conn != null;
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    Ticket ticket = new Ticket(
                            rs.getInt("ticketId"),
                            rs.getString("ISBN"),
                            rs.getString("userId"),
                            rs.getDate("borrowedDate") != null ? rs.getDate("borrowedDate")
                                    .toLocalDate() : null,
                            rs.getDate("returnedDate") != null ? rs.getDate("returnedDate")
                                    .toLocalDate() : null,
                            rs.getInt("quantity"),
                            rs.getString("status")
                    );
                    ticketList.add(ticket);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void loadBookfromDatabase(String query, ObservableList<Book> bookList) {
        bookList.clear();
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
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
                        bookList.add(book);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void loadUserfromDatabase(String query, ObservableList<User> userList) {
        userList.clear();
            try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
                assert connection != null;
                try (Statement stmt = connection.createStatement();
                        ResultSet rs = stmt.executeQuery(query)) {
                    while (rs.next()) {
                        User user = new User(
                                rs.getString("userId"),
                                rs.getString("name"),
                                rs.getString("identityNumber"),
                                rs.getDate("birth").toLocalDate(),
                                rs.getString("gender"),
                                rs.getString("phoneNumber"),
                                rs.getString("email"),
                                rs.getString("address"),
                                rs.getString("password"),
                                rs.getString("role")
                        );
                        userList.add(user);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }
    public static void loadFeedbackfromDatabase(String query, ObservableList<Feedback> feedbackList) {
        feedbackList.clear();
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            assert conn != null;
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    sourceCode.Models.Feedback feedback = new sourceCode.Models.Feedback(
                            rs.getInt("feedbackId"),
                            rs.getString("userId"),
                            rs.getString("ISBN"),
                            rs.getString("comment"),
                            rs.getInt("rating"),
                            rs.getDate("date").toLocalDate()
                    );
                    feedbackList.add(feedback);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

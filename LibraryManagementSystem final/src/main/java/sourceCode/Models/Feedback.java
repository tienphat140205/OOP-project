package sourceCode.Models;

import java.time.LocalDate;

public class Feedback {

    private int feedbackID;
    private String userID;
    private String ISBN;
    private String comment;
    private int rating;
    private LocalDate date;

    public Feedback() {
        feedbackID = 0;
        userID = "";
        ISBN = "";
        comment = "";
        rating = 0;
        date = null;
    }

    public Feedback(int feedbackID, String userID, String ISBN, String comment, int rating,
            LocalDate date) {
        this.feedbackID = feedbackID;
        this.userID = userID;
        this.ISBN = ISBN;
        this.comment = comment;
        this.rating = rating;
        this.date = date;
    }

    public int getFeedbackID() {
        return feedbackID;
    }

    public void setFeedbackID(int feedbackID) {
        this.feedbackID = feedbackID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}

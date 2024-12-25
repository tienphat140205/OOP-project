package sourceCode.UserControllers.Function;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Scanner;

import org.json.JSONObject;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class Chatbot extends Application {

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=AIzaSyAy2xi9SEzl5ZKaRbfa_m9o1GSgMQFLYrA"; // Replace with actual API key

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        VBox messagesContainer = new VBox(10);
        messagesContainer.setPadding(new Insets(10));

        ScrollPane chatScrollPane = new ScrollPane(messagesContainer);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.getStyleClass().add("scroll-pane");

        TextField userInput = new TextField();
        userInput.getStyleClass().add("input-area");
        userInput.setPromptText("Type your message here...");

        Button sendButton = new Button("Send");
        sendButton.getStyleClass().add("send-button");
        
        HBox inputContainer = new HBox(10);
        inputContainer.setAlignment(Pos.CENTER);
        inputContainer.getChildren().addAll(userInput, sendButton);
        HBox.setHgrow(userInput, Priority.ALWAYS);

        VBox layout = new VBox(10);
        layout.getChildren().addAll(chatScrollPane, inputContainer);
        layout.setPadding(new Insets(10));
        layout.setStyle("-fx-background-color: #f8f9fa;");
        VBox.setVgrow(chatScrollPane, Priority.ALWAYS);

        Scene scene = new Scene(layout, 800, 600);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/sourceCode/UserCSS/chatbot.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();

        sendButton.setOnAction(e -> {
            sendUserMessage(userInput, messagesContainer);
        });

        userInput.setOnAction(e -> {
            sendUserMessage(userInput, messagesContainer);
        });

        userInput.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().toString().equals("ENTER")) {
                sendUserMessage(userInput, messagesContainer);
            }
        });
    }

    private void sendUserMessage(TextField userInput, VBox messagesContainer) {
        String userMessage = userInput.getText();
        if (!userMessage.isEmpty()) {
            HBox userMessageBox = new HBox();
            userMessageBox.setAlignment(Pos.CENTER_RIGHT);
            Text userText = new Text(userMessage);
            VBox userBubble = new VBox(userText);
            userBubble.getStyleClass().add("user-bubble");
            userMessageBox.getChildren().add(userBubble);
            messagesContainer.getChildren().add(userMessageBox);

            userInput.clear();

            String botResponse = sendMessageToChatbot(
                    "As an expert in books, please answer the following question: " + userMessage);
            
            HBox botMessageBox = new HBox();
            botMessageBox.setAlignment(Pos.CENTER_LEFT);
            TextFlow botTextFlow = new TextFlow();
            VBox botBubble = new VBox(botTextFlow);
            botBubble.getStyleClass().add("chat-bubble");
            botMessageBox.getChildren().add(botBubble);
            messagesContainer.getChildren().add(botMessageBox);
            
            displayFormattedResponse(botResponse, botTextFlow);

            messagesContainer.heightProperty().addListener((obs, old, val) -> {
                messagesContainer.getParent().layout();
                ((ScrollPane) messagesContainer.getParent()).setVvalue(1.0);
            });
        }
    }

    private String sendMessageToChatbot(String message) {
        try {
            URL url = new URL(GEMINI_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            String payload = "{\n" +
                    "  \"contents\": [{\n" +
                    "    \"parts\": [{\"text\": \"" + message + "\"}]\n" +
                    "  }]\n" +
                    "}";
            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
                writer.write(payload);
            }
            int responseCode = conn.getResponseCode();
            StringBuilder responseBuilder = new StringBuilder();

            try (Scanner scanner = new Scanner(new InputStreamReader(conn.getInputStream()))) {
                while (scanner.hasNextLine()) {
                    responseBuilder.append(scanner.nextLine());
                }
            }
            String responseBody = responseBuilder.toString();
            System.out.println(
                    "API Response: " + responseBody);  // Print the raw response for debugging
            if (responseCode == HttpURLConnection.HTTP_OK) {
                JSONObject jsonResponse = new JSONObject(responseBody);

                if (jsonResponse.has("candidates")
                        && !jsonResponse.getJSONArray("candidates").isEmpty()) {
                    JSONObject firstCandidate = jsonResponse.getJSONArray("candidates")
                            .getJSONObject(0);
                    JSONObject content = firstCandidate.getJSONObject("content");

                    return content
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");
                } else {
                    return "Error: No candidates found in the response.";
                }
            } else {
                try (Scanner scanner = new Scanner(conn.getErrorStream())) {
                    StringBuilder errorResponse = new StringBuilder();
                    while (scanner.hasNextLine()) {
                        errorResponse.append(scanner.nextLine());
                    }
                    return "Error: Unable to contact chatbot API. Response code: " + responseCode
                            + "\nDetails: " + errorResponse;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    private void displayFormattedResponse(String botResponse, TextFlow botTextFlow) {
        botTextFlow.getChildren().clear();

        String[] lines = botResponse.split("\n");

        for (String line : lines) {
            Text textLine = new Text(line + "\n");
            if (line.contains("**")) {
                String[] boldParts = line.split("\\*\\*");
                for (int i = 0; i < boldParts.length; i++) {
                    if (i % 2 == 1) {
                        Text boldText = new Text(boldParts[i]);
                        boldText.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                        botTextFlow.getChildren().add(boldText);
                    } else {
                        botTextFlow.getChildren().add(new Text(boldParts[i]));
                    }
                }
            } else {
                if (line.contains("*")) {
                    String[] italicParts = line.split("\\*");
                    for (int i = 0; i < italicParts.length; i++) {
                        if (i % 2 == 1) {
                            Text italicText = new Text(italicParts[i]);
                            italicText.setFont(Font.font("Arial", FontWeight.NORMAL,
                                    12));  // Set italic style here if needed
                            botTextFlow.getChildren().add(italicText);
                        } else {
                            botTextFlow.getChildren().add(new Text(italicParts[i]));
                        }
                    }
                } else {
                    botTextFlow.getChildren().add(textLine);
                }
            }
        }
    }
}
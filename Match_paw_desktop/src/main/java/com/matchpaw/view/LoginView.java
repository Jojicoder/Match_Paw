package com.matchpaw.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchpaw.ApiClient;
import com.matchpaw.model.LoginResponse;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginView {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Executor executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    public static Scene create(Runnable onSuccess) {
        var titleLabel = new Label("MatchPaw");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 30));

        var subtitleLabel = new Label("Shelter Management System");
        subtitleLabel.setFont(Font.font("System", 14));
        subtitleLabel.setStyle("-fx-text-fill: #666;");

        var emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setMaxWidth(300);

        var passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);

        var loginButton = new Button("Login");
        loginButton.setMaxWidth(300);
        loginButton.setDefaultButton(true);
        loginButton.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 14;");

        var statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red;");

        var box = new VBox(14, titleLabel, subtitleLabel, emailField, passwordField, loginButton, statusLabel);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        box.setMaxWidth(380);
        box.setStyle("""
                -fx-background-color: white;
                -fx-border-color: #ddd;
                -fx-border-radius: 8;
                -fx-background-radius: 8;
                """);

        var root = new StackPane(box);
        root.setStyle("-fx-background-color: #ecf0f1;");

        loginButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            if (email.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Please enter your email and password.");
                return;
            }
            loginButton.setDisable(true);
            statusLabel.setStyle("-fx-text-fill: gray;");
            statusLabel.setText("Logging in...");

            executor.execute(() -> {
                try {
                    String body = mapper.writeValueAsString(Map.of("email", email, "password", password));
                    var resp = ApiClient.post("/api/users/login", body);

                    if (resp.statusCode() == 200) {
                        var loginResp = mapper.readValue(resp.body(), LoginResponse.class);
                        ApiClient.setSession(loginResp.getToken(), loginResp.getUserId(), loginResp.getRole());
                        Platform.runLater(onSuccess);
                    } else {
                        Platform.runLater(() -> {
                            statusLabel.setStyle("-fx-text-fill: red;");
                            statusLabel.setText("Invalid email or password.");
                            loginButton.setDisable(false);
                        });
                    }
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        statusLabel.setStyle("-fx-text-fill: red;");
                        statusLabel.setText("Connection error: " + ex.getMessage());
                        loginButton.setDisable(false);
                    });
                }
            });
        });

        return new Scene(root, 900, 600);
    }
}

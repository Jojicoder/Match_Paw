package com.matchpaw;

import com.matchpaw.view.LoginView;
import com.matchpaw.view.MainView;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("MatchPaw Shelter");
        stage.setResizable(true);
        showLogin();
        stage.show();
    }

    private void showLogin() {
        stage.setScene(LoginView.create(this::showMain));
    }

    private void showMain() {
        stage.setScene(MainView.create(this::showLogin));
    }

    public static void main(String[] args) {
        launch();
    }
}
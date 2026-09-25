package com.kad.ui;

import javafx.application.Application;
import javafx.stage.Stage;

public class MatchManagementLauncher extends Application {
    @Override
    public void start(Stage stage) {
        MatchManagementView view = new MatchManagementView();
        view.start(stage);  // Appelle la méthode start() de MatchManagementView
    }

    public static void main(String[] args) {
        launch(args);  // Lance l'application
    }
}

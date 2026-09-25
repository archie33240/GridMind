package com.kad.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import com.kad.database.DatabaseConnection;
import com.kad.tools.UiUtils;
import java.sql.SQLException;
import java.util.Optional;

public class GridMindApp extends Application {
    private Stage primaryStage; // ✅ Champ pour stocker la fenêtre principale

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        try {
            DatabaseConnection.startSession();
            System.out.println("DEBUG: Session PostgreSQL démarrée.");

            // ✅ Intercepte la fermeture de la fenêtre
            primaryStage.setOnCloseRequest(this::handleCloseRequest);

            // ✅ Crée le menu
            MenuBar menuBar = new MenuBar();
            Menu fileMenu = new Menu("Fichier");

            MenuItem saveMenuItem = new MenuItem("Sauvegarder");
            saveMenuItem.setOnAction(event -> saveAll());

            MenuItem exitMenuItem = new MenuItem("Quitter");
            exitMenuItem.setOnAction(event -> primaryStage.close());

            fileMenu.getItems().addAll(saveMenuItem, exitMenuItem);
            menuBar.getMenus().add(fileMenu);

            // ✅ Crée GridListPane
            GridListPane gridListPane = new GridListPane();

            // ✅ Crée les onglets
            TabPane tabPane = new TabPane();
            tabPane.getTabs().addAll(
                new Tab("Grilles", gridListPane),
                new Tab("Matchs", new MatchListPane(gridListPane)),
                new Tab("Compétitions", new MatchManagementView())
            );

            // ✅ Configure la scène
            BorderPane root = new BorderPane();
            root.setTop(menuBar);
            root.setCenter(tabPane);

            primaryStage.setTitle("GridMind - Gestion des Grilles et Compétitions");
            primaryStage.setScene(new Scene(root, 1200, 800));
            primaryStage.show();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ Gère la fermeture de la fenêtre
    private void handleCloseRequest(WindowEvent event) {
        try {
            if (DatabaseConnection.hasUncommittedChanges()) {
                ButtonType yesButton = new ButtonType("Oui");
                ButtonType noButton = new ButtonType("Non");
                Optional<ButtonType> result = UiUtils.showConfirmation(
                    "Attention",
                    "Modifications non sauvegardées",
                    "Vous risquez de perdre les modifications effectuées. Voulez-vous sauvegarder ?",
                    yesButton,
                    noButton
                );

                if (result.isPresent() && result.get() == yesButton) {
                    saveAll();
                }
                else {
                    cancelAll(); // ✅ Annule les modifications si l'utilisateur clique sur "Non"
                }
            }
            primaryStage.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ Sauvegarde toutes les modifications
    private void saveAll() {
        try {
            DatabaseConnection.commitSession();
            UiUtils.showAlert(Alert.AlertType.INFORMATION, "Succès", "Modifications sauvegardées !");
            System.out.println("DEBUG: Modifications sauvegardées.");
        } catch (SQLException e) {
            e.printStackTrace();
            UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de sauvegarder: " + e.getMessage());
        }
    }

    // ✅ Annule toutes les modifications (ROLLBACK)
    private void cancelAll() {
        try {
            DatabaseConnection.rollbackSession();
            UiUtils.showAlert(Alert.AlertType.INFORMATION, "Annulé", "Modifications annulées !");
        } catch (SQLException e) {
            e.printStackTrace();
            UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'annuler: " + e.getMessage());
        }
    }
    // ✅ Méthode stop() (appelée automatiquement par JavaFX)
    @Override
    public void stop() {
        try {
            DatabaseConnection.closeSession();
            System.out.println("DEBUG: Session PostgreSQL fermée.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ Point d'entrée de l'application
    public static void main(String[] args) {
        launch(args);
    }
}
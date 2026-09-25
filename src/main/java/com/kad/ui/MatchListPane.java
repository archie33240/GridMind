package com.kad.ui;

import com.kad.dao.GridMindGridDAO;
import com.kad.dao.GridMindMatchDAO;
import com.kad.dao.impl.GridMindGridDAOImpl;
import com.kad.model.GridMindGrid;
import com.kad.model.GridMindMatch;
import com.kad.model.Match;
import com.kad.tools.UiUtils;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MatchListPane extends VBox {
//    private final ComboBox<GridMindGrid> gridComboBox = new ComboBox<>();
    private final ListView<GridMindMatch> matchListView = new ListView<>();
    private final TableView<GridMindMatch> tableView = new TableView<>();    
    private final Button loadMatchesButton = new Button("Charger les matchs");
    private final Button addMatchButton = new Button("Ajouter un Match");
    private final Button removeMatchButton = new Button("Retirer le match");
    private final Button updateResultButton = new Button("Mettre à jour le Résultat");

    // Nouveaux contrôles pour le filtre par date
    private final DatePicker startDatePicker = new DatePicker();
    private final DatePicker endDatePicker = new DatePicker();
    private final Button filterButton = new Button("Filtrer par date");
    private FilteredList<GridMindMatch> filteredMatches;
    
    // ✅ Déclarez gridDAO ici (champ de classe)
    private GridMindGridDAO gridDAO;
    private GridListPane gridListPane;  // ✅ Champ pour stocker la référence
    
    public MatchListPane(GridListPane gridListPane) {
        try {
        	this.gridListPane = gridListPane;
            this.gridDAO = new GridMindGridDAOImpl();  // ✅ Initialisation dans le constructeur
        } catch (SQLException e) {
            e.printStackTrace();
            UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'initialiser les DAO: " + e.getMessage());

            
        }    	
        // Configuration de la ListView pour les matchs
    	matchListView.setCellFactory(lv -> new ListCell<GridMindMatch>() {
    	    @Override
    	    protected void updateItem(GridMindMatch match, boolean empty) {
    	        super.updateItem(match, empty);
    	        setText(empty ? "" :
    	            (match.getHomeTeam() != null ? match.getHomeTeam() : "?") + " vs " +
    	            (match.getAwayTeam() != null ? match.getAwayTeam() : "?") +
    	            " | Résultat: " + (match.getResult() != null ? match.getResult() : "Non joué") +
    	            " | Date: " + (match.getMatchDate() != null ? match.getMatchDate().toString() : "?")
    	        );
    	    }
    	});
    

        // Configuration des DatePicker pour le filtre
        HBox dateFilterBox = new HBox(10,
            new Label("Du:"), startDatePicker,
            new Label("Au:"), endDatePicker,
            filterButton
        );

        // Boutons
        HBox buttonBox = new HBox(10, loadMatchesButton, addMatchButton, removeMatchButton, updateResultButton);

        // Disposition : ComboBox + Filtre par date + Boutons en haut, ListView en bas
        getChildren().addAll(
            // gridComboBox,
            dateFilterBox,
            new HBox(10, loadMatchesButton, addMatchButton, removeMatchButton, updateResultButton),  // ✅ Ajoutez removeMatchButton
            buttonBox,
            new Label("Matchs de la grille:"),
            matchListView
        );

        // Actions des boutons
        loadMatchesButton.setOnAction(e -> loadMatchesForSelectedGrid());
        addMatchButton.setOnAction(e -> showAddMatchDialog());
        removeMatchButton.setOnAction(e -> removeSelectedMatch());
        updateResultButton.setOnAction(e -> showUpdateResultDialog());
        filterButton.setOnAction(e -> applyDateFilter());
    }

    private void loadMatchesForSelectedGrid() {
        // ✅ Utilisez gridListPane
        GridMindGrid selectedGrid = gridListPane.getSelectedGrid();
        if (selectedGrid != null) {
            try {
            	GridMindMatchDAO matchDAO = new GridMindMatchDAO();
                List<GridMindMatch> matches = matchDAO.getMatchesForGrid(selectedGrid.getId());
                filteredMatches = new FilteredList<>(FXCollections.observableArrayList(matches));
                matchListView.setItems(filteredMatches);

                // Définir les dates par défaut pour le filtre
                setDefaultDateRange(selectedGrid.getPlayDeadline());
            } catch (SQLException e) {
            	UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les matchs: " + e.getMessage());
            }
        } else {
        	UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une grille dans l'onglet 'Grilles'.");
        }
    }

    private void removeSelectedMatch() {
        GridMindGrid selectedGrid = gridListPane.getSelectedGrid();
        GridMindMatch selectedMatch = matchListView.getSelectionModel().getSelectedItem();

        if (selectedGrid == null) {
        	UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une grille");
            return;
        }
        if (selectedMatch == null) {
        	UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un match.");
            return;
        }

        // ✅ Logique pour retirer le match de la grille
        try {
            // Exemple : Utilisez un DAO pour supprimer le match
            // gridMindMatchDAO.removeMatchFromGrid(selectedGrid.getId(), selectedMatch.getId());
        	UiUtils.showAlert(Alert.AlertType.INFORMATION, "Succès", "Match retiré avec succès !");
            // ✅ Rechargez les matchs après suppression
            loadMatchesForSelectedGrid();
        } catch (Exception e) {
        	UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retirer le match: " + e.getMessage());
        }
    }
    
    private void setDefaultDateRange(ZonedDateTime playDeadline) {
        if (playDeadline != null) {
            // Date de fin : playDeadline
            endDatePicker.setValue(playDeadline.toLocalDate());

            // Date de début : playDeadline - 7 jours
            startDatePicker.setValue(playDeadline.minusDays(7).toLocalDate());
        }
    }

    private void applyDateFilter() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
        	UiUtils.showAlert(Alert.AlertType.INFORMATION, "Succès", "Veuillez sélectionner une plage de dates valide");
            return;
        }

        filteredMatches.setPredicate(match -> {
            ZonedDateTime matchDate = match.getMatchDate();
            if (matchDate == null) return false;

            LocalDate matchLocalDate = matchDate.toLocalDate();
            return !matchLocalDate.isBefore(startDate) && !matchLocalDate.isAfter(endDate);
        });
    }

private void showAddMatchDialog() {
    GridMindGrid selectedGrid = gridListPane.getSelectedGrid();
    if (selectedGrid == null) {
    	UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une grille.");
        return;
    }

    // 1. Charger les matchs disponibles
    List<Match> availableMatches;
    try {
    	GridMindMatchDAO matchDAO = new GridMindMatchDAO();
        availableMatches = matchDAO.getAvailableMatches(selectedGrid.getId());
    } catch (SQLException e) {
    	UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les matchs: " + e.getMessage());
        return;
    }

    // 2. Créer le dialogue
    Dialog<Match> dialog = new Dialog<>();
    dialog.setTitle("Ajouter un match");
    dialog.setHeaderText("Sélectionnez un match à ajouter à la grille");

    // 3. Configurer les boutons
    ButtonType addButton = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

    // 4. Créer une TableView simple
    TableView<Match> tableView = new TableView<>();
    TableColumn<Match, String> matchCol = new TableColumn<>("Match");
    matchCol.setCellValueFactory(cellData -> {
        Match match = cellData.getValue();
        return new SimpleStringProperty(
            "Match ID: " + match.getId() +
            " | Date: " + match.getMatchDate()
        );
    });
    tableView.getColumns().add(matchCol);
    tableView.getItems().setAll(FXCollections.observableArrayList(availableMatches));

    // 5. Configurer le contenu
    dialog.getDialogPane().setContent(tableView);

    // 6. Configurer le résultat
    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == addButton) {
            return tableView.getSelectionModel().getSelectedItem();
        }
        return null;
    });

    // 7. Afficher le dialogue
    Optional<Match> result = dialog.showAndWait();
    result.ifPresent(selectedMatch -> {
        try {GridMindMatchDAO matchDAO = new GridMindMatchDAO(); 
            matchDAO.addMatchToGrid(
                selectedGrid.getId(),
                selectedMatch.getId()
            );
        } catch (SQLException e) {
        	UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter le match: " + e.getMessage());
        }
    });
}


    private void showUpdateResultDialog() {
        GridMindMatch selectedMatch = matchListView.getSelectionModel().getSelectedItem();  // <-- Utilise la ListView
        if (selectedMatch == null) {
        	UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un match dans la liste");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Mettre à jour le Résultat");
        dialog.setHeaderText("Match: " + selectedMatch.getHomeTeam() + " vs " + selectedMatch.getAwayTeam());

        ComboBox<String> resultCombo = new ComboBox<>(FXCollections.observableArrayList("1", "N", "2"));
        dialog.getDialogPane().setContent(new VBox(10, new Label("Résultat:"), resultCombo));

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return resultCombo.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            try {
            	GridMindMatchDAO matchDAO = new GridMindMatchDAO();
                matchDAO.updateMatchResult(selectedMatch.getId(), result);
                selectedMatch.setResult(result);
                matchListView.refresh();  // Rafraîchit l'affichage
            } catch (SQLException e) {
            	UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de mettre à jour le résultat: " + e.getMessage());
            }
        });
    }

}
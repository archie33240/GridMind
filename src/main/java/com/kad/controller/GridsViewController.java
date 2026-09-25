package com.kad.controller;

import java.util.List;

import com.kad.model.GridMindGridPrediction;
import com.kad.model.GridMindMatch;
import com.kad.model.GridMindMatchPrediction;
import com.kad.model.GridMindSimpleGrid;
import com.kad.tools.UiUtils;
import com.kad.dao.GridMindGridPredictionDAO;
import com.kad.dao.GridMindMatchDAO;
import com.kad.dao.GridMindMatchPredictionDAO;

import javafx.scene.control.Alert; // ✅ Import manquant pour Alert
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label; // ✅ Pour Label
import javafx.scene.layout.GridPane; // ✅ Pour GridPane
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node; // ✅ Pour Node (optionnel, mais utile)
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GridsViewController {
	private UUID gridMindGridId; // ✅ Stocke l'ID de la grille théorique de base
	private GridMindMatchDAO gridMindMatchDAO;
	private UUID currentGridPredictionId; // ✅ Variable pour stocker gridPredictionId
	
	private GridMindGridPrediction currentGridPrediction;
	private GridMindGridPredictionDAO gridPredictionDAO;
	private GridMindMatchPredictionDAO matchPredictionDAO;
	
	/**
	 * Définit la grille de pronostics actuelle.
	 * @param gridPrediction La grille de pronostics à définir.
	 */
	public void setCurrentGridPrediction(GridMindGridPrediction gridPrediction) {
	    this.currentGridPrediction = gridPrediction;
	}
	// Méthode pour injecter le DAO (appelée depuis ton application)
	public void setGridPredictionDAO(GridMindGridPredictionDAO gridPredictionDAO) {
	    this.gridPredictionDAO = gridPredictionDAO;
	}
	/**
	 * Récupère la grille de pronostics actuelle.
	 * @return La grille de pronostics actuelle.
	 */
	// Méthode pour définir l'ID de la grille de pronostics
	public void setCurrentGridPredictionId(UUID gridPredictionId) {
	    this.currentGridPredictionId = gridPredictionId;
	}

	// Méthode pour récupérer la grille de pronostics actuelle
	private GridMindGridPrediction getCurrentGridPrediction() {
	    if (currentGridPredictionId == null) {
	        return null; // ou lance une exception si nécessaire
	    }
	    return gridPredictionDAO.getGridPredictionById(currentGridPredictionId);
	}
	
	public void setMatchPredictionDAO(GridMindMatchPredictionDAO matchPredictionDAO) {
	    this.matchPredictionDAO = matchPredictionDAO;
	}
	
	// Méthode pour recevoir gridMindGridId et gridMindMatchDAO
	public void setGridMindGridId(UUID gridMindGridId) {
	    this.gridMindGridId = gridMindGridId;
	}

	public void setGridMindMatchDAO(GridMindMatchDAO gridMindMatchDAO) {
	    this.gridMindMatchDAO = gridMindMatchDAO;
	}
	@FXML
    private GridPane gridsContainer;

    @FXML
    private Label pageLabel;

    private List<GridMindSimpleGrid> allGrids;
    private int currentPage = 0;
    private static final int GRIDS_PER_PAGE = 8;
    
    // ✅ Méthode appelée automatiquement par FXMLLoader après l'injection des champs
    @FXML
    private void initialize() {
        System.out.println("🔹 [DEBUG] GridsViewController initialisé");
        applyStyles(); // ✅ Appelle applyStyles ici
    }

    public void setGrids(List<GridMindSimpleGrid> grids) {
        this.allGrids = grids;
        this.currentPage = 0;
        updatePage();
    }

private void updatePage() {
    gridsContainer.getChildren().clear();

    int startIndex = currentPage * GRIDS_PER_PAGE;
    int endIndex = Math.min(startIndex + GRIDS_PER_PAGE, allGrids.size());

    if (currentGridPrediction == null) {
        System.out.println("DEBUG: currentGridPrediction est null !");
        return;
    }
    System.out.println("DEBUG: currentGridPrediction.getId() = " + currentGridPrediction.getId());

    // ✅ Utilisez l'ID de la grille théorique pour charger les matchs
    UUID theoreticalGridId = currentGridPrediction.getGridMindGridId();
    System.out.println("DEBUG: Chargement des matchs pour la grille théorique ID = " + theoreticalGridId);

    if (theoreticalGridId != null) {
        try {
            gridMindMatchDAO.loadMatchesForGrid(theoreticalGridId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    } else {
        System.err.println("DEBUG: theoreticalGridId est null !");
    }

    // ✅ Récupérez les pronostics de matchs pour cette grille
    List<GridMindMatchPrediction> matchPredictions;
    try {
        matchPredictions = getMatchPredictionsForCurrentGrid();
    } catch (SQLException e) {
        e.printStackTrace();
        Label errorLabel = new Label("Erreur lors du chargement des prédictions.");
        gridsContainer.getChildren().add(errorLabel);
        return; // ✅ Arrêtez l'exécution si une erreur survient
    }

    // Crée un VBox principal pour contenir toutes les lignes
    VBox mainVBox = new VBox(1);

    // Ajoute les titres des grilles (Grille 1, Grille 2, etc.)
    HBox titlesRow = new HBox(5);
    Label emptyTitleLabel = new Label();
    emptyTitleLabel.setPrefWidth(240); // ✅ Même largeur que matchLabel
    emptyTitleLabel.setMinWidth(240); // Largeur minimale pour éviter les variations
    titlesRow.getChildren().add(emptyTitleLabel);

 // Ajoutez les titres des grilles
    for (int i = startIndex; i < endIndex; i++) {
        StackPane titlePane = new StackPane(); // Utilisez StackPane pour centrer le texte
        titlePane.setPrefWidth(80); // ✅ Largeur = 3 cases (20px) + 2 espacements (10px) + 2 bordures (1px) = 62px → Arrondissez à 80px pour plus de marge
        titlePane.setPadding(new Insets(0, 10, 0, 10)); // Même padding que gridBox

        Label gridTitle = new Label("Grille " + (i + 1));
        gridTitle.setStyle(
            "-fx-font-weight: bold; " +
            "-fx-font-size: 12px; " +
            "-fx-alignment: center;" // Centre le texte horizontalement
        );
        titlePane.getChildren().add(gridTitle);
        titlesRow.getChildren().add(titlePane);
    }
    mainVBox.getChildren().add(titlesRow);

    // Ajoute les lignes pour chaque match
    for (int matchIndex = 0; matchIndex < matchPredictions.size(); matchIndex++) {
        GridMindMatchPrediction matchPrediction = matchPredictions.get(matchIndex);

        // Crée un HBox pour cette ligne de match
        HBox matchRow = new HBox(5);

        // Nom du match (à gauche)
        Label matchLabel = new Label(
            matchPrediction.getHomeTeam() + " - " + matchPrediction.getAwayTeam()
        );
        matchLabel.setStyle(
            "-fx-font-weight: bold; " +
            "-fx-font-size: 11px;" +
            "-fx-pref-width: 240px;"
        );
        matchRow.getChildren().add(matchLabel);

        // Résultats pour chaque grille
        for (int i = startIndex; i < endIndex; i++) {
            GridMindSimpleGrid grid = allGrids.get(i);
            String combination = grid.getCombination();
            char result = combination.charAt(matchIndex);

            // Crée un HBox pour les 3 cases (1, N, 2) de cette grille
            HBox gridBox = new HBox(1);
            gridBox.setPadding(new Insets(0, 10, 0, 10));
            gridBox.setPrefWidth(80); // ✅ Largeur fixe pour aligner avec titlePane et oddsPane

            StackPane cell1 = createResultCell('1', result == '1');
            StackPane cellN = createResultCell('N', result == 'N');
            StackPane cell2 = createResultCell('2', result == '2');

            gridBox.getChildren().addAll(cell1, cellN, cell2);
            matchRow.getChildren().add(gridBox);
        }

        mainVBox.getChildren().add(matchRow);
    }

    // Ajoute une ligne pour la somme des côtes
    HBox oddsRow = new HBox(5); // Même espacement horizontal que titlesRow

 // Ajoutez un Label vide pour aligner avec la colonne des noms des matchs
 Label emptyOddsLabel = new Label();
 emptyOddsLabel.setPrefWidth(240); // Même largeur que les noms des matchs
 emptyOddsLabel.setMinWidth(240); // Largeur minimale
 oddsRow.getChildren().add(emptyOddsLabel);

 // Ajoutez les sommes des côtes pour chaque grille
 for (int i = startIndex; i < endIndex; i++) {
     GridMindSimpleGrid grid = allGrids.get(i);
     double totalOdds = gridPredictionDAO.calculateTotalOdds(grid, matchPredictions);

     StackPane oddsPane = new StackPane(); // Utilisez StackPane pour centrer le texte
     oddsPane.setPrefWidth(80); // Largeur fixe pour chaque grille
     oddsPane.setPadding(new Insets(0, 10, 0, 10)); // ✅ Même padding que gridBox

     Label oddsLabel = new Label(totalOdds > 0 ? String.format("%.2f", totalOdds) : "");
     oddsLabel.setStyle(
         "-fx-font-weight: bold; " +
         "-fx-font-size: 12px; " +
         "-fx-alignment: center;" // Centre le texte horizontalement
     );
     oddsPane.getChildren().add(oddsLabel);
     oddsRow.getChildren().add(oddsPane);
 }

    mainVBox.getChildren().add(oddsRow);
    gridsContainer.getChildren().add(mainVBox);

    pageLabel.setText("Page " + (currentPage + 1) + "/" + ((allGrids.size() + GRIDS_PER_PAGE - 1) / GRIDS_PER_PAGE));
}

// Méthode pour créer une case de résultat (1, N, 2)
private StackPane createResultCell(char resultChar, boolean hasCheck) {
    StackPane cell = new StackPane();
    cell.setPrefSize(20, 20); // Taille carrée
    cell.setStyle(
        "-fx-background-color: white; " +
        "-fx-border-color: red; " +
        "-fx-border-width: 1px; " +
        "-fx-alignment: center;"
    );

    // Label pour 1, N ou 2 (en rouge, police réduite)
    Label resultLabel = new Label(String.valueOf(resultChar));
    resultLabel.setStyle(
        "-fx-font-weight: bold; " +
        "-fx-text-fill: red; " +
        "-fx-font-size: 8px;" + // Police encore plus réduite pour laisser plus de place au X
        "-fx-alignment: center;" // ✅ Centre le texte
    );

    cell.getChildren().add(resultLabel);

    // Ajoute la croix (X) en noir si le résultat est coché
    if (hasCheck) {
        Label checkLabel = new Label("X");
        checkLabel.setStyle(
            "-fx-font-weight: bold; " +
            "-fx-text-fill: black; " +
            "-fx-font-size: 12px;" + // Police deux fois plus grande pour le X
            "-fx-alignment: center;" // ✅ Centre le texte
        );
        cell.getChildren().add(checkLabel);
    }

    return cell;
}

//Méthode pour récupérer les prédictions de matchs
private List<GridMindMatchPrediction> getMatchPredictionsForCurrentGrid() throws SQLException {
 if (currentGridPrediction == null) {
     throw new IllegalStateException("currentGridPrediction n'est pas initialisé !");
 }
 return matchPredictionDAO.getPredictionsForGridPrediction(currentGridPrediction.getId());
}

private void applyStyles() {
    String style = """
        .grid-box {
            -fx-padding: 10px;
            -fx-border-color: #cccccc;
            -fx-border-width: 1px;
            -fx-background-color: white;
        }
        .match-label {
            -fx-font-weight: bold;
            -fx-padding: 0 10px 0 0;
            -fx-alignment: CENTER-LEFT;
        }
    """;
    gridsContainer.setStyle(style);
}

	//Méthode pour récupérer les matchs
	private List<GridMindMatch> getMatchesForGrid() {
		try {
			return gridMindMatchDAO.getMatchesForGrid(gridMindGridId); // ✅ Utilise gridMindGridId
		} catch (SQLException e) {
			e.printStackTrace();
			UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les matchs: " + e.getMessage());
			return new ArrayList<>();
		}
	}
	
	@FXML
    private void handleNextPage() {
        if ((currentPage + 1) * GRIDS_PER_PAGE < allGrids.size()) {
            currentPage++;
            updatePage();
        }
    }

    @FXML
    private void handlePreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            updatePage();
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) gridsContainer.getScene().getWindow();
        stage.close();
    }
}

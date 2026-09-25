package com.kad.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
//import javafx.css.converter.StringConverter;
import javafx.util.StringConverter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;

import com.kad.model.GridMindGrid;
import com.kad.model.GridMindGridPrediction;
import com.kad.model.GridMindMatch;
import com.kad.model.GridMindMatchPrediction;
import com.kad.model.GridMindSimpleGrid;
import com.kad.model.GridMindGridPredictionSummary;
import com.kad.model.MatchOdds;
import com.kad.tools.UiUtils;
import com.kad.ui.GridListPane;
import com.kad.dao.impl.GridMindMatchPredictionDAOImpl;
import com.kad.dao.impl.GridMindSimpleGridDAOImpl;
import com.kad.dao.GridMindSimpleGridDAO;
import com.kad.database.DatabaseConnection;
import com.kad.kernel.GridMindSimpleGridGenerator;
import com.kad.dao.GridMindGridDAO;
import com.kad.dao.GridMindMatchDAO;
import com.kad.dao.GridMindGridPredictionDAO;
import com.kad.dao.GridMindMatchPredictionDAO;
import com.kad.dao.GridMindSimpleGridBatchInserter;
import com.kad.dao.impl.GridMindGridDAOImpl;
import com.kad.dao.impl.GridMindGridPredictionDAOImpl;
import com.kad.dao.MatchOddsDAO;

import java.io.IOException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la gestion des pronostics (LotoFoot).
 * Gère l'interface utilisateur et les interactions avec les DAO.
 */
public class GridMindPredictionController {

	private GridMindMatchDAO gridMindMatchDAO;
	private GridMindGridPrediction currentGridPrediction;
	private GridListPane gridListPane;  // ✅ Champ pour stocker la référence

	@FXML private Label gridNameLabel;  // ✅ Pour afficher le nom de la grille théorique
	@FXML private TextField predictionNameField;  // ✅ Pour éditer le nom du pronostic
	
	// Pour les PRONOSTICS (éditables, déjà fonctionnel)
	@FXML private VBox matchesContainer;  // ✅ Remplace predictionsTableView
	
    // Champs pour les fourchettes
    @FXML private TextField minHomeWinField, maxHomeWinField;
    @FXML private TextField minDrawField, maxDrawField;
    @FXML private TextField minAwayWinField, maxAwayWinField;
    @FXML private TextField minOddField;  // Côte minimale globale
    @FXML private TextField maxOddField;  // Côte maximale globale

    // Champs pour la garantie
    @FXML private ComboBox<String> guaranteeTypeComboBox;
    @FXML private TextField guaranteePercentageField;

    // Champs Synthèse de la grille
    @FXML private Label simplesLabel;
    @FXML private Label doublesLabel;
    @FXML private Label triplesLabel;
    @FXML private Label combinationsLabel;
    @FXML private Label minOddsLabel;
    @FXML private Label maxOddsLabel;
    
    // Boutons
    @FXML private Button generateGridsButton;
    @FXML private Label summarySimpleGridNumber;
    
    private UUID currentGridId;
    // ✅ Champ pour l'ID du pronostic de grille
    private UUID currentGridPredictionId;  
    
    private Map<UUID, GridMindMatch> matchMap = new HashMap<>();
    // DAO
    private GridMindMatchPredictionDAO predictionDAO;
    private GridMindGridPredictionDAO gridPredictionDAO;
    private GridMindGridDAO gridDAO;  // ✅ Ajoutez cette ligne
    // Instanciez le DAO dans le constructeur ou une méthode d'initialisation
    private MatchOddsDAO matchOddsDAO; 
    private ObservableList<GridMindMatchPrediction> predictionsList = FXCollections.observableArrayList();
    private GridMindSimpleGridDAO simpleGridDAO = new GridMindSimpleGridDAOImpl();
 

    public GridMindPredictionController() {
        try {
            this.gridMindMatchDAO = new GridMindMatchDAO();  // ✅ Ajoutez cette ligne
//            this.gridMindGridPredictionDAO = new GridMindGridPredictionDAOImpl(); // ✅ Initialisation directe
            this.gridDAO = new GridMindGridDAOImpl();            // ✅ Ajoutez cette ligne
            this.predictionDAO = new GridMindMatchPredictionDAOImpl(this.gridMindMatchDAO);
            this.gridPredictionDAO = new GridMindGridPredictionDAOImpl();  // ✅ Gestion de SQLException
            System.out.println("✅ Contrôleur GridMindPredictionController instancié !");
        } catch (SQLException e) {
            e.printStackTrace();
            UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'initialiser les DAO: " + e.getMessage());
        }
    }
    
 // Setter
    public void setGridPredictionDAO(GridMindGridPredictionDAO gridPredictionDAO) {
        this.gridPredictionDAO = gridPredictionDAO;
    }
    
 // Méthode pour définir la grille de pronostics
    public void setCurrentGridPrediction(GridMindGridPrediction gridPrediction) {
        this.currentGridPrediction = gridPrediction;
    }

    // Méthode pour récupérer les prédictions de matchs
    private List<GridMindMatchPrediction> getMatchPredictionsForCurrentGrid() throws SQLException {
        if (currentGridPrediction == null) {
            throw new IllegalStateException("currentGridPrediction n'est pas initialisé !");
        }
        return predictionDAO.getPredictionsForGridPrediction(currentGridPrediction.getId());
    }
    
    public GridMindPredictionController(MatchOddsDAO matchOddsDAO) {
        this.matchOddsDAO = matchOddsDAO;
    }
    
    // Méthode pour initialiser le contrôleur avec l'ID de la grille
    public void setCurrentGridId(UUID gridId) {
        this.currentGridId = gridId;
    }
    
 // ✅ Méthode pour définir GridListPane
    public void setGridListPane(GridListPane gridListPane) {
        this.gridListPane = gridListPane;
    }
    
    public void setCurrentGridPredictionId(UUID gridPredictionId) {
        this.currentGridPredictionId = gridPredictionId;
    }

 // Dans GridMindPredictionController.java
    public void setGridMindMatchDAO(GridMindMatchDAO gridMindMatchDAO) {
        this.gridMindMatchDAO = gridMindMatchDAO;
    }

    // ✅ Setter pour matchOddsDAO
    public void setMatchOddsDAO(MatchOddsDAO matchOddsDAO) {
        this.matchOddsDAO = matchOddsDAO;
        System.out.println("🔹 [DEBUG] GridMindPredictionController setMatchOddsDAO matchOddsDAO injecté dans le contrôleur : " + 
        					(matchOddsDAO != null));  // ✅ Log de vérification
    }

    // ✅ Setter pour predictionDAO (si besoin)
    public void setPredictionDAO(GridMindMatchPredictionDAO dao) {
        this.predictionDAO = dao;
    }
    
    public void setGridDAO(GridMindGridDAO gridDAO) {
        this.gridDAO = gridDAO;
    }
    
@FXML
private void initialize() {
    // ✅ Vérifie que les DAO sont bien initialisés
    if (matchOddsDAO == null) {
        System.err.println("❌ matchOddsDAO n'est pas initialisé !");
    }

    // ✅ Configuration pour le ComboBox (existant)
    ObservableList<String> guaranteeTypes = FXCollections.observableArrayList("N", "N-1", "N-2", "N-3");
    guaranteeTypeComboBox.setItems(guaranteeTypes);

    // ✅ Log pour vérifier que initialize() est appelé
    System.out.println("🔹 [DEBUG] initialize() appelé !");

    // ✅ Menu contextuel (conservé)
    ContextMenu contextMenu = new ContextMenu();

    // Option 1: Choisir un pronostic existant
    MenuItem choosePredictionItem = new MenuItem("Choisir un pronostic existant");
    choosePredictionItem.setOnAction(event -> showPredictionList());

    // Option 2: Créer un nouveau pronostic
    MenuItem newPredictionItem = new MenuItem("Nouveau pronostic");
    newPredictionItem.setOnAction(event -> createNewPrediction());

    contextMenu.getItems().addAll(choosePredictionItem, newPredictionItem);

    // ✅ Applique le menu contextuel à la VBox (au lieu de predictionsTableView)
    matchesContainer.setOnMouseClicked(event -> {
        if (event.getButton() == MouseButton.SECONDARY) {  // Clic droit
            contextMenu.show(matchesContainer, event.getScreenX(), event.getScreenY());
        }
    });

    // ✅ Supprime tout ce qui concerne predictionsTableView et les TableColumn
    // (plus besoin de configureTableColumns, setEditable, etc.)
}
    
public void loadPredictionsFromDatabase() {
    try {
        // ✅ Récupère la connexion de session
        Connection conn = DatabaseConnection.getSessionConnection();
        System.out.println("🔹 [DEBUG] loadPredictionsFromDatabase Chargement pour predictionId: " + currentGridPredictionId);

        // 1. Charge le pronostic
        GridMindGridPrediction gridPrediction = gridPredictionDAO.getPredictionById(currentGridPredictionId);
        if (gridPrediction == null) {
            UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Pronostic introuvable !");
            return;
        }
        UUID gridId = gridPrediction.getGridMindGridId();

        // ✅ Logs de debug
        System.out.println("🔹 [DEBUG] loadPredictionsFromDatabase gridPrediction chargé: minHomeWin=" + gridPrediction.getMinHomeWin() +
                           ", maxHomeWin=" + gridPrediction.getMaxHomeWin() +
                           ", guaranteeType=" + gridPrediction.getGuaranteeType());

        // 2. Affiche le nom de la grille théorique
        GridMindGrid theoreticalGrid = gridDAO.getGridById(gridId);

        // 3. Charge les matchs de la grille théorique
        List<GridMindMatch> matches = gridMindMatchDAO.getMatchesForGrid(gridId);
        System.out.println("🔹 [DEBUG] loadPredictionsFromDatabase Nombre de matchs chargés: " + matches.size());
        matchMap.clear();
        for (GridMindMatch match : matches) {
            matchMap.put(match.getId(), match);
            System.out.println("🔹 [DEBUG] loadPredictionsFromDatabase Match chargé : " + match.getLineNumber() + " - " +
                              match.getHomeTeam() + " vs " + match.getAwayTeam());
        }

        // 4. Charge les pronostics de matchs pour currentGridPredictionId
        List<GridMindMatchPrediction> loadedPredictions = predictionDAO.getPredictionsForPrediction(currentGridPredictionId);
        System.out.println("🔹 [DEBUG] loadPredictionsFromDatabase appelle getPredictionsForPrediction Nombre de pronostics chargés : " + loadedPredictions.size());
        for (GridMindMatchPrediction p : loadedPredictions) {
            System.out.println("🔹 [DEBUG] Pronostic : " + p.getId() + ", homeWinProperty = " + p.homeWinProperty());
        }

        // 5. Si aucun pronostic de match n'existe, crée-en par défaut
        if (loadedPredictions.isEmpty() && !matches.isEmpty()) {
            for (GridMindMatch match : matches) {
                GridMindMatchPrediction prediction = new GridMindMatchPrediction();
                prediction.setId(UUID.randomUUID());
                prediction.setGridMindMatchId(match.getId());
                prediction.setGridMindGridId(gridId);
                prediction.setGridMindGridPredictionId(currentGridPredictionId);
                prediction.setHomeWin(false);
                prediction.setDraw(false);
                prediction.setAwayWin(false);
                prediction.setHomeWinOddOverride(match.getHomeWinOdd());
                prediction.setDrawOddOverride(match.getDrawOdd());
                prediction.setAwayWinOddOverride(match.getAwayWinOdd());
                loadedPredictions.add(prediction);
            }
            // Sauvegarde les pronostics de matchs par défaut
            for (GridMindMatchPrediction prediction : loadedPredictions) {
                predictionDAO.save(prediction);
            }
            // Rafraîchit la liste des pronostics de matchs
            loadedPredictions = predictionDAO.getPredictionsForPrediction(currentGridPredictionId);
        }

        // 6. Met à jour predictionsList et affiche les matchs dans la VBox
        System.out.println("🔹 [DEBUG] GridMindPredictionController.loadPredictionsFromDatabase 1.0 = " + conn + " " + conn.isClosed());
        predictionsList.clear();
        System.out.println("🔹 [DEBUG] GridMindPredictionController.loadPredictionsFromDatabase 1.1 = " + conn + " "  + conn.isClosed());
        predictionsList.addAll(loadedPredictions);
        System.out.println("🔹 [DEBUG] GridMindPredictionController.loadPredictionsFromDatabase 1.2 = " + conn + " "  + conn.isClosed());
        loadMatchesIntoVBox(predictionsList, matches);  // ✅ Appelle la nouvelle méthode
        System.out.println("🔹 [DEBUG] GridMindPredictionController.loadPredictionsFromDatabase 2.0 = " + conn + " "  + conn.isClosed());

        // 7. Affiche les fourchettes et la garantie du pronostic
        predictionNameField.setText(gridPrediction.getName());

        // Charge TOUTES les fourchettes
        minHomeWinField.setText(String.valueOf(gridPrediction.getMinHomeWin()));
        maxHomeWinField.setText(String.valueOf(gridPrediction.getMaxHomeWin()));
        minDrawField.setText(String.valueOf(gridPrediction.getMinDraw()));
        maxDrawField.setText(String.valueOf(gridPrediction.getMaxDraw()));
        minAwayWinField.setText(String.valueOf(gridPrediction.getMinAwayWin()));
        maxAwayWinField.setText(String.valueOf(gridPrediction.getMaxAwayWin()));

        // Charge les fourchettes de côtes
        minOddField.setText(String.valueOf(gridPrediction.getMinOdd()));
        maxOddField.setText(String.valueOf(gridPrediction.getMaxOdd()));

        // Charge la garantie
        guaranteeTypeComboBox.setValue(gridPrediction.getGuaranteeType());
        guaranteePercentageField.setText(String.valueOf(gridPrediction.getGuaranteePercentage()));
        
        // ✅ NOUVEAU : Met à jour le nombre de grilles générées
        System.out.println("🔹 [DEBUG] GridMindPredictionController.loadPredictionsFromDatabase avant appel à generatedGridsCount = " + conn);
        int generatedGridsCount = simpleGridDAO.getGeneratedGridsCount(gridPrediction.getId());
        System.out.println("🔹 [DEBUG] GridMindPredictionController.loadPredictionsFromDatabase après appel à generatedGridsCount = " + conn);
        System.out.println("🔹 [DEBUG] GridMindPredictionController.loadPredictionsFromDatabase : generatedGridsCount = " + generatedGridsCount);
        summarySimpleGridNumber.setText(String.valueOf(generatedGridsCount));

        // ✅ Log pour vérifier l'exécution
        System.out.println("🔹 [DEBUG] GridMindPredictionController.loadPredictionsFromDatabase Nombre de grilles générées : " + simpleGridDAO.getGeneratedGridsCount(gridPrediction.getId()));

        // 8. Met à jour la synthèse
        updateSummary();

    } catch (SQLException e) {
        System.err.println("❌ [ERREUR] Dans loadPredictionsFromDatabase: " + e.getMessage());
        e.printStackTrace();
        UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les données: " + e.getMessage());
    }
}
    
 // Affiche une liste des pronostics existants pour cette grille
    private void showPredictionList() {
        try {
            List<GridMindGridPrediction> existingPredictions = gridPredictionDAO.getAllPredictionsForGrid(currentGridId);
            if (existingPredictions.isEmpty()) {
            	UiUtils.showAlert(Alert.AlertType.INFORMATION, "Information", "Aucun pronostic existant pour cette grille.");
                return;
            }

            // Créez un ChoiceDialog avec le premier élément sélectionné par défaut
            ChoiceDialog<GridMindGridPrediction> dialog = new ChoiceDialog<>(
                existingPredictions.get(0),  // ✅ Premier élément sélectionné par défaut
                existingPredictions           // ✅ Liste complète des choix
            );

            dialog.setTitle("Choisir un pronostic");
            dialog.setHeaderText("Sélectionnez un pronostic existant:");
            dialog.setContentText("Nom du pronostic:");

            // Affichez le dialogue et récupérez la sélection
            Optional<GridMindGridPrediction> result = dialog.showAndWait();
            result.ifPresent(selectedPrediction -> {
                System.out.println("🔹 [DEBUG] dans showPredictionList appel à loadPredictionsForSelectedGridPrediction - Pronostic sélectionné : " + selectedPrediction.getId());
                loadPredictionsForSelectedGridPrediction(selectedPrediction);
            });
        } catch (SQLException e) {
        	UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les pronostics: " + e.getMessage());
        }
    }

    // Crée un nouveau pronostic
    private void createNewPrediction() {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nouveau pronostic");
        dialog.setHeaderText("Créez un nouveau pronostic pour cette grille:");
        dialog.setContentText("Nom du pronostic:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            try {
                Connection conn = null;
                // Créez un nouveau GridMindGridPrediction
                GridMindGridPrediction newPrediction = new GridMindGridPrediction();
                newPrediction.setId(UUID.randomUUID());
                newPrediction.setGridMindGridId(currentGridId);
                newPrediction.setName(name);
                // Valeurs par défaut pour les fourchettes
                newPrediction.setMinOdd(1.0);
                newPrediction.setMaxOdd(100.0);
                newPrediction.setGuaranteeType("N");
                newPrediction.setGuaranteePercentage(80.0);

                // Sauvegardez le nouveau pronostic global
                conn = DatabaseConnection.getSessionConnection(); // ✅ Utilise la connexion avec auto-commit = false
                System.out.println("🔹 [DEBUG] GridMindPredictionController.createNewPrediction 1 conn: " + conn + " " + conn.isClosed());

                
                gridPredictionDAO.save(newPrediction);

                // Chargez les pronostics de matchs pour ce nouveau pronostic
                System.out.println("🔹 [DEBUG] dans createNewPrediction appel à loadPredictionsForSelectedGridPrediction - Pronostic sélectionné : " + newPrediction.getId());
                loadPredictionsForSelectedGridPrediction(newPrediction);
            } catch (SQLException e) {
            	UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de créer le pronostic: " + e.getMessage());
            }
        });
    }

// Charge les pronostics de matchs pour un GridMindGridPrediction sélectionné
private void loadPredictionsForSelectedGridPrediction(GridMindGridPrediction gridPrediction) {
    System.out.println("🔹 [DEBUG] loadPredictionsForSelectedGridPrediction appelé pour : " + gridPrediction.getId());
    try {
        // 1. Charge les matchs de la grille
        List<GridMindMatch> matches = gridMindMatchDAO.getMatchesForGrid(currentGridId);
        matchMap.clear();
        for (GridMindMatch match : matches) {
            matchMap.put(match.getId(), match);
        }

        // 2. Charge les pronostics de matchs pour ce pronostic global
        List<GridMindMatchPrediction> predictions = predictionDAO.getPredictionsForGridPrediction(gridPrediction.getId());
        System.out.println("🔹 [DEBUG] loadPredictionsForSelectedGridPrediction appelle getPredictionsForPrediction Nombre de pronostics chargés : " + predictions.size());


        // 3. Si aucun pronostic de match n'existe, crée-en par défaut
        if (predictions.isEmpty() && !matches.isEmpty()) {
            for (GridMindMatch match : matches) {
                GridMindMatchPrediction prediction = new GridMindMatchPrediction();
                prediction.setId(UUID.randomUUID());
                prediction.setGridMindMatchId(match.getId());
                prediction.setGridMindGridId(currentGridId);
                prediction.setGridMindGridPredictionId(gridPrediction.getId());
                prediction.setHomeWin(false);
                prediction.setDraw(false);
                prediction.setAwayWin(false);
                prediction.setHomeWinOddOverride(match.getHomeWinOdd());
                prediction.setDrawOddOverride(match.getDrawOdd());
                prediction.setAwayWinOddOverride(match.getAwayWinOdd());
                predictions.add(prediction);
            }
        }

        // 4. Met à jour predictionsList et affiche les matchs dans la VBox
        predictionsList.clear();
        predictionsList.addAll(predictions);
        loadMatchesIntoVBox(predictionsList, matches);  // ✅ Utilise la nouvelle méthode

        // 5. Affiche les fourchettes et la garantie
        minOddField.setText(String.valueOf(gridPrediction.getMinOdd()));
        maxOddField.setText(String.valueOf(gridPrediction.getMaxOdd()));
        guaranteeTypeComboBox.setValue(gridPrediction.getGuaranteeType());
        guaranteePercentageField.setText(String.valueOf(gridPrediction.getGuaranteePercentage()));
        
        // ✅ NOUVEAU : Met à jour le nombre de grilles générées
        int generatedGridsCount = simpleGridDAO.getGeneratedGridsCount(gridPrediction.getId());
        System.out.println("🔹 [DEBUG] GridMindPredictionController.loadPredictionsForSelectedGridPrediction : generatedGridsCount = " + generatedGridsCount);
        summarySimpleGridNumber.setText(String.valueOf(generatedGridsCount));

        // ✅ Log pour vérifier l'exécution
        System.out.println("🔹 [DEBUG] Nombre de grilles générées : " + simpleGridDAO.getGeneratedGridsCount(gridPrediction.getId()));
        
    	} catch (SQLException e) {
        System.err.println("❌ [DEBUG] Erreur dans loadPredictionsForSelectedGridPrediction : " + e.getMessage());
        UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les pronostics: " + e.getMessage());
    	} finally {
            System.out.println("🔹 [DEBUG] loadPredictionsForSelectedGridPrediction terminé");
        }
    
    }
    
    /**
     * Configure les colonnes du TableView pour les pronostics de matchs.
     * Utilise des cellules éditables (CheckBox pour V/N/D, TextField pour les côtes).
     */
private void loadMatchesIntoVBox(List<GridMindMatchPrediction> predictions, List<GridMindMatch> gridMindMatches) {
    matchesContainer.getChildren().clear();  // Vide le conteneur

    // ✅ Crée une map pour associer match_id aux scores
    Map<UUID, String> matchScores = new HashMap<>();
    Connection conn = DatabaseConnection.getSessionConnection(); // ✅ Pas de try-with-resources

    try {
        if (!gridMindMatches.isEmpty()) {
            String sql = "SELECT id, home_score, away_score FROM match WHERE id = ANY(?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            // Utilise un Array pour passer les matchIds
            List<UUID> matchIds = gridMindMatches.stream()
                .map(GridMindMatch::getMatchId)
                .collect(Collectors.toList());
            Array matchIdArray = conn.createArrayOf("uuid", matchIds.toArray());
            stmt.setArray(1, matchIdArray);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                UUID matchId = (UUID) rs.getObject("id");
                Integer homeScore = (Integer) rs.getObject("home_score");
                Integer awayScore = (Integer) rs.getObject("away_score");
                String score = (homeScore != null && awayScore != null) ?
                    homeScore + "-" + awayScore : "";
                matchScores.put(matchId, score);
            }

            // ✅ Fermer uniquement le PreparedStatement et le ResultSet
            rs.close();
            stmt.close();
        }
    } catch (SQLException e) {
        e.printStackTrace();
        System.err.println("❌ Erreur lors du chargement des scores: " + e.getMessage());
    }

    // ✅ Crée une map pour associer les GridMindMatch aux prédictions
    Map<UUID, GridMindMatch> gridMindMatchMap = new HashMap<>();
    for (GridMindMatch gridMindMatch : gridMindMatches) {
        gridMindMatchMap.put(gridMindMatch.getId(), gridMindMatch);
    }

    // ✅ Parcourt les prédictions et crée les HBox
    for (GridMindMatchPrediction prediction : predictions) {
        GridMindMatch gridMindMatch = gridMindMatchMap.get(prediction.getGridMindMatchId());
        if (gridMindMatch == null) continue;

        String homeTeam = gridMindMatch.getHomeTeam();
        String awayTeam = gridMindMatch.getAwayTeam();
        int lineNumber = gridMindMatch.getLineNumber();

        // ---- N° de ligne ----
        Label lineNumberLabel = new Label(String.valueOf(lineNumber));
        lineNumberLabel.setPrefWidth(30);
        lineNumberLabel.setAlignment(Pos.CENTER);

        // ---- Équipes ----
        Label homeTeamLabel = new Label(homeTeam);
        homeTeamLabel.setPrefWidth(120);

        // ✅ Label pour le score
        Label scoreLabel = new Label();
        scoreLabel.setPrefWidth(50);
        scoreLabel.setAlignment(Pos.CENTER);
        String score = matchScores.get(gridMindMatch.getMatchId());
        scoreLabel.setText(score != null ? score : "");

        Label awayTeamLabel = new Label(awayTeam);
        awayTeamLabel.setPrefWidth(120);

        // ---- CheckBox pour V/N/D ----
        CheckBox homeWinCheckBox = new CheckBox();
        homeWinCheckBox.setSelected(prediction.isHomeWin());
        homeWinCheckBox.setOnAction(e -> {
            prediction.setHomeWin(homeWinCheckBox.isSelected());
            updateSummary();
        });

        CheckBox drawCheckBox = new CheckBox();
        drawCheckBox.setSelected(prediction.isDraw());
        drawCheckBox.setOnAction(e -> {
            prediction.setDraw(drawCheckBox.isSelected());
            updateSummary();
        });

        CheckBox awayWinCheckBox = new CheckBox();
        awayWinCheckBox.setSelected(prediction.isAwayWin());
        awayWinCheckBox.setOnAction(e -> {
            prediction.setAwayWin(awayWinCheckBox.isSelected());
            updateSummary();
        });

        // ---- TextField pour les côtes ----
        TextField homeOddField = new TextField();
        homeOddField.setPrefWidth(40);
        homeOddField.setAlignment(Pos.CENTER_RIGHT);
        Double homeOddOverride = prediction.getHomeWinOddOverride();
        Double defaultHomeOdd = gridMindMatch != null ? gridMindMatch.getHomeWinOdd() : 1.0;
        homeOddField.setText(String.valueOf(homeOddOverride != null ? homeOddOverride : defaultHomeOdd));
        homeOddField.setOnAction(e -> {
            try {
                prediction.setHomeWinOddOverride(Double.parseDouble(homeOddField.getText()));
                updateSummary();
            } catch (NumberFormatException ex) {
                UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Valeur de côte invalide: " + homeOddField.getText());
            }
        });

        TextField drawOddField = new TextField();
        drawOddField.setPrefWidth(40);
        drawOddField.setAlignment(Pos.CENTER_RIGHT);
        Double drawOddOverride = prediction.getDrawOddOverride();
        Double defaultDrawOdd = gridMindMatch != null ? gridMindMatch.getDrawOdd() : 1.0;
        drawOddField.setText(String.valueOf(drawOddOverride != null ? drawOddOverride : defaultDrawOdd));
        drawOddField.setOnAction(e -> {
            try {
                prediction.setDrawOddOverride(Double.parseDouble(drawOddField.getText()));
                updateSummary();
            } catch (NumberFormatException ex) {
                UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Valeur de côte invalide: " + drawOddField.getText());
            }
        });

        TextField awayOddField = new TextField();
        awayOddField.setPrefWidth(40);
        awayOddField.setAlignment(Pos.CENTER_RIGHT);
        Double awayOddOverride = prediction.getAwayWinOddOverride();
        Double defaultAwayOdd = gridMindMatch != null ? gridMindMatch.getAwayWinOdd() : 1.0;
        awayOddField.setText(String.valueOf(awayOddOverride != null ? awayOddOverride : defaultAwayOdd));
        awayOddField.setOnAction(e -> {
            try {
                prediction.setAwayWinOddOverride(Double.parseDouble(awayOddField.getText()));
                updateSummary();
            } catch (NumberFormatException ex) {
                UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Valeur de côte invalide: " + awayOddField.getText());
            }
        });

        // ---- Construction de la HBox pour ce match ----
        HBox matchBox = new HBox(10,
            lineNumberLabel,
            homeTeamLabel,
            scoreLabel, // ✅ Score ajouté ici
            awayTeamLabel,
            homeWinCheckBox,
            drawCheckBox,
            awayWinCheckBox,
            homeOddField,
            drawOddField,
            awayOddField
        );
        matchBox.setAlignment(Pos.CENTER_LEFT);
        matchBox.setStyle("-fx-padding: 5px");

        // ✅ Ajoute la HBox au conteneur
        matchesContainer.getChildren().add(matchBox);
    }
}

@FXML
private void handleViewGrids() {
    System.out.println("🔹 [DEBUG] Bouton 'Voir les grilles' cliqué");
try	{
	Connection conn = DatabaseConnection.getSessionConnection();
    System.out.println("🔹 [DEBUG] GridMindPredictionController.handleViewGrids 1.0 = " + conn + " " + conn.isClosed());
	} catch (SQLException e) {
        e.printStackTrace();
        UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "handleViewGrids Impossible de charger les grilles: " + e.getMessage());
    }
    // ✅ Ouvre une nouvelle fenêtre pour afficher les grilles
    openGridsWindow();
}

private void openGridsWindow() {
    try {
        // 1. Charge les grilles depuis la base
        List<GridMindSimpleGrid> grids = simpleGridDAO.getAllGeneratedGrids(currentGridPredictionId);

        if (grids.isEmpty()) {
            UiUtils.showAlert(Alert.AlertType.INFORMATION, "Information", "Aucune grille générée pour ce pronostic.");
            return;
        }
        Connection conn = DatabaseConnection.getSessionConnection();
        System.out.println("🔹 [DEBUG] GridMindPredictionController.openGridsWindow 1.0 = " + conn + " " + conn.isClosed());

        // 2. Récupère l'objet GridMindGridPrediction (assure-toi qu'il est bien chargé)
        GridMindGridPrediction gridPrediction = gridPredictionDAO.getGridPredictionById(currentGridPredictionId);
        if (gridPrediction == null) {
            System.err.println("[DEBUG] Erreur : gridPrediction est null pour l'ID " + currentGridPredictionId);
            return;
        }        
        // 2. Crée une nouvelle fenêtre pour afficher les grilles
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/grids_view.fxml"));
        Parent root = loader.load();

        // 3. Passe les grilles au contrôleur de la nouvelle fenêtre
        GridsViewController gridsViewController = loader.getController();
        GridMindMatchPredictionDAO matchPredictionDAO = new GridMindMatchPredictionDAOImpl(gridMindMatchDAO); // ✅ Injection
        gridsViewController.setMatchPredictionDAO(matchPredictionDAO);
        // Passe l'ID de la grille de pronostics
        gridsViewController.setCurrentGridPrediction(gridPrediction);
        gridsViewController.setMatchPredictionDAO(this.predictionDAO);
      //  this.predictionDAO = new GridMindMatchPredictionDAOImpl(this.gridMindMatchDAO);
     // Injecte le DAO si nécessaire
        gridsViewController.setGridPredictionDAO(this.gridPredictionDAO);
        gridsViewController.setGridMindMatchDAO(gridMindMatchDAO); // ✅ Injecte l'instance
        gridsViewController.setGrids(grids);

        // 4. Affiche la fenêtre
        Stage stage = new Stage();
        stage.setTitle("GridMind - Grilles générées");
        stage.setScene(new Scene(root));
        stage.show();

    } catch (IOException | SQLException e) {
        e.printStackTrace();
        UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les grilles: " + e.getMessage());
    }
}

    @FXML
    private void handleClose() {
        Stage stage = (Stage) matchesContainer.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void handleCancel() {
        // Logique pour annuler (ex: recharger les données ou fermer la fenêtre)
    }
    
    @FXML
    private void savePredictions() throws Exception {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getSessionConnection(); // ✅ Utilise la connexion avec auto-commit = false
            System.out.println("🔹 [DEBUG] GridMindPredictionController.savePredictions 1 conn: " + conn + " " + conn.isClosed());

            // 1. Sauvegarde le pronostic de grille
            saveGridPrediction(conn); // ✅ Passe la connexion aux méthodes
            System.out.println("🔹 [DEBUG] GridMindPredictionController.savePredictions 2 conn: " + conn + " " + conn.isClosed());

            // 2. Sauvegarde les pronostics de matchs
            saveMatchPredictions(conn); // ✅ Passe la connexion aux méthodes
            System.out.println("🔹 [DEBUG] GridMindPredictionController.savePredictions 3 conn: " + conn);

            UiUtils.showAlert(Alert.AlertType.INFORMATION, "Succès", "Grille et pronostics sauvegardés avec succès !");
            
            // ✅ Rafraîchir l'arbre des grilles
            if (gridListPane != null) {
                gridListPane.loadGrids();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur",
                "GridMindPredictionController.savePredictions Impossible d'enregistrer les modifications: " + e.getMessage());
        }
    }
    
    /**
     * Sauvegarde le pronostic de grille (nom, fourchettes, garantie).
     */
    private void saveGridPrediction(Connection conn) throws SQLException {

        // ✅ Affichez les valeurs pour déboguer
    	System.out.println("Valeurs des champs :");
    	System.out.println("minHomeWinField: " + minHomeWinField.getText());
        System.out.println("maxHomeWinField: " + maxHomeWinField.getText());
        System.out.println("minDrawField: " + minDrawField.getText());
        System.out.println("maxDrawField: " + maxDrawField.getText());
        System.out.println("minAwayWinField: " + minAwayWinField.getText());
        System.out.println("maxAwayWinField: " + maxAwayWinField.getText());
        GridMindGridPrediction gridPrediction = gridPredictionDAO.getPredictionById(currentGridPredictionId);

        if (gridPrediction == null) {
            // ✅ Si le pronostic n'existe pas (cas rare), créez-en un nouveau
            gridPrediction = new GridMindGridPrediction();
            gridPrediction.setId(UUID.randomUUID());
            gridPrediction.setGridMindGridId(currentGridId);
        }
        // ✅ Utilisez predictionNameField pour le nom du pronostic (pas gridNameField)
        gridPrediction.setName(predictionNameField.getText().isEmpty() ? "Grille sans nom" : predictionNameField.getText());
        
        // ✅ Fourchettes de pronostics (NOUVEAU)
        // ✅ Parsez les valeurs (avec gestion des champs vides)
        gridPrediction.setMinHomeWin(minHomeWinField.getText().isEmpty() ? 0 : Integer.parseInt(minHomeWinField.getText()));
        gridPrediction.setMaxHomeWin(maxHomeWinField.getText().isEmpty() ? 0 : Integer.parseInt(maxHomeWinField.getText()));
        gridPrediction.setMinDraw(minDrawField.getText().isEmpty() ? 0 : Integer.parseInt(minDrawField.getText()));
        gridPrediction.setMaxDraw(maxDrawField.getText().isEmpty() ? 0 : Integer.parseInt(maxDrawField.getText()));
        gridPrediction.setMinAwayWin(minAwayWinField.getText().isEmpty() ? 0 : Integer.parseInt(minAwayWinField.getText()));
        gridPrediction.setMaxAwayWin(maxAwayWinField.getText().isEmpty() ? 0 : Integer.parseInt(maxAwayWinField.getText()));
        
        // ✅ Affichez les valeurs après parsing
        System.out.println("Valeurs après parsing :");
        System.out.println("minHomeWin: " + gridPrediction.getMinHomeWin());
        System.out.println("maxHomeWin: " + gridPrediction.getMaxHomeWin());
        System.out.println("minDraw: " + gridPrediction.getMinDraw());
        System.out.println("maxDraw: " + gridPrediction.getMaxDraw());
        System.out.println("minAwayWin: " + gridPrediction.getMinAwayWin());
        System.out.println("maxAwayWin: " + gridPrediction.getMaxAwayWin());
        
     // ✅ Sauvegardez le nom
        gridPrediction.setName(predictionNameField.getText());  

        // ✅ Fourchettes de côtes
        gridPrediction.setMinOdd(Double.parseDouble(minOddField.getText()));
        gridPrediction.setMaxOdd(Double.parseDouble(maxOddField.getText()));
        
     // ✅ Garantie
        gridPrediction.setGuaranteeType(guaranteeTypeComboBox.getValue());
        gridPrediction.setGuaranteePercentage(Double.parseDouble(guaranteePercentageField.getText()));

        if (gridPredictionDAO.getPredictionForGrid(currentGridId) == null) {
            gridPredictionDAO.save(gridPrediction);
        } else {
            gridPredictionDAO.update(gridPrediction);
        }
        this.currentGridPredictionId = gridPrediction.getId();
    }

    /**
     * Sauvegarde tous les pronostics de matchs.
     */
    private void saveMatchPredictions(Connection conn) throws SQLException {
        // ✅ Récupère les pronostics depuis predictionsList (au lieu de matchesContainer.getItems())
        List<GridMindMatchPrediction> predictions = predictionsList;  // ✅ Utilise predictionsList

        for (GridMindMatchPrediction prediction : predictions) {
            prediction.setGridMindGridPredictionId(currentGridPredictionId);
            prediction.setGridMindGridId(currentGridId);
            if (prediction.getId() == null) {
                prediction.setId(UUID.randomUUID());
                predictionDAO.save(prediction, conn);
            } else {
                predictionDAO.update(prediction, conn);
            }
        }
    }
    
    /**
     * Calcule la synthèse de la grille de pronostics.
     * @param predictions Liste des pronostics (GridMindMatchPrediction).
     * @return GridMindGridPredictionSummary avec les résultats.
     */
public GridMindGridPredictionSummary calculateGridMindGridPredictionSummary(List<GridMindMatchPrediction> predictions) {
    int simples = 0;
    int doubles = 0;
    int triples = 0;
    double minOddsSum = 0.0;
    double maxOddsSum = 0.0;

    for (GridMindMatchPrediction prediction : predictions) {
        int checkedCount = 0;
        if (prediction.isHomeWin()) checkedCount++;
        if (prediction.isDraw()) checkedCount++;
        if (prediction.isAwayWin()) checkedCount++;

        if (checkedCount == 1) simples++;
        else if (checkedCount == 2) doubles++;
        else if (checkedCount == 3) triples++;

        GridMindMatch match = matchMap.get(prediction.getGridMindMatchId());
        if (match == null) continue;

        // ✅ Liste des côtes pour les résultats cochés
        List<Double> checkedOdds = new ArrayList<>();

        if (prediction.isHomeWin()) {
            Double homeOdd = prediction.getHomeWinOddOverride() != null ?
                prediction.getHomeWinOddOverride() : match.getHomeWinOdd();
            checkedOdds.add(homeOdd != null ? homeOdd : 1.0);
        }
        if (prediction.isDraw()) {
            Double drawOdd = prediction.getDrawOddOverride() != null ?
                prediction.getDrawOddOverride() : match.getDrawOdd();
            checkedOdds.add(drawOdd != null ? drawOdd : 1.0);
        }
        if (prediction.isAwayWin()) {
            Double awayOdd = prediction.getAwayWinOddOverride() != null ?
                prediction.getAwayWinOddOverride() : match.getAwayWinOdd();
            checkedOdds.add(awayOdd != null ? awayOdd : 1.0);
        }

        // ✅ Trouve la côte minimale et maximale parmi les résultats cochés
        if (!checkedOdds.isEmpty()) {
            double minOdd = Collections.min(checkedOdds);
            double maxOdd = Collections.max(checkedOdds);
            minOddsSum += minOdd;
            maxOddsSum += maxOdd;
        }
    }

    long combinations = (long) (Math.pow(3, triples) * Math.pow(2, doubles));
    return new GridMindGridPredictionSummary(simples, doubles, triples, combinations, minOddsSum, maxOddsSum);
}

//Méthode pour mettre à jour la synthèse
private void updateSummary() {
 GridMindGridPredictionSummary summary = calculateGridMindGridPredictionSummary(predictionsList);
 simplesLabel.setText(String.valueOf(summary.getSimples()));
 doublesLabel.setText(String.valueOf(summary.getDoubles()));
 triplesLabel.setText(String.valueOf(summary.getTriples()));
 combinationsLabel.setText(String.valueOf(summary.getCombinations()));
 minOddsLabel.setText(String.format("%.2f", summary.getMinOddsSum()));
 maxOddsLabel.setText(String.format("%.2f", summary.getMaxOddsSum()));
}
    
@FXML
private void handleGenerateGrids(ActionEvent event) {
    try {

        // ✅ Supprime les grilles existantes pour cette prédiction
        simpleGridDAO.deleteGridsByPredictionId(currentGridPredictionId);
        
        // 1. Récupère les fourchettes depuis l'UI
        int minHomeWin = Integer.parseInt(minHomeWinField.getText());
        int maxHomeWin = Integer.parseInt(maxHomeWinField.getText());
        int minDraw = Integer.parseInt(minDrawField.getText());
        int maxDraw = Integer.parseInt(maxDrawField.getText());
        int minAwayWin = Integer.parseInt(minAwayWinField.getText());
        int maxAwayWin = Integer.parseInt(maxAwayWinField.getText());
        double minTotalOdds = Double.parseDouble(minOddField.getText());
        double maxTotalOdds = Double.parseDouble(maxOddField.getText());

        // 2. Récupère les pronostics
        GridMindGridPrediction gridPrediction = getGridPrediction(currentGridPredictionId);
        if (gridPrediction == null) {
            return; // L'erreur a déjà été affichée
        }
        UUID gridmindGridPredictionId = gridPrediction.getId(); // ✅ ID du pronostic
        
        // UUID gridmindGridPredictionId = getCurrentGridPredictionId();
        List<GridMindMatchPrediction> predictions = predictionsList;
        //List<GridMindMatchPrediction> predictions = getPredictionsForGrid(gridmindGridPredictionId);

        // 3. Génère les grilles
        GridMindSimpleGridGenerator generator = new GridMindSimpleGridGenerator();
        List<GridMindSimpleGrid> validSimpleGrids = generator.generateValidSimpleGrids(
            predictions,
            minHomeWin, maxHomeWin,
            minDraw, maxDraw,
            minAwayWin, maxAwayWin,
            minTotalOdds, maxTotalOdds,
            gridmindGridPredictionId
        );

        // 4. Sauvegarde les grilles (optionnel)
        GridMindSimpleGridBatchInserter batchInserter = new GridMindSimpleGridBatchInserter();
        batchInserter.saveSimpleGridsBatch(validSimpleGrids);

        // 5. Met à jour la synthèse
        updateSummarySimpleGridNumber(validSimpleGrids.size());

    } catch (NumberFormatException e) {
        UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Vérifiez les valeurs des fourchettes (doivent être des nombres).");
    } catch (SQLException e) {
        UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur lors de la sauvegarde des grilles : " + e.getMessage());
        e.printStackTrace(); // Affiche la stack trace dans la console pour le débogage
    } catch (Exception e) {
        UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur inattendue : " + e.getMessage());
        e.printStackTrace();
    }
}

	private void updateSummarySimpleGridNumber(int generatedGridsCount) {
		//String currentText = summarySimpleGridNumber.getText();
		summarySimpleGridNumber.setText(String.valueOf(generatedGridsCount));
	}
	
	private GridMindGridPrediction getGridPrediction(UUID currentGridPredictionId) {
	    try {
	        GridMindGridPrediction gridPrediction = gridPredictionDAO.getPredictionById(currentGridPredictionId);
	        if (gridPrediction == null) {
	            UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Pronostic introuvable !");
	            return null;
	        }
	        return gridPrediction;
	    } catch (SQLException e) {
	        UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la récupération du pronostic : " + e.getMessage());
	        return null;
	    }
	}

}
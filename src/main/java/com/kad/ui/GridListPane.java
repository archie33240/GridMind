package com.kad.ui;

import com.kad.controller.GridMindPredictionController;
import com.kad.dao.GridMindGridDAO;
import com.kad.dao.GridMindGridPredictionDAO;
import com.kad.dao.GridMindMatchDAO;
import com.kad.dao.GridMindMatchPredictionDAO;
import com.kad.dao.MatchOddsDAO;
import com.kad.dao.impl.GridMindGridDAOImpl;
import com.kad.dao.impl.GridMindGridPredictionDAOImpl;
import com.kad.dao.impl.GridMindMatchPredictionDAOImpl;
import com.kad.database.DatabaseConnection;
import com.kad.model.GridMindGrid;
import com.kad.model.GridMindGridPrediction;
import com.kad.model.GridMindMatch;
import com.kad.model.GridMindMatchPrediction;
import com.kad.model.GridType;
import com.kad.model.Match;
import com.kad.tools.DoubleStringConverter;
import com.kad.tools.UiUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javafx.util.StringConverter; // <-- Import nécessaire
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

//public class GridListPane extends VBox {
public class GridListPane extends BorderPane {
	private final TableView<GridMindGrid> gridTableView = new TableView<>();
	private TableView<GridMindMatch> matchTableView = new TableView<>();
	private Label gridNameLabel = new Label();
	private Label gridNumberLabel = new Label();
	private Label gridDeadlineLabel = new Label();
	private final Button refreshButton = new Button("Rafraîchir");
	private final Button createGridButton = new Button("Nouvelle Grille"); // Nouveau bouton
	private Button addMatchButton = new Button("Ajouter un match");
	private Button removeMatchButton = new Button("Retirer le match");
	private Button moveUpButton = new Button("↑");
	private Button moveDownButton = new Button("↓");
	// ✅ Champ de classe
	private GridMindGrid selectedGrid;
	// ✅ Remplacez TableView par TreeTableView
	private TreeTableView<GridMindItem> gridTreeTableView = new TreeTableView<>();
	// ✅ Dans GridListPane.java (champs de classe)
	private GridMindMatchDAO gridMindMatchDAO;
	private GridMindGridDAO gridDAO;
	private GridMindGridPredictionDAO gridPredictionDAO;
	private GridMindMatchPredictionDAO matchPredictionDAO;

	// Dans GridListPane.java
	public GridMindGrid getSelectedGrid() {
		System.out.println("DEBUG: getSelectedGrid() -> " + (selectedGrid != null ? selectedGrid.getName() : "null"));
		return selectedGrid;
	}

	// ✅ Classe pour représenter un item (grille théorique ou pronostic)
	public static class GridMindItem {
		private final String name;
		private final UUID id;
		private final boolean isTheoretical; // true = grille théorique, false = pronostic

		public GridMindItem(String name, UUID id, boolean isTheoretical) {
			this.name = name;
			this.id = id;
			this.isTheoretical = isTheoretical;
			System.out.println(
					"DEBUG: GridMindItem créé - Nom: " + name + ", ID: " + id + ", Théorique: " + isTheoretical); // ✅
																													// Debug
		}

		// Getters
		public String getName() {
			return name;
		}

		public UUID getId() {
			return id;
		}

		public boolean isTheoretical() {
			return isTheoretical;
		}
	}

	// ✅ Méthode pour configurer le TreeTableView
	private void setupGridTreeTableView() {
		// 1️⃣ Configuration des colonnes
		TreeTableColumn<GridMindItem, String> nameCol = new TreeTableColumn<>("Nom");
		nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue().getName()));
		nameCol.setPrefWidth(200); // Largeur minimale de 200 pixels
		gridTreeTableView.getColumns().add(nameCol);

		// 2️⃣ Chargement de la hiérarchie (grilles + pronostics)
		loadGridsHierarchy();

		// 4️⃣ Menu contextuel (clic droit)
		ContextMenu contextMenu = new ContextMenu();

		// ✅ Crée les MenuItem une seule fois (en dehors du listener)
		MenuItem openMenuItem = new MenuItem("Ouvrir");
		MenuItem newPredictionMenuItem = new MenuItem("Nouveau pronostic");
		MenuItem deleteMenuItem = new MenuItem("Supprimer");

		// ✅ Met à jour selectedGrid ET le menu contextuel en fonction de la sélection
		gridTreeTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			contextMenu.getItems().clear(); // Efface les anciens items

			if (newValue != null) {
				GridMindItem selectedItem = newValue.getValue();
				System.out.println(
						"DEBUG: Sélection changée - Nom: " + selectedItem.getName() + ", ID: " + selectedItem.getId());

				// ✅ 1. Met à jour selectedGrid si c'est une grille théorique
				if (selectedItem.isTheoretical() && !selectedItem.getName().equals("Grilles")) {
				    selectedGrid = new GridMindGrid();
				    selectedGrid.setId(selectedItem.getId());
				    selectedGrid.setName(selectedItem.getName());
				    System.out.println("DEBUG: selectedGrid mis à jour: " + selectedGrid.getName());

				    // ✅ Charge les matchs pour la grille sélectionnée
				    try {
				        loadMatchesForGrid(selectedGrid); // ✅ Appel avec try-catch
				    } catch (SQLException e) {
				        e.printStackTrace();
				        UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les matchs: " + e.getMessage());
				    }
				} else {
					selectedGrid = null; // ✅ Réinitialise si ce n'est pas une grille théorique
				}

				// ✅ 2. Met à jour le menu contextuel
				if (selectedItem.getName().equals("Grilles")) {
					// ✅ Racine "Grilles" → Aucune option (ou ajoute "Nouvelle grille" ici)
					MenuItem newGridMenuItem = new MenuItem("Nouvelle grille");
					newGridMenuItem.setOnAction(event -> showCreateGridDialog());
					contextMenu.getItems().add(newGridMenuItem);
				} else if (selectedItem.isTheoretical()) {
					// ✅ Grille théorique → "Nouveau pronostic" + "Supprimer"
					contextMenu.getItems().addAll(newPredictionMenuItem, deleteMenuItem);
				} else {
					// ✅ Pronostic → "Ouvrir"
					contextMenu.getItems().add(openMenuItem);
				}
			}
		});

		// ✅ Attache le menu contextuel au TreeTableView
		gridTreeTableView.setContextMenu(contextMenu);

// 3️⃣ Double-clic : Ouvre, crée une grille ou un pronostic
		gridTreeTableView.setRowFactory(tv -> {
			TreeTableRow<GridMindItem> row = new TreeTableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && !row.isEmpty()) {
					GridMindItem item = row.getItem();
					System.out.println("DEBUG: Double-clic sur - Nom: " + item.getName() + ", ID: " + item.getId());

					if (item.getName().equals("Grilles")) {
						System.out.println(
								"DEBUG: Double-clic sur la racine 'Grilles' -> Création d'une nouvelle grille.");
						showCreateGridDialog();
					} else if (item.isTheoretical()) {
						System.out.println(
								"DEBUG: Double-clic sur une grille théorique -> Création d'un nouveau pronostic.");
						createNewPrediction(selectedGrid.getId()); // ✅ Utilise selectedGrid.getId()
					} else {
						System.out.println("DEBUG: Double-clic sur un pronostic -> Ouverture.");
						openPredictionWindow(item.getId());
					}
				}
			});
			return row;
		});

		// ✅ Configure les actions une seule fois
		openMenuItem.setOnAction(event -> {
			TreeItem<GridMindItem> selectedTreeItem = gridTreeTableView.getSelectionModel().getSelectedItem();
			if (selectedTreeItem != null) {
				GridMindItem selectedItem = selectedTreeItem.getValue();
				if (!selectedItem.isTheoretical()) {
					openPredictionWindow(selectedItem.getId());
				}
			}
		});

		newPredictionMenuItem.setOnAction(event -> {
			TreeItem<GridMindItem> selectedTreeItem = gridTreeTableView.getSelectionModel().getSelectedItem();
			if (selectedTreeItem != null) {
				GridMindItem selectedItem = selectedTreeItem.getValue();
				if (selectedItem.isTheoretical() && !selectedItem.getName().equals("Grilles")) {
					createNewPrediction(selectedItem.getId());
				}
			}
		});

		deleteMenuItem.setOnAction(event -> {
			TreeItem<GridMindItem> selectedTreeItem = gridTreeTableView.getSelectionModel().getSelectedItem();
			if (selectedTreeItem != null) {
				GridMindItem selectedItem = selectedTreeItem.getValue();
				if (selectedItem.isTheoretical() && !selectedItem.getName().equals("Grilles")) {
					deleteSelectedGrid(); // ✅ Appelle la suppression
				} else if (!selectedItem.isTheoretical()) {
		            deleteSelectedPrediction(); // ✅ Suppression d'un pronostic (sans commit)
		        }
			}
		});

		// ✅ Met à jour le menu contextuel en fonction de la sélection
		gridTreeTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			contextMenu.getItems().clear(); // Efface les anciens items

			if (newValue != null) {
				GridMindItem selectedItem = newValue.getValue();

				// ✅ Ajoute les options en fonction du type de l'élément sélectionné
				if (selectedItem.getName().equals("Grilles")) {
					// ✅ Racine "Grilles" → Aucune option
					// (Optionnel : ajouter "Nouvelle grille" ici)
				} else if (selectedItem.isTheoretical()) {
					// ✅ Grille théorique → "Nouveau pronostic" + "Supprimer"
					contextMenu.getItems().addAll(newPredictionMenuItem, deleteMenuItem);
				} else {
					// ✅ Pronostic → "Ouvrir" + "Supprimer"
					contextMenu.getItems().addAll(openMenuItem, deleteMenuItem);
				}
			}
		});

		// ✅ Attache le menu contextuel au TreeTableView
		gridTreeTableView.setContextMenu(contextMenu);
	}

	// ✅ Chargez la hiérarchie (grilles théoriques + pronostics)
	private void loadGridsHierarchy() {
		try {
			// ✅ 1. Chargez les grilles théoriques
			System.out.println("DEBUG: Chargement des grilles hiérarchiques..."); // ✅ Debug
			List<GridMindGrid> theoreticalGrids = gridDAO.getAllTheoreticalGrids();
			System.out.println("DEBUG: Nombre de grilles théoriques chargées: " + theoreticalGrids.size()); // ✅ Debug

			// ✅ 2. Créez la racine du TreeTableView
			TreeItem<GridMindItem> root = new TreeItem<>(new GridMindItem("Grilles", UUID.randomUUID(), true));
			gridTreeTableView.setRoot(root);

			// ✅ 3. Pour chaque grille théorique, ajoutez ses pronostics
			for (GridMindGrid theoreticalGrid : theoreticalGrids) {
				System.out.println("DEBUG: Grille théorique: " + theoreticalGrid.getName() + " (ID: "
						+ theoreticalGrid.getId() + ")"); // ✅ Debug
				TreeItem<GridMindItem> theoreticalItem = new TreeItem<>(
						new GridMindItem(theoreticalGrid.getName(), theoreticalGrid.getId(), true) // ✅ false =
																									// pronostic
				);
				// ✅ 4. Chargez les pronostics pour cette grille
				List<GridMindGridPrediction> predictions = gridPredictionDAO
						.getPredictionsForGrid(theoreticalGrid.getId());
				System.out.println("DEBUG: Nombre de pronostics pour cette grille: " + predictions.size()); // ✅ Debug
				for (GridMindGridPrediction prediction : predictions) {
					System.out.println(
							"DEBUG: Pronostic trouvé: " + prediction.getName() + " (ID: " + prediction.getId() + ")"); // ✅
																														// Debug
					TreeItem<GridMindItem> predictionItem = new TreeItem<>(
							new GridMindItem(prediction.getName(), prediction.getId(), false) // ✅ false = pronostic
																								// (pas théorique)
					);
					theoreticalItem.getChildren().add(predictionItem);
				}
				root.getChildren().add(theoreticalItem);
			}
			System.out.println("DEBUG: Hierarchie chargée avec succès."); // ✅ Debug
		} catch (SQLException e) {
			e.printStackTrace();
			UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les grilles: " + e.getMessage());
		}
	}

	private void createNewPrediction(UUID theoreticalGridId) {
		try {
	        // ✅ 1. Récupère la grille théorique pour obtenir son nom
	        GridMindGrid theoreticalGrid = gridDAO.getGridById(theoreticalGridId);
	        String gridName = theoreticalGrid != null ? theoreticalGrid.getName() : "Inconnu";

	        // ✅ 2. Crée un NOUVEAU pronostic (même si d'autres existent)
			GridMindGridPrediction newPrediction = new GridMindGridPrediction();
			newPrediction.setId(UUID.randomUUID());
			newPrediction.setGridMindGridId(theoreticalGridId);

		    // ✅ 3. Utilise le nom de la grille de base dans le nom du pronostic
			newPrediction.setName(gridName + " - " + java.time.LocalDateTime.now()
					.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
			newPrediction.setGuaranteeType("N");
			newPrediction.setMinOdd(1.0);
			newPrediction.setMaxOdd(100.0);
			newPrediction.setGuaranteePercentage(100.0);

			// ✅ 4. Sauvegarde le nouveau pronostic en base
			gridPredictionDAO.save(newPrediction);

			// ✅ 5. Copie les matchs de la grille théorique vers le nouveau pronostic
			List<GridMindMatch> theoreticalMatches = gridMindMatchDAO.getMatchesForGrid(theoreticalGridId);
			for (GridMindMatch match : theoreticalMatches) {
				GridMindMatchPrediction matchPrediction = new GridMindMatchPrediction();
				matchPrediction.setId(UUID.randomUUID());
				matchPrediction.setGridMindGridPredictionId(newPrediction.getId());
				matchPrediction.setGridMindMatchId(match.getId());
				matchPrediction.setGridMindGridId(theoreticalGridId); // ✅ NOUVELLE LIGNE : Remplit gridmind_grid_id
				matchPrediction.setHomeWinOddOverride(match.getHomeWinOdd());
				matchPrediction.setDrawOddOverride(match.getDrawOdd());
				matchPrediction.setAwayWinOddOverride(match.getAwayWinOdd());
				// ✅ Sauvegarde chaque pronostic de match
				matchPredictionDAO.save(matchPrediction);
			}

			// ✅ 6. Ouvre la fenêtre pour éditer le nouveau pronostic
			openPredictionWindow(newPrediction.getId());

			// ✅ 7. Rafraîchit l'arborescence (optionnel, si nécessaire)
			loadGridsHierarchy();

		} catch (SQLException e) {
			e.printStackTrace();
			UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur SQL",
					"GridListPane createNewPrediction Impossible de créer un nouveau pronostic: " + e.getMessage());
		}
	}

	public GridListPane() {
		System.out.println("DEBUG: Constructeur GridListPane appelé !"); // ✅ Breakpoint ici
		try {
			this.gridDAO = new GridMindGridDAOImpl();
			this.gridPredictionDAO = new GridMindGridPredictionDAOImpl();
			this.gridMindMatchDAO = new GridMindMatchDAO();
			this.matchPredictionDAO = new GridMindMatchPredictionDAOImpl(); // ✅ Initialisation
		} catch (SQLException e) {
			e.printStackTrace();
			UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'initialiser les DAO: " + e.getMessage());
		}
		System.out.println("Début de GridListPane");
		// ✅ Initialisez les deux composants
		setupGridTreeTableView(); // Pour le TreeTableView (arborescence)
		System.out.println("setupGridTreeTableView terminé");
		// setupGridTableView();
		System.out.println("setupGridTableView commenté");
		setupMatchTableView();
		System.out.println("setupMatchTableView terminé");
		setupLayout();
		System.out.println("setupLayout terminé");
		try {
			loadGridsHierarchy(); // ✅ Capturez SQLException ici
		} catch (Exception e) {
			e.printStackTrace();
			UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les grilles: " + e.getMessage());
		}
		System.out.println("loadGridsHierarchy terminé");
	}

	private void setupLayout() {
		// Panneau des détails de la grille (partie haute droite)
		VBox gridDetailsPane = new VBox(10);
		gridDetailsPane.setPadding(new Insets(10));
		gridDetailsPane.getChildren().addAll(new Label("Détails de la grille:"), gridNumberLabel, gridNameLabel,
				gridDeadlineLabel);

		// Panneau des matchs (partie basse)
		HBox matchButtons = new HBox(10, addMatchButton, removeMatchButton, moveUpButton, moveDownButton);
		matchButtons.setAlignment(Pos.CENTER_LEFT); // Alignement à gauche
		VBox matchPane = new VBox(10);
		matchPane.setPadding(new Insets(10));
		matchPane.getChildren().addAll(new Label("Matchs de la grille:"), matchTableView, matchButtons);

		// SplitPane pour séparer l'arbre des grilles et les détails
		SplitPane splitPane = new SplitPane();
		splitPane.getItems().addAll(gridTreeTableView, new VBox(10, gridDetailsPane, matchPane));
		splitPane.setDividerPositions(0.3);

		// Taille minimale pour le TreeTableView
		gridTreeTableView.setPrefWidth(300);
		gridTreeTableView.setPrefHeight(600);

		// Configuration des boutons ↑ et ↓
		addMatchButton.setOnAction(event -> {
			System.out.println("DEBUG: addMatchButton appelé !");
			GridMindMatch selectedMatch = matchTableView.getSelectionModel().getSelectedItem();
		    // ✅ Vérifie que selectedGrid est toujours valide
		    if (selectedGrid == null) {
		        System.out.println("DEBUG: selectedGrid est NULL dans addMatchButton !");
		        return;
		    }
			showAddMatchDialog();
		    }
		);

		removeMatchButton.setOnAction(event -> {
			GridMindMatch selectedMatch = matchTableView.getSelectionModel().getSelectedItem();
			if (selectedMatch != null) {
				removeSelectedMatch();
			}
		});

		moveUpButton.setOnAction(event -> {
			GridMindMatch selectedMatch = matchTableView.getSelectionModel().getSelectedItem();
			if (selectedMatch != null) {
				moveMatchUp(selectedMatch);
			}
		});

		moveDownButton.setOnAction(event -> {
			GridMindMatch selectedMatch = matchTableView.getSelectionModel().getSelectedItem();
			if (selectedMatch != null) {
				moveMatchDown(selectedMatch);
			}
		});

		// Désactiver les boutons si aucune ligne n'est sélectionnée
		matchTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			boolean isSelected = newSelection != null;
			moveUpButton.setDisable(!isSelected);
			moveDownButton.setDisable(!isSelected);
		});

		// Ajout au BorderPane
		setCenter(splitPane);
	}

	private void setupMatchTableView() {
		// Colonne pour le numéro de ligne
		TableColumn<GridMindMatch, Number> lineNumberCol = new TableColumn<>("N°");
		lineNumberCol.setCellValueFactory(new PropertyValueFactory<>("lineNumber"));
		lineNumberCol.setPrefWidth(50);

		TableColumn<GridMindMatch, String> homeTeamCol = new TableColumn<>("Domicile");
		homeTeamCol.setCellValueFactory(new PropertyValueFactory<>("homeTeam"));
		homeTeamCol.setPrefWidth(150);

		TableColumn<GridMindMatch, String> awayTeamCol = new TableColumn<>("Extérieur");
		awayTeamCol.setCellValueFactory(new PropertyValueFactory<>("awayTeam"));
		awayTeamCol.setPrefWidth(150);

		TableColumn<GridMindMatch, String> dateCol = new TableColumn<>("Date");
		dateCol.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getMatchDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
		dateCol.setPrefWidth(150);

		// Colonne pour la côte "Domicile" (éditable)
		TableColumn<GridMindMatch, Double> homeOddCol = new TableColumn<>("Côte Domicile");
		homeOddCol.setCellValueFactory(new PropertyValueFactory<>("homeWinOdd"));
		homeOddCol.setCellFactory(TextFieldTableCell.forTableColumn(new com.kad.tools.DoubleStringConverter()));
		homeOddCol.setOnEditCommit(event -> {
			GridMindMatch match = event.getRowValue();
			match.setHomeWinOdd(event.getNewValue());
			updateOddInDatabase(match.getGridId(), match.getMatchId(), "home", event.getNewValue());
			matchTableView.refresh(); // Rafraîchit le TableView pour afficher la nouvelle valeur
		});

		// Colonne pour la côte "Nul" (éditable)
		TableColumn<GridMindMatch, Double> drawOddCol = new TableColumn<>("Côte Nul");
		drawOddCol.setCellValueFactory(new PropertyValueFactory<>("drawOdd"));
		drawOddCol.setCellFactory(TextFieldTableCell.forTableColumn(new com.kad.tools.DoubleStringConverter()));
		drawOddCol.setOnEditCommit(event -> {
			GridMindMatch match = event.getRowValue();
			match.setDrawOdd(event.getNewValue());
				updateOddInDatabase(match.getGridId(), match.getMatchId(), "draw", event.getNewValue());
				matchTableView.refresh(); // Rafraîchit le TableView pour afficher la nouvelle valeur
		});

		// Colonne pour la côte "Extérieur" (éditable)
		TableColumn<GridMindMatch, Double> awayOddCol = new TableColumn<>("Côte Extérieur");
		awayOddCol.setCellValueFactory(new PropertyValueFactory<>("awayWinOdd")); // Ajoutez cette ligne
		awayOddCol.setCellFactory(TextFieldTableCell.forTableColumn(new com.kad.tools.DoubleStringConverter()));
		awayOddCol.setOnEditCommit(event -> {
			GridMindMatch match = event.getRowValue();
			match.setAwayWinOdd(event.getNewValue());
				updateOddInDatabase(match.getGridId(), match.getMatchId(), "away", event.getNewValue());
				matchTableView.refresh(); // Rafraîchit le TableView pour afficher la nouvelle valeur
		});

//        matchTableView.getColumns().addAll(lineNumberCol, homeTeamCol, awayTeamCol, dateCol, homeOddCol, drawOddCol, awayOddCol);
		List<TableColumn<GridMindMatch, ?>> columns = Arrays.asList(lineNumberCol, homeTeamCol, awayTeamCol, dateCol,
				homeOddCol, drawOddCol, awayOddCol);
		matchTableView.getColumns().addAll(columns);
		matchTableView.setEditable(true); // Activer l'édition dans le TableView
	}

	private void updateOddInDatabase(UUID gridId, UUID matchId, String oddType, double newValue) {
		try {
			GridMindMatchDAO dao = new GridMindMatchDAO(); 
			dao.updateOdds(gridId, matchId, oddType, newValue);
		} catch (SQLException e) {
	        UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de mettre à jour les côtes: " + e.getMessage());
	    }
	}

	private void loadGridDetails(GridMindGrid grid) {
		gridNameLabel.setText("Grille: " + grid.getName());
		gridDeadlineLabel
				.setText("Deadline: " + grid.getPlayDeadline().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
		try {
			GridMindMatchDAO dao = new GridMindMatchDAO(); 
			matchTableView.getItems().setAll(FXCollections.observableArrayList(dao.getMatchesForGrid(grid.getId())));
		} catch (SQLException e) {
			UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
		}
	}

	private void loadMatchesForGrid(GridMindGrid grid) throws SQLException {
		try {
			GridMindMatchDAO dao = new GridMindMatchDAO(); 
			matchTableView.getItems().setAll(FXCollections.observableArrayList(dao.getMatchesForGrid(grid.getId())));
		} catch (SQLException e) {
			UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
		}
	}

	// ✅ Nouvelle version (corrigée)
	// Dans GridListPane.java
	// ✅ Changez "private" en "public"
	public void loadGrids() throws Exception {
		try {
			// 1. Chargez toutes les grilles théoriques (GridMindGrid)
			List<GridMindGrid> theoreticalGrids = gridDAO.getAllGrids();

			// 2. Créez la racine de l'arbre
			TreeItem<GridMindItem> root = new TreeItem<>(new GridMindItem("Grilles", null, true));

			// 3. Pour chaque grille théorique, chargez ses pronostics
			for (GridMindGrid theoreticalGrid : theoreticalGrids) {
				// ✅ GridMindGrid = grille théorique → isTheoretical = true
				TreeItem<GridMindItem> theoreticalItem = new TreeItem<>(
						new GridMindItem(theoreticalGrid.getName(), theoreticalGrid.getId(), true));

				// 4. Chargez les pronostics (GridMindGridPrediction) pour cette grille
				List<GridMindGridPrediction> predictions = gridPredictionDAO
						.getPredictionsForGrid(theoreticalGrid.getId());
				for (GridMindGridPrediction prediction : predictions) {
					// ✅ GridMindGridPrediction = pronostic → isTheoretical = false
					theoreticalItem.getChildren()
							.add(new TreeItem<>(new GridMindItem(prediction.getName(), prediction.getId(), false)));
				}

				root.getChildren().add(theoreticalItem);
			}

			// 5. Mettez à jour le TreeTableView
			gridTreeTableView.setRoot(root);
			gridTreeTableView.refresh();
		} catch (SQLException e) {
			e.printStackTrace();
			UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Problème avec la base de données: " + e.getMessage());
		}
	}

	private void showAddMatchDialog() {
		// GridMindGrid selectedGrid = gridListPane.getSelectedGrid();
		GridMindGrid selectedGrid = this.getSelectedGrid(); // ✅ Utilisez "this" pour accéder à l'instance actuelle
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

		// 4. Créer une TableView avec des colonnes claires
		TableView<Match> tableView = new TableView<>();
		tableView.setPrefWidth(800); // ✅ Élargissez la fenêtre
		tableView.setPrefHeight(400);

		// ✅ Colonne pour la compétition
		TableColumn<Match, String> competitionCol = new TableColumn<>("Compétition");
		competitionCol.setCellValueFactory(cellData ->
		// new SimpleStringProperty(cellData.getValue().getCompetition().getName())
		new SimpleStringProperty(cellData.getValue().getCompetitionName()));
		competitionCol.setPrefWidth(150);

		// ✅ Colonne pour les équipes (Domicile vs Extérieur)
		TableColumn<Match, String> teamsCol = new TableColumn<>("Match");
		teamsCol.setCellValueFactory(cellData -> {
			Match match = cellData.getValue();
			return new SimpleStringProperty(
					// match.getHomeTeam().getName() + " vs " + match.getAwayTeam().getName()
					match.getHomeTeamName() + " vs " + match.getAwayTeamName());
		});
		teamsCol.setPrefWidth(300);

		// ✅ Colonne pour la date
		TableColumn<Match, String> dateCol = new TableColumn<>("Date");
		dateCol.setCellValueFactory(
				cellData -> new SimpleStringProperty(cellData.getValue().getMatchDate().toString()));
		dateCol.setPrefWidth(200);

		// ✅ Ajoutez les colonnes à la TableView
		tableView.getColumns().addAll(competitionCol, teamsCol, dateCol);
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

		// 7. Afficher le dialogue et traiter le résultat
		Optional<Match> result = dialog.showAndWait();
		if (result.isPresent()) {
			Match selectedMatch = result.get();
			try {
				// ✅ Ajoutez le match à la grille
				gridMindMatchDAO.addMatchToGrid(selectedGrid.getId(), selectedMatch.getId());
				// UiUtils.showAlert(Alert.AlertType.INFORMATION, "Succès", "Match ajouté avec succès !");
				// ✅ Rechargez les matchs
				loadMatchesForGrid(selectedGrid);
			} catch (Exception e) {
				UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter le match: " + e.getMessage());
			}
		}
	}

	private void removeSelectedMatch() {
		GridMindMatch selectedMatch = matchTableView.getSelectionModel().getSelectedItem();
		// GridMindGrid selectedGrid =
		// gridTableView.getSelectionModel().getSelectedItem();
		GridMindGrid selectedGrid = this.getSelectedGrid();

		if (selectedGrid == null || selectedMatch == null) {
			UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une grille et un match.");
			return;
		}

		try {
			GridMindMatchDAO dao = new GridMindMatchDAO(); 
			// Supprimer le match de la grille
			dao.removeMatchFromGrid(selectedGrid.getId(), selectedMatch.getMatchId());

			// Renuméroter les matchs restants
			renumberMatches(selectedGrid.getId());

			// Rafraîchir la liste des matchs
			refreshGridMatches(selectedGrid);
		} catch (SQLException e) {
			UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retirer le match: " + e.getMessage());
		}
	}

	private void renumberMatches(UUID gridId) throws SQLException {
		try {
			GridMindMatchDAO dao = new GridMindMatchDAO(); 
			// Récupérer les matchs de la grille triés par line_number
			List<GridMindMatch> matches = dao.getMatchesForGrid(gridId);

			// Mettre à jour les line_number de manière séquentielle
			int newLineNumber = 1;
			for (GridMindMatch match : matches) {
				if (match.getLineNumber() != newLineNumber) {
					dao.updateLineNumber(gridId, match.getMatchId(), newLineNumber);
				}
				newLineNumber++;
			}
		} catch (SQLException e) {
			UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de renuméroter les matchs");
		}
	}

	private void moveMatchUp(GridMindMatch match) {
		int currentIndex = matchTableView.getItems().indexOf(match);
		if (currentIndex <= 0) {
			return; // Déjà en haut de la liste
		}

		ObservableList<GridMindMatch> matches = matchTableView.getItems();
		GridMindMatch previousMatch = matches.get(currentIndex - 1);

		// Échanger les line_number
		int tempLineNumber = match.getLineNumber();
		match.setLineNumber(previousMatch.getLineNumber());
		previousMatch.setLineNumber(tempLineNumber);

		// Mettre à jour la base de données
		try {
			GridMindMatchDAO dao = new GridMindMatchDAO(); 
			dao.updateLineNumber(match.getGridId(), match.getMatchId(), match.getLineNumber());
			dao.updateLineNumber(previousMatch.getGridId(), previousMatch.getMatchId(), previousMatch.getLineNumber());
		} catch (SQLException e) {
			UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur",
					"Impossible de mettre à jour l'ordre des matchs: " + e.getMessage());
			return;
		}

		// Rafraîchir le TableView
		Collections.sort(matches, Comparator.comparingInt(GridMindMatch::getLineNumber));
		matchTableView.refresh();
	}

	private void moveMatchDown(GridMindMatch match) {
		int currentIndex = matchTableView.getItems().indexOf(match);
		ObservableList<GridMindMatch> matches = matchTableView.getItems();
		if (currentIndex >= matches.size() - 1) {
			return; // Déjà en bas de la liste
		}

		GridMindMatch nextMatch = matches.get(currentIndex + 1);

		// Échanger les line_number
		int tempLineNumber = match.getLineNumber();
		match.setLineNumber(nextMatch.getLineNumber());
		nextMatch.setLineNumber(tempLineNumber);

		// Mettre à jour la base de données
		try {
			GridMindMatchDAO dao = new GridMindMatchDAO();
			dao.updateLineNumber(match.getGridId(), match.getMatchId(), match.getLineNumber());
			dao.updateLineNumber(nextMatch.getGridId(), nextMatch.getMatchId(), nextMatch.getLineNumber());
		} catch (SQLException e) {
			UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur",
					"Impossible de mettre à jour l'ordre des matchs: " + e.getMessage());
			return;
		}

		// Rafraîchir le TableView
		Collections.sort(matches, Comparator.comparingInt(GridMindMatch::getLineNumber));
		matchTableView.refresh();
	}

	private void showCreateGridDialog() {
		Dialog<GridMindGrid> dialog = new Dialog<>();
		dialog.setTitle("Nouvelle Grille");
		dialog.setHeaderText("Créer une nouvelle grille");

		// Champs du formulaire
		TextField nameField = new TextField();
		// Spinner<Integer> gridNumberSpinner = new Spinner<>(1, 50, 1);
		TextField gridNumberField = new TextField();
		gridNumberField.setPromptText("Numéro de grille (3 chiffres max)");
		// Dans GridListPane.java
		ComboBox<GridType> gridTypeCombo = new ComboBox<>(FXCollections.observableArrayList(GridType.values()));
		gridTypeCombo.setConverter(new StringConverter<GridType>() {
			@Override
			public String toString(GridType type) {
				return type != null ? type.getDisplayName() : "";
			}

			@Override
			public GridType fromString(String string) {
				try {
					return GridType.fromDisplayName(string);
				} catch (IllegalArgumentException e) {
					return null; // ou affiche une erreur
				}
			}
		});

		TextField numberOfMatchesField = new TextField();
		numberOfMatchesField.setPromptText("Nombre de matchs (2 chiffres max)");
		DatePicker deadlinePicker = new DatePicker(ZonedDateTime.now().plusDays(7).toLocalDate());
		TextField deadlineTimeField = new TextField(); // <-- Déclaration de deadlineTimeField
		deadlineTimeField.setPromptText("HH:mm");
		TextArea descriptionArea = new TextArea();

		// Mise en page du dialogue
		dialog.getDialogPane()
				.setContent(new VBox(10, new Label("Nom:"), nameField, new Label("N° Grille:"), gridNumberField,
						new Label("Nombre de matchs:"), numberOfMatchesField, // <-- Champ pour le nombre de matchs
						new Label("Type:"), gridTypeCombo, new Label("Date limite:"), deadlinePicker,
						new Label("Heure limite:"), deadlineTimeField, new Label("Description:"), descriptionArea));

		// Boutons du dialogue
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

		// Conversion du résultat
		dialog.setResultConverter(button -> {
			if (button == ButtonType.OK) {
				try {
					LocalDate deadlineDate = deadlinePicker.getValue();
					if (deadlineDate == null) {
						UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une date limite.");
						return null;
					}

					String timeText = deadlineTimeField.getText().trim();
					if (!timeText.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
						UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur",
								"Format d'heure invalide. Utilisez HH:mm (ex: 18:40).");
						return null;
					}

					String[] timeParts = timeText.split(":");
					int hour = Integer.parseInt(timeParts[0]);
					int minute = Integer.parseInt(timeParts[1]);

					LocalTime deadlineTime = LocalTime.of(hour, minute);
					ZonedDateTime playDeadline = ZonedDateTime.of(deadlineDate, deadlineTime, ZoneId.systemDefault());

					GridMindGrid grid = new GridMindGrid();
					grid.setId(UUID.randomUUID());
					grid.setName(nameField.getText());
					grid.setPlayDeadline(playDeadline); // Stocke en TIMESTAMPTZ (UTC+1/UTC+2)
					// grid.setGridNumber(gridNumberSpinner.getValue());

					// Récupère et valide le numéro de grille
					String gridNumberText = gridNumberField.getText().trim();
					gridNumberField.setTextFormatter(new TextFormatter<>(change -> {
						if (change.getControlNewText().matches("\\d{0,3}")) {
							return change;
						}
						return null; // Rejette la modification si elle ne correspond pas au format
					}));
					int gridNumber = Integer.parseInt(gridNumberText); // Convertit en entier
					grid.setGridNumber(gridNumber); // <-- Numéro de grille saisi

					// Récupère et valide le nombre de matchs
					String matchesNumberText = numberOfMatchesField.getText().trim();
					numberOfMatchesField.setTextFormatter(new TextFormatter<>(change -> {
						if (change.getControlNewText().matches("\\d{0,2}")) {
							return change;
						}
						return null; // Rejette la modification si elle ne correspond pas au format
					}));
					int numberOfMatches = Integer.parseInt(matchesNumberText); // Convertit en entier
					grid.setNumberOfMatches(numberOfMatches); // <-- Nombre de matchs saisi
					grid.setGridType(gridTypeCombo.getValue()); // <-- Utilise la valeur sélectionnée
					grid.setPlayDeadline(playDeadline);
					grid.setDescription(descriptionArea.getText());
					return grid;
				} catch (Exception e) {
					UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de créer la grille: " + e.getMessage());
					return null;
				}
			}
			return null;
		});

		// Affiche le dialogue
		dialog.showAndWait().ifPresent(grid -> {
		    try {
		        GridMindGridDAO dao = new GridMindGridDAOImpl();
		        dao.createGrid(grid, DatabaseConnection.getSessionConnection()); // ✅ Passe la connexion de session

		        UiUtils.showAlert(Alert.AlertType.INFORMATION, "Succès", "La grille a été créée avec succès !");
		        loadGridsHierarchy(); // ✅ Rafraîchit l'arborescence

		        // ✅ Marque qu'il y a des modifications non validées
		        // (Optionnel : si tu veux utiliser hasUncommittedChanges() plus tard)
		        // DatabaseConnection.setHasUncommittedChanges(true);

		    } catch (SQLException e) {
		        e.printStackTrace();
		        UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de créer la grille: " + e.getMessage());
		    }
		    // ✅ Pas de finally nécessaire (pas de commit/rollback/close)
		});
	}

private void refreshGridMatches(GridMindGrid selectedGrid) {
    try {
        GridMindMatchDAO gridMindMatchDAO = new GridMindMatchDAO(); // ✅ Pas de try-with-resources
        // Récupérer les GridMindMatch pour la grille sélectionnée
        List<GridMindMatch> gridMindMatches = gridMindMatchDAO.getMatchesForGrid(selectedGrid.getId());

        // Mettre à jour le TableView avec les GridMindMatch
        matchTableView.getItems().setAll(FXCollections.observableArrayList(gridMindMatches));

    } catch (SQLException e) {
        UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de recharger les matchs: " + e.getMessage());
    }
}

// ✅ Version unifiée et corrigée (dans GridListPane)
private void openPredictionWindow(UUID predictionId) {
    System.out.println("DEBUG: openPredictionWindow appelé avec ID: " + predictionId);
    try {
        GridMindMatchPredictionDAO predictionDAO;
        // ✅ Récupère la connexion de session
        Connection conn = DatabaseConnection.getSessionConnection();
        try {
            System.out.println("🔹 [DEBUG] GridListPane 1.1 openPredictionWindow - conn: " + conn +
                    " closed? " + conn.isClosed());
        } catch (SQLException e) {
            System.err.println("❌ GridListPane 1.1 openPredictionWindow - Erreur lors de la vérification de l'état de la connexion : " + e.getMessage());
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/grid_prediction.fxml"));
        Parent root = loader.load();

        GridMindPredictionController controller = loader.getController();
        if (controller == null) {
            System.err.println("❌ GridListPane 1.2 openPredictionWindow - Erreur : Le contrôleur n'a pas été instancié !");
            return;
        }
        try {
            System.out.println("🔹 [DEBUG] GridListPane 2.1 openPredictionWindow - conn: " + conn +
                    " closed? " + conn.isClosed());
        } catch (SQLException e) {
            System.err.println("❌ GridListPane 2.1 openPredictionWindow - Erreur lors de la vérification de l'état de la connexion : " + e.getMessage());
        }

        // ✅ INJECTE LES DAO MANQUANTS
        controller.setGridMindMatchDAO(this.gridMindMatchDAO);
        controller.setGridDAO(this.gridDAO);

        // ✅ Injecte les DAO dans le contrôleur
        MatchOddsDAO matchOddsDAO = new MatchOddsDAO();
        controller.setMatchOddsDAO(matchOddsDAO);

        //GridMindMatchPredictionDAO predictionDAO = new GridMindMatchPredictionDAOImpl();
        try {
            predictionDAO = new GridMindMatchPredictionDAOImpl(); // ✅ Capture l'exception
        } catch (SQLException e) {
            e.printStackTrace();
            // Ou afficher un message d'erreur
            System.err.println("Erreur lors de l'initialisation de GridMindMatchPredictionDAO : " + e.getMessage());
            // Initialiser avec une valeur par défaut (ex: null ou un DAO vide)
            predictionDAO = null;
        }        
        controller.setPredictionDAO(predictionDAO);

        // ✅ Passe la référence à GridListPane pour rafraîchir l'arbre après sauvegarde
        controller.setGridListPane(this);

        // ✅ Charge le pronostic par son ID
        GridMindGridPrediction prediction = null;
        try {
            prediction = gridPredictionDAO.getPredictionById(predictionId);
        } catch (SQLException e) {
            e.printStackTrace();
            UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le pronostic: " + e.getMessage());
        }
        if (prediction == null) {
            UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Pronostic introuvable !");
            return;
        }
     // Après avoir récupéré le pronostic
     // ✅ Récupère la grille théorique avec gestion de SQLException
        GridMindGrid grid = null;
        try {
            grid = gridDAO.getGridById(prediction.getGridMindGridId());
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de la récupération de la grille : " + e.getMessage());
            e.printStackTrace();
            // ✅ Affiche un message d'erreur à l'utilisateur si nécessaire
            UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la grille : " + e.getMessage());
            return;  // ✅ Quitte la méthode si la grille n'est pas récupérée
        }

        // Récupère le libellé du GridType
        String gridTypeLabel = grid != null ? grid.getGridType().getDisplayName() : "Inconnu";
        
        controller.setCurrentGridId(prediction.getGridMindGridId());
        controller.setCurrentGridPredictionId(predictionId);
        controller.loadPredictionsFromDatabase();

        Stage predictionStage = new Stage();
        predictionStage.setTitle("GridMind - " + gridTypeLabel + " - " + prediction.getName());
        predictionStage.initModality(Modality.WINDOW_MODAL);
        predictionStage.initOwner((Stage) getScene().getWindow());
        
        // ✅ Définis une taille fixe pour la fenêtre de pronostics
        predictionStage.setWidth(1000);   // Largeur en pixels (ajuste selon tes besoins)
        predictionStage.setHeight(700);   // Hauteur en pixels (ajuste selon tes besoins)
        predictionStage.setMaximized(false);  // ✅ Désactive le mode plein écran
        
        predictionStage.setScene(new Scene(root));
        predictionStage.showAndWait();

    } catch (IOException e) {
        System.err.println("❌ Erreur de chargement du FXML :");
        e.printStackTrace();
        UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur",
                "Impossible d'ouvrir la fenêtre de pronostics: " + e.getMessage());
    }
}

	private void deleteSelectedGrid() {
		// Récupère le TreeItem sélectionné
		TreeItem<GridMindItem> selectedTreeItem = gridTreeTableView.getSelectionModel().getSelectedItem();

		// Vérifie qu'un élément est bien sélectionné
		if (selectedTreeItem == null) {
			return;
		}

		// Récupère l'objet GridMindItem depuis le TreeItem
		GridMindItem selectedItem = selectedTreeItem.getValue();

		// Vérifie que c'est une grille théorique (et pas la racine "Grilles")
		if (!selectedItem.isTheoretical() || selectedItem.getName().equals("Grilles")) {
			return;
		}

		// Affiche la confirmation
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Supprimer la grille");
		alert.setHeaderText("Supprimer la grille : " + selectedItem.getName());
		alert.setContentText(
				"Cette action supprimera la grille, ses matchs, et tous les pronostics associés. Continuer ?");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.OK) {
			try {
				GridMindGridDAO gridDAO = new GridMindGridDAOImpl();
				gridDAO.delete(selectedItem.getId());
				loadGridsHierarchy(); // Rafraîchit l'arborescence
				UiUtils.showAlert(Alert.AlertType.INFORMATION, "Succès", "La grille et ses données ont été supprimées.");
			} catch (SQLException e) {
				e.printStackTrace();
				UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer la grille: " + e.getMessage());
			}
		}
	}
	
	private void deleteSelectedPrediction() {
	    TreeItem<GridMindItem> selectedTreeItem = gridTreeTableView.getSelectionModel().getSelectedItem();
	    if (selectedTreeItem == null) {
	        return;
	    }

	    GridMindItem selectedItem = selectedTreeItem.getValue();
	    if (selectedItem.isTheoretical() || selectedItem.getName().equals("Grilles")) {
	        return;
	    }

	    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
	    alert.setTitle("Supprimer le pronostic");
	    alert.setHeaderText("Supprimer le pronostic : " + selectedItem.getName());
	    alert.setContentText("Cette action supprimera le pronostic et tous ses matchs associés. Continuer ?");

	    Optional<ButtonType> result = alert.showAndWait();
	    if (result.isPresent() && result.get() == ButtonType.OK) {
	        try {
	            GridMindGridPredictionDAO gridPredictionDAO = new GridMindGridPredictionDAOImpl();
	            gridPredictionDAO.delete(selectedItem.getId()); // ✅ Suppression en base (sans commit/rollback)
	            loadGridsHierarchy(); // Rafraîchit l'interface
	            UiUtils.showAlert(Alert.AlertType.INFORMATION, "Succès", "Le pronostic et ses données ont été supprimés (en attente de sauvegarde).");
	        } catch (SQLException e) {
	            e.printStackTrace();
	            UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le pronostic: " + e.getMessage());
	        }
	    }
	}
}

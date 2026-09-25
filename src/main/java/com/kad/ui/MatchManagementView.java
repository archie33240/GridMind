package com.kad.ui;

import com.kad.model.*;


import com.kad.dao.*;
import com.kad.database.DatabaseConnection;
import com.kad.tools.DateTimeUtils;
import com.kad.tools.UiUtils;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
// import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
// import javafx.scene.control.cell.TextFieldTableCell;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javafx.util.StringConverter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.List;
import java.util.UUID;


public class MatchManagementView extends VBox {

	private VBox root;	

	private ObservableList<CountryArea> countryAreas = FXCollections.observableArrayList();
    private ComboBox<CountryArea> countryAreaComboBox = new ComboBox<>();

    private ObservableList<Season> seasons = FXCollections.observableArrayList();
    private ComboBox<Season> seasonComboBox = new ComboBox<>();

    private ObservableList<Competition> competitions = FXCollections.observableArrayList();
    private ComboBox<Competition> competitionComboBox = new ComboBox<>(); // ✅ Sans passer competitions

    private ObservableList<Matchday> matchdays = FXCollections.observableArrayList();
    private ComboBox<Matchday> matchdayComboBox = new ComboBox<>();

    private ObservableList<Team> teams = FXCollections.observableArrayList();

    private ObservableList<Match> matches = FXCollections.observableArrayList();
    private TableView<Match> matchTable = new TableView<>();

    // Constructeur : remplace la méthode start()
    public MatchManagementView() {
        // Initialisation de l'interface
        initializeUI();

        // Chargement des données
        loadData();
    } 
    
    private void loadData() {
        // Charger les CountryArea
        try {
            CountryAreaDAO dao = new CountryAreaDAO();
            List<CountryArea> areas = dao.getAllCountryAreas();
            System.out.println("Nombre de pays chargés : " + areas.size());
            countryAreas.setAll(areas);
        } catch (SQLException e) {
            e.printStackTrace();
            UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les pays : " + e.getMessage());           
        }

        // Charger les saisons
        try {
            SeasonDAO seasonDAO = new SeasonDAO();
            List<Season> loadedSeasons = seasonDAO.getAllSeasons();
            seasons.setAll(loadedSeasons);
            System.out.println("Saisons chargées : " + loadedSeasons.size());
        } catch (SQLException e) {
            e.printStackTrace();
            UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les saisons : " + e.getMessage());           
        }

        // Charger les compétitions
        try {
            CompetitionDAO competitionDAO = new CompetitionDAO();
            List<Competition> loadedCompetitions = competitionDAO.getAllCompetitions();
            competitions.setAll(loadedCompetitions);
            System.out.println("Compétitions chargées : " + loadedCompetitions.size());
        } catch (SQLException e) {
            e.printStackTrace();
            UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les compétitions.");
        }

     // Chargement des équipes :
        try {
            TeamDAO teamDAO = new TeamDAO();
            List<Team> loadedTeams = teamDAO.getAllTeams();
            teams.setAll(loadedTeams);
            System.out.println("Équipes chargées : " + loadedTeams.size());
        } catch (SQLException e) {
            e.printStackTrace();
            UiUtils.showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les équipes : " + e.getMessage());
        }
    }
    
    private void initializeUI() {    
        // Configuration des colonnes du TableView
        setupTableColumns();

        // Disposition des listes déroulantes
        GridPane comboBoxGrid = setupComboBoxes();

        // Boutons
        HBox buttonBox = setupButtons();

        // Disposition globale
        root = new VBox(10, comboBoxGrid, matchTable, buttonBox);
        root.setPadding(new Insets(10));
        
        this.getChildren().add(root);  // <-- Ajoute root à ce VBox        
    }
        // Garde la méthode start() pour la compatibilité (optionnel)
        public void start(Stage stage) {
            Scene scene = new Scene(root, 1000, 600);        	
            stage.setScene(new Scene(this));
            stage.setTitle("Gestion des Compétitions");
            stage.show();
        }
              
    private GridPane setupComboBoxes() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        // Pays
        Label paysLabel = new Label("Pays:");
        paysLabel.setPrefWidth(80);
        grid.add(paysLabel, 1, 0);
        countryAreaComboBox.setPrefWidth(200);
        grid.add(countryAreaComboBox, 2, 0);
        countryAreaComboBox.setItems(countryAreas);
        countryAreaComboBox.setConverter(new StringConverter<CountryArea>() {
            @Override public String toString(CountryArea countryArea) { return countryArea != null ? countryArea.getName() : ""; }
            @Override public CountryArea fromString(String string) {
                return countryAreas.stream().filter(c -> c.getName().equals(string)).findFirst().orElse(null);
            }
        });

        // Écouteur pour les compétitions
        countryAreaComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    CompetitionDAO competitionDAO = new CompetitionDAO();
                    List<Competition> filteredCompetitions = competitionDAO.getCompetitionsByCountryArea(newVal.getId());
                    competitions.setAll(filteredCompetitions);
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les compétitions pour ce pays.");
                }
            }
        });

        // Saison
        Label saisonLabel = new Label("Saison:");
        saisonLabel.setPrefWidth(80);
        grid.add(saisonLabel, 3, 0);
        seasonComboBox.setPrefWidth(150);
        grid.add(seasonComboBox, 4, 0);
        seasonComboBox.setItems(seasons);
        seasonComboBox.setConverter(new StringConverter<Season>() {
            @Override public String toString(Season season) {
                return season != null ? String.format("%d-%d", season.getStartDate().getYear(), season.getEndDate().getYear()) : "";
            }
            @Override public Season fromString(String string) { return null; }
        });

        // Compétition
        Label competitionLabel = new Label("Compétition:");
        competitionLabel.setPrefWidth(80);
        grid.add(competitionLabel, 1, 1);
        competitionComboBox.setPrefWidth(200);
        grid.add(competitionComboBox, 2, 1);
        competitionComboBox.setItems(competitions);
        competitionComboBox.setConverter(new StringConverter<Competition>() {
            @Override public String toString(Competition competition) { return competition != null ? competition.getName() : ""; }
            @Override public Competition fromString(String string) { return null; }
        });

        // Journée
        Label journeeLabel = new Label("Journée:");
        journeeLabel.setPrefWidth(80);
        grid.add(journeeLabel, 3, 1);
        matchdayComboBox.setPrefWidth(150);
        grid.add(matchdayComboBox, 4, 1);
        matchdayComboBox.setItems(matchdays);
        matchdayComboBox.setConverter(new StringConverter<Matchday>() {
            @Override
            public String toString(Matchday matchday) {
                return matchday != null ? matchday.toString() : "";
            }

            @Override
            public Matchday fromString(String string) {
                // Corrige cette méthode pour retourner le bon Matchday
                return matchdays.stream()
                        .filter(matchday -> matchday.toString().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });


        // Écouteurs pour les compétitions, saisons et journées
        competitionComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateMatchdays();
            loadTeamsForCompetitionAndSeason(newVal != null ? newVal.getId() : null, seasonComboBox.getValue() != null ? seasonComboBox.getValue().getId() : null);
        });

        seasonComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateMatchdays();
            loadTeamsForCompetitionAndSeason(competitionComboBox.getValue() != null ? competitionComboBox.getValue().getId() : null, newVal != null ? newVal.getId() : null);
        });
        
        // Filtrer les matchs par journée
        matchdayComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                System.out.println("Journée sélectionnée : " + newVal.getId()); // Log pour vérifier
                loadMatchesForMatchday(newVal.getId());
            } else {
                System.out.println("Aucune journée sélectionnée.");
            }
        });

        return grid;
    }

    @SuppressWarnings("unchecked")
    private void setupTableColumns() {
        // Colonne N°
    	TableColumn<Match, Number> rowNumberCol = new TableColumn<>("N°");
    	rowNumberCol.setCellValueFactory(cellData -> {
    	    int rowIndex = matchTable.getItems().indexOf(cellData.getValue()) + 1;
    	    return new ReadOnlyObjectWrapper<>(rowIndex);
    	});
    	rowNumberCol.setPrefWidth(30);

        // Colonne Date/Heure
        TableColumn<Match, ZonedDateTime> matchDateCol = new TableColumn<>("Date/Heure");
        matchDateCol.setCellValueFactory(new PropertyValueFactory<>("matchDate"));
        matchDateCol.setPrefWidth(140);
        matchDateCol.setCellFactory(column -> new TableCell<Match, ZonedDateTime>() {
            @Override
            protected void updateItem(ZonedDateTime date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            }
        });

        // Colonne Équipe à domicile
        TableColumn<Match, UUID> homeTeamCol = new TableColumn<>("Équipe à domicile");
        homeTeamCol.setCellValueFactory(new PropertyValueFactory<>("homeTeamId"));
        homeTeamCol.setPrefWidth(250);
        homeTeamCol.setCellFactory(column -> new TableCell<Match, UUID>() {
            @Override
            protected void updateItem(UUID teamId, boolean empty) {
                super.updateItem(teamId, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ComboBox<Team> comboBox = new ComboBox<>(teams);
                    comboBox.setValue(teams.stream().filter(t -> t.getId().equals(teamId)).findFirst().orElse(null));
                    comboBox.setOnAction(event -> getTableView().getItems().get(getIndex()).setHomeTeamId(comboBox.getValue().getId()));
                    comboBox.setPrefWidth(240);

                    // Affichage direct du nom sans traitement
                    comboBox.setCellFactory(lv -> new ListCell<Team>() {
                        @Override
                        protected void updateItem(Team team, boolean empty) {
                            super.updateItem(team, empty);
                            setText(empty || team == null ? "" : team.getName());
                        }
                    });

                    // Affichage direct du nom sélectionné
                    comboBox.setButtonCell(new ListCell<Team>() {
                        @Override
                        protected void updateItem(Team team, boolean empty) {
                            super.updateItem(team, empty);
                            setText(empty || team == null ? "" : team.getName());
                        }
                    });

                    setGraphic(comboBox);
                }
            }
        });


        // Colonne Score Domicile
        TableColumn<Match, Short> homeScoreCol = new TableColumn<>("Score Domicile");
        homeScoreCol.setCellValueFactory(new PropertyValueFactory<>("homeScore"));
        homeScoreCol.setPrefWidth(70);

        // Configuration de la cellule pour l'affichage et l'édition
        homeScoreCol.setCellFactory(column -> new TableCell<Match, Short>() {
            private final TextField textField = new TextField();
            private final CustomShortStringConverter converter = new CustomShortStringConverter();

            @Override
            protected void updateItem(Short score, boolean empty) {
                super.updateItem(score, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(score == null ? "" : String.valueOf(score));
                }
            }

            @Override
            public void startEdit() {
                if (!isEmpty()) {
                    super.startEdit();
                    // ✅ Utilise le convertisseur pour afficher la valeur
                    textField.setText(converter.toString(getItem()));
                    setText(null);
                    setGraphic(textField);
                    textField.selectAll();

                    // ✅ NOUVEAU : Écouteur pour valider la modification quand on quitte le TextField
                    textField.setOnAction(event -> {
                        try {
                            Short newValue = converter.fromString(textField.getText());
                            commitEdit(newValue);
                        } catch (Exception e) {
                            cancelEdit(); // Annule si la conversion échoue
                        }
                    });

                    // ✅ NOUVEAU : Écouteur pour valider la modification quand on perd le focus
                    textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                        if (!newVal) { // Si on perd le focus
                            try {
                                Short newValue = converter.fromString(textField.getText());
                                commitEdit(newValue);
                            } catch (Exception e) {
                                cancelEdit(); // Annule si la conversion échoue
                            }
                        }
                    });
                }
            }

            
            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem() == null ? "" : String.valueOf(getItem()));
                setGraphic(null);
            }

            @Override
            public void commitEdit(Short newValue) {
                super.commitEdit(newValue);
                setText(newValue == null ? "" : String.valueOf(newValue));
                setGraphic(null);
            }
        });

        // Gestion de l'édition pour la colonne Score Domicile
        homeScoreCol.setOnEditCommit(event -> {
            Match match = event.getTableView().getItems().get(event.getTablePosition().getRow());
            match.setHomeScore(event.getNewValue());
            matchTable.refresh(); // Rafraîchit la ligne modifiée

            // ✅ NOUVEAU : Appelle updateMatch pour enregistrer en base
            try {
                updateMatch(match);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de mettre à jour le score : " + e.getMessage());
            }
        });

        // Colonne Score Extérieur
        TableColumn<Match, Short> awayScoreCol = new TableColumn<>("Score Extérieur");
        awayScoreCol.setCellValueFactory(new PropertyValueFactory<>("awayScore"));
        awayScoreCol.setPrefWidth(70);
        awayScoreCol.setCellFactory(column -> new TableCell<Match, Short>() {
            private final TextField textField = new TextField();
            private final CustomShortStringConverter converter = new CustomShortStringConverter();

            @Override
            protected void updateItem(Short score, boolean empty) {
                super.updateItem(score, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(score == null ? "" : String.valueOf(score)); // Affiche une chaîne vide si le score est null
                }
            }

            @Override
            public void startEdit() {
                if (!isEmpty()) {
                    super.startEdit();
                    // ✅ Utilise le convertisseur pour afficher la valeur
                    textField.setText(converter.toString(getItem()));
                    setText(null);
                    setGraphic(textField);
                    textField.selectAll();
                    

                    // ✅ NOUVEAU : Écouteur pour valider la modification quand on quitte le TextField
                    textField.setOnAction(event -> {
                        try {
                            Short newValue = converter.fromString(textField.getText());
                            commitEdit(newValue);
                        } catch (Exception e) {
                            cancelEdit(); // Annule si la conversion échoue
                        }
                    });

                    // ✅ NOUVEAU : Écouteur pour valider la modification quand on perd le focus
                    textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                        if (!newVal) { // Si on perd le focus
                            try {
                                Short newValue = converter.fromString(textField.getText());
                                commitEdit(newValue);
                            } catch (Exception e) {
                                cancelEdit(); // Annule si la conversion échoue
                            }
                        }
                    });               
                }
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem() == null ? "" : String.valueOf(getItem()));
                setGraphic(null);
            }

            @Override
            public void commitEdit(Short newValue) {
                super.commitEdit(newValue);
                setText(newValue == null ? "" : String.valueOf(newValue));
                setGraphic(null);
            }
        });

        // Gestion de l'édition pour la colonne Score Extérieur
        awayScoreCol.setOnEditCommit(event -> {
            Match match = event.getTableView().getItems().get(event.getTablePosition().getRow());
            match.setAwayScore(event.getNewValue());
            matchTable.refresh(); // Rafraîchit la ligne modifiée

            // ✅ NOUVEAU : Appelle updateMatch pour enregistrer en base
            try {
                updateMatch(match);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de mettre à jour le score : " + e.getMessage());
            }
        });
        
        // Ajoute les colonnes au tableau
        // matchTable.getColumns().addAll(homeScoreCol, awayScoreCol);

        // Active l'édition du tableau
        matchTable.setEditable(true);
        
        // Colonne Équipe à l'extérieur
        TableColumn<Match, UUID> awayTeamCol = new TableColumn<>("Équipe à l'extérieur");
        awayTeamCol.setCellValueFactory(new PropertyValueFactory<>("awayTeamId"));
        awayTeamCol.setPrefWidth(250);
        awayTeamCol.setCellFactory(column -> new TableCell<Match, UUID>() {
            @Override
            protected void updateItem(UUID teamId, boolean empty) {
                super.updateItem(teamId, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ComboBox<Team> comboBox = new ComboBox<>(teams);
                    comboBox.setValue(teams.stream().filter(t -> t.getId().equals(teamId)).findFirst().orElse(null));
                    comboBox.setOnAction(event -> getTableView().getItems().get(getIndex()).setAwayTeamId(comboBox.getValue().getId()));
                    comboBox.setPrefWidth(240);

                    // Affichage direct du nom sans traitement
                    comboBox.setCellFactory(lv -> new ListCell<Team>() {
                        @Override
                        protected void updateItem(Team team, boolean empty) {
                            super.updateItem(team, empty);
                            setText(empty || team == null ? "" : team.getName());
                        }
                    });

                    // Affichage direct du nom sélectionné
                    comboBox.setButtonCell(new ListCell<Team>() {
                        @Override
                        protected void updateItem(Team team, boolean empty) {
                            super.updateItem(team, empty);
                            setText(empty || team == null ? "" : team.getName());
                        }
                    });

                    setGraphic(comboBox);
                }
            }
        });

     // Colonne pour le bouton de modification (icône de crayon)
        TableColumn<Match, Void> editCol = new TableColumn<>("Modifier");
        editCol.setCellFactory(param -> new TableCell<Match, Void>() {
            private final FontIcon editIcon = new FontIcon(FontAwesomeSolid.PENCIL_ALT);

            {
                editIcon.setIconSize(16);
                editIcon.setIconColor(javafx.scene.paint.Color.BLUE);
                editIcon.setOnMouseClicked(event -> {
                    Match match = getTableView().getItems().get(getIndex());
                    showMatchDialog(match); // Appelle la fenêtre de modification
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editIcon);
                }
            }
        });

        // Colonne pour le bouton de suppression (icône de croix)
        TableColumn<Match, Void> deleteCol = new TableColumn<>("Supprimer");
        deleteCol.setCellFactory(param -> new TableCell<Match, Void>() {
            private final FontIcon deleteIcon = new FontIcon(FontAwesomeSolid.TIMES);

            {
                deleteIcon.setIconSize(16);
                deleteIcon.setIconColor(javafx.scene.paint.Color.RED);
                deleteIcon.setOnMouseClicked(event -> {
                    Match match = getTableView().getItems().get(getIndex());
                    showDeleteConfirmation(match); // Affiche une confirmation avant de supprimer
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteIcon);
                }
            }
        });

        // Ajoute les colonnes au tableau
        // matchTable.getColumns().addAll(editCol, deleteCol);
     // ✅ NOUVELLES COLONNES POUR LES CÔTES
     // Colonne Côte Victoire Domicile
     TableColumn<Match, BigDecimal> homeWinOddCol = new TableColumn<>("Côte Dom.");
     homeWinOddCol.setCellValueFactory(cellData -> {
         MatchOdds odds = cellData.getValue().getOdds();
         return new SimpleObjectProperty<>(odds != null ? odds.getHomeWinOdd() : null);
     });
     homeWinOddCol.setPrefWidth(70);
     homeWinOddCol.setCellFactory(column -> new TableCell<Match, BigDecimal>() {
         @Override
         protected void updateItem(BigDecimal value, boolean empty) {
             super.updateItem(value, empty);
             if (empty || value == null) {
                 setText("");
             } else {
                 setText(String.format("%.2f", value)); // Affiche avec 2 décimales
             }
         }
     });

     // Colonne Côte Match Nul
     TableColumn<Match, BigDecimal> drawOddCol = new TableColumn<>("Côte Nul");
     drawOddCol.setCellValueFactory(cellData -> {
         MatchOdds odds = cellData.getValue().getOdds();
         return new SimpleObjectProperty<>(odds != null ? odds.getDrawOdd() : null);
     });
     drawOddCol.setPrefWidth(70);
     drawOddCol.setCellFactory(column -> new TableCell<Match, BigDecimal>() {
         @Override
         protected void updateItem(BigDecimal value, boolean empty) {
             super.updateItem(value, empty);
             if (empty || value == null) {
                 setText("");
             } else {
                 setText(String.format("%.2f", value));
             }
         }
     });

     // Colonne Côte Victoire Extérieur
     TableColumn<Match, BigDecimal> awayWinOddCol = new TableColumn<>("Côte Ext.");
     awayWinOddCol.setCellValueFactory(cellData -> {
         MatchOdds odds = cellData.getValue().getOdds();
         return new SimpleObjectProperty<>(odds != null ? odds.getAwayWinOdd() : null);
     });
     awayWinOddCol.setPrefWidth(70);
     awayWinOddCol.setCellFactory(column -> new TableCell<Match, BigDecimal>() {
         @Override
         protected void updateItem(BigDecimal value, boolean empty) {
             super.updateItem(value, empty);
             if (empty || value == null) {
                 setText("");
             } else {
                 setText(String.format("%.2f", value));
             }
         }
     });        

        // Ajout des colonnes au tableau
        // matchTable.getColumns().addAll(
        //    rowNumberCol, matchDateCol, homeTeamCol, homeScoreCol, awayScoreCol, awayTeamCol, editCol, deleteCol
        // );

        matchTable.getColumns().addAll(
        	    rowNumberCol, matchDateCol, homeTeamCol, homeScoreCol, awayScoreCol, awayTeamCol,
        	    homeWinOddCol, drawOddCol, awayWinOddCol,  // ✅ Ajoute les colonnes de côtes ici
        	    editCol, deleteCol
        	);
        matchTable.setEditable(true);
        matchTable.setFixedCellSize(35);
        matchTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        matchTable.setItems(matches);
    }

    private HBox setupButtons() {
        Button addButton = new Button("Ajouter un match");
        Button saveButton = new Button("Enregistrer les modifications");
        addButton.setOnAction(event -> showMatchDialog(null));
        saveButton.setOnAction(event -> showAlert(Alert.AlertType.INFORMATION, "Modifications enregistrées", 
        																		"Les modifications ont été sauvegardées."));
        HBox buttonBox = new HBox(20, addButton, saveButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 10, 0));

        return buttonBox;
    }

private void showMatchDialog(Match match) {
    Dialog<Match> dialog = new Dialog<>();
    dialog.setTitle(match == null ? "Ajouter un match" : "Modifier le match");
    dialog.setHeaderText(match == null ? "Créer un nouveau match" : "Modifier le match existant");
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    // Récupère les équipes déjà engagées dans un match pour cette journée
    Set<UUID> teamsAlreadyPlaying = new HashSet<>();
    if (match == null) {
        // Si c'est un nouveau match, vérifie les matchs existants pour cette journée
        for (Match existingMatch : matches) {
            if (existingMatch.getMatchdayId().equals(matchdayComboBox.getValue().getId())) {
                teamsAlreadyPlaying.add(existingMatch.getHomeTeamId());
                teamsAlreadyPlaying.add(existingMatch.getAwayTeamId());
            }
        }
    }

    // Filtre les équipes disponibles
    ObservableList<Team> availableTeams = teams.filtered(team ->
        !teamsAlreadyPlaying.contains(team.getId()) ||
        (match != null && (team.getId().equals(match.getHomeTeamId()) || team.getId().equals(match.getAwayTeamId())))
    );

    // Crée une copie modifiable et trie les équipes par ordre alphabétique
    ObservableList<Team> sortedTeams = FXCollections.observableArrayList(availableTeams);
    FXCollections.sort(sortedTeams, (team1, team2) ->
        team1.getName().compareToIgnoreCase(team2.getName())
    );

    // Création des ComboBox pour les équipes avec les équipes triées
    ComboBox<Team> homeTeamCombo = new ComboBox<>(sortedTeams);
    ComboBox<Team> awayTeamCombo = new ComboBox<>(sortedTeams);

    // Ajoute le StringConverter pour afficher uniquement le nom de l'équipe
    homeTeamCombo.setConverter(new StringConverter<Team>() {
        @Override
        public String toString(Team team) {
            return team != null ? team.getName() : "";
        }

        @Override
        public Team fromString(String string) {
            return null;
        }
    });

    awayTeamCombo.setConverter(new StringConverter<Team>() {
        @Override
        public String toString(Team team) {
            return team != null ? team.getName() : "";
        }

        @Override
        public Team fromString(String string) {
            return null;
        }
    });

    TextField homeScoreField = new TextField();
    TextField awayScoreField = new TextField();

    // Champs pour la date et l'heure
    DatePicker matchDatePicker = new DatePicker();
    TextField matchTimeField = new TextField();
    matchTimeField.setPromptText("HH:mm"); // Exemple : 20:45

    // Configuration des valeurs par défaut si on modifie un match
    if (match != null) {
        homeTeamCombo.setValue(teams.stream().filter(t -> t.getId().equals(match.getHomeTeamId())).findFirst().orElse(null));
        awayTeamCombo.setValue(teams.stream().filter(t -> t.getId().equals(match.getAwayTeamId())).findFirst().orElse(null));
        // Gestion des scores null
        homeScoreField.setText(match.getHomeScore() != null ? String.valueOf(match.getHomeScore()) : "");
        awayScoreField.setText(match.getAwayScore() != null ? String.valueOf(match.getAwayScore()) : "");

        // Remplir le DatePicker et le champ d'heure
        matchDatePicker.setValue(match.getMatchDate().toLocalDate());
        matchTimeField.setText(match.getMatchDate().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    // Mise en page
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));
    grid.add(new Label("Équipe à domicile:"), 0, 0);
    grid.add(homeTeamCombo, 1, 0);
    grid.add(new Label("Équipe à l'extérieur:"), 0, 1);
    grid.add(awayTeamCombo, 1, 1);
    grid.add(new Label("Score équipe à domicile:"), 0, 2);
    grid.add(homeScoreField, 1, 2);
    grid.add(new Label("Score équipe à l'extérieur:"), 0, 3);
    grid.add(awayScoreField, 1, 3);
    grid.add(new Label("Date:"), 0, 4);
    grid.add(matchDatePicker, 1, 4);
    grid.add(new Label("Heure (HH:mm):"), 0, 5);
    grid.add(matchTimeField, 1, 5);

    dialog.getDialogPane().setContent(grid);

    // Conversion du résultat
    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == ButtonType.OK) {
            try {
                LocalDate date = matchDatePicker.getValue();
                if (date == null) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une date.");
                    return null;
                }

                String timeText = matchTimeField.getText().trim();
                if (!DateTimeUtils.isValidTimeFormat(timeText)) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Format d'heure invalide. Utilisez HH:mm (ex: 19:55).");
                    return null;
                }

                String[] timeParts = timeText.split(":");
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                ZonedDateTime matchDateTime = DateTimeUtils.combineDateAndTime(date, timeText);

                // Vérification des équipes sélectionnées
                if (homeTeamCombo.getValue() == null || awayTeamCombo.getValue() == null) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une équipe à domicile et une équipe à l'extérieur.");
                    return null; // Ne ferme pas la boîte de dialog
                }

                // Vérification que les équipes sont différentes
                if (homeTeamCombo.getValue().getId().equals(awayTeamCombo.getValue().getId())) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Les équipes à domicile et à l'extérieur doivent être différentes.");
                    return null; // Ne ferme pas la boîte de dialog
                }

                // Conversion des scores (null si vide)
                Short homeScore = homeScoreField.getText().isEmpty() ? null : Short.parseShort(homeScoreField.getText());
                Short awayScore = awayScoreField.getText().isEmpty() ? null : Short.parseShort(awayScoreField.getText());

                // Création du ZonedDateTime
                LocalTime time = LocalTime.of(hour, minute);
                ZonedDateTime zonedDateTime = ZonedDateTime.of(date, time, ZoneId.systemDefault());

                return new Match(
                    match != null ? match.getId() : UUID.randomUUID(),
                    seasonComboBox.getValue().getId(),
                    competitionComboBox.getValue().getId(),
                    homeTeamCombo.getValue().getId(),
                    awayTeamCombo.getValue().getId(),
                    zonedDateTime,
                    homeScore,
                    awayScore,
                    matchdayComboBox.getValue().getId()
                );
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Valeur numérique invalide dans l'heure ou le score. Vérifiez les champs.");
                return null; // Ne ferme pas la boîte de dialog
            } catch (DateTimeParseException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Heure invalide : " + e.getMessage());
                return null; // Ne ferme pas la boîte de dialog
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur inattendue : " + e.getMessage());
                return null; // Ne ferme pas la boîte de dialog
            }
        }
        return null; // Retourne null si l'utilisateur annule
    });

    // Affiche la boîte de dialogue et gère le résultat
    Optional<Match> result = dialog.showAndWait();
    result.ifPresent(newMatch -> {
        if (newMatch != null) {
            if (match == null) {
                matches.add(newMatch);
                System.out.println("Ajout d'un nouveau match : " + newMatch);
                System.out.println("Nombre de matchs après ajout : " + matches.size());
                saveMatch(newMatch);
            } else {
                matches.set(matches.indexOf(match), newMatch);
                System.out.println("Mise à jour d'un match : " + newMatch);
                updateMatch(newMatch);
            }
        }
    });
}

    // Méthode pour afficher une alerte
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ✅ Méthode déplacée à l'intérieur de la classe
    private void updateMatchdays() {
        if (competitionComboBox.getValue() != null && seasonComboBox.getValue() != null) {
            try {
                MatchdayDAO matchdayDAO = new MatchdayDAO();
                List<Matchday> loadedMatchdays = matchdayDAO.getMatchdaysByCompetitionAndSeason(
                    competitionComboBox.getValue().getId(),
                    seasonComboBox.getValue().getId()
                );
                matchdays.setAll(loadedMatchdays);
                System.out.println("Journées chargées : " + matchdays.size()); // Log pour vérifier
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les journées : " + e.getMessage());
            }
        } else {
            matchdays.clear();
            System.out.println("Aucune compétition ou saison sélectionnée.");
        }
    }

    // Méthode pour charger les matchs d'une journée
    private void loadMatchesForMatchday(UUID matchdayId) {
        if (matchdayId != null) {
            try {
                MatchDAO matchDAO = new MatchDAO();
                List<Match> loadedMatches = matchDAO.getMatchesByMatchday(matchdayId);
                matches.setAll(loadedMatches);
                System.out.println("Matchs chargés : " + loadedMatches.size());
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les matchs : " + e.getMessage());
            }
        } else {
            matches.clear();
        }
    }
    
    // Méthode pour charger les équipes associées à une compétition et une saison
    private void loadTeamsForCompetitionAndSeason(UUID competitionId, UUID seasonId) {
        if (competitionId != null && seasonId != null) {
            try {
                TeamCompetitionSeasonDAO teamCompetitionSeasonDAO = new TeamCompetitionSeasonDAO();
                List<UUID> teamIds = teamCompetitionSeasonDAO.getTeamIdsByCompetitionAndSeason(competitionId, seasonId);
                TeamDAO teamDAO = new TeamDAO();
                List<Team> loadedTeams = new ArrayList<>();
                for (UUID teamId : teamIds) {
                    Team team = teamDAO.getTeamById(teamId);
                    if (team != null) {
                        loadedTeams.add(team);
                    }
                }
                teams.setAll(loadedTeams);
                System.out.println("Équipes chargées : " + loadedTeams.size());
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les équipes : " + e.getMessage());
            }
        } else {
            teams.clear();
        }
    }

    private void saveMatch(Match match) {
        String sql = "INSERT INTO match (id, season_id, competition_id, home_team_id, away_team_id, match_date, home_score, away_score, matchday_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getSessionConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, match.getId());
            stmt.setObject(2, match.getSeasonId());
            stmt.setObject(3, match.getCompetitionId());
            stmt.setObject(4, match.getHomeTeamId());
            stmt.setObject(5, match.getAwayTeamId());
            stmt.setTimestamp(6, java.sql.Timestamp.from(match.getMatchDate().toInstant()));  // Conversion en Timestamp
            stmt.setObject(7, match.getHomeScore());
            stmt.setObject(8, match.getAwayScore());
            stmt.setObject(9, match.getMatchdayId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'enregistrement du match : " + e.getMessage());
        }
    }


private void updateMatch(Match match) {
    String sql = "UPDATE match SET season_id = ?, competition_id = ?, home_team_id = ?, away_team_id = ?, "
               + "match_date = ?, home_score = ?, away_score = ?, matchday_id = ? WHERE id = ?";

    Connection conn = DatabaseConnection.getSessionConnection(); // ✅ Pas de try-with-resources ici
    try (PreparedStatement stmt = conn.prepareStatement(sql)) { // ✅ try-with-resources uniquement pour PreparedStatement
        stmt.setObject(1, match.getSeasonId());
        stmt.setObject(2, match.getCompetitionId());
        stmt.setObject(3, match.getHomeTeamId());
        stmt.setObject(4, match.getAwayTeamId());
        stmt.setTimestamp(5, java.sql.Timestamp.from(match.getMatchDate().toInstant()));
        stmt.setObject(6, match.getHomeScore());
        stmt.setObject(7, match.getAwayScore());
        stmt.setObject(8, match.getMatchdayId());
        stmt.setObject(9, match.getId());

        stmt.executeUpdate();
        System.out.println("Match mis à jour avec succès !");
    } catch (SQLException e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour du match : " + e.getMessage());
    }
    // ❌ PAS de conn.close() ici ! La connexion reste ouverte pour la session
}
    private void showDeleteConfirmation(Match match) {
        // Implémente la logique pour afficher une confirmation de suppression
        // Par exemple, tu pourrais utiliser une boîte de dialogue pour confirmer la suppression

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le match");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce match ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            // Logique pour supprimer le match
            // Par exemple : deleteMatch(match);
        }
    }

}

//Dans MatchManagementView
class CustomShortStringConverter extends StringConverter<Short> {
    @Override
    public String toString(Short value) {
        // Affiche une chaîne vide si le score est null
        return value != null ? String.valueOf(value) : "";
    }

    @Override
    public Short fromString(String string) {
        // Retourne null si la chaîne est vide
        return string.isEmpty() ? null : Short.parseShort(string);
    }
}




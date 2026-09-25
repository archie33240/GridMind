package com.kad.model;

import java.time.ZonedDateTime;
import java.util.UUID;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class GridMindMatch {
    private UUID id;
    private UUID gridId;
    private UUID matchId;
    private int lineNumber;    
    private String homeTeam;  // <-- Ajoute ce champ
    private String awayTeam;  // <-- Ajoute ce champ    
    private ZonedDateTime matchDate;  // Date du match    
    private Short homeScore;  // ✅ Type Short pour smallint
    private Short awayScore;  // ✅ Type Short pour smallint
    // Utilisation de DoubleProperty pour les côtes
    private final DoubleProperty homeWinOdd = new SimpleDoubleProperty(1.0);
    private final DoubleProperty drawOdd = new SimpleDoubleProperty(1.0);
    private final DoubleProperty awayWinOdd = new SimpleDoubleProperty(1.0);
    
    private String result;  // '1', 'N', ou '2'

    // Constructeur par défaut (pour la compatibilité)
    public GridMindMatch() {
        this.id = null; // ou UUID.randomUUID() si tu préfères
    }
    
    // Constructeur minimal pour la génération des grilles : GridMindSimpleGridGenerator
    public GridMindMatch(UUID id) {
        this.id = id;
    }
    
    // Getters et Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getGridId() { return gridId; }
    public void setGridId(UUID gridId) { this.gridId = gridId; }

    public UUID getMatchId() { return matchId; }
    public void setMatchId(UUID matchId) { this.matchId = matchId; }

    public int getLineNumber() { return lineNumber; }
    public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }

    public String getHomeTeam() { return homeTeam; }
    public void setHomeTeam(String homeTeam) { this.homeTeam = homeTeam; }

    public String getAwayTeam() { return awayTeam; }
    public void setAwayTeam(String awayTeam) { this.awayTeam = awayTeam; }    

    public ZonedDateTime getMatchDate() { return matchDate; }
    public void setMatchDate(ZonedDateTime matchDate) { this.matchDate = matchDate; }
    
    public Short getHomeScore() { return homeScore; }
    public void setHomeScore(Short homeScore) { this.homeScore = homeScore; }

    public Short getAwayScore() { return awayScore; }
    public void setAwayScore(Short awayScore) { this.awayScore = awayScore; }
    
    public double getHomeWinOdd() { return homeWinOdd.get(); }
    public void setHomeWinOdd(double value) { homeWinOdd.set(value); }
    public DoubleProperty homeWinOddProperty() { return homeWinOdd; }

    public double getDrawOdd() { return drawOdd.get(); }
    public void setDrawOdd(double value) { drawOdd.set(value); }
    public DoubleProperty drawOddProperty() { return drawOdd; }

    public double getAwayWinOdd() { return awayWinOdd.get(); }
    public void setAwayWinOdd(double value) { awayWinOdd.set(value); }
    public DoubleProperty awayWinOddProperty() { return awayWinOdd; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    
    // Méthode pour récupérer le nom de l'équipe visiteuse

}

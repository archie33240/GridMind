package com.kad.model;

import java.util.UUID;

import com.kad.dao.GridMindMatchDAO;

import javafx.beans.property.SimpleBooleanProperty;

public class GridMindMatchPrediction {
    private UUID id;
    private UUID gridMindMatchId;
    private UUID gridMindGridId;
    private UUID gridMindGridPredictionId;
    private SimpleBooleanProperty homeWin = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty draw = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty awayWin = new SimpleBooleanProperty(false);
    private Double homeWinOddOverride;
    private Double drawOddOverride;
    private Double awayWinOddOverride;

    // ✅ Constructeurs, getters et setters ONLY
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getGridMindMatchId() { return gridMindMatchId; }
    public void setGridMindMatchId(UUID gridMindMatchId) { this.gridMindMatchId = gridMindMatchId; }
    public UUID getGridMindGridId() { return gridMindGridId; }
    public void setGridMindGridId(UUID gridMindGridId) { this.gridMindGridId = gridMindGridId; }
    public UUID getGridMindGridPredictionId() { return gridMindGridPredictionId; }
    public void setGridMindGridPredictionId(UUID gridMindGridPredictionId) { this.gridMindGridPredictionId = gridMindGridPredictionId; }

    public boolean isHomeWin() { return homeWin.get(); }
    public void setHomeWin(boolean homeWin) { this.homeWin.set(homeWin); }
    public SimpleBooleanProperty homeWinProperty() { return homeWin; }
    
    public boolean isDraw() { return draw.get(); }
    public void setDraw(boolean draw) { this.draw.set(draw); }
    public SimpleBooleanProperty drawProperty() { return draw; }
    
    public boolean isAwayWin() { return awayWin.get(); }
    public void setAwayWin(boolean awayWin) { this.awayWin.set(awayWin); }
    public SimpleBooleanProperty awayWinProperty() { return awayWin; }

    public Double getHomeWinOddOverride() { return homeWinOddOverride; }
    public void setHomeWinOddOverride(Double homeWinOddOverride) { this.homeWinOddOverride = homeWinOddOverride; }
    public Double getDrawOddOverride() { return drawOddOverride; }
    public void setDrawOddOverride(Double drawOddOverride) { this.drawOddOverride = drawOddOverride; }
    public Double getAwayWinOddOverride() { return awayWinOddOverride; }
    public void setAwayWinOddOverride(Double awayWinOddOverride) { this.awayWinOddOverride = awayWinOddOverride; }
    
    // Méthode pour récupérer le nom de l'équipe à domicile
 // Dans GridMindMatchPrediction
    public String getHomeTeam() {
        GridMindMatch gridMindMatch = GridMindMatchDAO.getGridMindMatchById(gridMindMatchId);
        return gridMindMatch != null ? gridMindMatch.getHomeTeam() : "Inconnu";
    }

    // Méthode pour récupérer le nom de l'équipe visiteuse
    public String getAwayTeam() {
        GridMindMatch gridMindMatch = GridMindMatchDAO.getGridMindMatchById(gridMindMatchId);
        return gridMindMatch != null ? gridMindMatch.getAwayTeam() : "Inconnu";
    }

}
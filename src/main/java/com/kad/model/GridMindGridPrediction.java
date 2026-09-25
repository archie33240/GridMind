package com.kad.model;

import java.time.ZonedDateTime;
import java.util.UUID;

public class GridMindGridPrediction {
    private UUID id;
    private UUID gridMindGridId;
    private String name;
    private int minHomeWin;       // ✅ Ajoutez ces attributs
    private int maxHomeWin;       // ✅
    private int minDraw;          // ✅
    private int maxDraw;          // ✅
    private int minAwayWin;       // ✅
    private int maxAwayWin;       // ✅
    private double minOdd;
    private double maxOdd;
    private String guaranteeType;
    private double guaranteePercentage;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    // ✅ Constructeurs (optionnel)
    public GridMindGridPrediction() {}

    // ✅ Constructeurs, getters et setters ONLY
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getGridMindGridId() { return gridMindGridId; }
    public void setGridMindGridId(UUID gridMindGridId) { this.gridMindGridId = gridMindGridId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    // ✅ Getters/Setters pour les nouveaux attributs
    public int getMinHomeWin() { return minHomeWin; }
    public void setMinHomeWin(int minHomeWin) { this.minHomeWin = minHomeWin; }

    public int getMaxHomeWin() { return maxHomeWin; }
    public void setMaxHomeWin(int maxHomeWin) { this.maxHomeWin = maxHomeWin; }

    public int getMinDraw() { return minDraw; }
    public void setMinDraw(int minDraw) { this.minDraw = minDraw; }

    public int getMaxDraw() { return maxDraw; }
    public void setMaxDraw(int maxDraw) { this.maxDraw = maxDraw; }

    public int getMinAwayWin() { return minAwayWin; }
    public void setMinAwayWin(int minAwayWin) { this.minAwayWin = minAwayWin; }

    public int getMaxAwayWin() { return maxAwayWin; }
    public void setMaxAwayWin(int maxAwayWin) { this.maxAwayWin = maxAwayWin; }
    
    public double getMinOdd() {
        return minOdd <= 0.0 ? 1.0 : minOdd;  // ✅ Valeur par défaut: 1.0
    }

    public void setMinOdd(double minOdd) { this.minOdd = minOdd; }
    
    public double getMaxOdd() {
        return maxOdd <= 0.0 ? 10.0 : maxOdd;  // ✅ Valeur par défaut: 10.0 (exemple)
    }
    
    public void setMaxOdd(double maxOdd) { this.maxOdd = maxOdd; }
    
    public String getGuaranteeType() { return guaranteeType; }
    public void setGuaranteeType(String guaranteeType) { this.guaranteeType = guaranteeType; }
    
    public double getGuaranteePercentage() {
        return guaranteePercentage <= 0.0 ? 100.0 : guaranteePercentage;  // ✅ Valeur par défaut: 100%
    }
    public void setGuaranteePercentage(double guaranteePercentage) { this.guaranteePercentage = guaranteePercentage; }
    
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
}
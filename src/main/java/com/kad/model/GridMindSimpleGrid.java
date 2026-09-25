package com.kad.model;
import java.time.LocalDateTime;
import java.util.UUID;

public class GridMindSimpleGrid {
    private UUID id;
    private UUID gridmindGridPredictionId;
    private String combination;  // Ex: "1N21N2N1"
    private double totalOdds;
    private LocalDateTime createdAt;

    // Constructeur
    public GridMindSimpleGrid(UUID id, UUID gridmindGridPredictionId, String combination, double totalOdds, LocalDateTime createdAt) {
        this.id = id;
        this.gridmindGridPredictionId = gridmindGridPredictionId;
        this.combination = combination;
        this.totalOdds = totalOdds;
        this.createdAt = createdAt;
    }
    
    public GridMindSimpleGrid() {
        // Constructeur vide
    }
    
    // Getters et Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getGridmindGridPredictionId() {
        return gridmindGridPredictionId;
    }

    public void setGridmindGridPredictionId(UUID gridmindGridPredictionId) {
        this.gridmindGridPredictionId = gridmindGridPredictionId;
    }

    public String getCombination() {
        return combination;
    }

    public void setCombination(String combination) {
        this.combination = combination;
    }

    public double getTotalOdds() {
        return totalOdds;
    }

    public void setTotalOdds(double totalOdds) {
        this.totalOdds = totalOdds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "GridMindSimpleGrid{" +
                "id=" + id +
                ", gridmindGridPredictionId=" + gridmindGridPredictionId +
                ", combination='" + combination + '\'' +
                ", totalOdds=" + totalOdds +
                ", createdAt=" + createdAt +
                '}';
    }
}
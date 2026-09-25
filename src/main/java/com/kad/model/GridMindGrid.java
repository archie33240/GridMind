package com.kad.model;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

public class GridMindGrid {
    private UUID id;
    private String name;
    private int gridNumber;
    private ZonedDateTime playDeadline;
    private int numberOfMatches;
    private String flashcodePath;
    private GridType gridType; // ✅ Utilise com.kad.model.GridType (et non un nested enum)
    private String description;
    private List<GridMindMatch> matches;

    public GridMindGrid() {
        this.matches = new ArrayList<>();
    }

 // Dans GridMindGrid.java
/*    public boolean isTheoretical() {
        return this.getGridType() == GridType.THEORETICAL;  // ✅ Supposons que vous avez un enum GridType
        // OU
        return this.getParentGridId() == null;  // ✅ Si les grilles théoriques n'ont pas de parent
    } */
    
    // Getters et Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getGridNumber() { return gridNumber; }
    public void setGridNumber(int gridNumber) { this.gridNumber = gridNumber; }

    public ZonedDateTime getPlayDeadline() { return playDeadline; }
    public void setPlayDeadline(ZonedDateTime playDeadline) { this.playDeadline = playDeadline; }

    public int getNumberOfMatches() { return numberOfMatches; }
    public void setNumberOfMatches(int numberOfMatches) { this.numberOfMatches = numberOfMatches; }

    public String getFlashcodePath() { return flashcodePath; }
    public void setFlashcodePath(String flashcodePath) { this.flashcodePath = flashcodePath; }

    public GridType getGridType() { return gridType; }
    public void setGridType(GridType gridType) { this.gridType = gridType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<GridMindMatch> getMatches() { return matches; }
    public void setMatches(List<GridMindMatch> matches) { this.matches = matches; }

    // Méthode utile pour ajouter un match
    public void addMatch(GridMindMatch match) {
        this.matches.add(match);
    }
}

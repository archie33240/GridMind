package com.kad.model;

import java.util.UUID;

public class Competition {
    private UUID id;                     // Identifiant unique
    private String name;                 // Nom de la compétition (max 30 caractères)
    private String countryArea;          // Zone géographique (ex: "FR" pour France)
    private CompetitionType competitionType;  // Type de compétition
    private Integer victoryPoints;       // Points pour une victoire
    private Integer drawPoints;          // Points pour un match nul
    private Integer lossPoints;          // Points pour une défaite
    private CompetitionTeamScope teamScope;  // Portée de la compétition (club ou nationale)

    // Constructeur
    public Competition(UUID id, String name, String countryArea, CompetitionType competitionType,
                       Integer victoryPoints, Integer drawPoints, Integer lossPoints, CompetitionTeamScope teamScope) {
        this.id = id;
        this.name = name;
        this.countryArea = countryArea;
        this.competitionType = competitionType;
        this.victoryPoints = victoryPoints;
        this.drawPoints = drawPoints;
        this.lossPoints = lossPoints;
        this.teamScope = teamScope;
    }

    // Getters et Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountryArea() {
        return countryArea;
    }

    public void setCountryArea(String countryArea) {
        this.countryArea = countryArea;
    }

    public CompetitionType getCompetitionType() {
        return competitionType;
    }

    public void setCompetitionType(CompetitionType competitionType) {
        this.competitionType = competitionType;
    }

    public Integer getVictoryPoints() {
        return victoryPoints;
    }

    public void setVictoryPoints(Integer victoryPoints) {
        this.victoryPoints = victoryPoints;
    }

    public Integer getDrawPoints() {
        return drawPoints;
    }

    public void setDrawPoints(Integer drawPoints) {
        this.drawPoints = drawPoints;
    }

    public Integer getLossPoints() {
        return lossPoints;
    }

    public void setLossPoints(Integer lossPoints) {
        this.lossPoints = lossPoints;
    }

    public CompetitionTeamScope getTeamScope() {
        return teamScope;
    }

    public void setTeamScope(CompetitionTeamScope teamScope) {
        this.teamScope = teamScope;
    }

    // Méthode toString pour afficher les informations
    @Override
    public String toString() {
        return "Competition{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", countryArea='" + countryArea + '\'' +
                ", competitionType=" + competitionType +
                ", victoryPoints=" + victoryPoints +
                ", drawPoints=" + drawPoints +
                ", lossPoints=" + lossPoints +
                ", teamScope=" + teamScope +
                '}';
    }
}

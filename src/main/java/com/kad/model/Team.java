package com.kad.model;

import java.util.UUID;

public class Team {
    private UUID id;
    private String name;
    private String countryArea; // ISO code (2 lettres)
    private CompetitionTeamScope teamType;

    // Constructeurs
    public Team() {
        this.teamType = CompetitionTeamScope.club; // Valeur par défaut
    }

    public Team(UUID id, String name, String countryArea, CompetitionTeamScope teamType) {
        this.id = id;
        this.name = name;
        this.countryArea = countryArea;
        this.teamType = teamType;
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
        if (name.matches(".*[ÃÅÄÂÊØ].*")) {
            throw new IllegalArgumentException("Nom d'équipe invalide : caractères non autorisés.");
        }
        this.name = name;
    }

    public String getCountryArea() {
        return countryArea;
    }

    public void setCountryArea(String countryArea) {
        this.countryArea = countryArea;
    }

    public CompetitionTeamScope getTeamType() {
        return teamType;
    }

    public void setTeamType(CompetitionTeamScope teamType) {
        this.teamType = teamType;
    }

    @Override
    public String toString() {
        return String.format("%s (%s, %s)", name, countryArea, teamType);
    }
}

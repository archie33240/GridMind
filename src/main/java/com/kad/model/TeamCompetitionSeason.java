package com.kad.model;

import java.util.UUID;

public class TeamCompetitionSeason {
    private UUID competitionId; // Clé étrangère vers Competition
    private UUID teamId;        // Clé étrangère vers Team
    private UUID seasonId;      // Clé étrangère vers Season

    // Constructeur par défaut
    public TeamCompetitionSeason() {}

    // Constructeur avec paramètres
    public TeamCompetitionSeason(UUID competitionId, UUID teamId, UUID seasonId) {
        this.competitionId = competitionId;
        this.teamId = teamId;
        this.seasonId = seasonId;
    }

    // Getters et Setters
    public UUID getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(UUID competitionId) {
        this.competitionId = competitionId;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public void setTeamId(UUID teamId) {
        this.teamId = teamId;
    }

    public UUID getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(UUID seasonId) {
        this.seasonId = seasonId;
    }

    @Override
    public String toString() {
        return String.format(
            "TeamCompetitionSeason [competitionId=%s, teamId=%s, seasonId=%s]",
            competitionId, teamId, seasonId
        );
    }
}

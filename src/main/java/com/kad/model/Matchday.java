package com.kad.model;

import java.util.UUID;

public class Matchday {
    private UUID id;
    private UUID competitionId; // Clé étrangère vers Competition
    private UUID seasonId;      // Clé étrangère vers Season
    private short matchdayNumber; // smallint en Java = short

    // Constructeurs
    public Matchday() {}

    public Matchday(UUID id, UUID competitionId, UUID seasonId, short matchdayNumber) {
        this.id = id;
        this.competitionId = competitionId;
        this.seasonId = seasonId;
        this.matchdayNumber = matchdayNumber;
    }

    // Getters et Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(UUID competitionId) {
        this.competitionId = competitionId;
    }

    public UUID getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(UUID seasonId) {
        this.seasonId = seasonId;
    }

    public short getMatchdayNumber() {
        return matchdayNumber;
    }

    public void setMatchdayNumber(short matchdayNumber) {
        this.matchdayNumber = matchdayNumber;
    }

    @Override
    public String toString() {
        return String.format("Journée %d", matchdayNumber);
    }
}

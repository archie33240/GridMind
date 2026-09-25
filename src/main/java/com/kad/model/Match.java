package com.kad.model;

import java.time.ZonedDateTime;
import java.util.UUID;

public class Match {
    private UUID id;
    private UUID seasonId;
    private UUID competitionId;
    private String competitionName;
    private Team homeTeam;    // ✅ Ajoute ce champ
    private Team awayTeam;    // ✅ Ajoute ce champ
    private UUID homeTeamId;
    private String homeTeamName;    
    private UUID awayTeamId;
    private String awayTeamName;    
    private ZonedDateTime matchDate;
    private Short homeScore; // smallint → Short (peut être null)
    private Short awayScore; // smallint → Short (peut être null)
    private String status;   // Exemple : "scheduled", "played", "postponed"
    private UUID matchdayId; // Peut être null
    private MatchOdds odds;

    // Constructeurs
    public Match() {
        this.status = "scheduled"; // Valeur par défaut
    }

    public Match(UUID id, UUID seasonId, UUID competitionId, UUID homeTeamId, UUID awayTeamId,
                 ZonedDateTime matchDate, Short homeScore, Short awayScore, UUID matchdayId) {
        this.id = id;
        this.seasonId = seasonId;
        this.competitionId = competitionId;
        this.homeTeamId = homeTeamId;
        this.awayTeamId = awayTeamId;
        this.matchDate = matchDate;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.matchdayId = matchdayId;
    }

    // Getters et Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(UUID seasonId) {
        this.seasonId = seasonId;
    }

    public UUID getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(UUID competitionId) {
        this.competitionId = competitionId;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }
    // ✅ Getters/Setters pour les IDs 
    public UUID getHomeTeamId() {
        return homeTeamId;
    }

    public void setHomeTeamId(UUID homeTeamId) {
        this.homeTeamId = homeTeamId;
    }
    public UUID getAwayTeamId() {
        return awayTeamId;
    }

    public void setAwayTeamId(UUID awayTeamId) {
        this.awayTeamId = awayTeamId;
    }
    // ✅ Getters/Setters pour les objets Team
    public Team getHomeTeam() { return homeTeam; }
    public void setHomeTeam(Team homeTeam) { this.homeTeam = homeTeam; }

    public Team getAwayTeam() { return awayTeam; }
    public void setAwayTeam(Team awayTeam) { this.awayTeam = awayTeam; }

    // ✅ Méthodes pour récupérer le NOM
    public String getHomeTeamName() { 
    	return homeTeam != null ? homeTeam.getName() : null;
    }
    
    public String getAwayTeamName() {
        return awayTeam != null ? awayTeam.getName() : null;
    }
    
    public ZonedDateTime getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(ZonedDateTime matchDate) {
        this.matchDate = matchDate;
    }

    public Short getHomeScore() {
        return homeScore;
    }

    public void setHomeScore(Short homeScore) {
        this.homeScore = homeScore;
    }

    public Short getAwayScore() {
        return awayScore;
    }

    public void setAwayScore(Short awayScore) {
        this.awayScore = awayScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getMatchdayId() {
        return matchdayId;
    }

    public void setMatchdayId(UUID matchdayId) {
        this.matchdayId = matchdayId;
    }
    public MatchOdds getOdds() { return odds; }
    public void setOdds(MatchOdds odds) { this.odds = odds; }
    
    @Override
    public String toString() {
        return String.format("%s - %s vs %s (%s - %s) à %s",
                competitionName,
                homeTeamName, awayTeamName,
                homeScore != null ? homeScore : " ",
                awayScore != null ? awayScore : " ",
                matchDate);
    }
}

package com.kad.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class MatchOdds {
    private UUID id;
    private UUID matchId;
    private BigDecimal homeWinOdd;
    private BigDecimal drawOdd;
    private BigDecimal awayWinOdd;
    private String source;
    private Instant createdAt;  // Correspond à `timestamp without time zone`

    // Constructeur
    public MatchOdds(UUID matchId, BigDecimal homeWinOdd, BigDecimal drawOdd, BigDecimal awayWinOdd, String source) {
        this.id = UUID.randomUUID();  // ou laisser la base le générer
        this.matchId = matchId;
        this.homeWinOdd = homeWinOdd;
        this.drawOdd = drawOdd;
        this.awayWinOdd = awayWinOdd;
        this.source = source;
        this.createdAt = Instant.now();  // La base mettra `now()` par défaut
    }

    // Getters et Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getMatchId() { return matchId; }
    public void setMatchId(UUID matchId) { this.matchId = matchId; }

    public BigDecimal getHomeWinOdd() { return homeWinOdd; }
    public void setHomeWinOdd(BigDecimal homeWinOdd) { this.homeWinOdd = homeWinOdd; }

    public BigDecimal getDrawOdd() { return drawOdd; }
    public void setDrawOdd(BigDecimal drawOdd) { this.drawOdd = drawOdd; }

    public BigDecimal getAwayWinOdd() { return awayWinOdd; }
    public void setAwayWinOdd(BigDecimal awayWinOdd) { this.awayWinOdd = awayWinOdd; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
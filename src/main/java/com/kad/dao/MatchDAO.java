package com.kad.dao;

import com.kad.model.Match;
import com.kad.model.MatchOdds;
import com.kad.database.DatabaseConnection;
import java.sql.*;
import java.time.ZonedDateTime;
import java.time.ZoneId; // Ajoute cette ligne
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MatchDAO {
    private Connection conn;

    public MatchDAO() throws SQLException {
        this.conn = DatabaseConnection.getSessionConnection();
    }

public List<Match> getMatchesByMatchday(UUID matchdayId) throws SQLException {
    List<Match> matches = new ArrayList<>();
    String sql = "SELECT " +
                 "  m.id AS match_id, " +
                 "  m.season_id, " +
                 "  m.competition_id, " +
                 "  m.home_team_id, " +
                 "  m.away_team_id, " +
                 "  m.match_date, " +
                 "  m.home_score, " +
                 "  m.away_score, " +
//                 "  m.status, " +
                 "  m.matchday_id, " +
                 "  mo.id AS odds_id, " +
                 "  mo.home_win_odd, " +
                 "  mo.draw_odd, " +
                 "  mo.away_win_odd, " +
                 "  mo.source, " +
                 "  mo.created_at " +
                 "FROM match m " +
                 "LEFT JOIN match_odds mo ON m.id = mo.match_id " +
                 "WHERE m.matchday_id = ? " +
                 "ORDER BY m.match_date";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setObject(1, matchdayId);
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                // Conversion de Timestamp en ZonedDateTime
                Timestamp timestamp = rs.getTimestamp("match_date");
                ZonedDateTime matchDate = timestamp != null ? timestamp.toInstant().atZone(ZoneId.systemDefault()) : null;

                // ✅ Gestion universelle des scores (Integer → Short)
                Object homeScoreObj = rs.getObject("home_score");
                Object awayScoreObj = rs.getObject("away_score");
                Short homeScore = homeScoreObj != null ? ((Number) homeScoreObj).shortValue() : null;
                Short awayScore = awayScoreObj != null ? ((Number) awayScoreObj).shortValue() : null;

                // Log pour vérifier les valeurs
                System.out.println("Scores récupérés : " + homeScore + " - " + awayScore);

                Match match = new Match(
                    (UUID) rs.getObject("match_id"),
                    (UUID) rs.getObject("season_id"),
                    (UUID) rs.getObject("competition_id"),
                    (UUID) rs.getObject("home_team_id"),
                    (UUID) rs.getObject("away_team_id"),
                    matchDate,
                    homeScore,
                    awayScore,
                    // rs.getString("status"),
                    (UUID) rs.getObject("matchday_id")
                );

                // Récupère les côtes si elles existent
                UUID oddsId = (UUID) rs.getObject("odds_id");
                if (oddsId != null) {
                    MatchOdds odds = new MatchOdds(
                        (UUID) rs.getObject("match_id"),
                        rs.getBigDecimal("home_win_odd"),
                        rs.getBigDecimal("draw_odd"),
                        rs.getBigDecimal("away_win_odd"),
                        rs.getString("source")
                    );
                    odds.setId(oddsId);
                    odds.setCreatedAt(rs.getTimestamp("created_at") != null ?
                                      rs.getTimestamp("created_at").toInstant() : null);
                    match.setOdds(odds);
                }

                matches.add(match);
            }
        }
    }
    System.out.println("Nombre de matchs retournés : " + matches.size());
    return matches;
}
}

package com.kad.dao;

import java.sql.*;
import java.util.UUID;

import com.kad.database.DatabaseConnection;
import com.kad.model.MatchOdds;

import java.math.BigDecimal;
import java.time.Instant;

public class MatchOddsDAO {
    private Connection connection;

    public MatchOddsDAO(Connection connection) {
        this.connection = connection;
    }

    public MatchOddsDAO() {
		// TODO Auto-generated constructor stub
	}

	// Sauvegarde ou met à jour les côtes pour un match
    public void saveOrUpdate(MatchOdds odds) throws SQLException {
        String sql = "INSERT INTO match_odds (id, match_id, home_win_odd, draw_odd, away_win_odd, source, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (id) DO UPDATE SET " +
                     "home_win_odd = EXCLUDED.home_win_odd, " +
                     "draw_odd = EXCLUDED.draw_odd, " +
                     "away_win_odd = EXCLUDED.away_win_odd, " +
                     "source = EXCLUDED.source";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, odds.getId());
            stmt.setObject(2, odds.getMatchId());
            stmt.setBigDecimal(3, odds.getHomeWinOdd());
            stmt.setBigDecimal(4, odds.getDrawOdd());
            stmt.setBigDecimal(5, odds.getAwayWinOdd());
            stmt.setString(6, odds.getSource());
            stmt.setTimestamp(7, Timestamp.from(odds.getCreatedAt()));
            stmt.executeUpdate();
        }
    }

// Récupère les côtes d'un match
public MatchOdds getByMatchId(UUID matchId) throws SQLException {
    System.out.println("🔹 [DEBUG] MatchOddsDAO.getByMatchId 1.0 appelé avec matchId : " + matchId);  // ✅ Log
    String sql = "SELECT * FROM match_odds WHERE match_id = ?";

    // ✅ Récupère la connexion globale (ne pas la fermer ici)
    Connection conn = DatabaseConnection.getSessionConnection();

        // ✅ Vérifie que la connexion est ouverte
        if (conn == null || conn.isClosed()) {
            System.err.println("❌ [DEBUG] MatchOddsDAO.getByMatchId 1.1 La connexion est fermée ou null dans MatchOddsDAO.getByMatchId !");
            throw new SQLException("MatchOddsDAO.getByMatchId 1.2 La connexion à la base de données est fermée.");
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, matchId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal homeWinOdd = rs.getBigDecimal("home_win_odd");
                    BigDecimal drawOdd = rs.getBigDecimal("draw_odd");
                    BigDecimal awayWinOdd = rs.getBigDecimal("away_win_odd");
                    String source = rs.getString("source");

                    MatchOdds odds = new MatchOdds(
                        rs.getObject("match_id", UUID.class),
                        homeWinOdd,
                        drawOdd,
                        awayWinOdd,
                        source
                    );
                    odds.setId(rs.getObject("id", UUID.class));
                    if (rs.getTimestamp("created_at") != null) {
                        odds.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                    }
                    return odds;
                } else {
                    System.err.println("❌ [DEBUG] MatchOddsDAO.getByMatchId 2.0 Aucune côte trouvée pour matchId : " + matchId);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ [DEBUG] MatchOddsDAO.getByMatchId 3.0 Erreur SQL dans MatchOddsDAO.getByMatchId : " + e.getMessage());
            throw e;  // Relance l'exception pour que le contrôleur puisse la gérer
        }

        return null;  // Retourne null si aucune côte n'est trouvée
    }

    // Supprime les côtes d'un match
    public void deleteByMatchId(UUID matchId) throws SQLException {
        String sql = "DELETE FROM match_odds WHERE match_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, matchId);
            stmt.executeUpdate();
        }
    }
}
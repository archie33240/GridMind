package com.kad.dao;

import com.kad.model.Matchday;
import com.kad.database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MatchdayDAO {
    private Connection conn;

    public MatchdayDAO() throws SQLException {
        this.conn = DatabaseConnection.getSessionConnection();
    }

    // Méthode pour récupérer les journées par compétition et saison
    public List<Matchday> getMatchdaysByCompetitionAndSeason(UUID competitionId, UUID seasonId) throws SQLException {
        List<Matchday> matchdays = new ArrayList<>();
        String sql = "SELECT id, competition_id, season_id, matchday_number " +
                     "FROM matchday " +
                     "WHERE competition_id = ? AND season_id = ? " +
                     "ORDER BY matchday_number";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, competitionId);
            stmt.setObject(2, seasonId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Matchday matchday = new Matchday(
                        (UUID) rs.getObject("id"),
                        (UUID) rs.getObject("competition_id"),
                        (UUID) rs.getObject("season_id"),
                        rs.getShort("matchday_number")
                    );
                    matchdays.add(matchday);
                }
            }
        }
        return matchdays;
    }
}

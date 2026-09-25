package com.kad.dao;

import com.kad.database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TeamCompetitionSeasonDAO {
    private Connection conn;

    public TeamCompetitionSeasonDAO() throws SQLException {
        this.conn = DatabaseConnection.getSessionConnection();
    }

    public List<UUID> getTeamIdsByCompetitionAndSeason(UUID competitionId, UUID seasonId) throws SQLException {
        List<UUID> teamIds = new ArrayList<>();
        String sql = "SELECT team_id FROM team_cmpttn_season WHERE competition_id = ? AND season_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, competitionId);
            stmt.setObject(2, seasonId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    teamIds.add((UUID) rs.getObject("team_id"));
                }
            }
        }
        return teamIds;
    }

}

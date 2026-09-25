package com.kad.dao;

import com.kad.model.Team;
import com.kad.model.CompetitionTeamScope;
import com.kad.database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TeamDAO {
    private Connection conn;

    public TeamDAO() throws SQLException {
        this.conn = DatabaseConnection.getSessionConnection();
    }

    public Team getTeamById(UUID teamId) throws SQLException {
        String sql = "SELECT id, name, country_area, team_type FROM team WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, teamId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String teamTypeStr = rs.getString("team_type");
                    CompetitionTeamScope teamType = teamTypeStr != null ?
                        CompetitionTeamScope.valueOf(teamTypeStr) :
                        CompetitionTeamScope.club; // Valeur par défaut

                    return new Team(
                        (UUID) rs.getObject("id"),
                        rs.getString("name"), // Utilise le nom tel quel
                        rs.getString("country_area"),
                        teamType
                    );
                }
            }
        }
        return null;
    }
   
    public List<Team> getAllTeams() throws SQLException {
        List<Team> teams = new ArrayList<>();
        String sql = "SELECT id, name, country_area, team_type FROM team";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String teamTypeStr = rs.getString("team_type");
                    CompetitionTeamScope teamType = teamTypeStr != null ?
                        CompetitionTeamScope.valueOf(teamTypeStr) :
                        CompetitionTeamScope.club;

                    Team team = new Team(
                        (UUID) rs.getObject("id"),
                        rs.getString("name"), // Utilise le nom tel quel
                        rs.getString("country_area"),
                        teamType
                    );
                    teams.add(team);
                }
            }
        }
        return teams;
    }

}

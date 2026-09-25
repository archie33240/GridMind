package com.kad.dao;

import com.kad.model.Competition;
import com.kad.model.CompetitionTeamScope;
import com.kad.model.CompetitionType;
import com.kad.database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CompetitionDAO {
    private Connection conn;

    // Constructeur par défaut (utilise DatabaseConnection)
    public CompetitionDAO() throws SQLException {
        this.conn = DatabaseConnection.getSessionConnection(); // ✅ Utilisation de DatabaseConnection
    }

    // Ajouter une compétition
    public void addCompetition(Competition competition) throws SQLException {
        String sql = "INSERT INTO competition (id, name, country_area, competition_type, victory_points, draw_points, loss_points, team_scope) " +
                     "VALUES (?, ?, ?, ?::competition_type, ?, ?, ?, ?::competition_team_scope)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, competition.getId());
            stmt.setString(2, competition.getName());
            stmt.setString(3, competition.getCountryArea());
            stmt.setString(4, competition.getCompetitionType().name());
            stmt.setInt(5, competition.getVictoryPoints());
            stmt.setInt(6, competition.getDrawPoints());
            stmt.setInt(7, competition.getLossPoints());
            stmt.setString(8, competition.getTeamScope().name());
            stmt.executeUpdate();
        }
    }

    // Lister toutes les compétitions
    public List<Competition> getAllCompetitions() throws SQLException {
        List<Competition> competitions = new ArrayList<>();
        String sql = "SELECT id, name, country_area, competition_type, victory_points, draw_points, loss_points, team_scope FROM competition";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Competition competition = new Competition(
                    (UUID) rs.getObject("id"),
                    rs.getString("name"),
                    rs.getString("country_area"),
                    CompetitionType.valueOf(rs.getString("competition_type")),
                    rs.getInt("victory_points"),
                    rs.getInt("draw_points"),
                    rs.getInt("loss_points"),
                    CompetitionTeamScope.valueOf(rs.getString("team_scope"))
                );
                competitions.add(competition);
            }
        }
        return competitions;
    }

    // Compétitions par secteur géographique
 // Exemple de méthode correcte dans CompetitionDAO
    public List<Competition> getCompetitionsByCountryArea(String countryAreaId) throws SQLException {
        List<Competition> competitions = new ArrayList<>();
        String sql = "SELECT id, name, country_area, competition_type, victory_points, draw_points, loss_points, team_scope " +
                     "FROM competition WHERE country_area = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, countryAreaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Competition competition = new Competition(
                        (UUID) rs.getObject("id"),
                        rs.getString("name"),
                        rs.getString("country_area"),
                        CompetitionType.valueOf(rs.getString("competition_type")),
                        rs.getInt("victory_points"),
                        rs.getInt("draw_points"),
                        rs.getInt("loss_points"),
                        CompetitionTeamScope.valueOf(rs.getString("team_scope"))
                    );
                    competitions.add(competition);
                }
            }
        }
        return competitions;
    }
 
    // Autres méthodes (update, delete, findById, etc.) à ajouter selon vos besoins
}

package com.kad.dao;

import com.kad.model.Season;
import com.kad.database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SeasonDAO {
    private Connection conn;

    // Constructeur par défaut (gère la connexion)
    public SeasonDAO() throws SQLException {
        this.conn = DatabaseConnection.getSessionConnection();
    }

    // Récupérer toutes les saisons
    public List<Season> getAllSeasons() throws SQLException {
        List<Season> seasons = new ArrayList<>();
        String sql = "SELECT id, start_date, end_date FROM season ORDER BY start_date DESC";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                seasons.add(new Season(
                    (UUID) rs.getObject("id"),
                    rs.getDate("start_date").toLocalDate(),
                    rs.getDate("end_date").toLocalDate()
                ));
            }
        }
        return seasons;
    }
}

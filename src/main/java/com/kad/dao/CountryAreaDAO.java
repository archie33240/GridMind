package com.kad.dao;

import com.kad.model.CountryArea;
import com.kad.model.AreaType;
import com.kad.database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CountryAreaDAO {
    private Connection conn;

    // Constructeur par défaut (utilise DatabaseConnection)
    public CountryAreaDAO() throws SQLException {
        this.conn = DatabaseConnection.getSessionConnection(); // ✅ Utilisation de DatabaseConnection
    }

    // Ajouter une zone
    public void ajouterCountryArea(CountryArea area) throws SQLException {
        String sql = "INSERT INTO country_area (id, name, area_type, parent_id) VALUES (?, ?, ?::area_type, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, area.getId());
            stmt.setString(2, area.getName());
            stmt.setString(3, area.getAreaType().name().toLowerCase());
            stmt.setString(4, area.getParentId());
            stmt.executeUpdate();
        }
    }

    // Lister toutes les zones
    public List<CountryArea> getAllCountryAreas() throws SQLException {
        List<CountryArea> areas = new ArrayList<>();
        String sql = "SELECT id, name, area_type, parent_id FROM country_area ORDER BY id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                areas.add(new CountryArea(
                    rs.getString("id"),
                    rs.getString("name"),
                    AreaType.valueOf(rs.getString("area_type").toUpperCase()),
                    rs.getString("parent_id")
                ));
            }
        }

        // Trier pour mettre FR, DE, IT, ES, PT en premier
        areas.sort((a1, a2) -> {
            List<String> priorityIds = List.of("FR", "DE", "IT", "ES", "PT");
            boolean a1Priority = priorityIds.contains(a1.getId());
            boolean a2Priority = priorityIds.contains(a2.getId());
            if (a1Priority && !a2Priority) return -1;
            if (!a1Priority && a2Priority) return 1;
            return a1.getName().compareTo(a2.getName());
        });

        return areas;
    }
    // Autres méthodes (update, delete, findById, etc.) à ajouter selon vos besoins
}

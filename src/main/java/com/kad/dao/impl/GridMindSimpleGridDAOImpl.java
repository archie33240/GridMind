package com.kad.dao.impl;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.kad.dao.GridMindSimpleGridDAO;
import com.kad.database.DatabaseConnection;
import com.kad.model.GridMindSimpleGrid;

public class GridMindSimpleGridDAOImpl implements GridMindSimpleGridDAO {
    private Connection connection;

    // Constructeur par défaut (crée une nouvelle connexion)
    // public GridMindSimpleGridDAOImpl() throws SQLException {
    public GridMindSimpleGridDAOImpl() {
        this.connection = DatabaseConnection.getSessionConnection();
    }

    // Constructeur avec une connexion existante (pour la cohérence)
    public GridMindSimpleGridDAOImpl(Connection conn) {
        this.connection = conn;
    }

    @Override
    public void save(GridMindSimpleGrid simpleGrid, Connection conn) throws SQLException {
        String sql = "INSERT INTO gridmind_simple_grid (id, gridmind_grid_prediction_id, combination, total_odds, created_at) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, simpleGrid.getId());
            stmt.setObject(2, simpleGrid.getGridmindGridPredictionId());
            stmt.setString(3, simpleGrid.getCombination());
            stmt.setDouble(4, simpleGrid.getTotalOdds());
            stmt.setObject(5, simpleGrid.getCreatedAt());

            stmt.executeUpdate();
        }
    }

    @Override
    public void update(GridMindSimpleGrid simpleGrid, Connection conn) throws SQLException {
        String sql = "UPDATE gridmind_simple_grid SET gridmind_grid_prediction_id = ?, combination = ?, total_odds = ? " +
                     "WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, simpleGrid.getGridmindGridPredictionId());
            stmt.setString(2, simpleGrid.getCombination());
            stmt.setDouble(3, simpleGrid.getTotalOdds());
            stmt.setObject(4, simpleGrid.getId());

            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(UUID id, Connection conn) throws SQLException {
        String sql = "DELETE FROM gridmind_simple_grid WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteGridsByPredictionId(UUID gridPredictionId) throws SQLException {
        String sql = "DELETE FROM gridmind_simple_grid WHERE gridmind_grid_prediction_id = ?";
        Connection conn = DatabaseConnection.getSessionConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, gridPredictionId);
            stmt.executeUpdate(); // ✅ Exécute la suppression
        }
    }
    
    @Override
    public GridMindSimpleGrid getById(UUID id, Connection conn) throws SQLException {
        String sql = "SELECT id, gridmind_grid_prediction_id, combination, total_odds, created_at " +
                     "FROM gridmind_simple_grid WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToSimpleGrid(rs);
            }
        }
        return null;
    }

    @Override
    public List<GridMindSimpleGrid> getSimpleGridsForPrediction(UUID gridmindGridPredictionId, Connection conn) throws SQLException {
        List<GridMindSimpleGrid> simpleGrids = new ArrayList<>();
        String sql = "SELECT id, gridmind_grid_prediction_id, combination, total_odds, created_at " +
                     "FROM gridmind_simple_grid WHERE gridmind_grid_prediction_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, gridmindGridPredictionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                simpleGrids.add(mapResultSetToSimpleGrid(rs));
            }
        }
        return simpleGrids;
    }

    @Override
    public List<GridMindSimpleGrid> getSimpleGridsByOddsRange(double minOdds, double maxOdds, Connection conn) throws SQLException {
        List<GridMindSimpleGrid> simpleGrids = new ArrayList<>();
        String sql = "SELECT id, gridmind_grid_prediction_id, combination, total_odds, created_at " +
                     "FROM gridmind_simple_grid WHERE total_odds BETWEEN ? AND ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, minOdds);
            stmt.setDouble(2, maxOdds);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                simpleGrids.add(mapResultSetToSimpleGrid(rs));
            }
        }
        return simpleGrids;
    }
    
    public List<GridMindSimpleGrid> getAllGeneratedGrids(UUID gridmindGridPredictionId) throws SQLException {
        List<GridMindSimpleGrid> grids = new ArrayList<>();
        String sql = "SELECT * FROM gridmind_simple_grid WHERE gridmind_grid_prediction_id = ? ORDER BY created_at";
        Connection conn = DatabaseConnection.getSessionConnection();
        try (
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, gridmindGridPredictionId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                GridMindSimpleGrid grid = new GridMindSimpleGrid();
                grid.setId((UUID) rs.getObject("id"));
                grid.setGridmindGridPredictionId((UUID) rs.getObject("gridmind_grid_prediction_id"));
                grid.setCombination(rs.getString("combination"));
                grid.setTotalOdds(rs.getDouble("total_odds"));
                grid.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                grids.add(grid);
            }
        }
        return grids;
    }

    // Méthode utilitaire pour mapper un ResultSet à un objet GridMindSimpleGrid
private GridMindSimpleGrid mapResultSetToSimpleGrid(ResultSet rs) throws SQLException {
    UUID id = (UUID) rs.getObject("id");
    UUID gridmindGridPredictionId = (UUID) rs.getObject("gridmind_grid_prediction_id");
    String combination = rs.getString("combination");
    double totalOdds = rs.getDouble("total_odds");

    // ✅ Convertir ZonedDateTime en LocalDateTime
    LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime(); // ✅ Conversion en LocalDateTime

    return new GridMindSimpleGrid(id, gridmindGridPredictionId, combination, totalOdds, createdAt);
}

    @Override
    public int getGeneratedGridsCount(UUID gridmindGridPredictionId) throws SQLException {
        Connection conn = DatabaseConnection.getSessionConnection();
        System.out.println("🔹 [DEBUG] Dans getGeneratedGridsCount - Connexion : " + (conn != null && !conn.isClosed() ? "OUVERTE" : "FERMÉE"));

        if (conn == null || conn.isClosed()) {
            System.err.println("❌ [ERREUR] La connexion est fermée dans getGeneratedGridsCount !");
            throw new SQLException("La connexion est fermée !");
        }

        String sql = "SELECT COUNT(*) AS count FROM gridmind_simple_grid WHERE gridmind_grid_prediction_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, gridmindGridPredictionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }
}
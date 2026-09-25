package com.kad.dao.impl;

import java.sql.*;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.kad.dao.GridMindGridPredictionDAO;  // ✅ Implémentez GridMindGridPredictionDAO
import com.kad.database.DatabaseConnection;
import com.kad.model.GridMindGridPrediction;
import com.kad.model.GridMindMatchPrediction;
import com.kad.model.GridMindSimpleGrid;

public class GridMindGridPredictionDAOImpl implements GridMindGridPredictionDAO {
    private Connection connection;

    public GridMindGridPredictionDAOImpl() throws SQLException {
        this.connection = DatabaseConnection.getSessionConnection();
    }

    public GridMindGridPredictionDAOImpl(Connection conn) {
        this.connection = conn;
    }
    
    @Override
    public UUID getGridMindGridIdByPredictionId(UUID gridPredictionId) throws SQLException {
        String sql = "SELECT gridmind_grid_id FROM gridmind_grid_prediction WHERE id = ?";
        Connection conn = DatabaseConnection.getSessionConnection();
        try (
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, gridPredictionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return (UUID) rs.getObject("gridmind_grid_id");
            }
        }
        return null; // ou lève une exception si nécessaire
    }
    
    @Override
    public void save(GridMindGridPrediction gridPrediction) throws SQLException {
        String sql = "INSERT INTO gridmind_grid_prediction (id, gridmind_grid_id, name, min_home_win, max_home_win, min_draw, max_draw, min_away_win, max_away_win, min_odd, max_odd, guarantee_type, guarantee_percentage) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, gridPrediction.getId());
            stmt.setObject(2, gridPrediction.getGridMindGridId());
            stmt.setString(3, gridPrediction.getName());
            stmt.setInt(4, gridPrediction.getMinHomeWin());
            stmt.setInt(5, gridPrediction.getMaxHomeWin());
            stmt.setInt(6, gridPrediction.getMinDraw());
            stmt.setInt(7, gridPrediction.getMaxDraw());
            stmt.setInt(8, gridPrediction.getMinAwayWin());
            stmt.setInt(9, gridPrediction.getMaxAwayWin());
            stmt.setDouble(10, gridPrediction.getMinOdd());
            stmt.setDouble(11, gridPrediction.getMaxOdd());
            stmt.setString(12, gridPrediction.getGuaranteeType());
            stmt.setDouble(13, gridPrediction.getGuaranteePercentage());
            stmt.executeUpdate();
        }
    }

    @Override
    public void update(GridMindGridPrediction gridPrediction) throws SQLException {
        String sql = "UPDATE gridmind_grid_prediction SET name = ?, min_home_win = ?, max_home_win = ?, min_draw = ?, max_draw = ?, min_away_win = ?, max_away_win = ?, min_odd = ?, max_odd = ?, guarantee_type = ?, guarantee_percentage = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, gridPrediction.getName());
            stmt.setInt(2, gridPrediction.getMinHomeWin());
            stmt.setInt(3, gridPrediction.getMaxHomeWin());
            stmt.setInt(4, gridPrediction.getMinDraw());
            stmt.setInt(5, gridPrediction.getMaxDraw());
            stmt.setInt(6, gridPrediction.getMinAwayWin());
            stmt.setInt(7, gridPrediction.getMaxAwayWin());
            stmt.setDouble(8, gridPrediction.getMinOdd());
            stmt.setDouble(9, gridPrediction.getMaxOdd());
            stmt.setString(10, gridPrediction.getGuaranteeType());
            stmt.setDouble(11, gridPrediction.getGuaranteePercentage());
            stmt.setObject(12, gridPrediction.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public GridMindGridPrediction getPredictionForGrid(UUID gridId) throws SQLException {
        String sql = "SELECT * FROM gridmind_grid_prediction WHERE gridmind_grid_id = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, gridId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                GridMindGridPrediction prediction = new GridMindGridPrediction();
                prediction.setId(rs.getObject("id", UUID.class));
                prediction.setGridMindGridId(rs.getObject("gridmind_grid_id", UUID.class));
                prediction.setName(rs.getString("name"));
                prediction.setMinHomeWin(rs.getInt("min_home_win"));
                prediction.setMaxHomeWin(rs.getInt("max_home_win"));
                prediction.setMinDraw(rs.getInt("min_draw"));
                prediction.setMaxDraw(rs.getInt("max_draw"));
                prediction.setMinAwayWin(rs.getInt("min_away_win"));
                prediction.setMaxAwayWin(rs.getInt("max_away_win"));
                prediction.setMinOdd(rs.getDouble("min_odd"));
                prediction.setMaxOdd(rs.getDouble("max_odd"));
                prediction.setGuaranteeType(rs.getString("guarantee_type"));
                prediction.setGuaranteePercentage(rs.getDouble("guarantee_percentage"));
                return prediction;
            }
        }
        return null;
    }

    @Override
    public GridMindGridPrediction getPredictionById(UUID id) throws SQLException {
        String query = "SELECT * FROM gridmind_grid_prediction WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                GridMindGridPrediction prediction = new GridMindGridPrediction();
                prediction.setId(rs.getObject("id", UUID.class));
                prediction.setGridMindGridId(rs.getObject("gridmind_grid_id", UUID.class));
                prediction.setName(rs.getString("name"));
                // ✅ Chargez TOUTES les propriétés
                prediction.setMinHomeWin(rs.getInt("min_home_win"));
                prediction.setMaxHomeWin(rs.getInt("max_home_win"));
                prediction.setMinDraw(rs.getInt("min_draw"));
                prediction.setMaxDraw(rs.getInt("max_draw"));
                prediction.setMinAwayWin(rs.getInt("min_away_win"));
                prediction.setMaxAwayWin(rs.getInt("max_away_win"));
                prediction.setMinOdd(rs.getDouble("min_odd"));
                prediction.setMaxOdd(rs.getDouble("max_odd"));
                prediction.setGuaranteeType(rs.getString("guarantee_type"));
                prediction.setGuaranteePercentage(rs.getDouble("guarantee_percentage"));
                return prediction;
            }
        }
        return null;
    }
    
    @Override
    public List<GridMindGridPrediction> getAllPredictionsForGrid(UUID gridId) throws SQLException {
        List<GridMindGridPrediction> predictions = new ArrayList<>();
        String sql = "SELECT * FROM gridmind_grid_prediction WHERE gridmind_grid_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, gridId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                GridMindGridPrediction prediction = new GridMindGridPrediction();
                prediction.setId(rs.getObject("id", UUID.class));
                prediction.setGridMindGridId(rs.getObject("gridmind_grid_id", UUID.class));
                prediction.setName(rs.getString("name"));
                prediction.setMinHomeWin(rs.getInt("min_home_win"));
                prediction.setMaxHomeWin(rs.getInt("max_home_win"));
                prediction.setMinDraw(rs.getInt("min_draw"));
                prediction.setMaxDraw(rs.getInt("max_draw"));
                prediction.setMinAwayWin(rs.getInt("min_away_win"));
                prediction.setMaxAwayWin(rs.getInt("max_away_win"));
                prediction.setMinOdd(rs.getDouble("min_odd"));
                prediction.setMaxOdd(rs.getDouble("max_odd"));
                prediction.setGuaranteeType(rs.getString("guarantee_type"));
                prediction.setGuaranteePercentage(rs.getDouble("guarantee_percentage"));
                predictions.add(prediction);
            }
        }
        return predictions;
    }
    
    @Override
    public List<GridMindGridPrediction> getPredictionsForGrid(UUID gridId) throws SQLException {
        List<GridMindGridPrediction> predictions = new ArrayList<>();
        String sql = "SELECT * FROM gridmind_grid_prediction WHERE gridmind_grid_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, gridId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                GridMindGridPrediction prediction = new GridMindGridPrediction();
                prediction.setId(rs.getObject("id", UUID.class));
                prediction.setGridMindGridId(rs.getObject("gridmind_grid_id", UUID.class));
                prediction.setName(rs.getString("name"));
                prediction.setMinHomeWin(rs.getInt("min_home_win"));
                prediction.setMaxHomeWin(rs.getInt("max_home_win"));
                prediction.setMinDraw(rs.getInt("min_draw"));
                prediction.setMaxDraw(rs.getInt("max_draw"));
                prediction.setMinAwayWin(rs.getInt("min_away_win"));
                prediction.setMaxAwayWin(rs.getInt("max_away_win"));
                prediction.setMinOdd(rs.getDouble("min_odd"));
                prediction.setMaxOdd(rs.getDouble("max_odd"));
                prediction.setGuaranteeType(rs.getString("guarantee_type"));
                prediction.setGuaranteePercentage(rs.getDouble("guarantee_percentage"));
                prediction.setCreatedAt(rs.getTimestamp("created_at") != null ?
                    rs.getTimestamp("created_at").toInstant().atZone(ZoneId.systemDefault()) : null);
                prediction.setUpdatedAt(rs.getTimestamp("updated_at") != null ?
                    rs.getTimestamp("updated_at").toInstant().atZone(ZoneId.systemDefault()) : null);

                predictions.add(prediction);
                System.out.println("DEBUG [DAO]: Pronostic trouvé - ID: " + prediction.getId() + ", Nom: " + prediction.getName());
            }
        }
        System.out.println("DEBUG [DAO]: Nombre total de pronostics pour la grille " + gridId + ": " + predictions.size());
        return predictions;
    }

    @Override
    public void delete(UUID id) throws SQLException {
        Connection conn = DatabaseConnection.getSessionConnection(); // ✅ Utilise la connexion de session
        try {
            // ✅ 1. Supprime les feuilles : gridmind_match_prediction
            String deleteMatchPredictionsSql =
                "DELETE FROM gridmind_match_prediction WHERE gridmind_grid_prediction_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteMatchPredictionsSql)) {
                stmt.setObject(1, id);
                int deleted = stmt.executeUpdate();
                System.out.println("🌿 [Feuilles] " + deleted + " pronostics de matchs supprimés pour la grille de pronostic ID: " + id);
            }

            // ✅ 2. Supprime le tronc : gridmind_grid_prediction
            String deleteGridPredictionSql = "DELETE FROM gridmind_grid_prediction WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteGridPredictionSql)) {
                stmt.setObject(1, id);
                int deleted = stmt.executeUpdate();
                System.out.println("🌳 [Tronc] " + deleted + " grille de pronostic supprimée (ID: " + id + ").");
            }

            // ❌ PAS de commit() ici ! La transaction sera validée dans le menu principal.
        } catch (SQLException e) {
            conn.rollback(); // ✅ Annule la transaction en cas d'erreur
            System.err.println("❌ Erreur lors de la suppression de la grille de pronostic : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public GridMindGridPrediction getGridPredictionById(UUID gridPredictionId) {
        String sql = "SELECT * FROM gridmind_grid_prediction WHERE id = ?";
        Connection conn = DatabaseConnection.getSessionConnection();
        
        try (
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, gridPredictionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                GridMindGridPrediction gridPrediction = new GridMindGridPrediction();
                gridPrediction.setId(rs.getObject("id", UUID.class));
                gridPrediction.setGridMindGridId(rs.getObject("gridmind_grid_id", UUID.class)); // ✅ Initialise gridmindGridId
                // Récupère les autres champs de la grille de pronostics
                // Exemple : gridPrediction.setName(rs.getString("name"));
                return gridPrediction;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Retourne null si aucune grille trouvée
    }
    
    @Override
    public double calculateTotalOdds(GridMindSimpleGrid grid, List<GridMindMatchPrediction> matchPredictions) {
        String combination = grid.getCombination();
        double totalOdds = 0.0;

        for (int i = 0; i < combination.length(); i++) {
            char result = combination.charAt(i);
            GridMindMatchPrediction matchPrediction = matchPredictions.get(i);

            // Si le résultat est coché (X), ajoute la côte surchargée correspondante
            if (result == '1' && matchPrediction.getHomeWinOddOverride() != null) {
                totalOdds += matchPrediction.getHomeWinOddOverride();
            } else if (result == 'N' && matchPrediction.getDrawOddOverride() != null) {
                totalOdds += matchPrediction.getDrawOddOverride();
            } else if (result == '2' && matchPrediction.getAwayWinOddOverride() != null) {
                totalOdds += matchPrediction.getAwayWinOddOverride();
            }
        }

        return totalOdds;
    }
}
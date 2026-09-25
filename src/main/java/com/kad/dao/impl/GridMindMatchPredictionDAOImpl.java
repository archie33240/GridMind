package com.kad.dao.impl;

import com.kad.dao.GridMindMatchDAO;
import com.kad.dao.GridMindMatchPredictionDAO;
import com.kad.model.GridMindMatchPrediction;
import com.kad.database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GridMindMatchPredictionDAOImpl implements GridMindMatchPredictionDAO {
    private Connection conn;  // Pour les transactions
    private GridMindMatchDAO gridMindMatchDAO;
    // Constructeur par défaut (auto-commit)
 // Dans GridMindMatchPredictionDAOImpl
    public GridMindMatchPredictionDAOImpl() throws SQLException {
    	this(new GridMindMatchDAO()); // Appelle le constructeur avec une nouvelle instance
    }
    public GridMindMatchPredictionDAOImpl(GridMindMatchDAO gridMindMatchDAO) {
        this.gridMindMatchDAO = gridMindMatchDAO;
    }

    // Constructeur pour les transactions
    public GridMindMatchPredictionDAOImpl(Connection conn, GridMindMatchDAO gridMindMatchDAO) {
        this.conn = conn;
        this.gridMindMatchDAO = gridMindMatchDAO;
    }

    // Setter
    public void setGridMindMatchDAO(GridMindMatchDAO gridMindMatchDAO) {
        this.gridMindMatchDAO = gridMindMatchDAO;
    }
    @Override
    public GridMindMatchPrediction getMatchPredictionById(UUID id) {
        GridMindMatchPrediction prediction = new GridMindMatchPrediction();
        return prediction;
    }
    @Override
    public void save(GridMindMatchPrediction prediction, Connection conn) throws SQLException {
        // ✅ Requête mise à jour avec les nouveaux champs
        String sql = "INSERT INTO gridmind_match_prediction (" +
                     "id, gridmind_match_id, gridmind_grid_prediction_id, gridmind_grid_id, " +
                     "home_win, draw, away_win, home_win_odd_override, draw_odd_override, away_win_odd_override) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection localConn = (conn != null) ? conn : DatabaseConnection.getSessionConnection();
        try (PreparedStatement stmt = localConn.prepareStatement(sql)) {
            stmt.setObject(1, prediction.getId() != null ? prediction.getId() : UUID.randomUUID());
            stmt.setObject(2, prediction.getGridMindMatchId());
            stmt.setObject(3, prediction.getGridMindGridPredictionId()); // ✅ Nouveau champ
            stmt.setObject(4, prediction.getGridMindGridId());           // ✅ Nouveau champ
            stmt.setBoolean(5, prediction.isHomeWin());
            stmt.setBoolean(6, prediction.isDraw());
            stmt.setBoolean(7, prediction.isAwayWin());
            stmt.setDouble(8, prediction.getHomeWinOddOverride());
            stmt.setDouble(9, prediction.getDrawOddOverride());
            stmt.setDouble(10, prediction.getAwayWinOddOverride());
            stmt.executeUpdate();
        }
    }

public List<GridMindMatchPrediction> getPredictionsForPrediction(UUID predictionId) throws SQLException {
    String sql = """
        SELECT mp.*
        FROM gridmind_match_prediction mp
        JOIN gridmind_match gm ON mp.gridmind_match_id = gm.id
        WHERE mp.gridmind_grid_prediction_id = ?
        ORDER BY gm.line_number;  
        """;

    List<GridMindMatchPrediction> predictions = new ArrayList<>();
    Connection conn = DatabaseConnection.getSessionConnection();

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setObject(1, predictionId);
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                GridMindMatchPrediction prediction = new GridMindMatchPrediction();
                prediction.setId(rs.getObject("id", UUID.class));
                prediction.setGridMindMatchId(rs.getObject("gridmind_match_id", UUID.class));
                prediction.setHomeWin(rs.getBoolean("home_win"));
                prediction.setDraw(rs.getBoolean("draw"));
                prediction.setAwayWin(rs.getBoolean("away_win"));
                prediction.setHomeWinOddOverride(rs.getDouble("home_win_odd_override"));
                prediction.setDrawOddOverride(rs.getDouble("draw_odd_override"));
                prediction.setAwayWinOddOverride(rs.getDouble("away_win_odd_override"));
                prediction.setGridMindGridId(rs.getObject("gridmind_grid_id", UUID.class));
                prediction.setGridMindGridPredictionId(rs.getObject("gridmind_grid_prediction_id", UUID.class));
                predictions.add(prediction); // ✅ Ajoutez la prédiction à la liste
            }
        }
    }
    return predictions;
}
    
 // ✅ Surcharge pour garder la compatibilité avec l'ancien code
    @Override
    public void save(GridMindMatchPrediction prediction) throws SQLException {
        save(prediction, null); // ✅ Appelle la version avec Connection
    }
    
    @Override
    public void update(GridMindMatchPrediction prediction, Connection conn) throws SQLException {
        System.out.println("🔹 [DEBUG] GridMindMatchPredictionDAOImpl update Mise à jour en base de predictionId: " + prediction.getId() +
                ", homeWin: " + prediction.isHomeWin() +
                ", draw: " + prediction.isDraw() +
                ", awayWin: " + prediction.isAwayWin());
        String sql = "UPDATE gridmind_match_prediction SET " +
                     "home_win = ?, draw = ?, away_win = ?, " +
                     "home_win_odd_override = ?, draw_odd_override = ?, away_win_odd_override = ? " +
                     "WHERE id = ?";

        Connection localConn = (conn != null) ? conn : DatabaseConnection.getSessionConnection();
        try (PreparedStatement stmt = localConn.prepareStatement(sql)) {
            stmt.setBoolean(1, prediction.isHomeWin());
            stmt.setBoolean(2, prediction.isDraw());
            stmt.setBoolean(3, prediction.isAwayWin());
            stmt.setDouble(4, prediction.getHomeWinOddOverride());
            stmt.setDouble(5, prediction.getDrawOddOverride());
            stmt.setDouble(6, prediction.getAwayWinOddOverride());
            stmt.setObject(7, prediction.getId());
            stmt.executeUpdate();
            System.out.println("🔹 [DEBUG] GridMindMatchPredictionDAOImpl update Requête SQL exécutée avec succès.");            
        } 
    }

 // ✅ Surcharge pour compatibilité
    @Override
    public void update(GridMindMatchPrediction prediction) throws SQLException {
        update(prediction, null);
    }
    
    @Override
    public List<GridMindMatchPrediction> getPredictionsForMatch(UUID matchId) throws SQLException {
        List<GridMindMatchPrediction> predictions = new ArrayList<>();
        String sql = "SELECT * FROM gridmind_match_prediction WHERE gridmind_match_id = ?";

        try (Connection localConn = DatabaseConnection.getSessionConnection();
             PreparedStatement stmt = localConn.prepareStatement(sql)) {
            stmt.setObject(1, matchId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                GridMindMatchPrediction prediction = new GridMindMatchPrediction();
                prediction.setId((UUID) rs.getObject("id"));
                prediction.setGridMindMatchId((UUID) rs.getObject("gridmind_match_id"));
                prediction.setGridMindGridPredictionId((UUID) rs.getObject("gridmind_grid_prediction_id")); // ✅ Nouveau champ
                prediction.setGridMindGridId((UUID) rs.getObject("gridmind_grid_id")); // ✅ Nouveau champ
                prediction.setHomeWin(rs.getBoolean("home_win"));
                prediction.setDraw(rs.getBoolean("draw"));
                prediction.setAwayWin(rs.getBoolean("away_win"));
                prediction.setHomeWinOddOverride(rs.getDouble("home_win_odd_override"));
                prediction.setDrawOddOverride(rs.getDouble("draw_odd_override"));
                prediction.setAwayWinOddOverride(rs.getDouble("away_win_odd_override"));
                predictions.add(prediction);
            }
        }
        return predictions;
    }

    @Override
    public void delete(UUID predictionId) throws SQLException {
        String sql = "DELETE FROM gridmind_match_prediction WHERE id = ?";

        try (Connection localConn = DatabaseConnection.getSessionConnection();
             PreparedStatement stmt = localConn.prepareStatement(sql)) {
            stmt.setObject(1, predictionId);
            stmt.executeUpdate();
        }
    }

    public List<GridMindMatchPrediction> getPredictionsForGrid(UUID gridId) throws SQLException {
        List<GridMindMatchPrediction> predictions = new ArrayList<>();
        // ✅ Utilisez directement gridmind_grid_id (plus simple et plus rapide)
        String sql = "SELECT * FROM gridmind_match_prediction WHERE gridmind_grid_id = ?";
        System.out.println("🔹 [DEBUG] Exécutant getPredictionsForGrid avec gridId: " + gridId + ", SQL: " + sql);  // ✅ Log de la requête
        
        try (
            Connection localConn = DatabaseConnection.getSessionConnection();
            PreparedStatement stmt = localConn.prepareStatement(sql)
        ) {
            stmt.setObject(1, gridId);
            ResultSet rs = stmt.executeQuery();
            
            int count = 0; // ✅ Déclaration de count
            while (rs.next()) {
                count++;            	
                GridMindMatchPrediction prediction = new GridMindMatchPrediction();
                prediction.setId((UUID) rs.getObject("id"));
                prediction.setGridMindMatchId((UUID) rs.getObject("gridmind_match_id"));
                prediction.setGridMindGridPredictionId((UUID) rs.getObject("gridmind_grid_prediction_id")); // ✅ Nouveau champ
                prediction.setGridMindGridId((UUID) rs.getObject("gridmind_grid_id")); // ✅ Nouveau champ
                prediction.setHomeWin(rs.getBoolean("home_win"));
                prediction.setDraw(rs.getBoolean("draw"));
                prediction.setAwayWin(rs.getBoolean("away_win"));
                prediction.setHomeWinOddOverride(rs.getDouble("home_win_odd_override"));
                prediction.setDrawOddOverride(rs.getDouble("draw_odd_override"));
                prediction.setAwayWinOddOverride(rs.getDouble("away_win_odd_override"));
                predictions.add(prediction);
            }
            System.out.println("🔹 [DEBUG] getPredictionsForGrid a retourné " + count + " résultats.");  // ✅ Nombre de résultats            
        }
        return predictions;
    }

    @Override
    public List<GridMindMatchPrediction> getPredictionsForGridPrediction(UUID gridPredictionId) throws SQLException {
        List<GridMindMatchPrediction> predictions = new ArrayList<>();
        String sql = "SELECT * FROM gridmind_match_prediction WHERE gridmind_grid_prediction_id = ?";
        Connection conn = DatabaseConnection.getSessionConnection();
        try (
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, gridPredictionId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                GridMindMatchPrediction prediction = new GridMindMatchPrediction();
                prediction.setId(rs.getObject("id", UUID.class));
                prediction.setGridMindMatchId(rs.getObject("gridmind_match_id", UUID.class));
                prediction.setGridMindGridPredictionId(rs.getObject("gridmind_grid_prediction_id", UUID.class));
                prediction.setGridMindGridId(rs.getObject("gridmind_grid_id", UUID.class));
                prediction.setHomeWin(rs.getBoolean("home_win"));
                prediction.setDraw(rs.getBoolean("draw"));
                prediction.setAwayWin(rs.getBoolean("away_win"));
                prediction.setHomeWinOddOverride(rs.getDouble("home_win_odd_override"));
                prediction.setDrawOddOverride(rs.getDouble("draw_odd_override"));
                prediction.setAwayWinOddOverride(rs.getDouble("away_win_odd_override"));
                predictions.add(prediction);
            }
        }
        return predictions;
    }

}    
package com.kad.dao.impl;

import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.kad.dao.GridMindGridDAO;  // ✅ Implémentez GridMindGridDAO, pas GridMindGridPredictionDAO
import com.kad.dao.GridMindGridPredictionDAO;
import com.kad.dao.GridMindMatchDAO;
import com.kad.dao.GridMindMatchPredictionDAO;
import com.kad.database.DatabaseConnection;
import com.kad.model.GridMindGrid;
import com.kad.model.GridMindGridPrediction;
import com.kad.model.GridType;

public class GridMindGridDAOImpl implements GridMindGridDAO {  // ✅ Corrigez l'interface
    private Connection connection;
    private final GridMindGridPredictionDAO gridPredictionDAO;
    private final GridMindMatchPredictionDAO matchPredictionDAO;
    private final GridMindMatchDAO matchDAO;
    
    // Constructeur principal :  initialise toutes les dépendances (connexion + DAO)
    public GridMindGridDAOImpl(
            Connection conn,
            GridMindGridPredictionDAO gridPredictionDAO,
            GridMindMatchPredictionDAO matchPredictionDAO,
            GridMindMatchDAO matchDAO) {
        this.connection = conn;
        this.gridPredictionDAO = gridPredictionDAO;
        this.matchPredictionDAO = matchPredictionDAO;
        this.matchDAO = matchDAO;
    }

    // Constructeur secondaire : utilise des instances par défaut pour les DAO
    public GridMindGridDAOImpl(Connection conn) throws SQLException {
        this(
            conn,
            new GridMindGridPredictionDAOImpl(),  // Instance par défaut
            new GridMindMatchPredictionDAOImpl(new GridMindMatchDAO()), // Instance par défaut
            new GridMindMatchDAO()                 // Instance par défaut
        );
    }

 // Constructeur sans paramètre : crée une nouvelle connexion et des DAO par défaut
    public GridMindGridDAOImpl() throws SQLException {
        this(DatabaseConnection.getSessionConnection()); // Appelle le constructeur avec la connexion
    }
    
    @Override
    public List<GridMindGrid> getAllTheoreticalGrids() throws SQLException {
        List<GridMindGrid> grids = new ArrayList<>();
        String sql = "SELECT * FROM gridmind_grid";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                GridMindGrid grid = new GridMindGrid();
                grid.setId(rs.getObject("id", UUID.class));
                grid.setName(rs.getString("name"));
                grid.setGridNumber(rs.getInt("grid_number"));
                rs.getTimestamp("play_deadline").toLocalDateTime().atZone(ZoneId.systemDefault());  // ✅ Solution
                grids.add(grid);
            }
        }
        return grids;
    }
    
    
public void createGrid(GridMindGrid grid, Connection conn) throws SQLException {
    String sql = "INSERT INTO gridmind_grid (id, name, grid_number, play_deadline, grid_type, number_of_matches, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setObject(1, grid.getId());
        stmt.setString(2, grid.getName());
        stmt.setInt(3, grid.getGridNumber());
        if (grid.getPlayDeadline() != null) {
            stmt.setTimestamp(4, Timestamp.from(grid.getPlayDeadline().toInstant()));
        } else {
            stmt.setTimestamp(4, null);
        }
        stmt.setObject(5, grid.getGridType().name(), Types.OTHER);
        stmt.setInt(6, grid.getNumberOfMatches());
        stmt.setString(7, grid.getDescription());
        stmt.executeUpdate();
    }
    // ✅ Pas de commit ici : la transaction est gérée par showCreateGridDialog
}

    public List<GridMindGrid> getAllGrids() throws SQLException {
        List<GridMindGrid> grids = new ArrayList<>();
        String sql = "SELECT * FROM gridmind_grid ORDER BY grid_number";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                GridMindGrid grid = new GridMindGrid();
                grid.setId((UUID) rs.getObject("id"));
                grid.setName(rs.getString("name"));
                grid.setGridNumber(rs.getInt("grid_number"));
                Timestamp timestamp = rs.getTimestamp("play_deadline");
                if (timestamp != null) {
                    grid.setPlayDeadline(timestamp.toInstant().atZone(ZoneId.systemDefault()));
                }
                grid.setNumberOfMatches(rs.getInt("number_of_matches"));
                grid.setDescription(rs.getString("description"));
                grid.setGridType(GridType.valueOf(rs.getString("grid_type")));
                grids.add(grid);
            }
        }
        return grids;
    }

    public GridMindGrid getGridById(UUID gridId) throws SQLException {
        String sql = "SELECT * FROM gridmind_grid WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, gridId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                GridMindGrid grid = new GridMindGrid();
                grid.setId(rs.getObject("id", UUID.class));
                grid.setName(rs.getString("name"));
                grid.setGridNumber(rs.getInt("grid_number"));
                Timestamp timestamp = rs.getTimestamp("play_deadline");
                if (timestamp != null) {
                    grid.setPlayDeadline(timestamp.toInstant().atZone(ZoneId.systemDefault()));
                }
                grid.setNumberOfMatches(rs.getInt("number_of_matches"));
                grid.setDescription(rs.getString("description"));
                grid.setGridType(GridType.valueOf(rs.getString("grid_type")));
                return grid;
            }
        }
        return null;
    }
    
@Override
public void delete(UUID gridId) throws SQLException {
    Connection conn = DatabaseConnection.getSessionConnection();
    try {
        // ✅ 1. Supprime les feuilles : gridmind_match_prediction
        String deleteMatchPredictionsSql =
            "DELETE FROM gridmind_match_prediction " +
            "WHERE gridmind_grid_prediction_id IN " +
            "(SELECT id FROM gridmind_grid_prediction WHERE gridmind_grid_id = ?)";
        try (PreparedStatement stmt = conn.prepareStatement(deleteMatchPredictionsSql)) {
            stmt.setObject(1, gridId);
            int deleted = stmt.executeUpdate();
            System.out.println("🌿 [Feuilles] " + deleted + " pronostics de matchs supprimés.");
        }

        // ✅ 2. Supprime les branches : gridmind_grid_prediction
        String deleteGridPredictionsSql =
            "DELETE FROM gridmind_grid_prediction WHERE gridmind_grid_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteGridPredictionsSql)) {
            stmt.setObject(1, gridId);
            int deleted = stmt.executeUpdate();
            System.out.println("🌿 [Branche 1] " + deleted + " pronostics de grille supprimés.");
        }

        // ✅ 3. Supprime les branches : gridmind_match
        String deleteMatchesSql =
            "DELETE FROM gridmind_match WHERE grid_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteMatchesSql)) {
            stmt.setObject(1, gridId);
            int deleted = stmt.executeUpdate();
            System.out.println("🌿 [Branche 2] " + deleted + " matchs théoriques supprimés.");
        }

        // ✅ 4. Supprime le tronc : gridmind_grid
        String deleteGridSql = "DELETE FROM gridmind_grid WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteGridSql)) {
            stmt.setObject(1, gridId);
            int deleted = stmt.executeUpdate();
            System.out.println("🌳 [Tronc] " + deleted + " grille théorique supprimée.");
        }

        // ❌ PAS de commit() ici ! La transaction sera validée dans le menu principal.
    } catch (SQLException e) {
        conn.rollback(); // ✅ Annule la transaction en cas d'erreur
        System.err.println("❌ Erreur lors de la suppression : " + e.getMessage());
        throw e;
    }
}

}
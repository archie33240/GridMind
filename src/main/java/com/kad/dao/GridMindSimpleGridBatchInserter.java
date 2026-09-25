package com.kad.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

import com.kad.database.DatabaseConnection;
import com.kad.model.GridMindSimpleGrid;

public class GridMindSimpleGridBatchInserter {
    private static final int BATCH_SIZE = 1000; // 1000 grilles par transaction

    public void saveSimpleGridsBatch(List<GridMindSimpleGrid> simpleGrids) throws SQLException {
        try (Connection conn = DatabaseConnection.getSessionConnection()) {
            conn.setAutoCommit(false); // Désactive l'auto-commit pour les lots

            String sql = "INSERT INTO gridmind_simple_grid (id, gridmind_grid_prediction_id, combination, total_odds, created_at) " +
                         "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int count = 0;
                for (GridMindSimpleGrid simpleGrid : simpleGrids) {
                    stmt.setObject(1, simpleGrid.getId(), Types.OTHER); // UUID
                    stmt.setObject(2, simpleGrid.getGridmindGridPredictionId(), Types.OTHER); // UUID
                    stmt.setString(3, simpleGrid.getCombination());
                    stmt.setDouble(4, simpleGrid.getTotalOdds());
                    // ✅ CORRECTION : Convertir ZonedDateTime en OffsetDateTime
                    System.out.println("🔹 [DEBUG] created_at : " + simpleGrid.getCreatedAt());
//                    stmt.setObject(5, simpleGrid.getCreatedAt().toOffsetDateTime(), Types.TIMESTAMP_WITH_TIMEZONE);
                    stmt.setObject(5, Timestamp.valueOf(simpleGrid.getCreatedAt()), Types.TIMESTAMP);

                    stmt.addBatch();
                    count++;

                    // Exécute le lot toutes les 1000 grilles
                    if (count % BATCH_SIZE == 0) {
                    	System.out.println("🔹 [DEBUG] created_at : " + simpleGrid.getCreatedAt() + " (Type: " + 
                    					simpleGrid.getCreatedAt().getClass().getSimpleName() + ")");
                        stmt.executeBatch();
                        conn.commit();
                        System.out.println("✅ Lot de " + BATCH_SIZE + " grilles sauvegardées.");
                    }
                }

                // Sauvegarde les grilles restantes
                if (count % BATCH_SIZE != 0) {
                    stmt.executeBatch();
                    conn.commit();
                    System.out.println("✅ Lot final de " + (count % BATCH_SIZE) + " grilles sauvegardées.");
                }
            } catch (SQLException e) {
                conn.rollback(); // Annule la transaction en cas d'erreur
                throw e;
            } finally {
                conn.setAutoCommit(true); // Réactive l'auto-commit
            }
        }
    }
}
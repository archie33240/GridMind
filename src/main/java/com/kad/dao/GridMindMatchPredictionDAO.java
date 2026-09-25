package com.kad.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.kad.model.GridMindMatchPrediction;

public interface GridMindMatchPredictionDAO {
    void save(GridMindMatchPrediction prediction) throws SQLException;
    void save(GridMindMatchPrediction prediction, Connection conn) throws SQLException;
    void update(GridMindMatchPrediction prediction) throws SQLException;
    void update(GridMindMatchPrediction prediction, Connection conn) throws SQLException;
    List<GridMindMatchPrediction> getPredictionsForGrid(UUID gridId) throws SQLException;
    List<GridMindMatchPrediction> getPredictionsForGridPrediction(UUID gridPredictionId) throws SQLException;
    List<GridMindMatchPrediction> getPredictionsForPrediction(UUID predictionId) throws SQLException;
    List<GridMindMatchPrediction> getPredictionsForMatch(UUID matchId) throws SQLException;  // ✅ Ajoutez cette ligne
    void delete(UUID id) throws SQLException;
    /**
     * Récupère une prédiction par son ID.
     * @param id L'ID de la prédiction.
     * @return L'objet GridMindMatchPrediction correspondant.
     * @throws SQLException Si une erreur survient lors de l'accès à la base de données.
     */
    GridMindMatchPrediction getMatchPredictionById(UUID id) throws SQLException;
}
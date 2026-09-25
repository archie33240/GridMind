package com.kad.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.kad.model.GridMindGridPrediction;
import com.kad.model.GridMindMatchPrediction;
import com.kad.model.GridMindSimpleGrid;

public interface GridMindGridPredictionDAO {
    void save(GridMindGridPrediction gridPrediction) throws SQLException;
    void update(GridMindGridPrediction gridPrediction) throws SQLException;
    GridMindGridPrediction getPredictionForGrid(UUID gridId) throws SQLException;
    List<GridMindGridPrediction> getAllPredictionsForGrid(UUID gridId) throws SQLException;
    List<GridMindGridPrediction> getPredictionsForGrid(UUID gridId) throws SQLException;
    GridMindGridPrediction getPredictionById(UUID predictionId) throws SQLException;  // ✅ Ajoutez cette ligne
    UUID getGridMindGridIdByPredictionId(UUID gridPredictionId) throws SQLException;
    void delete(UUID id) throws SQLException;
    /**
     * Calcule la somme des côtes pour une grille donnée.
     * @param grid La grille pour laquelle calculer la somme.
     * @param matches La liste des matchs associés à la grille.
     * @return La somme des côtes des résultats cochés (X).
     */
    double calculateTotalOdds(GridMindSimpleGrid grid, List<GridMindMatchPrediction> matchPredictions);
    /**
     * Récupère une grille de pronostics par son ID.
     * @param gridPredictionId L'ID de la grille de pronostics.
     * @return La grille de pronostics correspondante, ou null si non trouvée.
     */
    GridMindGridPrediction getGridPredictionById(UUID gridPredictionId);

}
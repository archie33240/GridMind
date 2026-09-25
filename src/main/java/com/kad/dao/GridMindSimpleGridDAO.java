package com.kad.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.kad.model.GridMindSimpleGrid;

public interface GridMindSimpleGridDAO {
    // Sauvegarder une grille simple
    void save(GridMindSimpleGrid simpleGrid, Connection conn) throws SQLException;

    // Mettre à jour une grille simple (si besoin)
    void update(GridMindSimpleGrid simpleGrid, Connection conn) throws SQLException;

    // Supprimer une grille simple
    void delete(UUID id, Connection conn) throws SQLException;

    // Supprimer toutes les grilles simples d'un pronostic
    void deleteGridsByPredictionId(UUID gridPredictionId) throws SQLException;

    // Récupérer une grille simple par son ID
    GridMindSimpleGrid getById(UUID id, Connection conn) throws SQLException;

    // Récupérer toutes les grilles simples pour un pronostic donné
    List<GridMindSimpleGrid> getSimpleGridsForPrediction(UUID gridmindGridPredictionId, Connection conn) throws SQLException;

    // Récupérer les grilles simples dans une fourchette de côtes
    List<GridMindSimpleGrid> getSimpleGridsByOddsRange(double minOdds, double maxOdds, Connection conn) throws SQLException;

    // Récupérer les grilles simples pour consultation
    List<GridMindSimpleGrid> getAllGeneratedGrids(UUID gridmindGridPredictionId) throws SQLException;
    
    /**
     * Compte le nombre de grilles simples générées pour un pronostic donné.
     * @param gridmindGridPredictionId L'ID du pronostic.
     * @return Le nombre de grilles simples.
     * @throws SQLException Si une erreur SQL survient.
     */
    int getGeneratedGridsCount(UUID gridmindGridPredictionId) throws SQLException;
}
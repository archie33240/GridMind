package com.kad.kernel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.kad.model.GridMindMatchPrediction;
import com.kad.model.GridMindSimpleGrid;

public class GridMindSimpleGridGenerator {

    public List<GridMindSimpleGrid> generateValidSimpleGrids(
            List<GridMindMatchPrediction> predictions,
            int minHomeWin, int maxHomeWin,
            int minDraw, int maxDraw,
            int minAwayWin, int maxAwayWin,
            double minTotalOdds, double maxTotalOdds,
            UUID gridmindGridPredictionId) {

        List<GridMindSimpleGrid> validSimpleGrids = new ArrayList<>();
        generateAndFilterCombinations(
            predictions, 0, "", 0, 0, 0, 0.0,
            minHomeWin, maxHomeWin, minDraw, maxDraw, minAwayWin, maxAwayWin,
            minTotalOdds, maxTotalOdds,
            gridmindGridPredictionId, validSimpleGrids
        );
        return validSimpleGrids;
    }

    private void generateAndFilterCombinations(
            List<GridMindMatchPrediction> predictions,
            int index, String currentCombination,
            int homeWinCount, int drawCount, int awayWinCount, double currentTotalOdds,
            int minHomeWin, int maxHomeWin,
            int minDraw, int maxDraw,
            int minAwayWin, int maxAwayWin,
            double minTotalOdds, double maxTotalOdds,
            UUID gridmindGridPredictionId, List<GridMindSimpleGrid> validSimpleGrids) {

        if (index == predictions.size()) {
            if (homeWinCount >= minHomeWin && homeWinCount <= maxHomeWin &&
                drawCount >= minDraw && drawCount <= maxDraw &&
                awayWinCount >= minAwayWin && awayWinCount <= maxAwayWin &&
                currentTotalOdds >= minTotalOdds && currentTotalOdds <= maxTotalOdds) {

                validSimpleGrids.add(new GridMindSimpleGrid(
                    UUID.randomUUID(),
                    gridmindGridPredictionId,
                    currentCombination,
                    currentTotalOdds,
                    LocalDateTime.now()
                ));
            }
            return;
        }

        GridMindMatchPrediction prediction = predictions.get(index);

        // Récupère les côtes UNIQUEMENT depuis GridMindMatchPrediction
        Double homeOdd = prediction.getHomeWinOddOverride() != null ?
            prediction.getHomeWinOddOverride() : 1.0;
        Double drawOdd = prediction.getDrawOddOverride() != null ?
            prediction.getDrawOddOverride() : 1.0;
        Double awayOdd = prediction.getAwayWinOddOverride() != null ?
            prediction.getAwayWinOddOverride() : 1.0;

        // Ajoute "1" si la case victoire à domicile est cochée ET que ça ne dépasse pas maxHomeWin
        if (prediction.isHomeWin() && homeWinCount < maxHomeWin) {
            generateAndFilterCombinations(
                predictions, index + 1, currentCombination + "1",
                homeWinCount + 1, drawCount, awayWinCount, currentTotalOdds + homeOdd,
                minHomeWin, maxHomeWin, minDraw, maxDraw, minAwayWin, maxAwayWin,
                minTotalOdds, maxTotalOdds,
                gridmindGridPredictionId, validSimpleGrids
            );
        }

        // Ajoute "N" si la case match nul est cochée ET que ça ne dépasse pas maxDraw
        if (prediction.isDraw() && drawCount < maxDraw) {
            generateAndFilterCombinations(
                predictions, index + 1, currentCombination + "N",
                homeWinCount, drawCount + 1, awayWinCount, currentTotalOdds + drawOdd,
                minHomeWin, maxHomeWin, minDraw, maxDraw, minAwayWin, maxAwayWin,
                minTotalOdds, maxTotalOdds,
                gridmindGridPredictionId, validSimpleGrids
            );
        }

        // Ajoute "2" si la case défaite à domicile est cochée ET que ça ne dépasse pas maxAwayWin
        if (prediction.isAwayWin() && awayWinCount < maxAwayWin) {
            generateAndFilterCombinations(
                predictions, index + 1, currentCombination + "2",
                homeWinCount, drawCount, awayWinCount + 1, currentTotalOdds + awayOdd,
                minHomeWin, maxHomeWin, minDraw, maxDraw, minAwayWin, maxAwayWin,
                minTotalOdds, maxTotalOdds,
                gridmindGridPredictionId, validSimpleGrids
            );
        }
    }
}
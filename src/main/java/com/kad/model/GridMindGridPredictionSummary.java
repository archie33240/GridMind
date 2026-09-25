package com.kad.model;

public class GridMindGridPredictionSummary {
    private final int simples;
    private final int doubles;
    private final int triples;
    private final long combinations;
    private final double minOddsSum;
    private final double maxOddsSum;

    public GridMindGridPredictionSummary(int simples, int doubles, int triples,
                                         long combinations, double minOddsSum, double maxOddsSum) {
        this.simples = simples;
        this.doubles = doubles;
        this.triples = triples;
        this.combinations = combinations;
        this.minOddsSum = minOddsSum;
        this.maxOddsSum = maxOddsSum;
    }

    // Getters
    public int getSimples() { return simples; }
    public int getDoubles() { return doubles; }
    public int getTriples() { return triples; }
    public long getCombinations() { return combinations; }
    public double getMinOddsSum() { return minOddsSum; }
    public double getMaxOddsSum() { return maxOddsSum; }
}
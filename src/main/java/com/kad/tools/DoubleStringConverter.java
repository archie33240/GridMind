package com.kad.tools;

import javafx.util.StringConverter;

public class DoubleStringConverter extends StringConverter<Double> {
    @Override
    public String toString(Double value) {
        return value == null ? "" : String.format("%.2f", value).replace(".", ","); // Affiche 2 décimales avec virgule
    }

    @Override
    public Double fromString(String text) {
        try {
            if (text.isEmpty()) {
                return 1.0; // Valeur par défaut
            }
            // Remplace les virgules par des points pour le parsing
            String normalizedText = text.replace(",", ".");
            double value = Double.parseDouble(normalizedText);
            // Limite à 2 décimales (optionnel)
            return Math.round(value * 100) / 100.0;
        } catch (NumberFormatException e) {
            return 1.0; // Valeur par défaut en cas d'erreur
        }
    }
}
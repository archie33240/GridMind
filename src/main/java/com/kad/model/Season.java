package com.kad.model;

import java.time.LocalDate;
import java.util.UUID;

public class Season {
    private UUID id;
    private LocalDate startDate;
    private LocalDate endDate;

    // Constructeur par défaut
    public Season() {}

    // Constructeur avec paramètres
    public Season(UUID id, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters et Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    // Méthode pour obtenir l'année de début
    public int getStartYear() {
        return startDate.getYear();
    }

    // Méthode pour obtenir l'année de fin
    public int getEndYear() {
        return endDate.getYear();
    }

    // Méthode toString personnalisée pour afficher les années
    @Override
    public String toString() {
        return String.format("Saison %d-%d", getStartYear(), getEndYear());
    }
}

package com.kad.model;

public enum GridType {
    LOTO_FOOT("Loto Foot"),
    LOTO_RUGBY("Loto Rugby"),
    LOTO_BASKET("Loto Basket");

    private final String displayName;

    GridType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static GridType fromDisplayName(String displayName) {
        for (GridType type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Nom de type de grille invalide: " + displayName);
    }
}

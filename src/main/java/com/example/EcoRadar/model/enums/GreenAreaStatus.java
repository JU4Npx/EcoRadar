package com.example.EcoRadar.model.enums;

public enum GreenAreaStatus {
    ACTIVE("Ativa"),
    INACTIVE("Inativa"),
    UNDER_MAINTENANCE("Em manutenção");

    private final String displayName;

    GreenAreaStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

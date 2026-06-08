package com.example.EcoRadar.model.enums;

public enum EventStatus {
    SCHEDULED("Agendado"),
    COMPLETED("Concluído"),
    CANCELED("Cancelado");

    private final String displayName;

    EventStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

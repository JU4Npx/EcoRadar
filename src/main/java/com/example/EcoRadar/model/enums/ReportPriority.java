package com.example.EcoRadar.model.enums;

public enum ReportPriority {
    LOW("Baixa"),
    MEDIUM("Média"),
    HIGH("Alta");

    private final String label;

    ReportPriority(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
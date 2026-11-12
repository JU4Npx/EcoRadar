package com.example.EcoRadar.model.enums;

public enum ReportStatus {
    RECEIVED("Recebida"),
    IN_PROGRESS("Em análise"),
    RESOLVED("Resolvida");

    private final String label;

    ReportStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
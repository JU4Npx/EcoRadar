package com.example.EcoRadar.model.enums;

public enum ReportCategory {
    LIXO("Descarte irregular"),
    ARVORE("Árvore ou vegetação"),
    AGUA("Água e drenagem"),
    POLUICAO("Poluição"),
    INFRAESTRUTURA("Infraestrutura"),
    OUTRO("Outro problema");

    private final String label;

    ReportCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
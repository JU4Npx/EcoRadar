package com.example.EcoRadar.model.enums;

public enum GreenAreaType {
    PRACA("PRAÇA"),
    PRAIA("PRAIA"),
    PARQUE("PARQUE"),
    FEIRA_DE_ARTESANATO("FEIRA DE ARTESANATO"),
    CENTRO_DE_CONVENCOES("CENTRO DE CONVENÇÕES"),
    MUSEU("MUSEU");

    private final String displayName;

    GreenAreaType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

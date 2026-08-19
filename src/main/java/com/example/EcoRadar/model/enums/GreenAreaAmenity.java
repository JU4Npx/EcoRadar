package com.example.EcoRadar.model.enums;

public enum GreenAreaAmenity {
    ACCESSIBILITY("Acessibilidade", "bi-universal-access"),
    RESTROOM("Banheiros", "bi-badge-wc"),
    PARKING("Estacionamento", "bi-p-circle"),
    LIGHTING("Iluminação", "bi-lightbulb"),
    PET_FRIENDLY("Aceita animais", "bi-heart"),
    PLAYGROUND("Parquinho", "bi-balloon"),
    TRAIL("Trilhas", "bi-signpost-split"),
    FITNESS_EQUIPMENT("Equipamentos de exercício", "bi-activity"),
    PICNIC_AREA("Área para piquenique", "bi-basket"),
    BIKE_RACK("Bicicletário", "bi-bicycle");

    private final String displayName;
    private final String icon;

    GreenAreaAmenity(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }
}

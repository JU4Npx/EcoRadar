package com.example.EcoRadar.model.enums;

public enum EventCategory {
    CLEANUP("Mutirão de limpeza"),        // 🧹
    TREE_PLANTING("Plantio de árvores"),  // 🌳
    WORKSHOP("Oficina"),       // 🛠
    LECTURE("Palestra"),        // 🗣
    FAIR("Feira"),           // 🛍
    SHOW("Show"),           // 🎶
    SPORTS("Evento esportivo"),         // 🏃
    EDUCATION("Educação ambiental"),      // 🎓
    OTHER("Outro");           // 📅

    private final String displayName;

    EventCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

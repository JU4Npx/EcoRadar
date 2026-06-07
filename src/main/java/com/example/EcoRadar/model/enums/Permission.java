package com.example.EcoRadar.model.enums;

public enum Permission {

    MANAGE_USERS("Gerenciar usuarios"),
    GRANT_ADMIN("Promover/remover administradores"),

    CREATE_EVENT("Criar eventos"),
    EDIT_EVENT("Editar eventos"),
    DELETE_EVENT("Remover eventos"),

    CREATE_GREEN_AREA("Criar areas verdes"),
    EDIT_GREEN_AREA("Editar areas verdes"),
    DELETE_GREEN_AREA("Remover areas verdes");

    private final String displayName;

    Permission(String displayName) {

        this.displayName = displayName;
    }

    public String getDisplayName() {

        return displayName;
    }
}

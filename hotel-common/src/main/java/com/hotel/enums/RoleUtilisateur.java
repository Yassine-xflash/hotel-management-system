package com.hotel.enums;

public enum RoleUtilisateur {
    ADMIN("Administrateur"),
    EMPLOYE("Employé"),
    CLIENT("Client");

    private final String libelle;

    RoleUtilisateur(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}

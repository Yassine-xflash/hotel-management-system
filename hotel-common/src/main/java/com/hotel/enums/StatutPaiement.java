package com.hotel.enums;

public enum StatutPaiement {
    EN_ATTENTE("En attente"),
    PAYE("Payé"),
    REMBOURSE("Remboursé"),
    ECHOUE("Échoué");

    private final String libelle;

    StatutPaiement(String libelle) {
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

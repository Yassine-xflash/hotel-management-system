package com.hotel.enums;

public enum TypeChambre {
    SIMPLE("Chambre Simple", 1),
    DOUBLE("Chambre Double", 2),
    SUITE("Suite", 2),
    DELUXE("Chambre Deluxe", 3),
    FAMILIALE("Chambre Familiale", 4);

    private final String libelle;
    private final int capacite;

    TypeChambre(String libelle, int capacite) {
        this.libelle = libelle;
        this.capacite = capacite;
    }

    public String getLibelle() {
        return libelle;
    }

    public int getCapacite() {
        return capacite;
    }

    @Override
    public String toString() {
        return libelle;
    }
}

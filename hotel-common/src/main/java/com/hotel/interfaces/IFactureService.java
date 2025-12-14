package com.hotel.interfaces;

import com.hotel.entities.Facture;
import com.hotel.entities.Reservation;

public interface IFactureService {

    Facture genererFacture(Reservation reservation);

    Facture findById(int id);

    Facture findByReservation(Reservation reservation);

    void save(Facture facture);
}

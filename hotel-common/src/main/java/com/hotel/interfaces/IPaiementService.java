package com.hotel.interfaces;

import com.hotel.entities.Paiement;
import com.hotel.enums.StatutPaiement;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IPaiementService {

    Paiement creerPaiement(Paiement paiement);

    boolean traiterPaiement(Integer paiementId, com.hotel.enums.TypePaiement methodePaiement);

    boolean rembourserPaiement(Integer paiementId);

    Paiement getPaiementById(Integer id);

    Paiement getPaiementParReservation(Integer reservationId);

    List<Paiement> getAllPaiements();

    List<Paiement> getPaiementsParStatut(StatutPaiement statut);
}
package com.hotel.interfaces;

import com.hotel.entities.Paiement;
import com.hotel.enums.StatutPaiement;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IPaiementService extends Remote {

    Paiement creerPaiement(Paiement paiement) throws RemoteException;

    boolean traiterPaiement(Integer paiementId, String methodePaiement) throws RemoteException;

    boolean rembourserPaiement(Integer paiementId) throws RemoteException;

    Paiement getPaiementById(Integer id) throws RemoteException;

    Paiement getPaiementParReservation(Integer reservationId) throws RemoteException;

    List<Paiement> getAllPaiements() throws RemoteException;

    List<Paiement> getPaiementsParStatut(StatutPaiement statut) throws RemoteException;

    String genererFacture(Integer reservationId) throws RemoteException;

    Double calculerRevenusPeriode(java.time.LocalDate dateDebut, java.time.LocalDate dateFin) throws RemoteException;
}
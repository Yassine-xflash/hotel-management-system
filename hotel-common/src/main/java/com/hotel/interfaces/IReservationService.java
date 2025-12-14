package com.hotel.interfaces;

import com.hotel.entities.Reservation;
import com.hotel.enums.StatutReservation;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;

public interface IReservationService extends Remote {

    Reservation creerReservation(Reservation reservation) throws RemoteException;

    Reservation modifierReservation(Reservation reservation) throws RemoteException;

    boolean annulerReservation(Integer reservationId) throws RemoteException;

    boolean confirmerReservation(Integer reservationId) throws RemoteException;

    Reservation getReservationById(Integer id) throws RemoteException;

    List<Reservation> getAllReservations() throws RemoteException;

    List<Reservation> getReservationsParClient(Integer clientId) throws RemoteException;

    List<Reservation> getReservationsParChambre(Integer chambreId) throws RemoteException;

    List<Reservation> getReservationsParStatut(StatutReservation statut) throws RemoteException;

    List<Reservation> getReservationsPourPeriode(LocalDate dateDebut, LocalDate dateFin) throws RemoteException;

    List<Reservation> getReservationsActives() throws RemoteException;

    void changerStatut(Integer reservationId, StatutReservation nouveauStatut) throws RemoteException;
}
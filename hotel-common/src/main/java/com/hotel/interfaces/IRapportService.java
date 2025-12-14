package com.hotel.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.Map;

public interface IRapportService extends Remote {

    Map<String, Object> genererRapportOccupation(LocalDate dateDebut, LocalDate dateFin) throws RemoteException;

    Map<String, Object> genererRapportRevenus(LocalDate dateDebut, LocalDate dateFin) throws RemoteException;

    Map<String, Object> genererRapportClients() throws RemoteException;

    Double calculerTauxOccupation(LocalDate dateDebut, LocalDate dateFin) throws RemoteException;

    Map<String, Object> getStatistiquesChambres() throws RemoteException;

    Map<String, Object> getStatistiquesReservations(LocalDate dateDebut, LocalDate dateFin) throws RemoteException;

    Map<String, Object> getTopClients(int nombre) throws RemoteException;

    String exporterRapport(Map<String, Object> rapport) throws RemoteException;
}
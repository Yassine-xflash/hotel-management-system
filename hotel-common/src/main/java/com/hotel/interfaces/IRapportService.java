package com.hotel.interfaces;

import com.hotel.entities.Client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.Map;

public interface IRapportService {

    Map<String, Object> genererRapportOccupation(LocalDate dateDebut, LocalDate dateFin);

    Map<String, Object> genererRapportRevenus(LocalDate dateDebut, LocalDate dateFin);

    Map<String, Object> genererRapportClients();

    Double calculerTauxOccupation(LocalDate dateDebut, LocalDate dateFin);

    Map<String, Object> getStatistiquesChambres();

    Map<String, Object> getStatistiquesReservations(LocalDate dateDebut, LocalDate dateFin);

    Map<String, Object> getTopClients(int nombre);

    String exporterRapport(Map<String, Object> rapport);

    Map<String, Object> genererRapportHistoriqueClient(Client client);
}

package com.hotel.interfaces;

import com.hotel.entities.Client;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IClientService extends Remote {

    Client ajouterClient(Client client) throws RemoteException;

    Client modifierClient(Client client) throws RemoteException;

    boolean supprimerClient(Integer clientId) throws RemoteException;

    Client getClientById(Integer id) throws RemoteException;

    Client getClientByEmail(String email) throws RemoteException;

    List<Client> getAllClients() throws RemoteException;

    List<Client> rechercherClientsParNom(String nom) throws RemoteException;

    List<Object> getHistoriqueReservations(Integer clientId) throws RemoteException;
}
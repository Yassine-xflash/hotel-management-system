package com.hotel.interfaces;

import com.hotel.entities.Chambre;
import com.hotel.enums.TypeChambre;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;

public interface IChambreService extends Remote {

    Chambre ajouterChambre(Chambre chambre) throws RemoteException;

    Chambre modifierChambre(Chambre chambre) throws RemoteException;

    boolean supprimerChambre(Integer chambreId) throws RemoteException;

    Chambre getChambreById(Integer id) throws RemoteException;

    Chambre getChambreByNumero(String numero) throws RemoteException;

    List<Chambre> getAllChambres() throws RemoteException;

    List<Chambre> getChambresDisponibles() throws RemoteException;

    List<Chambre> getChambresDisponiblesParType(TypeChambre type) throws RemoteException;

    List<Chambre> getChambresDisponiblesPourPeriode(LocalDate dateArrivee, LocalDate dateDepart) throws RemoteException;

    boolean verifierDisponibilite(Integer chambreId, LocalDate dateArrivee, LocalDate dateDepart) throws RemoteException;

    void mettreAJourDisponibilite(Integer chambreId, Boolean disponible) throws RemoteException;
}
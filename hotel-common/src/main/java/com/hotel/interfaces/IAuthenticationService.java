package com.hotel.interfaces;

import com.hotel.entities.Utilisateur;
import com.hotel.enums.RoleUtilisateur;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IAuthenticationService extends Remote {

    Utilisateur authentifier(String username, String motDePasse) throws RemoteException;

    Utilisateur creerUtilisateur(String username, String motDePasse, RoleUtilisateur role) throws RemoteException;

    boolean modifierMotDePasse(Integer utilisateurId, String ancienMotDePasse, String nouveauMotDePasse) throws RemoteException;

    boolean desactiverUtilisateur(Integer utilisateurId) throws RemoteException;

    boolean activerUtilisateur(Integer utilisateurId) throws RemoteException;

    boolean usernameExiste(String username) throws RemoteException;

    Utilisateur getUtilisateurById(Integer id) throws RemoteException;

    void deconnecter(Integer utilisateurId) throws RemoteException;
}
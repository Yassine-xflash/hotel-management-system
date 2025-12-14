package com.hotel.client.utils;

import com.hotel.entities.Utilisateur;

public class SessionManager {
    private static Utilisateur utilisateurConnecte;

    public static void setUtilisateurConnecte(Utilisateur utilisateur) {
        utilisateurConnecte = utilisateur;
    }

    public static Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public static boolean estConnecte() {
        return utilisateurConnecte != null;
    }

    public static void deconnecter() {
        if (utilisateurConnecte != null) {
            try {
                ServiceManager.getAuthenticationService().deconnecter(utilisateurConnecte.getId());
            } catch (Exception e) {
                System.err.println("Erreur lors de la déconnexion: " + e.getMessage());
            }
            utilisateurConnecte = null;
        }
    }

    public static boolean estAdmin() {
        return estConnecte() &&
                utilisateurConnecte.getRole() == com.hotel.enums.RoleUtilisateur.ADMIN;
    }

    public static boolean estEmploye() {
        return estConnecte() &&
                (utilisateurConnecte.getRole() == com.hotel.enums.RoleUtilisateur.EMPLOYE ||
                        utilisateurConnecte.getRole() == com.hotel.enums.RoleUtilisateur.ADMIN);
    }

    public static boolean estClient() {
        return estConnecte() &&
                utilisateurConnecte.getRole() == com.hotel.enums.RoleUtilisateur.CLIENT;
    }
}
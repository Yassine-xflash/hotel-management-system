package com.hotel.rmi.server;

import com.hotel.interfaces.*;
import com.hotel.rmi.services.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIServer {
    private static final int PORT = 1099;

    public static void main(String[] args) {
        try {
            System.out.println("=== Démarrage du serveur RMI ===");

            Registry registry = LocateRegistry.createRegistry(PORT);
            System.out.println("✓ Registre RMI créé sur le port " + PORT);

            IChambreService chambreService = new ChambreServiceImpl();
            registry.rebind("ChambreService", chambreService);
            System.out.println("✓ Service ChambreService enregistré");

            IClientService clientService = new ClientServiceImpl();
            registry.rebind("ClientService", clientService);
            System.out.println("✓ Service ClientService enregistré");

            IReservationService reservationService = new ReservationServiceImpl();
            registry.rebind("ReservationService", reservationService);
            System.out.println("✓ Service ReservationService enregistré");

            IAuthenticationService authService = new AuthenticationServiceImpl();
            registry.rebind("AuthenticationService", authService);
            System.out.println("✓ Service AuthenticationService enregistré");

            System.out.println("\n=== Serveur RMI prêt ===");
            System.out.println("Les services sont disponibles sur rmi://localhost:" + PORT);
            System.out.println("Appuyez sur Ctrl+C pour arrêter le serveur\n");

            initialiserDonneesTest(chambreService, clientService, authService);

            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Erreur du serveur RMI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initialiserDonneesTest(IChambreService chambreService,
                                               IClientService clientService,
                                               IAuthenticationService authService) {
        try {
            System.out.println("Initialisation des données de test...");

            // Créer admin
            try {
                if (!authService.usernameExiste("admin")) {
                    authService.creerUtilisateur("admin", "admin123",
                            com.hotel.enums.RoleUtilisateur.ADMIN);
                    System.out.println("✓ Admin créé (admin/admin123)");
                }
            } catch (Exception e) {
                System.out.println("Admin déjà existant");
            }

            // Créer employé
            try {
                if (!authService.usernameExiste("employe")) {
                    authService.creerUtilisateur("employe", "employe123",
                            com.hotel.enums.RoleUtilisateur.EMPLOYE);
                    System.out.println("✓ Employé créé (employe/employe123)");
                }
            } catch (Exception e) {
                System.out.println("Employé déjà existant");
            }

            // Créer chambres
            if (chambreService.getAllChambres().isEmpty()) {
                com.hotel.entities.Chambre ch1 = new com.hotel.entities.Chambre(
                        "101", com.hotel.enums.TypeChambre.SIMPLE, 80.0);
                ch1.setEtage(1);
                ch1.setDescription("Chambre simple confortable");
                chambreService.ajouterChambre(ch1);

                com.hotel.entities.Chambre ch2 = new com.hotel.entities.Chambre(
                        "201", com.hotel.enums.TypeChambre.DOUBLE, 120.0);
                ch2.setEtage(2);
                chambreService.ajouterChambre(ch2);

                com.hotel.entities.Chambre ch3 = new com.hotel.entities.Chambre(
                        "301", com.hotel.enums.TypeChambre.SUITE, 200.0);
                ch3.setEtage(3);
                chambreService.ajouterChambre(ch3);

                System.out.println("✓ 3 chambres ajoutées");
            }

            System.out.println("Initialisation terminée!\n");

        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation: " + e.getMessage());
        }
    }
}

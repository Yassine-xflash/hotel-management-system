package com.hotel.ejb.server;

import com.hotel.ejb.services.PaiementServiceImpl;
import com.hotel.ejb.services.RapportServiceImpl;
import com.hotel.interfaces.IPaiementService;
import com.hotel.interfaces.IRapportService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class EJBServer {
    private static final int PORT = 1100;

    public static void main(String[] args) {
        try {
            System.out.println("=== Démarrage du serveur EJB ===");

            Registry registry = LocateRegistry.createRegistry(PORT);
            System.out.println("✓ Registre EJB créé sur le port " + PORT);

            IPaiementService paiementService = new PaiementServiceImpl();
            registry.rebind("PaiementService", paiementService);
            System.out.println("✓ Service PaiementService enregistré");

            IRapportService rapportService = new RapportServiceImpl();
            registry.rebind("RapportService", rapportService);
            System.out.println("✓ Service RapportService enregistré");

            System.out.println("\n=== Serveur EJB prêt ===");
            System.out.println("Les services EJB sont disponibles sur rmi://localhost:" + PORT);
            System.out.println("Appuyez sur Ctrl+C pour arrêter le serveur\n");

            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Erreur du serveur EJB: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

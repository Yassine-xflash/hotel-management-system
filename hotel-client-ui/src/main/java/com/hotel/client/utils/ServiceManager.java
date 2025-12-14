package com.hotel.client.utils;

import com.hotel.interfaces.*;

import java.rmi.Naming;

public class ServiceManager {
    private static final String RMI_HOST = "localhost";
    private static final int RMI_PORT = 1099;
    private static final int EJB_PORT = 1100;

    private static IChambreService chambreService;
    private static IClientService clientService;
    private static IReservationService reservationService;
    private static IAuthenticationService authenticationService;
    private static IPaiementService paiementService;
    private static IRapportService rapportService;

    public static void connecterServicesRMI() throws Exception {
        String rmiUrl = "rmi://" + RMI_HOST + ":" + RMI_PORT + "/";

        chambreService = (IChambreService) Naming.lookup(rmiUrl + "ChambreService");
        clientService = (IClientService) Naming.lookup(rmiUrl + "ClientService");
        reservationService = (IReservationService) Naming.lookup(rmiUrl + "ReservationService");
        authenticationService = (IAuthenticationService) Naming.lookup(rmiUrl + "AuthenticationService");

        System.out.println("✓ Connexion aux services RMI établie");
    }

    public static void connecterServicesEJB() throws Exception {
        String ejbUrl = "rmi://" + RMI_HOST + ":" + EJB_PORT + "/";

        paiementService = (IPaiementService) Naming.lookup(ejbUrl + "PaiementService");
        rapportService = (IRapportService) Naming.lookup(ejbUrl + "RapportService");

        System.out.println("✓ Connexion aux services EJB établie");
    }

    public static void initialiser() throws Exception {
        connecterServicesRMI();
        connecterServicesEJB();
    }

    // Getters
    public static IChambreService getChambreService() {
        return chambreService;
    }

    public static IClientService getClientService() {
        return clientService;
    }

    public static IReservationService getReservationService() {
        return reservationService;
    }

    public static IAuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public static IPaiementService getPaiementService() {
        return paiementService;
    }

    public static IRapportService getRapportService() {
        return rapportService;
    }
}
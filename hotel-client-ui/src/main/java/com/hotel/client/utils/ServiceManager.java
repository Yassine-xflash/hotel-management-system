package com.hotel.client.utils;

import com.hotel.interfaces.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class ServiceManager {
    private static final String RMI_HOST = "localhost";
    private static final int RMI_PORT = 1099;

    private static IChambreService chambreService;
    private static IClientService clientService;
    private static IReservationService reservationService;
    private static IAuthenticationService authenticationService;
    private static IPaiementService paiementService;
    private static IRapportService rapportService;
    private static IFactureService factureService;

    public static void connecterServicesRMI() throws Exception {
        String rmiUrl = "rmi://" + RMI_HOST + ":" + RMI_PORT + "/";

        chambreService = (IChambreService) java.rmi.Naming.lookup(rmiUrl + "ChambreService");
        clientService = (IClientService) java.rmi.Naming.lookup(rmiUrl + "ClientService");
        reservationService = (IReservationService) java.rmi.Naming.lookup(rmiUrl + "ReservationService");
        authenticationService = (IAuthenticationService) java.rmi.Naming.lookup(rmiUrl + "AuthenticationService");

        System.out.println("✓ Connexion aux services RMI établie");
    }

    public static void connecterServicesEJB() throws NamingException {
        final Properties jndiProperties = new Properties();
        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        jndiProperties.put(Context.PROVIDER_URL, "http-remoting://localhost:8080");
        final Context context = new InitialContext(jndiProperties);

        String appName = "";
        String moduleName = "hotel-ejb-server-1.0-SNAPSHOT";
        String distinctName = "";

        String paiementServiceJndi = "ejb:" + appName + "/" + moduleName + "/" + distinctName + "/PaiementServiceImpl!com.hotel.interfaces.IPaiementService";
        String rapportServiceJndi = "ejb:" + appName + "/" + moduleName + "/" + distinctName + "/RapportServiceImpl!com.hotel.interfaces.IRapportService";
        String factureServiceJndi = "ejb:" + appName + "/" + moduleName + "/" + distinctName + "/FactureServiceImpl!com.hotel.interfaces.IFactureService";

        paiementService = (IPaiementService) context.lookup(paiementServiceJndi);
        rapportService = (IRapportService) context.lookup(rapportServiceJndi);
        factureService = (IFactureService) context.lookup(factureServiceJndi);

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

    public static IFactureService getFactureService() {
        return factureService;
    }
}
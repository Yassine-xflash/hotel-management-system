package com.hotel.ejb.utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JPAUtil {
    private static EntityManagerFactory emf;

    static {
        try {
            emf = Persistence.createEntityManagerFactory("HotelPU");
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de l'EntityManagerFactory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static EntityManager getEntityManager() {
        if (emf == null) {
            throw new IllegalStateException("EntityManagerFactory n'est pas initialisé");
        }
        return emf.createEntityManager();
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}

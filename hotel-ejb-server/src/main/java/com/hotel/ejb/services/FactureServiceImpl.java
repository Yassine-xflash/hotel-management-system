package com.hotel.ejb.services;

import com.hotel.entities.Facture;
import com.hotel.entities.Reservation;
import com.hotel.interfaces.IFactureService;
import com.hotel.ejb.utils.JPAUtil;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.Date;
import java.util.UUID;

@Stateless
public class FactureServiceImpl implements IFactureService {

    @Override
    public Facture genererFacture(Reservation reservation) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Facture facture = new Facture();
            facture.setReservation(reservation);
            facture.setMontant(reservation.getMontantTotal());
            facture.setDateFacture(new Date());
            facture.setNumeroFacture("FACT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            em.persist(facture);
            em.getTransaction().commit();
            return facture;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la génération de la facture", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Facture findById(int id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Facture.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public Facture findByReservation(Reservation reservation) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Facture> query = em.createQuery(
                    "SELECT f FROM Facture f WHERE f.reservation = :reservation", Facture.class);
            query.setParameter("reservation", reservation);
            List<Facture> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    @Override
    public void save(Facture facture) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(facture);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la sauvegarde de la facture", e);
        } finally {
            em.close();
        }
    }
}

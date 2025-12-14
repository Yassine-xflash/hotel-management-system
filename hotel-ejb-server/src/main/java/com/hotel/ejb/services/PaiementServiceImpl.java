package com.hotel.ejb.services;

import com.hotel.entities.Paiement;
import com.hotel.entities.Reservation;
import com.hotel.enums.StatutPaiement;
import com.hotel.interfaces.IPaiementService;
import com.hotel.ejb.utils.JPAUtil;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Stateless
public class PaiementServiceImpl implements IPaiementService {
    

    @Override
    public Paiement creerPaiement(Paiement paiement) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            paiement.setReferenceTransaction(genererReferenceTransaction());
            em.persist(paiement);
            em.getTransaction().commit();
            return paiement;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors de la création du paiement", e);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean traiterPaiement(Integer paiementId, com.hotel.enums.TypePaiement methodePaiement) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Paiement paiement = em.find(Paiement.class, paiementId);
            if (paiement != null) {
                paiement.setStatut(StatutPaiement.PAYE);
                paiement.setMethodePaiement(methodePaiement);
                paiement.setDatePaiement(LocalDateTime.now());
                em.merge(paiement);
                em.getTransaction().commit();
                return true;
            }
            em.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors du traitement du paiement", e);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean rembourserPaiement(Integer paiementId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Paiement paiement = em.find(Paiement.class, paiementId);
            if (paiement != null && paiement.getStatut() == StatutPaiement.PAYE) {
                paiement.setStatut(StatutPaiement.REMBOURSE);
                em.merge(paiement);
                em.getTransaction().commit();
                return true;
            }
            em.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Erreur lors du remboursement", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Paiement getPaiementById(Integer id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Paiement.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public Paiement getPaiementParReservation(Integer reservationId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Paiement> query = em.createQuery(
                    "SELECT p FROM Paiement p WHERE p.reservation.id = :reservationId", Paiement.class);
            query.setParameter("reservationId", reservationId);
            List<Paiement> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Paiement> getAllPaiements() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Paiement p ORDER BY p.datePaiement DESC", Paiement.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Paiement> getPaiementsParStatut(StatutPaiement statut) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Paiement> query = em.createQuery(
                    "SELECT p FROM Paiement p WHERE p.statut = :statut", Paiement.class);
            query.setParameter("statut", statut);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    private String genererReferenceTransaction() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

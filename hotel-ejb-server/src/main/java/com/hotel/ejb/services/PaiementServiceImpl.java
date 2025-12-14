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
    public boolean traiterPaiement(Integer paiementId, String methodePaiement) {
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

    @Override
    public String genererFacture(Integer reservationId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Reservation reservation = em.find(Reservation.class, reservationId);
            if (reservation == null) {
                return "Réservation introuvable";
            }

            Paiement paiement = getPaiementParReservation(reservationId);

            StringBuilder facture = new StringBuilder();
            facture.append("==========================================\n");
            facture.append("           FACTURE HÔTEL                  \n");
            facture.append("==========================================\n\n");
            facture.append("Date: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
            facture.append("Numéro de réservation: ").append(reservation.getId()).append("\n\n");
            facture.append("------------------------------------------\n");
            facture.append("CLIENT:\n");
            facture.append("Nom: ").append(reservation.getClient().getNomComplet()).append("\n");
            facture.append("Email: ").append(reservation.getClient().getEmail()).append("\n");
            facture.append("Téléphone: ").append(reservation.getClient().getTelephone()).append("\n\n");
            facture.append("------------------------------------------\n");
            facture.append("DÉTAILS DE LA RÉSERVATION:\n");
            facture.append("Chambre: ").append(reservation.getChambre().getNumero()).append("\n");
            facture.append("Type: ").append(reservation.getChambre().getType().getLibelle()).append("\n");
            facture.append("Date d'arrivée: ").append(reservation.getDateArrivee()).append("\n");
            facture.append("Date de départ: ").append(reservation.getDateDepart()).append("\n");
            facture.append("Nombre de nuits: ").append(reservation.getNombreNuits()).append("\n");
            facture.append("Nombre de personnes: ").append(reservation.getNombrePersonnes()).append("\n\n");
            facture.append("------------------------------------------\n");
            facture.append("CALCUL:\n");
            facture.append("Prix par nuit: ").append(String.format("%.2f €", reservation.getChambre().getPrix())).append("\n");
            facture.append("Nombre de nuits: ").append(reservation.getNombreNuits()).append("\n");
            facture.append("TOTAL: ").append(String.format("%.2f €", reservation.getMontantTotal())).append("\n\n");

            if (paiement != null) {
                facture.append("------------------------------------------\n");
                facture.append("PAIEMENT:\n");
                facture.append("Statut: ").append(paiement.getStatut().getLibelle()).append("\n");
                if (paiement.getMethodePaiement() != null) {
                    facture.append("Méthode: ").append(paiement.getMethodePaiement()).append("\n");
                }
                if (paiement.getReferenceTransaction() != null) {
                    facture.append("Référence: ").append(paiement.getReferenceTransaction()).append("\n");
                }
            }

            facture.append("\n==========================================\n");
            facture.append("     Merci de votre visite !             \n");
            facture.append("==========================================\n");

            return facture.toString();
        } finally {
            em.close();
        }
    }

    @Override
    public Double calculerRevenusPeriode(LocalDate dateDebut, LocalDate dateFin) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Double> query = em.createQuery(
                    "SELECT SUM(p.montant) FROM Paiement p WHERE " +
                            "p.statut = :statut AND p.datePaiement >= :dateDebut AND p.datePaiement < :dateFin",
                    Double.class);
            query.setParameter("statut", StatutPaiement.PAYE);
            query.setParameter("dateDebut", dateDebut.atStartOfDay());
            query.setParameter("dateFin", dateFin.plusDays(1).atStartOfDay());

            Double result = query.getSingleResult();
            return result != null ? result : 0.0;
        } finally {
            em.close();
        }
    }

    private String genererReferenceTransaction() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

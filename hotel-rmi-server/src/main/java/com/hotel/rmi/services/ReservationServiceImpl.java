package com.hotel.rmi.services;

import com.hotel.entities.Reservation;
import com.hotel.enums.StatutReservation;
import com.hotel.interfaces.IReservationService;
import com.hotel.rmi.utils.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.util.List;

public class ReservationServiceImpl extends UnicastRemoteObject implements IReservationService {
    private static final long serialVersionUID = 1L;

    public ReservationServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public Reservation creerReservation(Reservation reservation) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            reservation.setMontantTotal(reservation.calculerMontantTotal());
            em.persist(reservation);
            em.getTransaction().commit();
            return reservation;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RemoteException("Erreur lors de la création de la réservation", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Reservation modifierReservation(Reservation reservation) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            reservation.setMontantTotal(reservation.calculerMontantTotal());
            Reservation updated = em.merge(reservation);
            em.getTransaction().commit();
            return updated;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RemoteException("Erreur lors de la modification de la réservation", e);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean annulerReservation(Integer reservationId) throws RemoteException {
        return changerStatutReservation(reservationId, StatutReservation.ANNULEE);
    }

    @Override
    public boolean confirmerReservation(Integer reservationId) throws RemoteException {
        return changerStatutReservation(reservationId, StatutReservation.CONFIRMEE);
    }

    private boolean changerStatutReservation(Integer reservationId, StatutReservation statut) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Reservation reservation = em.find(Reservation.class, reservationId);
            if (reservation != null) {
                reservation.setStatut(statut);
                em.merge(reservation);
                em.getTransaction().commit();
                return true;
            }
            em.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RemoteException("Erreur lors du changement de statut", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Reservation getReservationById(Integer id) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Reservation.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Reservation> getAllReservations() throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT r FROM Reservation r ORDER BY r.dateReservation DESC", Reservation.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Reservation> getReservationsParClient(Integer clientId) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                    "SELECT r FROM Reservation r WHERE r.client.id = :clientId ORDER BY r.dateReservation DESC",
                    Reservation.class);
            query.setParameter("clientId", clientId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Reservation> getReservationsParChambre(Integer chambreId) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                    "SELECT r FROM Reservation r WHERE r.chambre.id = :chambreId ORDER BY r.dateArrivee",
                    Reservation.class);
            query.setParameter("chambreId", chambreId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Reservation> getReservationsParStatut(StatutReservation statut) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                    "SELECT r FROM Reservation r WHERE r.statut = :statut", Reservation.class);
            query.setParameter("statut", statut);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Reservation> getReservationsPourPeriode(LocalDate dateDebut, LocalDate dateFin) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                    "SELECT r FROM Reservation r WHERE r.dateArrivee <= :dateFin AND r.dateDepart >= :dateDebut",
                    Reservation.class);
            query.setParameter("dateDebut", dateDebut);
            query.setParameter("dateFin", dateFin);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Reservation> getReservationsActives() throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            LocalDate aujourd = LocalDate.now();
            TypedQuery<Reservation> query = em.createQuery(
                    "SELECT r FROM Reservation r WHERE r.dateArrivee <= :aujourd AND r.dateDepart >= :aujourd " +
                            "AND r.statut = :statut", Reservation.class);
            query.setParameter("aujourd", aujourd);
            query.setParameter("statut", StatutReservation.EN_COURS);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void changerStatut(Integer reservationId, StatutReservation nouveauStatut) throws RemoteException {
        changerStatutReservation(reservationId, nouveauStatut);
    }
}
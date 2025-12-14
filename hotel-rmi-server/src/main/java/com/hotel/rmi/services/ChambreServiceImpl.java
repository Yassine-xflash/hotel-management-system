package com.hotel.rmi.services;

import com.hotel.entities.Chambre;
import com.hotel.entities.Reservation;
import com.hotel.enums.StatutReservation;
import com.hotel.enums.TypeChambre;
import com.hotel.interfaces.IChambreService;
import com.hotel.rmi.utils.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ChambreServiceImpl extends UnicastRemoteObject implements IChambreService {
    private static final long serialVersionUID = 1L;

    public ChambreServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public Chambre ajouterChambre(Chambre chambre) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(chambre);
            em.getTransaction().commit();
            return chambre;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RemoteException("Erreur lors de l'ajout de la chambre", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Chambre modifierChambre(Chambre chambre) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Chambre updated = em.merge(chambre);
            em.getTransaction().commit();
            return updated;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RemoteException("Erreur lors de la modification de la chambre", e);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean supprimerChambre(Integer chambreId) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Chambre chambre = em.find(Chambre.class, chambreId);
            if (chambre != null) {
                em.remove(chambre);
                em.getTransaction().commit();
                return true;
            }
            em.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RemoteException("Erreur lors de la suppression de la chambre", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Chambre getChambreById(Integer id) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Chambre.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public Chambre getChambreByNumero(String numero) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Chambre> query = em.createQuery(
                    "SELECT c FROM Chambre c WHERE c.numero = :numero", Chambre.class);
            query.setParameter("numero", numero);
            List<Chambre> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Chambre> getAllChambres() throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Chambre> query = em.createQuery("SELECT c FROM Chambre c", Chambre.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Chambre> getChambresDisponibles() throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Chambre> query = em.createQuery(
                    "SELECT c FROM Chambre c WHERE c.disponible = true", Chambre.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Chambre> getChambresDisponiblesParType(TypeChambre type) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Chambre> query = em.createQuery(
                    "SELECT c FROM Chambre c WHERE c.disponible = true AND c.type = :type", Chambre.class);
            query.setParameter("type", type);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Chambre> getChambresDisponiblesPourPeriode(LocalDate dateArrivee, LocalDate dateDepart) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Chambre> toutesLesChambres = getAllChambres();

            TypedQuery<Reservation> query = em.createQuery(
                    "SELECT r FROM Reservation r WHERE " +
                            "(r.dateArrivee <= :dateDepart AND r.dateDepart >= :dateArrivee) " +
                            "AND r.statut != :annulee", Reservation.class);
            query.setParameter("dateArrivee", dateArrivee);
            query.setParameter("dateDepart", dateDepart);
            query.setParameter("annulee", StatutReservation.ANNULEE);
            List<Reservation> reservations = query.getResultList();

            List<Integer> chambresReservees = reservations.stream()
                    .map(r -> r.getChambre().getId())
                    .collect(Collectors.toList());

            return toutesLesChambres.stream()
                    .filter(c -> !chambresReservees.contains(c.getId()))
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public boolean verifierDisponibilite(Integer chambreId, LocalDate dateArrivee, LocalDate dateDepart) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(r) FROM Reservation r WHERE " +
                            "r.chambre.id = :chambreId AND " +
                            "(r.dateArrivee <= :dateDepart AND r.dateDepart >= :dateArrivee) " +
                            "AND r.statut != :annulee", Long.class);
            query.setParameter("chambreId", chambreId);
            query.setParameter("dateArrivee", dateArrivee);
            query.setParameter("dateDepart", dateDepart);
            query.setParameter("annulee", StatutReservation.ANNULEE);

            Long count = query.getSingleResult();
            return count == 0;
        } finally {
            em.close();
        }
    }

    @Override
    public void mettreAJourDisponibilite(Integer chambreId, Boolean disponible) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Chambre chambre = em.find(Chambre.class, chambreId);
            if (chambre != null) {
                chambre.setDisponible(disponible);
                em.merge(chambre);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RemoteException("Erreur lors de la mise à jour de la disponibilité", e);
        } finally {
            em.close();
        }
    }
}

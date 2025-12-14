package com.hotel.rmi.services;

import com.hotel.entities.Client;
import com.hotel.interfaces.IClientService;
import com.hotel.rmi.utils.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ClientServiceImpl extends UnicastRemoteObject implements IClientService {
    private static final long serialVersionUID = 1L;

    public ClientServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public Client ajouterClient(Client client) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(client);
            em.getTransaction().commit();
            return client;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RemoteException("Erreur lors de l'ajout du client", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Client modifierClient(Client client) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Client updated = em.merge(client);
            em.getTransaction().commit();
            return updated;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RemoteException("Erreur lors de la modification du client", e);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean supprimerClient(Integer clientId) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Client client = em.find(Client.class, clientId);
            if (client != null) {
                em.remove(client);
                em.getTransaction().commit();
                return true;
            }
            em.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RemoteException("Erreur lors de la suppression du client", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Client getClientById(Integer id) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Client.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public Client getClientByEmail(String email) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Client> query = em.createQuery(
                    "SELECT c FROM Client c WHERE c.email = :email", Client.class);
            query.setParameter("email", email);
            List<Client> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Client> getAllClients() throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Client c", Client.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Client> rechercherClientsParNom(String nom) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Client> query = em.createQuery(
                    "SELECT c FROM Client c WHERE LOWER(c.nom) LIKE LOWER(:nom) OR LOWER(c.prenom) LIKE LOWER(:nom)",
                    Client.class);
            query.setParameter("nom", "%" + nom + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Object> getHistoriqueReservations(Integer clientId) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Object> query = em.createQuery(
                    "SELECT r FROM Reservation r WHERE r.client.id = :clientId ORDER BY r.dateReservation DESC",
                    Object.class);
            query.setParameter("clientId", clientId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
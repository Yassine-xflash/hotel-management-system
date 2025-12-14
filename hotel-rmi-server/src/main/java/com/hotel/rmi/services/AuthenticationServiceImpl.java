package com.hotel.rmi.services;

import com.hotel.entities.Utilisateur;
import com.hotel.enums.RoleUtilisateur;
import com.hotel.interfaces.IAuthenticationService;
import com.hotel.rmi.utils.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

public class AuthenticationServiceImpl extends UnicastRemoteObject implements IAuthenticationService {
    private static final long serialVersionUID = 1L;

    public AuthenticationServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public Utilisateur authentifier(String username, String motDePasse) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String hashedPassword = hashPassword(motDePasse);
            TypedQuery<Utilisateur> query = em.createQuery(
                    "SELECT u FROM Utilisateur u WHERE u.username = :username AND u.motDePasse = :password AND u.actif = true",
                    Utilisateur.class);
            query.setParameter("username", username);
            query.setParameter("password", hashedPassword);

            List<Utilisateur> results = query.getResultList();
            if (!results.isEmpty()) {
                Utilisateur utilisateur = results.get(0);
                em.getTransaction().begin();
                utilisateur.setDerniereConnexion(LocalDateTime.now());
                em.merge(utilisateur);
                em.getTransaction().commit();
                return utilisateur;
            }
            return null;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RemoteException("Erreur lors de l'authentification", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Utilisateur creerUtilisateur(String username, String motDePasse, RoleUtilisateur role) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            if (usernameExiste(username)) {
                throw new RemoteException("Le nom d'utilisateur existe déjà");
            }

            em.getTransaction().begin();
            Utilisateur utilisateur = new Utilisateur(username, hashPassword(motDePasse), role);
            em.persist(utilisateur);
            em.getTransaction().commit();
            return utilisateur;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RemoteException("Erreur lors de la création de l'utilisateur", e);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean modifierMotDePasse(Integer utilisateurId, String ancienMotDePasse, String nouveauMotDePasse) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Utilisateur utilisateur = em.find(Utilisateur.class, utilisateurId);
            if (utilisateur != null && utilisateur.getMotDePasse().equals(hashPassword(ancienMotDePasse))) {
                utilisateur.setMotDePasse(hashPassword(nouveauMotDePasse));
                em.merge(utilisateur);
                em.getTransaction().commit();
                return true;
            }
            em.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RemoteException("Erreur lors de la modification du mot de passe", e);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean desactiverUtilisateur(Integer utilisateurId) throws RemoteException {
        return changerStatutUtilisateur(utilisateurId, false);
    }

    @Override
    public boolean activerUtilisateur(Integer utilisateurId) throws RemoteException {
        return changerStatutUtilisateur(utilisateurId, true);
    }

    private boolean changerStatutUtilisateur(Integer utilisateurId, boolean actif) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Utilisateur utilisateur = em.find(Utilisateur.class, utilisateurId);
            if (utilisateur != null) {
                utilisateur.setActif(actif);
                em.merge(utilisateur);
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
    public boolean usernameExiste(String username) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(u) FROM Utilisateur u WHERE u.username = :username", Long.class);
            query.setParameter("username", username);
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    @Override
    public Utilisateur getUtilisateurById(Integer id) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Utilisateur.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public void deconnecter(Integer utilisateurId) throws RemoteException {
        System.out.println("Utilisateur " + utilisateurId + " déconnecté");
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du hashage du mot de passe", e);
        }
    }
}
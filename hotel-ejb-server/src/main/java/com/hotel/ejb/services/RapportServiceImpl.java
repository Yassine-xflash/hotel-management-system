package com.hotel.ejb.services;

import com.hotel.entities.Chambre;
import com.hotel.entities.Client;
import com.hotel.entities.Reservation;
import com.hotel.enums.StatutReservation;
import com.hotel.interfaces.IRapportService;
import com.hotel.ejb.utils.JPAUtil;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class RapportServiceImpl extends java.rmi.server.UnicastRemoteObject implements IRapportService {
    private static final long serialVersionUID = 1L;

    public RapportServiceImpl() throws java.rmi.RemoteException {
        super();
    }

    @Override
    public Map<String, Object> genererRapportOccupation(LocalDate dateDebut, LocalDate dateFin) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Map<String, Object> rapport = new HashMap<>();

            TypedQuery<Reservation> query = em.createQuery(
                    "SELECT r FROM Reservation r WHERE " +
                            "(r.dateArrivee <= :dateFin AND r.dateDepart >= :dateDebut) " +
                            "AND r.statut != :annulee", Reservation.class);
            query.setParameter("dateDebut", dateDebut);
            query.setParameter("dateFin", dateFin);
            query.setParameter("annulee", StatutReservation.ANNULEE);
            List<Reservation> reservations = query.getResultList();

            Long nombreChambres = em.createQuery("SELECT COUNT(c) FROM Chambre c", Long.class)
                    .getSingleResult();

            long nombreJours = ChronoUnit.DAYS.between(dateDebut, dateFin) + 1;

            long nuitesReservees = reservations.stream()
                    .mapToLong(Reservation::getNombreNuits)
                    .sum();

            long nuitesDisponibles = nombreChambres * nombreJours;
            double tauxOccupation = nuitesDisponibles > 0 ?
                    (nuitesReservees * 100.0) / nuitesDisponibles : 0.0;

            rapport.put("periode_debut", dateDebut.toString());
            rapport.put("periode_fin", dateFin.toString());
            rapport.put("nombre_chambres", nombreChambres);
            rapport.put("nombre_jours", nombreJours);
            rapport.put("nombre_reservations", reservations.size());
            rapport.put("nuits_reservees", nuitesReservees);
            rapport.put("nuits_disponibles", nuitesDisponibles);
            rapport.put("taux_occupation", String.format("%.2f%%", tauxOccupation));

            return rapport;
        } finally {
            em.close();
        }
    }

    @Override
    public Map<String, Object> genererRapportRevenus(LocalDate dateDebut, LocalDate dateFin) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Map<String, Object> rapport = new HashMap<>();

            TypedQuery<Double> revenusQuery = em.createQuery(
                    "SELECT SUM(p.montant) FROM Paiement p WHERE " +
                            "p.statut = :statut AND p.datePaiement >= :dateDebut AND p.datePaiement < :dateFin",
                    Double.class);
            revenusQuery.setParameter("statut", com.hotel.enums.StatutPaiement.PAYE);
            revenusQuery.setParameter("dateDebut", dateDebut.atStartOfDay());
            revenusQuery.setParameter("dateFin", dateFin.plusDays(1).atStartOfDay());
            Double revenus = revenusQuery.getSingleResult();
            revenus = revenus != null ? revenus : 0.0;

            TypedQuery<Long> countQuery = em.createQuery(
                    "SELECT COUNT(p) FROM Paiement p WHERE " +
                            "p.statut = :statut AND p.datePaiement >= :dateDebut AND p.datePaiement < :dateFin",
                    Long.class);
            countQuery.setParameter("statut", com.hotel.enums.StatutPaiement.PAYE);
            countQuery.setParameter("dateDebut", dateDebut.atStartOfDay());
            countQuery.setParameter("dateFin", dateFin.plusDays(1).atStartOfDay());
            Long nombrePaiements = countQuery.getSingleResult();

            double revenuMoyen = nombrePaiements > 0 ? revenus / nombrePaiements : 0.0;

            rapport.put("periode_debut", dateDebut.toString());
            rapport.put("periode_fin", dateFin.toString());
            rapport.put("revenus_totaux", String.format("%.2f €", revenus));
            rapport.put("nombre_paiements", nombrePaiements);
            rapport.put("revenu_moyen", String.format("%.2f €", revenuMoyen));

            return rapport;
        } finally {
            em.close();
        }
    }

    @Override
    public Map<String, Object> genererRapportClients() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Map<String, Object> rapport = new HashMap<>();

            Long nombreClients = em.createQuery("SELECT COUNT(c) FROM Client c", Long.class)
                    .getSingleResult();

            TypedQuery<Long> clientsAvecResaQuery = em.createQuery(
                    "SELECT COUNT(DISTINCT r.client) FROM Reservation r", Long.class);
            Long clientsAvecReservations = clientsAvecResaQuery.getSingleResult();

            rapport.put("nombre_total_clients", nombreClients);
            rapport.put("clients_avec_reservations", clientsAvecReservations);
            rapport.put("clients_sans_reservations", nombreClients - clientsAvecReservations);

            return rapport;
        } finally {
            em.close();
        }
    }

    @Override
    public Double calculerTauxOccupation(LocalDate dateDebut, LocalDate dateFin) {
        Map<String, Object> rapport = genererRapportOccupation(dateDebut, dateFin);
        String tauxStr = (String) rapport.get("taux_occupation");
        return Double.parseDouble(tauxStr.replace("%", ""));
    }

    @Override
    public Map<String, Object> getStatistiquesChambres() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Map<String, Object> stats = new HashMap<>();

            List<Chambre> chambres = em.createQuery("SELECT c FROM Chambre c", Chambre.class)
                    .getResultList();

            stats.put("nombre_total", chambres.size());
            stats.put("chambres_disponibles", chambres.stream()
                    .filter(Chambre::getDisponible).count());
            stats.put("chambres_occupees", chambres.stream()
                    .filter(c -> !c.getDisponible()).count());

            double prixMoyen = chambres.stream()
                    .mapToDouble(Chambre::getPrix)
                    .average()
                    .orElse(0.0);
            stats.put("prix_moyen", String.format("%.2f €", prixMoyen));

            Map<String, Long> repartition = chambres.stream()
                    .collect(Collectors.groupingBy(
                            c -> c.getType().getLibelle(),
                            Collectors.counting()
                    ));
            stats.put("repartition_par_type", repartition);

            return stats;
        } finally {
            em.close();
        }
    }

    @Override
    public Map<String, Object> getStatistiquesReservations(LocalDate dateDebut, LocalDate dateFin) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Map<String, Object> stats = new HashMap<>();

            TypedQuery<Reservation> query = em.createQuery(
                    "SELECT r FROM Reservation r WHERE r.dateReservation >= :dateDebut " +
                            "AND r.dateReservation < :dateFin", Reservation.class);
            query.setParameter("dateDebut", dateDebut.atStartOfDay());
            query.setParameter("dateFin", dateFin.plusDays(1).atStartOfDay());
            List<Reservation> reservations = query.getResultList();

            stats.put("nombre_total", reservations.size());

            Map<String, Long> repartitionStatut = reservations.stream()
                    .collect(Collectors.groupingBy(
                            r -> r.getStatut().getLibelle(),
                            Collectors.counting()
                    ));
            stats.put("repartition_par_statut", repartitionStatut);

            double dureeMoyenne = reservations.stream()
                    .mapToLong(Reservation::getNombreNuits)
                    .average()
                    .orElse(0.0);
            stats.put("duree_moyenne_sejour", String.format("%.1f nuits", dureeMoyenne));

            return stats;
        } finally {
            em.close();
        }
    }

    @Override
    public Map<String, Object> getTopClients(int nombre) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Map<String, Object> result = new HashMap<>();

            TypedQuery<Object[]> query = em.createQuery(
                    "SELECT r.client, COUNT(r), SUM(r.montantTotal) FROM Reservation r " +
                            "WHERE r.statut != :annulee " +
                            "GROUP BY r.client " +
                            "ORDER BY COUNT(r) DESC", Object[].class);
            query.setParameter("annulee", StatutReservation.ANNULEE);
            query.setMaxResults(nombre);

            List<Object[]> topClients = query.getResultList();
            List<Map<String, Object>> clientsList = new ArrayList<>();

            for (Object[] row : topClients) {
                Client client = (Client) row[0];
                Long nombreReservations = (Long) row[1];
                Double montantTotal = (Double) row[2];

                Map<String, Object> clientInfo = new HashMap<>();
                clientInfo.put("nom", client.getNomComplet());
                clientInfo.put("email", client.getEmail());
                clientInfo.put("nombre_reservations", nombreReservations);
                clientInfo.put("montant_total", String.format("%.2f €", montantTotal != null ? montantTotal : 0.0));

                clientsList.add(clientInfo);
            }

            result.put("top_clients", clientsList);
            return result;
        } finally {
            em.close();
        }
    }

    @Override
    public String exporterRapport(Map<String, Object> rapport) {
        StringBuilder sb = new StringBuilder();
        sb.append("==========================================\n");
        sb.append("            RAPPORT HÔTEL                 \n");
        sb.append("==========================================\n\n");

        for (Map.Entry<String, Object> entry : rapport.entrySet()) {
            sb.append(formatKey(entry.getKey())).append(": ");
            if (entry.getValue() instanceof Map) {
                sb.append("\n");
                @SuppressWarnings("unchecked")
                Map<String, Object> subMap = (Map<String, Object>) entry.getValue();
                for (Map.Entry<String, Object> subEntry : subMap.entrySet()) {
                    sb.append("  - ").append(subEntry.getKey()).append(": ").append(subEntry.getValue()).append("\n");
                }
            } else {
                sb.append(entry.getValue()).append("\n");
            }
        }

        sb.append("\n==========================================\n");
        return sb.toString();
    }

    private String formatKey(String key) {
        return key.replace("_", " ")
                .substring(0, 1).toUpperCase() + key.substring(1).replace("_", " ");
    }
}

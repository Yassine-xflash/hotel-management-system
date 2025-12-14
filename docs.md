# GUIDE UTILISATEUR - Système de Gestion d'Hôtel

## COMPTES D'ACCÈS

### Comptes Administrateurs et Employés

| Rôle               | Nom d'utilisateur | Mot de passe | Description                                    |
|--------------------|-------------------|--------------|------------------------------------------------|
| **Administrateur** | `admin`           | `admin123`   | Accès complet à toutes les fonctionnalités     |
| **Employé**        | `employe`         | `employe123` | Accès aux fonctionnalités de gestion quotidienne |
| **Client**         | `houda`           | `houda123`   | Accès à la fonctionnalité de réservation        |

---

## LANCEMENT DU SYSTÈME

### Ordre de démarrage

**Les serveurs DOIVENT être démarrés dans cet ordre :**

#### 1️- Serveur RMI (Port 1099)

**Via Terminal :**
```bash
cd hotel-rmi-server
mvn exec:java -Dexec.mainClass="com.hotel.rmi.server.RMIServer"
```

**Via IntelliJ :**
- Sélectionner la configuration `RMI Server`
- Cliquer sur ▶️ Run

**Attendre le message :**
```
=== Serveur RMI prêt ===
✓ Admin créé (admin/admin123)
✓ Employé créé (employe/employe123)
✓ 3 chambres ajoutées
```

---

#### 2️- Serveur EJB (Port 1100)

**Via Terminal :**
```bash
cd hotel-ejb-server
mvn exec:java -Dexec.mainClass="com.hotel.ejb.server.EJBServer"
```

**Via IntelliJ :**
- Sélectionner la configuration `EJB Server`
- Cliquer sur ▶️ Run

**Attendre le message :**
```
=== Serveur EJB prêt ===
```

---

#### 3️- Interface Client (JavaFX)

**Via Terminal :**
```bash
cd hotel-client-ui
mvn javafx:run
```

**Via IntelliJ :**
- Sélectionner la configuration `Hotel Client UI`
- Cliquer sur ▶️ Run

**Une fenêtre de connexion s'ouvre !**

---

## FONCTIONNALITÉS PAR RÔLE

### ADMINISTRATEUR (admin/admin123)

**Accès complet à 5 onglets :**

#### 1. Gestion des Chambres
- Voir toutes les chambres
- Ajouter de nouvelles chambres
- Modifier les chambres existantes
- Consulter la disponibilité

**Comment ajouter une chambre :**
1. Onglet "Chambres"
2. Cliquer sur "Ajouter Chambre"
3. Remplir :
    - Numéro (ex: 401)
    - Type (Simple, Double, Suite, Deluxe, Familiale)
    - Prix (ex: 150.00)
    - Étage (ex: 4)
    - Description
4. Cliquer sur "Sauvegarder"

---

#### 2. Gestion des Réservations
- Voir toutes les réservations
- Confirmer les réservations
- Annuler les réservations
- Filtrer par statut

**Statuts disponibles :**
- En attente
- Confirmée
- Annulée
- En cours
- Terminée

**Actions possibles :**
1. Sélectionner une réservation dans la liste
2. Cliquer sur "Confirmer" (passe en statut Confirmée)
3. Ou cliquer sur "Annuler" (passe en statut Annulée)

---

#### 3. Gestion des Clients
- Voir la liste complète des clients
- Rechercher des clients
- Consulter l'historique

**Informations affichées :**
- ID client
- Nom et prénom
- Email
- Téléphone
- Historique des réservations

---

#### 4. Paiements et Facturation
- Générer des factures détaillées
- Consulter l'historique des paiements
- Traiter les paiements

**Comment générer une facture :**
1. Onglet "Paiements"
2. Entrer l'ID de la réservation (ex: 1)
3. Cliquer sur "Générer Facture"
4. La facture s'affiche avec :
    - Informations client
    - Détails de la réservation
    - Calcul du montant
    - Statut du paiement


---

#### 5. Rapports et Statistiques
- Rapport d'occupation de l'hôtel
- Rapport des revenus
- Statistiques des chambres
- Top clients

**Rapport d'Occupation :**
1. Sélectionner une période (Date début → Date fin)
2. Cliquer sur "Rapport Occupation"
3. Affiche :
    - Nombre de chambres
    - Nombre de réservations
    - Nuits réservées
    - Taux d'occupation (%)

**Rapport des Revenus :**
1. Sélectionner une période
2. Cliquer sur "Rapport Revenus"
3. Affiche :
    - Revenus totaux
    - Nombre de paiements
    - Revenu moyen par paiement

---

### EMPLOYÉ (employe/employe123)

**Mêmes fonctionnalités que l'administrateur** pour la gestion quotidienne de l'hôtel.

---

### CLIENT

**Fonctionnalités :**
- Rechercher des chambres disponibles
- Filtrer par dates et type de chambre
- Voir les prix en temps réel
- Effectuer des réservations 

---


## BASE DE DONNÉES

**Type :** H2 Database (embarquée)  
**Localisation :** `~/hoteldb.mv.db`  
**Persistence :** JPA/Hibernate

**Pour réinitialiser la base de données :**
```bash
rm ~/hoteldb.mv.db
# Puis redémarrer les serveurs
```

---

## DÉPANNAGE

### "Connection refused" dans l'interface

**Cause :** Les serveurs RMI ou EJB ne sont pas démarrés

**Solution :**
1. Vérifier que les 2 serveurs sont lancés
2. Regarder les consoles pour confirmer qu'ils sont prêts
3. Vérifier les ports 1099 et 1100

---

### Port déjà utilisé (1099 ou 1100)

**Solution 1 - Arrêter le processus :**
```bash
# Linux/Mac
lsof -ti:1099 | xargs kill -9
lsof -ti:1100 | xargs kill -9

# Windows (PowerShell)
Get-Process -Id (Get-NetTCPConnection -LocalPort 1099).OwningProcess | Stop-Process
Get-Process -Id (Get-NetTCPConnection -LocalPort 1100).OwningProcess | Stop-Process
```

**Solution 2 - Changer les ports :**
- Modifier `RMIServer.java` (ligne PORT)
- Modifier `EJBServer.java` (ligne PORT)
- Modifier `ServiceManager.java` (constantes)

---

### JavaFX ne démarre pas

**Solution :**
Utiliser Maven au lieu de la configuration Run :
```bash
cd hotel-client-ui
mvn javafx:run
```

---

### Erreur de compilation

**Solution :**
```bash
# Recompiler tout le projet
mvn clean install
```

---

## WORKFLOW TYPIQUE

### Scénario : Gérer une nouvelle réservation

1. **Connexion** avec `admin` / `admin123`

2. **Vérifier les chambres disponibles**
    - Onglet "Chambres"
    - Cliquer sur "Actualiser"
    - Noter les chambres disponibles

3. **(Si nécessaire) Ajouter un client**
    - Onglet "Clients"
    - Vérifier si le client existe

4. **Consulter la réservation**
    - Onglet "Réservations"
    - Trouver la réservation en attente

5. **Confirmer la réservation**
    - Sélectionner la réservation
    - Cliquer sur "Confirmer"

6. **Générer la facture**
    - Onglet "Paiements"
    - Entrer l'ID de la réservation
    - Cliquer sur "Générer Facture"
    - Imprimer ou sauvegarder

7. **Consulter les statistiques**
    - Onglet "Rapports"
    - Sélectionner la période
    - Générer les rapports

---

## POUR VOTRE PRÉSENTATION

### Points clés à démontrer :

1. **Architecture distribuée**
    - Montrer les 2 serveurs qui tournent (RMI + EJB)
    - Expliquer la séparation des responsabilités

2. **Authentification sécurisée**
    - Se connecter avec admin
    - Montrer les différents rôles

3. **Gestion complète**
    - Ajouter une chambre
    - Consulter les réservations
    - Générer une facture

4. **Rapports et statistiques**
    - Montrer le rapport d'occupation
    - Montrer le rapport des revenus

5. **Base de données**
    - Expliquer JPA/Hibernate
    - Montrer que les données persistent

---

## INFORMATIONS TECHNIQUES

### Architecture
- **Backend RMI :** Services de base (Chambres, Clients, Réservations, Auth)
- **Backend EJB :** Services avancés (Paiements, Rapports)
- **Frontend :** JavaFX (Interface graphique)
- **Persistence :** JPA/Hibernate + H2 Database

### Technologies
- Java 11
- Maven
- RMI (Remote Method Invocation)
- EJB (Enterprise JavaBeans)
- JavaFX 17
- Hibernate 5.6
- H2 Database 2.1

### Ports utilisés
- **1099** - Serveur RMI
- **1100** - Serveur EJB


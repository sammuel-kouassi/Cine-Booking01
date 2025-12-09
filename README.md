# 🎬 Cine-Booking Backend API

Système backend complet et robuste pour la gestion de réservations de cinéma. Ce projet a été conçu avec une approche **TDD (Test Driven Development)** et met en œuvre des logiques métier complexes telles que la gestion de la concurrence des sièges et la planification intelligente des séances.

## 🚀 Fonctionnalités

### 👤 Utilisateurs (Clients)

  * **Authentification :** Inscription et Connexion sécurisée via JWT (Stateless).
  * **Catalogue :** Consultation des films disponibles.
  * **Réservation :**
      * Visualisation du plan de salle en temps réel (Sièges Libres/Occupés).
      * Sélection et réservation de places.
      * **Gestion de concurrence :** Empêche la surréservation (double booking) d'un même siège.
  * **Gestion :** Consultation de l'historique des réservations et annulation (sous conditions temporelles).

### 🛡️ Administrateurs

  * **Gestion du catalogue :** Ajout de films.
  * **Planification :** Création de séances avec vérification automatique des chevauchements horaires dans une même salle.
  * **Gestion des utilisateurs :** Promotion d'utilisateurs au rang d'Admin.
  * **Business Intelligence :** Rapports financiers (Revenus par film, taux d'occupation).

-----

## 🛠️ Stack Technique

  * **Langage :** Java 21+
  * **Framework :** Spring Boot 3.4 (Web, Data JPA, Security, Validation)
  * **Base de Données :** Oracle Database 23ai (Production) / H2 (Tests)
  * **Sécurité :** Spring Security + JJWT (JSON Web Token) + BCrypt
  * **Tests (TDD) :** JUnit 5, Mockito, Spring Security Test
  * **Outils :** Maven, Lombok

-----

## ⚙️ Prérequis

Avant de lancer le projet, assurez-vous d'avoir :

1.  **Java JDK 21** (ou version supérieure).
2.  **Maven**.
3.  Une instance **Oracle Database** (Local ou Docker) en cours d'exécution.

-----

## 💾 Installation et Configuration

### 1\. Cloner le projet

```bash
git clone https://github.com/sammuel-kouassi/Cine-Booking01.git
cd Cine-Booking01
```

### 2\. Configuration de la Base de Données (Oracle)

Connectez-vous à votre base Oracle (via `sqlplus` ou SQL Developer) en tant qu'administrateur (`sysdba`) et créez l'utilisateur dédié :

```sql
-- Si vous utilisez Oracle 23ai Free ou XE
ALTER SESSION SET CONTAINER = FREEPDB1; -- (Optionnel selon config)

CREATE USER cine_user IDENTIFIED BY cine_pass;
GRANT CONNECT, RESOURCE, DBA TO cine_user;
GRANT UNLIMITED TABLESPACE TO cine_user;
EXIT;
```

### 3\. Configuration de l'application

Vérifiez le fichier `src/main/resources/application.properties`. Assurez-vous que l'URL correspond à votre configuration Oracle (notamment le nom du service, ex: `FREE`, `XE`, `ORCL`).

```properties
spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/FREE
spring.datasource.username=cine_user
spring.datasource.password=cine_pass
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# Hibernate créera les tables automatiquement
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.OracleDialect
```

### 4\. Lancement

```bash
mvn spring-boot:run
```

*Au premier démarrage, un script `DataInitializer` s'exécutera automatiquement pour créer un Administrateur par défaut et une Salle de test.*

-----

## 🔑 Utilisation de l'API

L'API est sécurisée. La plupart des requêtes nécessitent un **Bearer Token** dans le header `Authorization`.

### 1️⃣ Authentification (Pour récupérer le Token)

| Méthode | URL | Body (JSON) | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/login` | `{ "email": "admin@cine.com", "password": "admin123" }` | **Connexion Admin** (Compte par défaut) |
| `POST` | `/api/auth/register` | `{ "fullName": "Nom", "email": "test@test.com", "password": "pass" }` | **Inscription Client** |

### 2️⃣ Parcours Client

| Méthode | URL | Description |
| :--- | :--- | :--- |
| `GET` | `/api/movies` | Voir tous les films |
| `GET` | `/api/screenings/{id}/seats` | Voir le plan de la salle (Dispo/Réservé) |
| `POST` | `/api/bookings` | Réserver : `{ "screeningId": 1, "seatIds": [1, 2] }` |
| `GET` | `/api/bookings/me` | Voir mes réservations |
| `DELETE`| `/api/bookings/{id}` | Annuler une réservation |

### 3️⃣ Parcours Administrateur (Token Admin requis)

| Méthode | URL | Description |
| :--- | :--- | :--- |
| `POST` | `/api/admin/movies` | Ajouter un film |
| `POST` | `/api/admin/screenings` | Planifier une séance (avec check de conflit) |
| `GET` | `/api/admin/reports/revenue` | Voir le chiffre d'affaires par film |
| `PUT` | `/api/admin/users/{id}/promote` | Promouvoir un utilisateur en Admin |

-----

## 🧪 Architecture et Choix Techniques

### Modèle de Données (Entity Relationship)

Le projet utilise un modèle relationnel optimisé pour Oracle :

  * **CinemaHall (Salle) 1--N Seat (Siège)** : Structure physique statique.
  * **Screening (Séance)** : Lie un Film à une Salle à une heure précise.
  * **Booking (Réservation)** : Lie un Utilisateur à une Séance.
  * **BookingSeat** : Table de jointure critique. La validation de la disponibilité se fait ici pour garantir l'intégrité des données.

### Gestion de la Concurrence

Pour éviter que deux utilisateurs réservent le même siège en même temps :

1.  Utilisation de transactions `@Transactional`.
2.  Vérification stricte en base de données avant insertion.
3.  Gestion des exceptions (`BusinessException`) renvoyant un code HTTP 409 (Conflict).

### Tests (TDD)

Le projet a été développé en suivant le cycle **Red-Green-Refactor**.

  * **Tests Unitaires (Mockito)** : Couvrent toute la logique métier (Services).
  * **Tests d'Intégration (@WebMvcTest, @DataJpaTest)** : Valident les Contrôleurs et les requêtes SQL complexes.
  * Utilisation de **H2 Database** pour des tests rapides et isolés.

-----

## ⚠️ Dépannage (Troubleshooting)

**Erreur `ORA-12541: TNS:no listener`**

  * Le service Oracle n'est pas démarré. Vérifiez `services.msc` sous Windows.

**Erreur `ORA-12514: listener does not know of service`**

  * L'application pointe vers un mauvais nom de service (ex: `FREEPDB1` au lieu de `FREE`).
  * Vérifiez le nom exact avec la commande SQL : `SELECT name FROM v$services;`

-----

## 👨‍💻 Auteur

Développé par **KOUASSI-SAMMUEL** - Étudiant en Génie Logiciel / Développeur Backend.
*Passionné par Java, Spring Boot et les architectures robustes.*

# KFOKAM48 — Suivi de présence, exercices et relectures entre pairs

Application de gestion d'une session de cours : le formateur ouvre une session et
diffuse un code de présence, les étudiants marquent leur présence et déposent le lien
de leur exercice, un pair relit l'exercice et rend une note, le formateur suit
l'ensemble dans un tableau récapitulatif.

- **Backend :** Java 21 LTS · Spring Boot 3.5.5 · Maven (wrapper `mvnw` commité) · PostgreSQL 16 · Flyway
- **Frontend :** React 18 + Vite 6 · CSS natif (aucune bibliothèque UI)

---

## 1. Prérequis

| Outil | Version | Vérification |
|---|---|---|
| Java | 17 ou plus (**21 utilisé**) | `java -version` |
| Docker Desktop | récent | `docker version` |
| Node.js | 18 ou plus (**22 utilisé**) | `node --version` |

Si `JAVA_HOME` n'est pas défini, Maven ne démarre pas. Sous PowerShell :

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
```

---

## 2. Démarrage — trois commandes

Depuis la racine du dépôt :

```bash
docker compose up -d
```

```bash
cd backend && ./mvnw spring-boot:run
```

```bash
cd frontend && npm install && npm run dev
```

Puis ouvrir <http://localhost:5173>.

Le schéma et les données de démonstration sont créés automatiquement au démarrage
du backend (migrations Flyway `V1` et `V2`). **Rien d'autre à lancer.**

> Sous Windows, remplacer `./mvnw` par `mvnw.cmd`.

---

## 3. Frontend

### Pourquoi React + Vite (contrainte F1)

React + Vite a été retenu pour construire une interface SPA légère, structurée en
composants, avec un outillage de développement et de build simple et adapté au
périmètre de l'épreuve. Vite démarre en moins d'une seconde et produit un build de
production en quelques secondes ; l'écosystème React est le plus répandu, ce qui
reste maintenable et compréhensible à l'oral. Aucun choix de design n'a été fait au
détriment de la conformité fonctionnelle.

### Architecture

```text
frontend/src/
├── api/          Couche API isolée (contrainte F3)
│   ├── client.js   client HTTP + normalisation des erreurs {code, message}
│   └── index.js    un service par domaine métier
├── components/   Composants réutilisables (boutons, champs, badges, alertes, tableaux)
├── context/      Contexte d'identité (aucune authentification — Q1)
├── features/     Écrans : formateur, étudiant, relecteur
├── hooks/        useRequete (chargement / erreur) et useEnvoi (soumission)
├── styles/       Design system CSS natif + media queries
└── app/          Coquille et navigation entre les trois rôles
```

**Aucun composant ne contient d'appel `fetch` direct** : tout passe par `src/api/`.
C'est la contrainte F3 du sujet.

### Les trois écrans (contrainte F2)

- **Formateur** : promotion, ouverture d'une session et affichage du code, ajout
  manuel d'une présence, clôture de la session, tableau récapitulatif.
- **Étudiant** : choix de son nom, marquage de la présence avec le code, dépôt d'un
  exercice, consultation de ses notes et commentaires.
- **Relecteur** : liste des exercices assignés, démarrage de la relecture, notation
  de 0 à 20 avec commentaire.

### Configuration

L'URL de l'API se configure par variable d'environnement. Un fichier
`.env.example` est fourni :

```
VITE_API_BASE_URL=http://localhost:8080
```

En développement, le proxy Vite (`vite.config.js`) relaie `/api` vers le backend :
aucun problème de CORS côté navigateur. Une configuration CORS est également
prévue côté backend pour les ports de développement usuels.

---

## 4. Backend

- **Architecture en couches** : `Controller → Service → Repository → Database`
- **Aucune entité exposée en JSON** : des DTO dédiés par route
- **Erreurs centralisées** : toutes les réponses d'erreur au format imposé
  `{ "code": "...", "message": "..." }`, sans jamais exposer de stack trace
- **Schéma versionné** par Flyway — `ddl-auto` n'est jamais utilisé

### Base de données

PostgreSQL 16 conteneurisé. Le port hôte est **5434** (le 5432 peut être déjà
occupé par une installation locale) ; le conteneur écoute en interne sur 5432.
Seeds : 1 promotion, 12 étudiants, 2 sessions, des présences, des exercices et des
relectures déjà notées, pour que l'application soit immédiatement exploitable.

---

## 5. Tests

**Test unitaire** (règle métier RG2 — un étudiant ne relit jamais son propre
exercice) :

```bash
cd backend && ./mvnw test
```

Le test s'exécute sur poste vierge : il ne dépend ni de Docker, ni d'une base locale.

---

## 6. Structure du dépôt

```text
/
├── api/contrat.yaml        Contrat d'API (5 opérations imposées + zone libre)
├── backend/                Spring Boot (Java 21, Maven, migrations)
├── frontend/               React + Vite
├── docs/                   Cahier des charges, journal, diagrammes, rapports
├── docker-compose.yml      PostgreSQL 16
└── SOUMISSION.md
```

---

## 7. Documentation

- `docs/CAHIER_DES_CHARGES.md` — exigences (EF), règles de gestion (RG), zones d'ombre et contradictions tranchées
- `docs/JOURNAL.md` — avancement par étape
- `docs/diagrammes/` — cas d'utilisation, modèle de données, séquence, états-transitions
- `docs/rapports/` — rapports d'audit et d'analyse
- `api/contrat.yaml` — référence contractuelle de l'API

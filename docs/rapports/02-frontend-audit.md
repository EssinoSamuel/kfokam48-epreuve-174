# Rapport 02 — Audit préalable au développement du frontend KFOKAM48

**Date :** 26/09/2026  
**Auteur :** ESSINO Samuel · 174  
**Projet :** Suivi de présence, exercices et relectures entre pairs  
**Périmètre :** Transition Backend → Frontend React + Vite  

---

## 1. État exact du backend existant

Le backend a été conçu, implémenté et validé sur machine réelle :
- **Stack :** Java 21 LTS, Spring Boot 3.5.5, Maven avec wrapper `mvnw` commité et fonctionnel.
- **Base de données :** PostgreSQL 16-alpine conteneurisé via `docker-compose.yml`, écoutant sur le port hôte **5434** (`jdbc:postgresql://localhost:5434/kfokam48`) pour éviter tout conflit avec une instance PostgreSQL locale sur le port 5432.
- **Migrations Flyway :**
  - `V1__schema_initial.sql` : 7 tables relationnelles avec contraintes CHECK d'intégrité (unicité des présences, unicité du dépôt, note entre 0 et 20, cycle de statuts, absence d'auto-relecture).
  - `V2__donnees_demonstration.sql` : 1 promotion (« Fullstack KFOKAM48 — Promotion 2026 »), 12 étudiants fixes (id 1 à 12, dont ESSINO Samuel id=1), 2 sessions (Session 1 clôturée avec présences, exercices et relectures notées ; Session 2 ouverte avec code actif `KF48B2`).
- **Tests de fumée exécutés :**
  - Démarrage Spring Boot réussi (`Started EpreuveApplication in 111.755 seconds`).
  - Connexion effective à PostgreSQL 16.14 et exécution sans erreur des migrations V1 et V2.
  - Endpoints vérifiés en direct : `GET /api/sante` (200), `GET /api/promotions` (200), `GET /api/sessions?promotionId=1` (200), `GET /api/sessions/1/tableau` (200), `POST /api/presences` avec code inconnu (400), `POST /api/presences` avec code expiré (410).

---

## 2. Inventaire des endpoints et ajustements contractuels

| Opération API | Verbe & Chemin | Statut Contrat | Statut Backend Actuel | Ajustement Requis |
|---|---|---|---|---|
| Ouvrir session | `POST /api/sessions` | **Imposé** | Implémenté (`SessionController`) | Conforme |
| Marquer présence | `POST /api/presences` | **Imposé** | Implémenté (`PresenceController`) | Conforme |
| Déposer exercice | `POST /api/exercices` | **Imposé** | Implémenté (`ExerciceController`) | Conforme |
| Rendre relecture | `POST /api/relectures/{id}` | **Imposé** | Implémenté sous `/{id}/note` | **Alignement** sur `POST /api/relectures/{id}` |
| Tableau récapitulatif | `GET /api/tableau?promotionId=` | **Imposé** | Implémenté sous `/api/sessions/{id}/tableau` | **Ajout** de `GET /api/tableau?promotionId=` |
| Clôturer session | `PATCH /api/sessions/{id}/cloture` | Zone libre (H2) | Implémenté (`SessionController`) | Conforme |
| Présence formateur | `POST /api/sessions/{id}/presences` | Zone libre (H3) | Implémenté sous `POST /api/presences/formateur` | **Alignement** sur `POST /api/sessions/{id}/presences` |
| Remplacer lien | `PUT /api/exercices/{id}` | Zone libre (Q13) | Implémenté (`ExerciceController`) | Conforme |
| Démarrer relecture | `POST /api/relectures/{id}/demarrer` | Zone libre (H4) | Implémenté sous `POST /api/relectures` (body id) | **Ajout de la route exacte** `POST /api/relectures/{id}/demarrer` |
| Référentiel UI | `GET /api/promotions`, `/etudiants` | Zone libre (H9) | Implémenté (`ReferentielController`) | Conforme |
| Sessions par promo | `GET /api/sessions?promotionId=` | Zone libre (H9) | Implémenté (`SessionController`) | Conforme |

> **Ajustement technique préalable :** Pour garantir la conformité B2 et F3, nous exposons les alias stricts pour `GET /api/tableau?promotionId=`, `POST /api/relectures/{id}` et `POST /api/relectures/{id}/demarrer`, ainsi qu'une configuration **CORS** autorisant `http://localhost:5173`.

---

## 3. Structure des réponses et gestion centralisée des erreurs

Toutes les erreurs du backend respectent le format imposé :
```json
{
  "code": "CODE_STABLE",
  "message": "Explication en français compréhensible par un humain"
}
```
Les statuts et codes métiers traités côté client :
- `400` : `CODE_INCONNU`, `LIEN_INVALIDE`, `NOTE_INVALIDE`, `TROP_DE_TENTATIVES`, `REQUETE_INVALIDE`
- `403` : `AUTO_RELECTURE`
- `404` : `PROMOTION_INCONNUE`, `SESSION_INCONNUE`, `ETUDIANT_INCONNU`, `EXERCICE_INCONNU`, `RELECTURE_INCONNUE`
- `409` : `DEJA_PRESENT`, `EXERCICE_DEJA_DEPOSE`, `RELECTURE_DEJA_RENDUE`, `RELECTURE_DEJA_COMMENCEE`, `SESSION_CLOTUREE`, `SESSION_DEJA_CLOTUREE`
- `410` : `CODE_EXPIRE`

---

## 4. Parcours des trois rôles utilisateur (Contrainte F2)

### Écran 1 : Formateur
- Sélecteur de promotion (données V2 : Promotion 1).
- Consultation du tableau de bord complet (Nom, Présences, Exercices déposés, Moyenne, Relectures en attente).
  - **Règle F3 :** la moyenne affichée provient exclusivement du champ `moyenne` de l'API. Si `null`, affichage de « — » (jamais 0).
- Ouverture d'une nouvelle session : saisie du titre, affichage grand format du code de présence généré (6 caractères) et du timer d'expiration (15 min).
- Ajout manuel d'une présence : sélection d'un étudiant pour la session courante (badge « Ajouté par le formateur »).
- Clôture manuelle de la session (Q12) : borne le dépôt d'exercice et met à jour l'interface.

### Écran 2 : Étudiant
- Choix de son identité (Q1, pas de mot de passe) dans la liste déroulante des étudiants.
- Marquage de présence : saisie du code, gestion claire des messages d'erreur (400, 410, 409, et blocage 2 min après 5 tentatives incorrectes - RG10).
- Dépôt de l'exercice : sélection de la session, saisie de l'URL du dépôt.
- Consultation de ses exercices : état d'avancement, note et commentaire reçus (garantissant l'anonymat du relecteur - RG11).
- Remplacement du lien (EF6/Q13) : modification possible tant que la session n'est pas clôturée et que la relecture n'a pas débuté.

### Écran 3 : Relecteur
- Choix de son identité d'étudiant relecteur.
- Consultation des exercices qui lui ont été assignés au hasard parmi les présents (Q7).
- Action « Démarrer la relecture » : verrouille le lien de l'exercice pour l'auteur (statut `EN_COURS`).
- Notation et commentaire : saisie d'une note entière de 0 à 20 et d'un texte d'évaluation.
- Validation finale irréversible (Q15).

---

## 5. Architecture Frontend retenue (React + Vite)

```text
frontend/
├── src/
│   ├── api/             # Couche API isolée (client.js, endpoints, gestion erreurs)
│   ├── components/      # UI réutilisable (Button, Card, Badge, Alert, Modal, Table)
│   ├── context/         # AppContext (rôle sélectionné, promotion, étudiant actif)
│   ├── features/        # Composants métier par rôle (Formateur, Etudiant, Relecteur)
│   ├── styles/          # Variables CSS, design system SaaS moderne, responsive mobile
│   ├── App.jsx          # Routage par onglet de rôle et orchestration
│   └── main.jsx         # Montage React
├── vite.config.js       # Proxy HTTP vers http://localhost:8080
└── package.json         # Scripts dev et build
```

---

## 6. Plan d'exécution des phases

- **Phase 0 :** Audit préalable validé (ce document).
- **Phase 1 :** Initialisation du projet `frontend/` (Vite, React, package.json, proxy).
- **Phase 2 :** Mise en place du Design System CSS moderne et des composants UI.
- **Phase 3 :** Création de la couche API centralisée et du contexte d'identité.
- **Phase 4 :** Implémentation du parcours Formateur.
- **Phase 5 :** Implémentation du parcours Étudiant.
- **Phase 6 :** Implémentation du parcours Relecteur.
- **Phase 7 :** Intégration globale, validation du build (`npm run build`) et test direct avec le backend actif.


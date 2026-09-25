# Journal de bord — 174 (ESSINO Samuel)

> Une entrée **par étape**, écrite **au moment où tu la termines**, pas à la fin de la journée.
> Chaque entrée répond aux trois mêmes questions : **Fait** / **Bloqué** (et combien de
> temps) / **IA** (ce qui a été demandé, et comment la réponse a été vérifiée).

---

## Étape 1 — Analyse et conception

**Fait :** cahier des charges (10 exigences fonctionnelles, 11 règles de gestion, dix
sections), quatre diagrammes en texte (D1 cas d'utilisation en PlantUML ; D2 modèle de
données, D3 séquence « marquer sa présence », D4 états-transitions en Mermaid — bonus),
contrat `api/contrat.yaml` complété (5 opérations imposées + 10 opérations de zone libre),
backlog de 15 issues, `.gitignore` posé avant tout code, commit `[JALON] analyse` poussé.

**Bloqué :** ~40 min sur la contradiction **Q10 / Q15** (correction de note possible vs
note définitive). Tranchée en faveur de **Q15** : le contrat imposé ne prévoit qu'un seul
`POST /api/relectures/{id}`, sans verbe de mise à jour, avec `409 RELECTURE_DEJA_RENDUE`.
~1 h 30 sur les exigences client **non couvertes par le contrat imposé** : clôture de
session (Q12), présence ajoutée par le formateur (Q14), « personne n'a commencé à le
relire » (Q13), visibilité des exercices en attente (Q11), listes de lecture des trois
écrans (Q1). Chacune a été analysée (source → interprétations → conséquences sur le
modèle, l'API, les tests et le barème) puis tranchée et documentée en section 7 (H2–H10).

**IA :** analyses contradictoires demandées sur la clôture de session, RG10 et EF6, puis
sur les exigences non couvertes. Vérification : relecture du corpus ligne à ligne
(CLIENT.md, contrat, sujet), contrôle de chaque ajout contre les cinq opérations gelées
(chemins, verbes, codes de statut, format d'erreur — B2), validation explicite avant
toute écriture dans le cahier des charges et le contrat.

---

## Étape 2 — Première version

*(avancement en cours — cette entrée est mise à jour au fil de l'étape)*

**Fait :**
- socle backend **Spring Boot 3.5.5 / Java 21** : `pom.xml` (web, JPA, validation, Flyway,
  actuator, H2 en scope test) et wrapper **`mvnw` commité avec son jar** — le formateur
  n'installe rien (contrainte B1) ;
- **PostgreSQL 16** via `docker compose` sur le port hôte **5434** (le 5432 est occupé par
  le PostgreSQL natif du poste) : healthcheck `pg_isready`, volume nommé, démarrage en une
  commande ;
- migrations **Flyway V1** (schéma à 7 tables aligné au mot près sur D2 ; les RG sont
  portées par des contraintes CHECK) et **V2** (données de démonstration versionnées :
  1 promotion, 12 étudiants, 2 sessions, 10 présences, 5 dépôts, 5 relectures) ;
  placeholders Flyway pour que les mêmes migrations servent aussi au H2 des tests ;
- **API REST complète** — 15 routes : les 5 opérations imposées strictement intactes
  (chemins, verbes, codes de statut, format d'erreur — B2) + 10 opérations de zone libre ;
  architecture en couches, DTO exclusifs (jamais d'entité exposée — B3), erreurs
  centralisées au format `{code, message}` (B4), horloge injectable pour tester les
  durées sans attendre ;
- **BUILD SUCCESS** : `mvnw compile`, 45 sources en javac release 21 ;
- fiche `SOUMISSION.md` : hash `0850cc2…` relevé, base PostgreSQL confirmée ;
- 4 commits atomiques pour ce socle (`build(backend)` → `feat(infra)` → `feat(api)`),
  journal compris.

**Bloqué :** ~20 min sur `JAVA_HOME` absent de l'environnement (le JDK 21 est bien
installé mais non exposé dans les variables système) — export manuel
`JAVA_HOME=C:\Program Files\Java\jdk-21` avant chaque build, à documenter dans le README.
Le daemon Docker n'a pas répondu depuis l'atelier : la validation réelle contre
PostgreSQL (démarrage + fumée des endpoints) reste à faire depuis le poste.

**IA :** demandé — relevé du hash du dernier commit dans `SOUMISSION.md`, puis commits
professionnels du backend avant d'enchaîner sur le frontend ; les tests sont volontairement
arrêtés là pour reprendre plus tard. Vérification : `git log --oneline` et `git status`
après chaque commit, `BUILD SUCCESS` javac, relecture des messages (`build` / `feat` /
`docs` avec corps explicatif), suppression de deux fichiers morts avant finalisation.

**Reste pour clore le backend (étape 2)** : tests unitaires (RG2, validateur de lien)
et d'intégration H2 (201/400/409/410, RG10, clôture, tableau) ; validation réelle
PostgreSQL (`docker compose up -d` + fumée des endpoints) ; `README.md` (3 commandes max,
framework justifié) avant le jalon `v0.1`.

---

## Étape 3 — Enveloppe

**Fait :**

**Bloqué :**

**IA :**

**Ce que j'ai sorti du périmètre pour absorber le changement, et pourquoi :**

---

## Étape 4 — Version finale

**Fait :**

**Bloqué :**

**IA :**

---

## Étape 5 — Épreuve Git

**Fait :**

**Bloqué :**

**IA :**

---

## Étape 6 — Soumission

**Fait :**

**Ce que je referais autrement avec une journée de plus :**

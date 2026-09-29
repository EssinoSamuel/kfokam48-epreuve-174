# Journal des modifications — KFOKAM48, matricule 174

Ce fichier suit l'ordre exact de l'historique Git (`git log --oneline`). Chaque
section correspond à un jalon de l'épreuve.

## [Analyse] — étape 1 — commit `[JALON] analyse`

- Cahier des charges (exigences fonctionnelles, règles de gestion, zones d'ombre
  tranchées), contrat d'API complété (5 opérations imposées intactes + zone libre),
  quatre diagrammes, backlog d'issues.
- `.gitignore` posé **avant** le premier commit de code.

## [0.1] — première version complète — étape 2 — commit `[JALON] v0.1`

### Ajouté

- Backend **Spring Boot 3.5.5 / Java 21**, architecture en couches
  (`Controller → Service → Repository`), DTO exclusifs par route, erreurs
  centralisées au format `{code, message}`. Wrapper `mvnw` commité : le correcteur
  n'installe rien.
- **PostgreSQL 16** via `docker compose` (port hôte 5434) et migrations Flyway
  `V1` (schéma) et `V2` (données de démonstration).
- Les **5 opérations imposées** du contrat, strictement conformes (chemins, verbes,
  codes de statut, format d'erreur) et la zone libre (tableau récapitulatif,
  listes, ajout manuel d'une présence, clôture de session).
- Frontend **React 18 + Vite 6**, trois écrans (formateur, étudiant, relecteur),
  CSS natif, appels API isolés dans `src/api/` (contrainte F3).
- Test unitaire `TirageAuSortTest` (RG2 : un étudiant ne relit jamais son propre
  exercice, ni deux fois le même).
- `README.md` d'installation et `SOUMISSION.md`.

## [1.0] — version finale — étape 4 — commit `[JALON] v1.0`

### Ajouté

- **Relecture par deux pairs** (enveloppe étape 3, sujet B) : chaque exercice
  déposé est assigné à **deux étudiants distincts**, tirés au sort parmi les
  présents, jamais l'auteur.
- Migration Flyway **`V3__relecture_par_deux_pairs.sql`** — fichier **ajouté**,
  aucun fichier déjà appliqué n'est modifié : suppression de la contrainte
  d'unicité héritée de Q6/RG4, index sur `relecture (exercice_id)`, vue
  `moyenne_exercice` (moyenne, nombre de notes rendues, statut) et attribution
  d'un second relecteur aux exercices déjà en attente. La migration survit à une
  base déjà remplie : aucune donnée existante n'est supprimée.
- **Moyenne calculée côté serveur** et exposée par l'API : `moyenne`,
  `notesRendues`, `statutNote` (`PROVISOIRE` / `DEFINITIVE`) et la liste des
  commentaires rendus. Le frontend n'effectue aucun calcul (contrainte F3).
- Écran étudiant : badge « Note provisoire » et libellé « 1 relecture sur 2 »
  tant que les deux pairs n'ont pas rendu.

### Corrigé

- **#16 — le formateur ne pouvait ajouter aucune présence à la main.** L'écran
  appelait `POST /api/sessions/{id}/presences`, chemin **imposé par le contrat** ;
  le backend n'exposait ce traitement que sous `POST /api/presences/session/{id}` :
  réponse **404**, aucun étudiant marquable (EF3, RG9, Q14). Un contrôleur dédié
  porte désormais le chemin du contrat.
  Le bug a d'abord été **reproduit par un test qui échouait**
  (`PresenceFormateurIT` : 3 erreurs) avant d'être corrigé (0 erreur).

### Modifié

- Cahier des charges mis à jour en conséquence du changement de besoin : EF6
  (remplacement du lien) sort du périmètre, EF7 reformulée, RG4 réécrite pour
  deux relecteurs, RG12 ajoutée, et section 7 complétée des conséquences C1–C4.
- Diagrammes D1 (cas d'utilisation), D2 (modèle de données) et D4 (états) alignés
  sur la nouvelle règle.
- `README.md` : migrations `V1` à `V3`, fonctionnement de la note provisoire.

### Retiré — périmètre sacrifié, assumé et documenté

- **EF6 — remplacement du lien d'exercice par son auteur.** Fonctionnalité
  *Should*, hors du cœur noté. La conserver aurait exigé de figer le premier
  relecteur, ce qui contredit la nouvelle règle « deux pairs par exercice ».
  Décision écrite dans `docs/CAHIER_DES_CHARGES.md` §7.

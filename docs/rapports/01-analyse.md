# Rapport 01 — Analyse et conception (étape 1)

## Objectif

Transformer une demande client incomplète, partiellement contradictoire, en un ensemble
cohérent : cahier des charges, modèle, diagrammes, contrat d'API complété, backlog —
**avant toute ligne de code**.

## Travail effectué

- Lecture intégrale et croisée de `SUJET.md`, `CLIENT.md` (16 questions), `api/contrat.yaml`
  (5 opérations imposées), des trois modèles et des consignes de travail.
- Matrice des 16 réponses client : utilité, règle/exigence produite, impact.
- Détection de la contradiction **Q10 / Q15** et des zones non tranchées (H1 à H10).
- Rédaction du cahier des charges (dix sections imposées : 10 EF, 11 RG, périmètre
  inclus/exclu, contraintes B1–B6 / F1–F3, livrables, démarche, Definition of Done).
- Quatre diagrammes en texte : D1 (cas d'utilisation, PlantUML), D2 (modèle de données),
  D3 (séquence « marquer sa présence »), D4 (états-transitions — bonus).
- Complétion du contrat : 10 opérations de zone libre, les 5 opérations imposées restant
  strictement intactes (chemins, verbes, codes de statut, format d'erreur).
- Backlog de 15 issues (14 Must, 1 Should) avec critères d'acceptation et renvois EF/RG.

## Décisions

| Réf | Décision | Justification courte |
|---|---|---|
| Contradiction | Q15 l'emporte sur Q10 : note définitive dès l'envoi | aucun second envoi prévu par le contrat imposé (`409 RELECTURE_DEJA_RENDUE`) |
| H2 | La clôture est une action explicite du formateur (`PATCH /api/sessions/{id}/cloture`) | Q12 : dépôt « jusqu'à clôture », incompatible avec le timer de 15 min (Q2) |
| H3 | Présence du formateur par `POST /api/sessions/{id}/presences` | l'opération imposée exige un code que le formateur n'a pas (Q14) |
| H4 | Démarrage explicite de la relecture (`POST /api/relectures/{id}/demarrer`) | « personne n'a commencé à le relire » (Q13) doit être observable |
| H5 | Dépôt non subordonné à la présence | aucune source ne l'exige (Q12/Q13) |
| H6 | Sans relecteur éligible, l'exercice reste `DEPOSE`, visible du formateur | Q7 suppose un autre étudiant présent ; Q11 exige la visibilité |
| H7 | Compteur RG10 persisté par (étudiant, session) + compteur étudiant pour codes inconnus | Q4 vise le devinage de codes ; un code inconnu n'identifie aucune session |
| H8 | Détail des exercices par session (`GET /api/sessions/{id}/exercices?statut=`) | le tableau imposé ne renvoie que des compteurs (Q11 : « clairement ») |
| H9 | 6 opérations de lecture pour les trois écrans | Q1 (liste), EF9 (note), EF7/EF8 (relectures assignées) |
| H10 | La clôture **n'invalide pas** le code de présence | Q3 est couvert par RG1 (expiration 15 min) — interprétation écartée après analyse |

## Notions utilisées

Contrat d'API imposé (B2) · opérations « gelées » vs zone libre · traçabilité
Qx → RG → EF → issue → API → test · critères d'acceptation vérifiables · hypothèse
explicite vs exigence inventée · états-transitions · stockage versionné du schéma (B5).

## Architecture cible (proposée)

Monolithe Spring Boot en couches strictes : `Controller → Service → Repository → DB`,
DTO en entrée/sortie (aucune entité JPA exposée), erreurs centralisées
(`@RestControllerAdvice`, format `{code, message}`), schéma versionné par migration.
Frontend en trois écrans (formateur, étudiant, relecteur) avec une couche d'appels API
dédiée. Aucune sur-architecture : ni microservices, ni message broker.

## Patterns utilisés

Aucun pattern ajouté pour lui-même. Seuls les idiomes imposés par le sujet (couches,
DTO/mapper, validation, gestion centralisée des erreurs, migration versionnée).

## Règles métier concernées

RG1 (expiration du code) · RG2 (pas d'auto-relecture) · RG3 (note 0–20 entière) ·
RG4 (un relecteur, au hasard parmi les présents) · RG5 (relecture définitive) ·
RG6 (exercice en attente visible) · RG7 (dépôt jusqu'à clôture) · RG8 (remplacement
borné) · RG9 (source de présence) · RG10 (blocage après 5 échecs) · RG11 (anonymat
du relecteur).

## Tests (planifiés)

- Unitaires : RG1 (expiration), RG3 (bornes 0–20), RG4 (jamais l'auteur), RG5 (second
  envoi refusé), RG7/RG8 (bornes du dépôt et du remplacement), RG10 (5 échecs → blocage,
  horloge injectée).
- Intégration : `POST /api/presences` (201 / 400 / 409 / 410), `PATCH .../cloture`,
  `GET /api/tableau`, `POST /api/relectures/{id}`.
- Reproductibilité : tests exécutables sur poste vierge (B6) avec base de test migrée.

## Problèmes rencontrés

1. Contradiction Q10/Q15 — arbitrage nécessaire.
2. Cinq exigences client non portables par le contrat imposé (clôture, présence
   formateur, début de relecture, détail des attentes, lectures d'écrans).
3. RG10 : un code inconnu n'identifie aucune session — le compteur par couple seul
   n'aurait jamais rien bloqué.

## Solutions

1. Q15 retenue (preuve : contrat imposé).
2. Dix opérations de zone libre, justifiées une par une, sans toucher aux cinq opérations
   gelées (section 7 du CDC, H2–H9).
3. Double comptage documenté : (étudiant, session) quand la session est identifiable,
   étudiant sinon (RG10, H7).

## Vérifications

- Chaque décision cite sa source (`Qx`, EF, RG).
- Chaque opération imposée a été relue après modification du contrat : chemins, verbes,
  codes de statut et format d'erreur inchangés.
- D2 couvre toutes les entités nécessaires aux RG (dont le compteur RG10) et rien de plus.
- D3 reprend exactement les codes HTTP du contrat (`201`, `400`, `409`, `410`).
- `.gitignore` posé avant tout code ; aucun code écrit avant le jalon d'analyse.

## Points du barème sécurisés

| Critère | Points | État |
|---|---:|---|
| Cahier des charges | 10 | rédigé, à nettoyer des `<...>` (identité, frontend) |
| Diagrammes | 12 (+3 bonus) | D1, D2, D3, D4 écrits |
| Backlog | 8 | 15 issues prêtes — à créer sur GitHub |
| Contrat d'API | 5 | complété, à valider au parseur OpenAPI |
| Mise à jour après étape 3 | 3 | à faire à l'étape 3 |

## Points restant à sécuriser

- Créer le dépôt GitHub public `kfokam48-epreuve-<matricule>` et pousser (Git : 32 pts
  dont l'épreuve `git-lab` à l'étape 5).
- Créer les 15 issues sur GitHub et les lier aux PR.
- Produit et conformité (15 pts) : étape 2 (backend + frontend + tests + démarrage).
- Journal (5 pts) : à alimenter à chaque étape (fait à l'étape 1).

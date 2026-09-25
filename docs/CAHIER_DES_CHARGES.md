# Cahier des charges — Suivi de présence, exercices et relectures entre pairs

**Auteur :** ESSINO Samuel · 174
**Version :** 1 · **Date :** 25/09/2026
**Frontend choisi :** React + Vite, parce que l'écosystème est le plus répandu pour une
application à trois écrans, le build se reproduit simplement (`npm ci && npm run build`)
et la couche d'appels API s'isole dans un module dédié (contrainte F3).

---

## 1. Contexte et objectif

La direction de la formation KFOKAM48 a besoin d'un outil pour suivre, au fil d'une
session de cours, la présence des étudiants, le dépôt de leurs exercices, leur
relecture par un pair et la synthèse de ces informations pour le formateur.
Aujourd'hui ce suivi n'existe pas de façon outillée : le formateur n'a aucune vue
d'ensemble fiable sur qui est venu, qui a rendu, qui a été relu, et avec quelle note.
L'application répond à ce besoin pour trois profils : le formateur qui anime la
session, l'étudiant qui y participe, et ce même étudiant lorsqu'il agit comme
relecteur d'un pair.

## 2. Acteurs et rôles

| Acteur | Ce qu'il peut faire | Ce qu'il ne peut pas faire |
|---|---|---|
| Formateur | Ouvrir une session et obtenir un code de présence · ajouter une présence manuellement · clôturer une session · consulter le tableau récapitulatif de sa promotion | Relire un exercice · modifier une note envoyée |
| Étudiant | Marquer sa présence avec un code · déposer le lien de son exercice · remplacer ce lien tant qu'il n'est pas en cours de relecture · consulter la note et le commentaire reçus sur son propre exercice | Relire son propre exercice (RG2) · voir le nom de son relecteur · voir le tableau du formateur |
| Relecteur | Consulter l'exercice qui lui est assigné · démarrer la relecture (ce qui verrouille le lien de l'exercice) · envoyer une note (0–20) et un commentaire, une seule fois | Relire plus d'un exercice par assignation · revenir sur une relecture déjà envoyée |

*Décision : le relecteur n'est **pas** un acteur distinct avec son propre compte.
D'après Q7, le système choisit le relecteur « au hasard, parmi les étudiants
présents à cette session » — c'est donc un étudiant, dans un rôle temporaire, pour
un exercice donné. Modélisé comme une relation (`Relecture`) entre deux étudiants
via une session, pas comme une nouvelle entité `Utilisateur`.*

## 3. Périmètre

**Inclus dans cette version :**
- Ouverture de session par le formateur avec génération d'un code de présence
- Marquage de présence par code (étudiant) et ajout manuel (formateur) — Q1, Q14
- Expiration du code de présence après 15 minutes — Q2
- Clôture explicite d'une session par le formateur
- Dépôt et remplacement du lien d'un exercice, jusqu'à clôture de la session ou
  début de relecture — Q12, Q13
- Assignation aléatoire d'un relecteur parmi les étudiants présents — Q7
- Envoi d'une note (0–20, entière) et d'un commentaire par le relecteur, une fois — Q9, Q15
- Consultation par l'étudiant relu de sa note et de son commentaire, sans le nom du
  relecteur — Q8
- Tableau récapitulatif du formateur par promotion — Q16

**Explicitement exclu :**
- Authentification par mot de passe — Q1
- Création/gestion des comptes étudiants, formateurs et promotions via l'application
  (traités comme données préexistantes — voir Hypothèse H1, section 7)
- Plus d'un relecteur par exercice — Q6
- Modification d'une note après envoi — Q15 (voir contradiction, section 7)
- Mise en forme visuelle avancée (non noté selon le sujet)

## 4. Exigences fonctionnelles

| Réf | Exigence | Critère d'acceptation | Priorité |
|---|---|---|---|
| EF1 | Le formateur ouvre une session pour une promotion | Quand je saisis un titre et une promotion valides, j'obtiens un code de présence et une heure d'expiration | Must |
| EF2 | L'étudiant marque sa présence à l'aide d'un code | Quand je saisis un code valide et non expiré, ma présence apparaît dans le tableau du formateur | Must |
| EF3 | Le formateur ajoute une présence manuellement | Quand j'ajoute un étudiant absent du code, sa présence apparaît marquée « ajouté par le formateur » | Must |
| EF4 | Le formateur clôture une session | Quand je clôture une session, plus aucun dépôt ni remplacement de lien d'exercice n'est accepté sur cette session | Must |
| EF5 | L'étudiant dépose le lien de son exercice | Quand je dépose un lien valide pour une session non clôturée, l'exercice est créé et passe « en attente de relecture » ; il reste « déposé » si aucun relecteur éligible n'existe (H6) | Must |
| EF6 | L'étudiant remplace le lien de son exercice | Quand la relecture n'a pas encore été commencée et que la session n'est pas clôturée, je peux remplacer le lien déposé ; sinon la tentative est refusée (409) | Should |
| EF7 | Un relecteur est assigné automatiquement à chaque exercice déposé | Quand un exercice est déposé et qu'un étudiant présent autre que l'auteur existe, l'un d'eux est désigné au hasard ; sans candidat, l'exercice reste « déposé » et visible du formateur (H6) | Must |
| EF8 | Le relecteur envoie une note et un commentaire | Quand j'envoie une note entière entre 0 et 20 avec un commentaire, la relecture est enregistrée et devient définitive | Must |
| EF9 | L'étudiant consulte sa note et son commentaire | Quand ma relecture est rendue, je vois la note et le commentaire, jamais le nom du relecteur | Must |
| EF10 | Le formateur consulte le tableau de sa promotion | Quand j'ouvre le tableau, je vois par étudiant : présences, exercices déposés, moyenne des notes, relectures encore dues | Must |

## 5. Exigences non fonctionnelles

| Réf | Exigence | Comment on la vérifie |
|---|---|---|
| ENF1 | L'écran de marquage de présence est utilisable sur un téléphone | Test manuel sur viewport mobile (< 400 px) |
| ENF2 | Le tableau du formateur répond en moins de 2 s pour une promotion de 60 étudiants | Mesure sur données de démonstration chargées |
| ENF3 | Toute erreur métier renvoie un message en français exploitable par l'utilisateur, jamais une trace technique | Revue des réponses `4xx`/`5xx` de chaque endpoint |

## 6. Règles de gestion

| Réf | Règle | Source |
|---|---|---|
| RG1 | Un code de présence expire 15 minutes après l'ouverture de la session | Q2 |
| RG2 | Un étudiant ne peut pas relire son propre exercice | Q5 |
| RG3 | Une note est un entier compris entre 0 et 20 | Q9 |
| RG4 | Un seul relecteur est assigné par exercice, choisi au hasard parmi les étudiants présents à la session | Q6, Q7 |
| RG5 | Une relecture envoyée est définitive : toute nouvelle tentative de soumission sur la même relecture est refusée (409) | Q15, confirmée par `api/contrat.yaml` (erreur `RELECTURE_DEJA_RENDUE`) |
| RG6 | Un exercice non relu reste visible comme « en attente » dans le tableau du formateur, sans limite de temps imposée | Q11 |
| RG7 | Un dépôt d'exercice n'est accepté que si la session n'est pas clôturée. La clôture est une action explicite du formateur, distincte de l'expiration du code de présence (15 min) | Q12 |
| RG8 | Le remplacement du lien d'un exercice est refusé si la session est clôturée (RG7), ou si la relecture a été commencée ou rendue (statuts `EN_COURS` ou `RENDUE`) | Q12 (implicite), Q13 |
| RG9 | Une présence ajoutée manuellement par le formateur est marquée distinctement d'une présence saisie par l'étudiant (`source = FORMATEUR`) | Q14 |
| RG10 | Après cinq échecs consécutifs, un étudiant est bloqué deux minutes avant nouvelle tentative. Les échecs sont comptés par couple (étudiant, session) quand la session est identifiable (code expiré) et par étudiant seul quand le code est inconnu — sinon le compteur ne protégerait jamais contre le devinage de codes (Q4). Le blocage est signalé via le `400` déjà imposé sur `POST /api/presences`, avec le `code` métier `TROP_DE_TENTATIVES`. Le compteur est persisté et remis à zéro après l'expiration du blocage ou une présence réussie | Q4 |
| RG11 | L'identité du relecteur n'est jamais exposée à l'étudiant relu : la note et le commentaire sont consultables, jamais le nom du relecteur | Q8 |

## 7. Zones d'ombre, hypothèses et contradictions

**Points que la demande ne tranche pas :**

| Point | Réponse client (Qx) ou hypothèse | Décision retenue | Conséquence |
|---|---|---|---|
| H1 — Création des acteurs | Aucune des 16 questions ni des 5 opérations imposées ne définit la création des étudiants, formateurs et promotions, bien qu'elles soient présupposées partout (Q1, contrat) | Considérés comme données préexistantes, injectées via données de démonstration/migration ; aucune opération de création n'est exposée par l'API | Pas de CRUD acteurs dans le périmètre ; script de seed obligatoire pour le démarrage (section 3 du sujet) |
| H2 — Clôture de session | Seules Q10 et Q12 emploient le mot « clôture » (vérifié par relecture exhaustive du corpus). Q10 (correction de note liée à la clôture) est sans objet depuis la contradiction Q10/Q15 tranchée en faveur de Q15. Reste Q12, qui autorise le dépôt d'exercice « jusqu'à clôture » avec la justification « certains n'ont pas de connexion le soir même » — incompatible avec les 15 minutes d'`expirationAt` (Q2), qui ne concerne que le code de présence | Clôture retenue comme action explicite et distincte du formateur, non couverte par le contrat imposé. Ajout d'un endpoint en zone libre : `PATCH /api/sessions/{id}/cloture` | Nouvel état sur `Session` (`ouverte` / `cloturee`), vérifié par EF4, RG7, RG8. Q3 (fin de session ↔ présence) reste expliquée par RG1 (expiration du code), sans lien avec la clôture |
| Remplacement de lien hors contrat | Q13 accorde une fonctionnalité que le contrat imposé (`POST /api/exercices` seul, avec 409 sur doublon) ne permet pas d'implémenter tel quel | Ajout d'un endpoint en zone libre : `PUT /api/exercices/{id}`. Priorité Should (EF6) : le parcours Must (EF1-EF5, EF7-EF10) reste complet et testable de bout en bout sans cette fonctionnalité | EF6, RG8 |
| Portée du compteur d'erreurs (Q4) | Q4 ne précise pas si les 5 erreurs se comptent par étudiant seul ou par (étudiant, session) | Compteur par (étudiant, session), cohérent avec le contexte de la question (saisie du code d'une session précise) et avec « sinon ils vont deviner les codes entre eux » qui vise le code de cette session | RG10. Pas de nouveau code de statut HTTP : `POST /api/presences` fait partie des 5 opérations imposées (verbe, chemin, codes gelés) — le blocage est signalé via le `400` déjà prévu, avec un `code` métier `TROP_DE_TENTATIVES` dans le corps |

| H3 — Présence ajoutée par le formateur | Q14 accorde l'ajout manuel, mais l'opération imposée `POST /api/presences` exige `{code, etudiantId}` — le formateur n'a pas de code | Opération de zone libre : `POST /api/sessions/{id}/presences` (body `{etudiantId}`) ; la présence créée porte `source = FORMATEUR`. L'opération imposée reste intacte (B2) | EF3, RG9 ; contrat +1 opération ; test du doublon (409) |
| H4 — « personne n'a commencé à le relire » (Q13) | Condition d'EF6 inobservable : aucune opération du contrat imposé ne marque le début d'une relecture | Opération de zone libre : `POST /api/relectures/{id}/demarrer` → statut `EN_COURS` ; le remplacement est refusé dès `EN_COURS` ou `RENDUE` (409) | RG8 reformulée ; D4 enrichi (ASSIGNEE → EN_COURS → RENDUE) ; envoi direct de la note toujours possible (RG5) |
| H5 — Dépôt et présence | Ni Q12 ni Q13 ne subordonnent le dépôt d'un exercice à la présence à la session | Le critère d'EF5 ne l'exige plus : dépôt accepté tant que la session n'est pas clôturée ; l'assignation reste réservée aux étudiants présents (Q7) | EF5 corrigée ; dépôts tardifs (« le soir même », Q12) compatibles |
| H6 — Aucun relecteur éligible | Q7 suppose implicitement qu'un autre étudiant est présent ; le cas contraire n'est pas traité | L'exercice reste `DEPOSE` sans relecteur, visible du formateur (Q11/RG6) ; pas de re-tentative automatique d'assignation (limite assumée) | EF7, D4 ; test : session à un seul étudiant |
| H7 — Persistance du compteur RG10 | Q4 n'indique ni où compter ni comment réinitialiser | Table versionnée `tentative_code (etudiant_id, session_id nullable, echecs, bloque_jusqua)` ; remise à zéro après l'expiration du blocage ou une présence réussie | D2, migrations, tests RG10 (horloge injectée) |
| H8 — Visibilité « claire » des exercices en attente (Q11) | Le `GET /api/tableau` imposé ne renvoie que des compteurs par étudiant | Opération de zone libre : `GET /api/sessions/{id}/exercices?statut=` pour le détail par session ; le tableau imposé reste inchangé | EF10 ; écran formateur |
| H9 — Lecture des trois écrans (F2) | Le contrat imposé n'offre aucune lecture : ni liste d'étudiants (Q1), ni notes de l'étudiant (EF9), ni relectures assignées (EF7/EF8) | Opérations de zone libre : `GET /api/promotions`, `GET /api/promotions/{id}/etudiants`, `GET /api/sessions?promotionId=`, `GET /api/etudiants/{id}/exercices` (note + commentaire, jamais le relecteur), `GET /api/etudiants/{id}/relectures` | Contrat +6 opérations de lecture ; F3 (couche API dédiée) |
| H10 — Clôture et code de présence (Q3) | « Pas de présence après la fin de session » pouvait se lire comme « la clôture invalide le code » | Interprétation écartée après analyse contradictoire : la clôture n'invalide pas le code ; Q3 est couvert par RG1 (expiration à 15 min). Aucun changement d'EF2/EF4 | Décision documentée pour l'oral ; D3 sans branche « session clôturée » |

**Contradictions relevées :**

| Réponses en conflit | Ce que j'ai choisi | Pourquoi |
|---|---|---|
| Q10 (correction de note possible jusqu'à clôture de session) vs Q15 (note définitive dès l'envoi) | Q15 : la note est définitive dès l'envoi | Le contrat d'API imposé (`api/contrat.yaml`) ne prévoit qu'une seule opération `POST /api/relectures/{id}`, sans verbe de mise à jour, avec un code d'erreur dédié `409 RELECTURE_DEJA_RENDUE` pour un second envoi. Une contrainte technique imposée l'emporte sur une réponse client ambiguë. Q10 est documentée mais écartée |

## 8. Contraintes techniques

**Backend (imposées par le sujet) :**
- B1 — Java 17+, Maven, wrapper `mvnw` commité
- B2 — `api/contrat.yaml` respecté à la lettre : chemins, verbes, codes de statut, format d'erreur
- B3 — Séparation Contrôleur / Service / Repository ; aucune requête base dans un contrôleur ; aucune entité JPA exposée en JSON (DTO obligatoires)
- B4 — Validation des entrées et gestion centralisée des erreurs (`@RestControllerAdvice`) ; aucune stack trace exposée
- B5 — Schéma versionné par Flyway ou Liquibase ; `ddl-auto=update` interdit hors tests
- B6 — Au moins un test unitaire sur une règle métier réelle et un test d'intégration sur un endpoint, exécutables sur poste vierge

**Frontend (imposées par le sujet) :**
- F1 — Framework déclaré et justifié dans le README ; build fonctionnel
- F2 — Trois écrans : formateur, étudiant, relecteur
- F3 — Appels API dans une couche dédiée ; gestion du chargement et des erreurs ; aucune règle métier dupliquée (la moyenne vient de l'API)

**Choix laissés libres, à documenter au fur et à mesure :**
- Base de données : `<à choisir>`
- Stratégie de migration : `<Flyway | Liquibase>`
- Stratégie de tests : `<à préciser>`

## 9. Livrables

- `docs/CAHIER_DES_CHARGES.md`
- `docs/diagrammes/` (D1, D2, D3, D4 optionnel)
- `api/contrat.yaml` complété : 5 opérations imposées + 10 opérations de zone libre (clôture, remplacement de lien, présence ajoutée par le formateur, démarrage de relecture, et 6 lectures pour les trois écrans — voir section 7, H2 à H9)
- Backlog en issues GitHub, priorisé Must/Should/Could
- `backend/` Spring Boot fonctionnel avec migrations et tests
- `frontend/` avec les trois écrans
- `README.md` d'installation testé depuis un clone vierge
- `docs/JOURNAL.md` tenu à jour à chaque étape
- `CHANGELOG.md` (étape 4)
- `SOUMISSION.md`

## 10. Démarche prévue

Respect strict des six étapes du sujet, dans l'ordre, avec un commit `[JALON]` dédié
et poussé à la fin des étapes 1, 2 et 4. Priorité stricte aux stories **Must** pour
la v0.1 ; les **Should**/**Could** (comme EF6, remplacement de lien) ne sont traitées
qu'une fois toutes les Must terminées et testées. En cas de retard, réduction du
périmètre plutôt que sacrifice de la démarche (analyse, tests, Git) — cohérent avec
le barème où le produit ne pèse que 15 points sur 100.

**Definition of Done — un ticket est terminé quand :**
- le code respecte B3/B4 (couches séparées, erreurs centralisées) ;
- au moins un test couvre la règle métier concernée ;
- la PR référence l'issue et les `EFx`/`RGx` concernés ;
- le `JOURNAL.md` est mis à jour ;
- la PR est mergée dans `main` sans casser le build.

---

## Journal des révisions

| Version | Quand | Ce qui a changé et pourquoi |
|---|---|---|
| 1 | <date> | Version initiale, construite en croisant SUJET.md, CLIENT.md et api/contrat.yaml |
| 1 (révision) | <date> | Correction : Q11 et Q13 retirées du rattachement erroné à la « clôture de session » (grep exhaustif du corpus). H2 (clôture), RG10 (portée du compteur d'erreurs) et la priorité de EF6 tranchées après analyse structurée complète (source, interprétations, conséquences, barème) et validation explicite |
| 1 (révision 2) | <date> | Décisions H3–H9 et RG11 : présence du formateur et démarrage de relecture en zone libre, dépôt non subordonné à la présence (EF5 corrigée), cas « aucun relecteur éligible », compteur RG10 persisté et précisé, détail des exercices par session (Q11), opérations de lecture des trois écrans. H10 écartée : la clôture n'invalide pas le code (Q3 couvert par RG1). Contrat complété : 15 opérations au total |

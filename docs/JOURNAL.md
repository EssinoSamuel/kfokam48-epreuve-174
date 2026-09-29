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
- **Sujet A — le bug.** Issue **#16** ouverte **avant** tout code. Le client décrivait un
  symptôme (« un seul étudiant apparaît dans ma liste ») : traduit en cause, c'est le
  chemin `POST /api/sessions/{id}/presences` **imposé par le contrat** qui n'existait pas
  côté backend (seul `POST /api/presences/session/{id}` était exposé) → **404**, aucun
  ajout manuel possible (EF3, RG9, Q14). Test d'intégration `PresenceFormateurIT` écrit
  **avant** le correctif et **qui échoue** : `Tests run: 3, Errors: 3`. Correctif dans la
  branche dédiée `fix/bug-presence-formateur`, commit référençant l'issue (`397a234`),
  puis test **vert** : `Tests run: 3, Errors: 0`.
- **Sujet B — le changement de besoin.** Analyse remise à jour **dans un commit qui le
  dit** (`b815737`) : EF6 retirée du périmètre, EF7 reformulée, **RG4 réécrite** pour deux
  relecteurs, **RG12 ajoutée** (deux relecteurs distincts), section 7 complétée des
  conséquences C1–C4 ; diagrammes D1, D2 et D4 corrigés.
- **Migration `V3__relecture_par_deux_pairs.sql`** : **ajoutée**, jamais une modification en
  place. Elle retire la contrainte d'unicité héritée de Q6/RG4, ajoute un index sur
  `relecture (exercice_id)`, crée la vue `moyenne_exercice` (moyenne, notes rendues,
  statut) et attribue un **second relecteur** aux exercices déjà en attente, en SQL
  portable, sans supprimer aucune donnée : elle survit à une base déjà remplie.
- Code : `TirageAuSort.choisir(presents, auteur, nombre)` tire N relecteurs distincts,
  `ExerciceService` en assigne deux, le DTO expose `moyenne`, `notesRendues`, `statutNote`
  et les commentaires **sans l'identité des relecteurs** (RG11/Q8) ; le frontend affiche
  « Note provisoire » tant que les deux pairs n'ont pas rendu.
- **Séparation exigée** : deux branches (`fix/bug-presence-formateur`,
  `feat/relecture-par-deux-pairs`) et deux fusions distinctes sur `main`.

**Bloqué :** ~1 h sur un **blocage de démarrage du backend contre PostgreSQL** :
`Migration checksum mismatch` sur les versions 1 et 2. Cause réelle : pour rendre `V1` et
`V2` exécutables sur le H2 des tests, leur contenu avait été paramétré par des placeholders
Flyway, **après** avoir été appliqué dans le volume PostgreSQL local — les empreintes ne
correspondaient plus. Les tests H2 ne pouvaient pas le voir (base neuve à chaque
exécution). Résolu en détruisant puis recréant le volume (`docker compose down -v`) :
`Successfully applied 3 migrations … now at version v3`. Leçon retenue et écrite dans le
`CHANGELOG` : **on ne retouche jamais une migration déjà appliquée**.
La machine est également restée saturée (démarrage Spring Boot mesuré à 226 s), ce qui a
fait échouer plusieurs commandes du poste.

**IA :** demandé — rédaction du test qui démontre le bug avant correction, analyse des
conséquences du changement sur le cahier des charges et les diagrammes, écriture de la
migration `V3`, refactorisation du tirage au sort. **Vérification :** le test du sujet A a
été exécuté et a échoué avant le correctif, puis est repassé au vert ; `mvnw test` complet
(7 tests, 0 échec) ; les migrations ont été appliquées pour de vrai sur PostgreSQL, pas
seulement sur H2 ; chaque affirmation du CHANGELOG a été confrontée à `git log`.

**Ce que j'ai sorti du périmètre pour absorber le changement, et pourquoi :**
**EF6 — le remplacement du lien d'exercice par son auteur.** C'était une exigence *Should*,
hors du cœur noté. La conserver aurait obligé à figer le premier relecteur dès que
quelqu'un commence à relire, ce qui contredit frontalement la nouvelle règle « chaque
exercice est relu par deux pairs différents » : les deux exigences ne peuvent pas tenir
ensemble. J'ai donc sacrifié celle qui n'était pas obligatoire, et je l'ai écrit dans
`docs/CAHIER_DES_CHARGES.md` §7 avec les quatre conséquences du changement (C1–C4).

---

## Étape 4 — Version finale

**Fait :**
- `[JALON] v1.0` posé, puis `CHANGELOG.md` écrit **dans l'ordre de l'historique Git**.
- `README.md` corrigé : il annonçait encore les migrations `V1` et `V2` ; il décrit
  maintenant `V1` à `V3` et le fonctionnement de la note provisoire.
- **Test de bout en bout des deux relecteurs**, exécuté contre PostgreSQL et non contre H2 :
  dépôt d'un exercice → deux relecteurs distincts assignés, l'auteur exclu (RG2/RG12) →
  première note rendue, l'API répond `statutNote: "PROVISOIRE"` avec la moyenne de la seule
  note reçue → seconde note rendue, `statutNote: "DEFINITIVE"` et moyenne des deux. La
  réponse vérifiée ne contient **que** des commentaires et un identifiant de relecture,
  jamais son nom (RG11/Q8).
- Parcours « clone vierge » validé : base vide, `Successfully applied 3 migrations … now at
  version v3` puis démarrage sur le port 8080.
- **Backlog trié** : `docs/BACKLOG.md` donne l'état des 17 tickets — 16 livrés, 1 sacrifié —
  avec la justification du sacrifice et la suite à donner.

**Bloqué :** le test de bout en bout a été retardé par la perte du proxy de port de Docker
Desktop après plusieurs heures d'inactivité (`La tentative de connexion a échoué` côté
pool Hikari alors que le conteneur était *healthy*) — réglé par un redémarrage du
conteneur. Autre blocage : **les deux nouveaux tickets (bug et évolution) n'ont pas pu être
créés sur GitHub**. Le jeton n'est pas exploitable depuis un shell non interactif : `gh`
n'est pas authentifié, et le gestionnaire d'identifiants refuse de livrer le jeton
mémorisé (`git credential fill` reste bloqué). Les contenus sont donc prêts dans
`.tmp/issues/` et devront être créés à la main — **l'issue du bug doit être créée en
premier**, pour porter le numéro 16 et faire se résoudre la référence `fix(#16)` du commit
de correctif.

**IA :** demandé — le scénario de bout en bout, la rédaction du `CHANGELOG` et des entrées
de journal. **Vérification :** la preuve du scénario est la réponse brute de l'API
(`moyenne`, `notesRendues`, `statutNote`, forme des commentaires), lue directement dans la
sortie des appels ; toute affirmation du CHANGELOG a été recontrôlée contre `git log`.


---

## Étape 5 — Épreuve Git

**Fait :**

**Bloqué :**

**IA :**

---

## Étape 6 — Soumission

**Fait :**

**Ce que je referais autrement avec une journée de plus :**

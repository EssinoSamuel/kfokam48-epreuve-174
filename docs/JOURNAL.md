# Journal de bord — <matricule>

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

**Fait :**

**Bloqué :**

**IA :**

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

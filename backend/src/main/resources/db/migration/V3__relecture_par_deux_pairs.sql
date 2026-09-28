-- ============================================================
-- V3 — Chaque exercice est relu par deux pairs différents
--
-- Conséquence directe de l'enveloppe d'étape 3, sujet B :
-- « un seul relecteur ça ne marche pas : quand il ne rend rien,
--  l'étudiant n'a aucune note. Chaque exercice est relu par deux
--  pairs différents, et la note retenue est la moyenne des deux. »
--
-- Cette migration MODIFIE une règle existante : Q6 / RG4 imposaient
-- un seul relecteur par exercice. La contrainte d'unicité qui la
-- portait est donc retirée — sans quoi l'évolution est impossible.
--
-- Règle de migration : on AJOUTE un fichier, on ne modifie JAMAIS
-- un fichier déjà appliqué (contrainte B5). La migration survit à une
-- base déjà remplie : aucune donnée existante n'est supprimée, seules
-- les notes déjà rendues sont conservées telles quelles.
--
-- Périmètre sacrifié (documenté dans docs/CAHIER_DES_CHARGES.md §7) :
-- le remplacement du lien d'exercice (EF6) sort du périmètre. C'était
-- une fonctionnalité *Should*, hors du cœur noté, et le maintien du
-- remplacement aurait exigé de figer le premier relecteur — ce qui
-- contredit la nouvelle règle « deux pairs par exercice ».
-- ============================================================

-- 1. Un exercice peut désormais avoir plusieurs relectures (2, voire plus
--    si l'on Choit d'en assigner davantage) : la contrainte bloquante tombe.
ALTER TABLE relecture DROP CONSTRAINT IF EXISTS uq_relecture_exercice;

-- 2. Note provisoire : à 0, la relecture existe mais n'a pas encore rendu.
--    0 est une vraie note côté API (RG3) : on ne confond pas, la valeur
--    sentinelle est NULL et la colonne reste nullable.
--    (aucune modification de colonne nécessaire : note est déjà nullable)

-- 3. Index utile : les requêtes « relectures d'un exercice » et
--    « moyenne des notes d'un exercice » passent par exercice_id.
CREATE INDEX IF NOT EXISTS idx_relecture_exercice ON relecture (exercice_id);

-- 4. La note affichée est la moyenne des notes rendues. Cette vue centralise
--    le calcul côté base pour que le frontend ne fasse aucun calcul (F3).
CREATE OR REPLACE VIEW moyenne_exercice AS
SELECT e.id                AS exercice_id,
       e.etudiant_id       AS auteur_id,
       COUNT(r.id)         AS relectures_attribuees,
       COUNT(r.note)       AS notes_rendues,
       CASE WHEN COUNT(r.note) = 0 THEN NULL
            ELSE ROUND(AVG(r.note), 2)
       END                 AS moyenne,
       CASE WHEN COUNT(r.note) = 0 THEN NULL
            WHEN COUNT(r.note) < 2 THEN 'PROVISOIRE'
            ELSE 'DEFINITIVE'
       END                 AS statut_note
FROM exercice e
LEFT JOIN relecture r ON r.exercice_id = e.id
GROUP BY e.id, e.etudiant_id;

-- 5. Deuxième relecteur pour les exercices déjà déposés : la base de
--    démonstration doit refléter la nouvelle règle sans être réinitialisée
--    (une migration, pas un nouveau seed).
--    Écrit en SQL portable (pas de LATERAL, absent de H2) : on retient le
--    premier étudiant présent, distinct de l'auteur et déjà relecteur évité.
--    Seuls les exercices EN_ATTENTE sont concernés : ceux déjà notés
--    conservent leurs notes telles quelles.
INSERT INTO relecture (exercice_id, relecteur_id, statut)
SELECT e.id, candidat.id, 'ASSIGNEE'
FROM exercice e
JOIN etudiant candidat
  ON candidat.promotion_id = e.etudiant_id
 AND candidat.id <> e.etudiant_id
 AND EXISTS (SELECT 1 FROM presence p
              WHERE p.session_id = e.session_id
                AND p.etudiant_id = candidat.id)
 AND NOT EXISTS (SELECT 1 FROM relecture deja
                  WHERE deja.exercice_id = e.id
                    AND deja.relecteur_id = candidat.id)
WHERE e.statut = 'EN_ATTENTE_DE_RELECTURE'
  AND NOT EXISTS (SELECT 1 FROM relecture r3
                  WHERE r3.exercice_id = e.id);

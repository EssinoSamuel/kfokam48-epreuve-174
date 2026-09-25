-- ============================================================
-- V2 — Données de démonstration (H1 ; sujet § Démarrage)
--
-- Injectées par migration versionnée : reproductibles, identiques chez
-- le formateur après un simple `docker compose up -d` + démarrage.
-- Aucun `ddl-auto=update` (contrainte B5).
--
-- Contenu : 1 promotion, 12 étudiants, 2 sessions (une clôturée, une ouverte),
-- présences, dépôts d'exercices, relectures assignées / en cours / rendues.
-- L'id 1..12 des étudiants est stable et sert aux exemples du README.
-- ============================================================

INSERT INTO promotion (id, nom) VALUES (1, 'Fullstack KFOKAM48 — Promotion 2026');
SELECT setval(pg_get_serial_sequence('promotion', 'id'), 1, true);

INSERT INTO etudiant (id, nom, promotion_id) VALUES
    (1,  'ESSINO Samuel',      1),
    (2,  'MBALLA Aïcha',       1),
    (3,  'NGONO Bertrand',     1),
    (4,  'FOTSO Chrétien',     1),
    (5,  'TCHOUAMO Nadège',    1),
    (6,  'ABEGA Rodrigue',     1),
    (7,  'KAMGA Sandrine',     1),
    (8,  'ONANA Yves',         1),
    (9,  'NJOYA Bénédicte',    1),
    (10, 'TAMBA Serge',        1),
    (11, 'EWANE Prisca',       1),
    (12, 'DJOKO Armand',       1);
SELECT setval(pg_get_serial_sequence('etudiant', 'id'), 12, true);

-- ------------------------------------------------------------
-- Session 1 — clôturée : cycle complet (présences, dépôts, relectures rendues)
-- ------------------------------------------------------------
INSERT INTO session (id, titre, promotion_id, code, ouverture_at, expiration_at, statut, cloturee_at)
VALUES (1, 'Session 1 — Requêtes HTTP et API REST', 1, 'KF48A1',
        TIMESTAMP WITH TIME ZONE '2026-09-22 08:00:00+01',
        TIMESTAMP WITH TIME ZONE '2026-09-22 08:15:00+01',
        'CLOTUREE',
        TIMESTAMP WITH TIME ZONE '2026-09-22 20:00:00+01');

-- 8 présences marquées par les étudiants, 2 ajoutées à la main par le formateur (RG9)
INSERT INTO presence (session_id, etudiant_id, source, marquee_at) VALUES
    (1,  1, 'ETUDIANT',   TIMESTAMP WITH TIME ZONE '2026-09-22 08:02:00+01'),
    (1,  2, 'ETUDIANT',   TIMESTAMP WITH TIME ZONE '2026-09-22 08:03:00+01'),
    (1,  3, 'ETUDIANT',   TIMESTAMP WITH TIME ZONE '2026-09-22 08:04:00+01'),
    (1,  4, 'ETUDIANT',   TIMESTAMP WITH TIME ZONE '2026-09-22 08:05:00+01'),
    (1,  5, 'ETUDIANT',   TIMESTAMP WITH TIME ZONE '2026-09-22 08:06:00+01'),
    (1,  6, 'ETUDIANT',   TIMESTAMP WITH TIME ZONE '2026-09-22 08:07:00+01'),
    (1,  7, 'ETUDIANT',   TIMESTAMP WITH TIME ZONE '2026-09-22 08:08:00+01'),
    (1,  8, 'ETUDIANT',   TIMESTAMP WITH TIME ZONE '2026-09-22 08:09:00+01'),
    (1,  9, 'FORMATEUR',  TIMESTAMP WITH TIME ZONE '2026-09-22 08:20:00+01'),
    (1, 10, 'FORMATEUR',  TIMESTAMP WITH TIME ZONE '2026-09-22 08:21:00+01');

-- Dépôts : 5 exercices, dont 3 déjà relus (RLU), 2 en attente de relecture
INSERT INTO exercice (id, session_id, etudiant_id, lien, statut, depose_at) VALUES
    (1, 1,  1, 'https://github.com/EssinoSamuel/kfokam48-epreuve-174', 'RELU',
        TIMESTAMP WITH TIME ZONE '2026-09-22 09:00:00+01'),
    (2, 1,  2, 'https://github.com/example/mballa-session1', 'RELU',
        TIMESTAMP WITH TIME ZONE '2026-09-22 09:10:00+01'),
    (3, 1,  3, 'https://github.com/example/ngono-session1', 'RELU',
        TIMESTAMP WITH TIME ZONE '2026-09-22 09:20:00+01'),
    (4, 1,  4, 'https://github.com/example/fotso-session1', 'EN_ATTENTE_DE_RELECTURE',
        TIMESTAMP WITH TIME ZONE '2026-09-22 09:30:00+01'),
    (5, 1,  5, 'https://github.com/example/tchouamo-session1', 'EN_ATTENTE_DE_RELECTURE',
        TIMESTAMP WITH TIME ZONE '2026-09-22 09:40:00+01');
SELECT setval(pg_get_serial_sequence('exercice', 'id'), 5, true);

-- Assignations : jamais l'auteur de l'exercice (RG2), un seul relecteur (Q6)
INSERT INTO relecture (id, exercice_id, relecteur_id, statut, demarree_at, note, commentaire, rendue_at) VALUES
    (1, 1, 2, 'RENDUE',
        TIMESTAMP WITH TIME ZONE '2026-09-22 10:00:00+01', 16,
        'Bonne séparation des couches, erreurs centralisées bien vues. Le test d''intégration manque sur l''endpoint de tableau.',
        TIMESTAMP WITH TIME ZONE '2026-09-22 10:30:00+01'),
    (2, 2, 3, 'RENDUE',
        TIMESTAMP WITH TIME ZONE '2026-09-22 10:05:00+01', 12,
        'Le contrat est respecté sur les créations, mais le code de présence ne vérifie pas l''expiration avant l''unicité.',
        TIMESTAMP WITH TIME ZONE '2026-09-22 10:40:00+01'),
    (3, 3, 1, 'RENDUE',
        TIMESTAMP WITH TIME ZONE '2026-09-22 10:10:00+01', 18,
        'Migration versionnée et données de démonstration exemplaires. Continuer ainsi.',
        TIMESTAMP WITH TIME ZONE '2026-09-22 10:50:00+01'),
    (4, 4, 6, 'EN_COURS',
        TIMESTAMP WITH TIME ZONE '2026-09-22 11:00:00+01', NULL, NULL, NULL),
    (5, 5, 7, 'ASSIGNEE',
        NULL, NULL, NULL, NULL);

SELECT setval(pg_get_serial_sequence('relecture', 'id'), 5, true);

-- ------------------------------------------------------------
-- Session 2 — ouverte, code encore valable : pour tester le marquage en direct
-- ------------------------------------------------------------
INSERT INTO session (id, titre, promotion_id, code, ouverture_at, expiration_at, statut, cloturee_at)
VALUES (2, 'Session 2 — Tests automatisés et revue de code', 1, 'KF48B2',
        TIMESTAMP WITH TIME ZONE '2026-09-25 08:00:00+01',
        TIMESTAMP WITH TIME ZONE '2026-09-25 08:15:00+01',
        'OUVERTE', NULL);

INSERT INTO presence (session_id, etudiant_id, source, marquee_at) VALUES
    (2,  1, 'ETUDIANT',  TIMESTAMP WITH TIME ZONE '2026-09-25 08:01:00+01'),
    (2, 11, 'ETUDIANT',  TIMESTAMP WITH TIME ZONE '2026-09-25 08:02:00+01'),
    (2, 12, 'ETUDIANT',  TIMESTAMP WITH TIME ZONE '2026-09-25 08:03:00+01');

SELECT setval(pg_get_serial_sequence('session', 'id'), 2, true);

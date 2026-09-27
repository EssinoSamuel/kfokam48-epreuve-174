-- ============================================================
-- V1 — Schéma initial
-- Source de vérité : docs/diagrammes/D2-modele-donnees.md
-- Toute évolution passe par une nouvelle migration + mise à jour de D2.
--
-- Pas de table `formateur` ni `utilisateur` : aucune opération du contrat
-- n'identifie le formateur (ni formateurId, ni authentification — Q1).
-- Comptes et promotions sont des données préexistantes (H1), injectées en V2.
-- ============================================================

CREATE TABLE promotion (
    id   BIGSERIAL PRIMARY KEY,
    nom  VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE etudiant (
    id           BIGSERIAL PRIMARY KEY,
    nom          VARCHAR(120) NOT NULL,
    promotion_id BIGINT NOT NULL REFERENCES promotion (id)
);

CREATE INDEX idx_etudiant_promotion ON etudiant (promotion_id);

CREATE TABLE session (
    id            BIGSERIAL PRIMARY KEY,
    titre         VARCHAR(160) NOT NULL,
    promotion_id  BIGINT NOT NULL REFERENCES promotion (id),
    code          VARCHAR(12) NOT NULL UNIQUE,
    ouverture_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    expiration_at TIMESTAMP WITH TIME ZONE NOT NULL,
    statut        VARCHAR(16) NOT NULL,
    cloturee_at   TIMESTAMP WITH TIME ZONE,
    -- Le code d'une session est unique (D2) : il sert de clé de recherche
    -- au marquage de présence.
    CONSTRAINT ck_session_statut CHECK (statut IN ('OUVERTE', 'CLOTUREE')),
    -- H2 (Q12) : cloturee_at est nul tant que la session est ouverte,
    -- et renseigné dès qu'elle est clôturée.
    CONSTRAINT ck_session_cloture CHECK (
        (statut = 'OUVERTE'   AND cloturee_at IS NULL)
     OR (statut = 'CLOTUREE' AND cloturee_at IS NOT NULL)
    ),
    CONSTRAINT ck_session_expiration CHECK (expiration_at > ouverture_at)
);

CREATE INDEX idx_session_promotion ON session (promotion_id);

CREATE TABLE presence (
    id          BIGSERIAL PRIMARY KEY,
    session_id  BIGINT NOT NULL REFERENCES session (id),
    etudiant_id BIGINT NOT NULL REFERENCES etudiant (id),
    source      VARCHAR(16) NOT NULL,
    marquee_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    -- Une seule présence par étudiant et par session.
    CONSTRAINT uq_presence_session_etudiant UNIQUE (session_id, etudiant_id),
    -- RG9 (Q14) : présence marquée par l'étudiant (avec le code) ou
    -- ajoutée à la main par le formateur.
    CONSTRAINT ck_presence_source CHECK (source IN ('ETUDIANT', 'FORMATEUR'))
);

CREATE TABLE exercice (
    id          BIGSERIAL PRIMARY KEY,
    session_id  BIGINT NOT NULL REFERENCES session (id),
    etudiant_id BIGINT NOT NULL REFERENCES etudiant (id),
    lien        VARCHAR(2048) NOT NULL,
    statut      VARCHAR(32) NOT NULL,
    depose_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    -- Un seul dépôt par étudiant et par session (le 409 imposé par le contrat).
    CONSTRAINT uq_exercice_session_etudiant UNIQUE (session_id, etudiant_id),
    CONSTRAINT ck_exercice_statut CHECK (
        statut IN ('DEPOSE', 'EN_ATTENTE_DE_RELECTURE', 'RELU')
    )
);

CREATE TABLE relecture (
    id           BIGSERIAL PRIMARY KEY,
    exercice_id  BIGINT NOT NULL REFERENCES exercice (id),
    relecteur_id BIGINT NOT NULL REFERENCES etudiant (id),
    statut       VARCHAR(16) NOT NULL,
    demarree_at  TIMESTAMP WITH TIME ZONE,
    note         INTEGER,
    commentaire  VARCHAR(2000),
    rendue_at    TIMESTAMP WITH TIME ZONE,
    -- Q6 : un seul relecteur par exercice.
    CONSTRAINT uq_relecture_exercice UNIQUE (exercice_id),
    CONSTRAINT ck_relecture_statut CHECK (
        statut IN ('ASSIGNEE', 'EN_COURS', 'RENDUE')
    ),
    -- RG3 : note entière de 0 à 20, nulle avant envoi.
    CONSTRAINT ck_relecture_note CHECK (note IS NULL OR (note >= 0 AND note <= 20)),
    -- Cohérence des états : EN_COURS implique un demarrage, RENDUE une note.
    CONSTRAINT ck_relecture_demarrage CHECK (
        statut = 'ASSIGNEE' OR demarree_at IS NOT NULL
    ),
    CONSTRAINT ck_relecture_rendue CHECK (
        (statut = 'RENDUE' AND note IS NOT NULL AND rendue_at IS NOT NULL)
     OR (statut <> 'RENDUE' AND note IS NULL AND rendue_at IS NULL)
    )
);

CREATE INDEX idx_relecture_relecteur ON relecture (relecteur_id);

-- Persistance du compteur d'échecs de code (RG10, H7).
-- session_id est nullable : une tentative sur un code inconnu ne peut pas
-- être rattachée à une session. Deux index uniques partiels couvrent les
-- deux cas sans qu'une ligne « code inconnu » n'entre en collision avec
-- une ligne « session connue ».
CREATE TABLE tentative_code (
    id            BIGSERIAL PRIMARY KEY,
    etudiant_id   BIGINT NOT NULL REFERENCES etudiant (id),
    session_id    BIGINT REFERENCES session (id),
    echecs        INTEGER NOT NULL DEFAULT 0,
    bloque_jusqua TIMESTAMP WITH TIME ZONE,
    CONSTRAINT ck_tentative_echecs CHECK (echecs >= 0)
);

-- Unicite du compteur RG10 (H7).
-- Ces index sont PARTIELS : PostgreSQL les supporte, H2 non. Ils passent donc
-- par un placeholder Flyway, vide sur le profil de test — cf. application.yml.
${index_uniq_tentative_code}

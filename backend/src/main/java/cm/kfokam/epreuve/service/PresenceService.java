package cm.kfokam.epreuve.service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam.epreuve.config.ReglesMetierProperties;
import cm.kfokam.epreuve.domaine.Etudiant;
import cm.kfokam.epreuve.domaine.Presence;
import cm.kfokam.epreuve.domaine.Session;
import cm.kfokam.epreuve.domaine.SourcePresence;
import cm.kfokam.epreuve.domaine.TentativeCode;
import cm.kfokam.epreuve.repository.PresenceRepository;
import cm.kfokam.epreuve.repository.TentativeCodeRepository;
import cm.kfokam.epreuve.web.erreur.CodeErreur;
import cm.kfokam.epreuve.web.erreur.ErreurMetierException;

/**
 * Règles de gestion du marquage de présence.
 *
 * <ul>
 *   <li><b>RG1</b> — le code n'est valable que pendant la durée configurée
 *       (15 minutes, Q2) : au-delà, {@code 410 CODE_EXPIRE} ;</li>
 *   <li><b>RG9 / EF3</b> — le formateur peut ajouter une présence à la main (Q14) ;</li>
 *   <li><b>RG10</b> — cinq échecs bloquent le couple (étudiant, session)
 *       pendant deux minutes (Q4). Aucun code de statut HTTP n'est ajouté à
 *       l'opération imposée : le 400 prévu porte le code métier
 *       {@code TROP_DE_TENTATIVES}.</li>
 * </ul>
 *
 * Le compteur est persisté (décision H7) : il survit donc à un redémarrage,
 * ce qu'un compteur en mémoire ne ferait pas.
 */
@Service
public class PresenceService {

    private final PresenceRepository presences;
    private final TentativeCodeRepository tentatives;
    private final ReglesMetierProperties regles;
    private final Clock horloge;

    public PresenceService(PresenceRepository presences,
                           TentativeCodeRepository tentatives,
                           ReglesMetierProperties regles,
                           Clock horloge) {
        this.presences = presences;
        this.tentatives = tentatives;
        this.regles = regles;
        this.horloge = horloge;
    }

    /**
     * Marque la présence d'un étudiant avec le code d'une session.
     *
     * @param session        session retrouvée (jamais null)
     * @param etudiant       étudiant qui saisit le code (jamais null)
     * @param codeReconnu    vrai si le code correspondait à une session réelle
     */
    @Transactional
    public Presence marquerAvecCode(Session session, Etudiant etudiant, boolean codeReconnu) {
        Instant maintenant = horloge.instant();
        long echecsConnus = verifierBlocage(etudiant, session, codeReconnu, maintenant);

        if (!session.codeValableA(maintenant)) {
            enregistrerEchec(etudiant, session, codeReconnu, maintenant, echecsConnus);
            throw new ErreurMetierException(CodeErreur.CODE_EXPIRE);
        }

        if (presences.existsBySessionIdAndEtudiantId(session.getId(), etudiant.getId())) {
            throw new ErreurMetierException(CodeErreur.DEJA_PRESENT);
        }

        Presence presence = presences.save(
                Presence.marquer(session, etudiant, SourcePresence.ETUDIANT, maintenant));
        reinitialiser(etudiant, session, codeReconnu);
        return presence;
    }

    /**
     * Ajout manuel d'une présence par le formateur (EF3, RG9 — Q14).
     */
    @Transactional
    public Presence ajouterParFormateur(Session session, Etudiant etudiant) {
        if (presences.existsBySessionIdAndEtudiantId(session.getId(), etudiant.getId())) {
            throw new ErreurMetierException(CodeErreur.DEJA_PRESENT);
        }
        return presences.save(Presence.marquer(
                session, etudiant, SourcePresence.FORMATEUR, horloge.instant()));
    }

    /**
     * RG10 : un étudiant déjà présent reçoit le 409 sans que son compteur
     * d'échecs ne soit incrémenté (il n'a pas « deviné » un code, il tente
     * un doublon).
     */
    @Transactional
    public void verifierDejaPresent(Session session, Etudiant etudiant) {
        if (presences.existsBySessionIdAndEtudiantId(session.getId(), etudiant.getId())) {
            throw new ErreurMetierException(CodeErreur.DEJA_PRESENT);
        }
    }

    @Transactional(readOnly = true)
    public boolean estPresent(Long sessionId, Long etudiantId) {
        return presences.existsBySessionIdAndEtudiantId(sessionId, etudiantId);
    }

    @Transactional(readOnly = true)
    public List<Etudiant> etudiantsPresents(Long sessionId) {
        return new ArrayList<>(presences.etudiantsPresents(sessionId));
    }

    @Transactional(readOnly = true)
    public long compterPourEtudiant(Long etudiantId) {
        return presences.countByEtudiantId(etudiantId);
    }

    @Transactional(readOnly = true)
    public long compterPourSession(Long sessionId) {
        return presences.countBySessionId(sessionId);
    }

    /** Échecs déjà enregistrés pour un couple (étudiant, session) — utilisé par les tests RG10. */
    @Transactional(readOnly = true)
    public int echecsEnregistres(Long etudiantId, Long sessionId, boolean codeReconnu) {
        Optional<TentativeCode> compteur = codeReconnu
                ? tentatives.findByEtudiantIdAndSessionId(etudiantId, sessionId)
                : tentatives.findByEtudiantIdAndSessionIsNull(etudiantId);
        return compteur.map(TentativeCode::getEchecs).orElse(0);
    }

    /**
     * RG10 pour un code <b>inconnu</b> : la session n'existe pas, le compteur
     * est donc porté par l'étudiant seul (H7).
     *
     * Le code de statut reste le 400 imposé par le contrat : on n'ajoute pas
     * de 429 (interdit) et on ne détourne pas le 404 (réservé aux ressources
     * par identifiant). Au 5e échec, le blocage est armé ; c'est la
     * <b>tentative suivante</b> qui recevra {@code TROP_DE_TENTATIVES}.
     */
    @Transactional
    public void enregistrerCodeInconnu(Etudiant etudiant) {
        Instant maintenant = horloge.instant();
        Optional<TentativeCode> existante = tentatives.findByEtudiantIdAndSessionIsNull(etudiant.getId());
        TentativeCode compteur = existante.orElseGet(() -> TentativeCode.pour(etudiant, null));

        if (compteur.estBloqueA(maintenant)) {
            throw new ErreurMetierException(CodeErreur.TROP_DE_TENTATIVES);
        }
        // Blocage expiré : la fenêtre repart de zéro avant de compter l'échec.
        if (compteur.getBloqueJusqua() != null) {
            compteur.reinitialiser();
        }
        compteur.enregistrerEchec(
                regles.tentatives().max(), maintenant, regles.tentatives().blocageMinutes());
        tentatives.save(compteur);
    }

    /** Remet à zéro les compteurs d'une session — utilisé par les tests RG10. */
    @Transactional
    public void reinitialiserCompteursDeLaSession(Long sessionId) {
        tentatives.supprimerPourSession(sessionId);
    }

    // ------------------------------------------------------------------
    // RG10 — mécanique interne du compteur
    // ------------------------------------------------------------------

    /**
     * Vérifie l'état de blocage <b>avant</b> toute autre règle : c'est la
     * première chose que doit rencontrer un étudiant qui tâtonne.
     *
     * @return le nombre d'échecs déjà enregistrés pour ce couple
     */
    private long verifierBlocage(Etudiant etudiant, Session session,
                                 boolean codeReconnu, Instant maintenant) {
        Optional<TentativeCode> existante = chercher(etudiant, session, codeReconnu);
        if (existante.isEmpty()) {
            return 0;
        }
        TentativeCode compteur = existante.get();
        if (compteur.estBloqueA(maintenant)) {
            throw new ErreurMetierException(CodeErreur.TROP_DE_TENTATIVES);
        }
        // Blocage expiré ou jamais posé : la fenêtre repart de zéro (H7).
        if (compteur.getBloqueJusqua() != null) {
            compteur.reinitialiser();
            tentatives.save(compteur);
            return 0;
        }
        return compteur.getEchecs();
    }

    /**
     * Enregistre un échec et arme le blocage au seuil configuré.
     *
     * Le compteur est reconstruit à partir des échecs déjà constatés par
     * {@link #verifierBlocage} : cela évite tout double comptage, sans avoir
     * à exposer un setter sur l'entité.
     */
    private void enregistrerEchec(Etudiant etudiant, Session session, boolean codeReconnu,
                                  Instant maintenant, long echecsConnus) {
        TentativeCode compteur = chercher(etudiant, session, codeReconnu)
                .orElseGet(() -> TentativeCode.pour(etudiant, codeReconnu ? session : null));
        compteur.reinitialiser();
        for (long i = 0; i < echecsConnus; i++) {
            compteur.enregistrerEchec(Integer.MAX_VALUE, maintenant, 0);
        }
        compteur.enregistrerEchec(
                regles.tentatives().max(), maintenant, regles.tentatives().blocageMinutes());
        tentatives.save(compteur);
    }

    private void reinitialiser(Etudiant etudiant, Session session, boolean codeReconnu) {
        chercher(etudiant, session, codeReconnu).ifPresent(compteur -> {
            compteur.reinitialiser();
            tentatives.save(compteur);
        });
    }

    private Optional<TentativeCode> chercher(Etudiant etudiant, Session session, boolean codeReconnu) {
        if (codeReconnu && session != null && session.getId() != null) {
            return tentatives.findByEtudiantIdAndSessionId(etudiant.getId(), session.getId());
        }
        return tentatives.findByEtudiantIdAndSessionIsNull(etudiant.getId());
    }
}

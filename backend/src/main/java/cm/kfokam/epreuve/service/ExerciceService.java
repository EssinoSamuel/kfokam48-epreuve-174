package cm.kfokam.epreuve.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam.epreuve.domaine.Etudiant;
import cm.kfokam.epreuve.domaine.Exercice;
import cm.kfokam.epreuve.domaine.Relecture;
import cm.kfokam.epreuve.domaine.Session;
import cm.kfokam.epreuve.domaine.StatutExercice;
import cm.kfokam.epreuve.domaine.StatutRelecture;
import cm.kfokam.epreuve.repository.EtudiantRepository;
import cm.kfokam.epreuve.repository.ExerciceRepository;
import cm.kfokam.epreuve.repository.PresenceRepository;
import cm.kfokam.epreuve.repository.RelectureRepository;
import cm.kfokam.epreuve.web.erreur.CodeErreur;
import cm.kfokam.epreuve.web.erreur.ErreurMetierException;

/**
 * Dépôt, remplacement et lecture des exercices, et assignation d'un relecteur.
 *
 * Règles couvertes :
 * <ul>
 *   <li><b>EF5 / RG7</b> — dépôt possible tant que la session n'est pas
 *       clôturée, sans être subordonné à la présence (décisions H5 et H2, Q12) ;</li>
 *   <li><b>EF6 / RG8</b> — remplacement du lien refusé si la session est
 *       clôturée, la relecture commencée ou rendue (Q13) ;</li>
 *   <li><b>EF7 / RG2 / RG4 / RG12</b> — deux relecteurs distincts tirés au sort
 *       parmi les étudiants présents, jamais l'auteur, jamais deux fois le même
 *       (Q5, Q7, et Q6 tel que modifié par l'enveloppe d'étape 3) ;</li>
 *   <li><b>H6</b> — sans candidat éligible, l'exercice reste {@code DEPOSE}.</li>
 * </ul>
 */
@Service
public class ExerciceService {

    /** Deux pairs relisent chaque exercice (enveloppe étape 3, RG4 modifié). */
    static final int NOMBRE_RELECTEURS = 2;

    private final ExerciceRepository exercices;
    private final EtudiantRepository etudiants;
    private final PresenceRepository presences;
    private final RelectureRepository relectures;
    private final SessionService sessionService;
    private final ValidateurLien validateurLien;
    private final TirageAuSort tirageAuSort;
    private final Clock horloge;

    public ExerciceService(ExerciceRepository exercices,
                           EtudiantRepository etudiants,
                           PresenceRepository presences,
                           RelectureRepository relectures,
                           SessionService sessionService,
                           ValidateurLien validateurLien,
                           TirageAuSort tirageAuSort,
                           Clock horloge) {
        this.exercices = exercices;
        this.etudiants = etudiants;
        this.presences = presences;
        this.relectures = relectures;
        this.sessionService = sessionService;
        this.validateurLien = validateurLien;
        this.tirageAuSort = tirageAuSort;
        this.horloge = horloge;
    }

    /** EF5 : dépose le lien d'un exercice pour une session. */
    @Transactional
    public Exercice deposer(Long sessionId, Long etudiantId, String lien) {
        Session session = sessionService.parId(sessionId);
        if (session.estCloturee()) {
            throw new ErreurMetierException(CodeErreur.SESSION_CLOTUREE);
        }
        Etudiant etudiant = etudiants.findById(etudiantId)
                .orElseThrow(() -> new ErreurMetierException(CodeErreur.ETUDIANT_INCONNU));

        String lienNormalise = validateurLien.normaliser(lien);

        if (exercices.findBySessionIdAndEtudiantId(sessionId, etudiantId).isPresent()) {
            throw new ErreurMetierException(CodeErreur.EXERCICE_DEJA_DEPOSE);
        }

        Exercice exercice = exercices.save(
                Exercice.deposer(session, etudiant, lienNormalise, Instant.now(horloge)));

        assignerRelecteurs(exercice, session, etudiant);
        return exercice;
    }

    /** EF6 / RG8 : remplacement du lien par son auteur (Q13). */
    @Transactional
    public Exercice remplacerLien(Long exerciceId, String lien) {
        Exercice exercice = exercices.findById(exerciceId)
                .orElseThrow(() -> new ErreurMetierException(CodeErreur.EXERCICE_INCONNU));

        if (exercice.getSession().estCloturee()) {
            throw new ErreurMetierException(CodeErreur.SESSION_CLOTUREE);
        }

        relectures.findByExerciceId(exerciceId).ifPresent(relecture -> {
            StatutRelecture statut = relecture.getStatut();
            if (statut == StatutRelecture.EN_COURS) {
                throw new ErreurMetierException(CodeErreur.RELECTURE_DEJA_COMMENCEE);
            }
            if (statut == StatutRelecture.RENDUE) {
                throw new ErreurMetierException(CodeErreur.RELECTURE_DEJA_RENDUE);
            }
        });

        exercice.remplacerLien(validateurLien.normaliser(lien));
        return exercice;
    }

    /**
     * RG2 / RG4 / RG12 : tirage de DEUX relecteurs distincts (enveloppe étape 3).
     *
     * <p>Avant : un seul relecteur par exercice (Q6). Raison du changement donnée
     * par le client : « quand il ne rend rien, l'étudiant n'a aucune note ».
     *
     * <p>Si un seul pair est présent, on n'en designate qu'un : on ne bloque pas
     * le dépôt pour autant (dégradation assumée), l'exercice reste visible comme
     * « en attente de relecture » (H6).
     */
    private void assignerRelecteurs(Exercice exercice, Session session, Etudiant auteur) {
        List<Etudiant> presents = presences.etudiantsPresents(session.getId());
        for (Etudiant relecteur : tirageAuSort.choisir(presents, auteur.getId(), NOMBRE_RELECTEURS)) {
            relectures.save(Relecture.assigner(exercice, relecteur));
        }
        if (exercice.getStatut() == StatutExercice.DEPOSE) {
            exercice.marquerRelecteurAssigne();
        }
    }

    @Transactional(readOnly = true)
    public List<Exercice> parSession(Long sessionId, StatutExercice statut) {
        sessionService.parId(sessionId);
        return exercices.trouverAvecDetailsPourSession(sessionId, statut);
    }

    @Transactional(readOnly = true)
    public List<Exercice> parEtudiant(Long etudiantId) {
        if (!etudiants.existsById(etudiantId)) {
            throw new ErreurMetierException(CodeErreur.ETUDIANT_INCONNU);
        }
        return exercices.trouverAvecDetailsPourEtudiant(etudiantId);
    }

    @Transactional(readOnly = true)
    public Exercice parId(Long exerciceId) {
        return exercices.findById(exerciceId)
                .orElseThrow(() -> new ErreurMetierException(CodeErreur.EXERCICE_INCONNU));
    }

    @Transactional(readOnly = true)
    public long compterPourEtudiant(Long etudiantId) {
        return exercices.countByEtudiantId(etudiantId);
    }

    /** Contrôle Q13 : seul l'auteur peut remplacer le lien de son exercice. */
    @Transactional(readOnly = true)
    public void verifierEstAuteur(Exercice exercice, Long etudiantId) {
        if (exercice.getEtudiant().getId() == null
                || !exercice.getEtudiant().getId().equals(etudiantId)) {
            throw new ErreurMetierException(CodeErreur.EXERCICE_INCONNU,
                    "Seul l'auteur de l'exercice peut en remplacer le lien.");
        }
    }
}

package cm.kfokam.epreuve.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam.epreuve.domaine.Relecture;
import cm.kfokam.epreuve.domaine.StatutRelecture;
import cm.kfokam.epreuve.repository.EtudiantRepository;
import cm.kfokam.epreuve.repository.RelectureRepository;
import cm.kfokam.epreuve.web.erreur.CodeErreur;
import cm.kfokam.epreuve.web.erreur.ErreurMetierException;

/**
 * Démarrage et notation d'une relecture.
 *
 * <ul>
 *   <li><b>Q13 / RG8</b> — démarrer verrouille le lien de l'exercice : à
 *       partir de là, {@code PUT /api/exercices/{id}} renverra 409 ;</li>
 *   <li><b>RG2</b> — un étudiant ne relit jamais son propre exercice (403) ;</li>
 *   <li><b>RG3</b> — note entière de 0 à 20 ;</li>
 *   <li><b>RG5</b> — une relecture rendue est définitive : la contradiction
 *       Q10/Q15 a été tranchée en faveur de Q15 (CDC section 7), il n'existe
 *       donc aucune opération de correction de note.</li>
 * </ul>
 */
@Service
public class RelectureService {

    private final RelectureRepository relectures;
    private final EtudiantRepository etudiants;
    private final Clock horloge;

    public RelectureService(RelectureRepository relectures,
                            EtudiantRepository etudiants,
                            Clock horloge) {
        this.relectures = relectures;
        this.etudiants = etudiants;
        this.horloge = horloge;
    }

    /** Un relecteur ouvre l'exercice qui lui est assigné : le lien se verrouille. */
    @Transactional
    public Relecture demarrer(Long relectureId) {
        Relecture relecture = parId(relectureId);
        if (relecture.getStatut() == StatutRelecture.RENDUE) {
            throw new ErreurMetierException(CodeErreur.RELECTURE_DEJA_RENDUE);
        }
        if (relecture.getStatut() == StatutRelecture.EN_COURS) {
            throw new ErreurMetierException(CodeErreur.RELECTURE_DEJA_COMMENCEE);
        }
        relecture.demarrer(Instant.now(horloge));
        return relecture;
    }

    /**
     * Envoi de la note et du commentaire (Q9). Registre définitif (RG5).
     *
     * @param relectureId identifiant de la relecture
     * @param note        entier de 0 à 20 ; {@code null} accepté ici pour
     *                    être rejeté en 400 (jamais un NPE → 500)
     * @param commentaire texte libre, facultatif
     */
    @Transactional
    public Relecture rendre(Long relectureId, Integer note, String commentaire) {
        Relecture relecture = parId(relectureId);

        if (note == null || note < 0 || note > 20) {
            throw new ErreurMetierException(CodeErreur.NOTE_INVALIDE,
                    "La note doit être un entier compris entre 0 et 20 (valeur reçue : "
                            + note + ").");
        }
        if (relecture.estAutoRelecture()) {
            throw new ErreurMetierException(CodeErreur.AUTO_RELECTURE);
        }
        if (relecture.getStatut() == StatutRelecture.RENDUE) {
            throw new ErreurMetierException(CodeErreur.RELECTURE_DEJA_RENDUE);
        }

        relecture.rendre(note, commentaire, Instant.now(horloge));
        relecture.getExercice().marquerRelu();
        return relecture;
    }

    @Transactional(readOnly = true)
    public Relecture parId(Long relectureId) {
        return relectures.findById(relectureId)
                .orElseThrow(() -> new ErreurMetierException(CodeErreur.RELECTURE_INCONNUE));
    }

    @Transactional(readOnly = true)
    public List<Relecture> pourRelecteur(Long etudiantId) {
        if (!etudiants.existsById(etudiantId)) {
            throw new ErreurMetierException(CodeErreur.ETUDIANT_INCONNU);
        }
        return relectures.trouverPourRelecteur(etudiantId);
    }

    /**
     * Moyenne des notes reçues par un étudiant (EF10, Q16).
     *
     * Calcul côté API (contrainte F3) : le frontend n'additionne jamais.
     * {@code null} si l'étudiant n'a encore reçu aucune note — ce n'est pas
     * la même information que 0, et le tableau les distingue.
     */
    @Transactional(readOnly = true)
    public Double moyenneRecue(Long etudiantId) {
        List<Integer> notes = relectures.findAll().stream()
                .filter(r -> r.getStatut() == StatutRelecture.RENDUE)
                .filter(r -> r.getNote() != null)
                .filter(r -> r.getExercice().getEtudiant().getId() != null
                        && r.getExercice().getEtudiant().getId().equals(etudiantId))
                .map(Relecture::getNote)
                .toList();
        if (notes.isEmpty()) {
            return null;
        }
        double somme = notes.stream().mapToInt(Integer::intValue).sum();
        double moyenne = somme / notes.size();
        // Arrondi à deux décimales : évite les 13.666666666666666 dans l'IHM.
        return Math.round(moyenne * 100.0) / 100.0;
    }

    /** Nombre de relectures non encore rendues, assignées à cet étudiant. */
    @Transactional(readOnly = true)
    public long compterEnAttentePourEtudiant(Long etudiantId) {
        return relectures.compterParRelecteurEtStatut(etudiantId, StatutRelecture.ASSIGNEE)
                + relectures.compterParRelecteurEtStatut(etudiantId, StatutRelecture.EN_COURS);
    }

    /** Relectures non rendues sur une session, tous relecteurs confondus. */
    @Transactional(readOnly = true)
    public long compterEnAttentePourSession(Long sessionId) {
        return relectures.compterEnAttenteParSession(sessionId);
    }
}

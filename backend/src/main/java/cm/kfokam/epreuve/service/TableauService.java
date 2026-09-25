package cm.kfokam.epreuve.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam.epreuve.domaine.Etudiant;
import cm.kfokam.epreuve.domaine.StatutRelecture;
import cm.kfokam.epreuve.repository.EtudiantRepository;
import cm.kfokam.epreuve.repository.PromotionRepository;
import cm.kfokam.epreuve.web.dto.LigneTableau;
import cm.kfokam.epreuve.web.erreur.CodeErreur;
import cm.kfokam.epreuve.web.erreur.ErreurMetierException;

/**
 * Tableau récapitulatif du formateur (EF10, Q16).
 *
 * La <b>moyenne est calculée ici</b>, côté API, jamais côté frontend
 * (contrainte F3 du sujet, et section « la moyenne vient de l'API » du
 * prompt maître). Le frontend se contente d'afficher.
 *
 * Deux conventions explicitées dans le cahier des charges :
 * <ul>
 *   <li>{@code moyenne} vaut {@code null} si l'étudiant n'a reçu aucune note ;</li>
 *   <li>{@code relecturesEnAttente} comptabilise les relectures <b>assignées
 *       à</b> l'étudiant et non encore rendues, y compris celles qu'il doit
 *       rendre sur l'exercice d'un pair.</li>
 * </ul>
 */
@Service
public class TableauService {

    private final PromotionRepository promotions;
    private final EtudiantRepository etudiants;
    private final PresenceService presenceService;
    private final ExerciceService exerciceService;
    private final RelectureService relectureService;

    public TableauService(PromotionRepository promotions,
                          EtudiantRepository etudiants,
                          PresenceService presenceService,
                          ExerciceService exerciceService,
                          RelectureService relectureService) {
        this.promotions = promotions;
        this.etudiants = etudiants;
        this.presenceService = presenceService;
        this.exerciceService = exerciceService;
        this.relectureService = relectureService;
    }

    @Transactional(readOnly = true)
    public List<LigneTableau> pourPromotion(Long promotionId) {
        if (!promotions.existsById(promotionId)) {
            throw new ErreurMetierException(CodeErreur.PROMOTION_INCONNUE);
        }
        List<LigneTableau> lignes = new ArrayList<>();
        for (Etudiant etudiant : etudiants.findByPromotionIdOrderByNomAsc(promotionId)) {
            lignes.add(LigneTableau.de(
                    etudiant.getId(),
                    etudiant.getNom(),
                    presenceService.compterPourEtudiant(etudiant.getId()),
                    exerciceService.compterPourEtudiant(etudiant.getId()),
                    relectureService.moyenneRecue(etudiant.getId()),
                    relectureService.compterEnAttentePourEtudiant(etudiant.getId())));
        }
        return lignes;
    }

    /** Nombre de relectures non rendues sur une session (colonne du tableau, Q16). */
    @Transactional(readOnly = true)
    public long relecturesEnAttenteDeLaSession(Long sessionId) {
        return relectureService.compterEnAttentePourSession(sessionId);
    }
}

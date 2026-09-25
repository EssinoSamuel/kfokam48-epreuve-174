package cm.kfokam.epreuve.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cm.kfokam.epreuve.domaine.Promotion;
import cm.kfokam.epreuve.domaine.StatutExercice;
import cm.kfokam.epreuve.domaine.StatutRelecture;
import cm.kfokam.epreuve.domaine.StatutSession;
import cm.kfokam.epreuve.repository.EtudiantRepository;
import cm.kfokam.epreuve.repository.PromotionRepository;
import cm.kfokam.epreuve.web.dto.Referentiel;
import cm.kfokam.epreuve.web.erreur.CodeErreur;
import cm.kfokam.epreuve.web.erreur.ErreurMetierException;

/**
 * Étudiants et référentiels d'interface.
 *
 * Décision H9 : Q1 exclut l'authentification, donc l'application a besoin de
 * sélecteurs (« qui suis-je ? quelle promotion ? »). Ces routes ne portent
 * aucune règle métier, elles alimentent les écrans. Elles sont déclarées ici
 * comme plomberie assumée, pas comme exigence client.
 */
@RestController
@RequestMapping("/api")
public class ReferentielController {

    private final PromotionRepository promotions;
    private final EtudiantRepository etudiants;

    public ReferentielController(PromotionRepository promotions, EtudiantRepository etudiants) {
        this.promotions = promotions;
        this.etudiants = etudiants;
    }

    /** Promotion par défaut : évite au formateur de choisir s'il n'y en a qu'une. */
    @GetMapping("/referentiel")
    public Referentiel.ReferentielVue referentiel(@RequestParam(required = false) Long promotionId) {
        List<Referentiel.PromotionVue> toutes = promotions.findAllByOrderByNomAsc().stream()
                .map(Referentiel.PromotionVue::de)
                .toList();

        Long cible = promotionId;
        if (cible == null && !toutes.isEmpty()) {
            cible = toutes.get(0).id();
        }

        List<Referentiel.EtudiantVue> listeEtudiants = cible == null
                ? List.of()
                : etudiants.findByPromotionIdOrderByNomAsc(cible).stream()
                        .map(e -> new Referentiel.EtudiantVue(
                                e.getId(), e.getNom(), e.getPromotion().getId()))
                        .toList();

        return new Referentiel.ReferentielVue(
                toutes,
                listeEtudiants,
                List.of(vue(StatutExercice.DEPOSE), vue(StatutExercice.EN_ATTENTE_DE_RELECTURE),
                        vue(StatutExercice.RELU)),
                List.of(vue(StatutSession.OUVERTE), vue(StatutSession.CLOTUREE)),
                List.of(vue(StatutRelecture.ASSIGNEE), vue(StatutRelecture.EN_COURS),
                        vue(StatutRelecture.RENDUE)));
    }

    /** Les promotions, pour le sélecteur du formateur. */
    @GetMapping("/promotions")
    public List<Referentiel.PromotionVue> promotions() {
        return promotions.findAllByOrderByNomAsc().stream()
                .map(Referentiel.PromotionVue::de)
                .toList();
    }

    /** Les étudiants d'une promotion — sélecteur « choisir mon nom » (Q1). */
    @GetMapping("/etudiants")
    public List<Referentiel.EtudiantVue> etudiantsDeLaPromotion(@RequestParam Long promotionId) {
        if (!promotions.existsById(promotionId)) {
            throw new ErreurMetierException(CodeErreur.PROMOTION_INCONNUE);
        }
        return etudiants.findByPromotionIdOrderByNomAsc(promotionId).stream()
                .map(e -> new Referentiel.EtudiantVue(e.getId(), e.getNom(), e.getPromotion().getId()))
                .toList();
    }

    @GetMapping("/etudiants/{id}")
    public Referentiel.EtudiantVue etudiant(@PathVariable Long id) {
        return etudiants.findById(id)
                .map(e -> new Referentiel.EtudiantVue(e.getId(), e.getNom(), e.getPromotion().getId()))
                .orElseThrow(() -> new ErreurMetierException(CodeErreur.ETUDIANT_INCONNU));
    }

    private Referentiel.EnumerationVue vue(Enum<?> valeur) {
        return new Referentiel.EnumerationVue(valeur.name(), libelle(valeur));
    }

    /** Libellés français affichés dans l'IHM : ne sont jamais devinés côté frontend. */
    private String libelle(Enum<?> valeur) {
        return switch (valeur.name()) {
            case "DEPOSE" -> "Déposé";
            case "EN_ATTENTE_DE_RELECTURE" -> "En attente de relecture";
            case "RELU" -> "Relu";
            case "OUVERTE" -> "Ouverte";
            case "CLOTUREE" -> "Clôturée";
            case "ASSIGNEE" -> "Assignée";
            case "EN_COURS" -> "En cours";
            case "RENDUE" -> "Rendue";
            default -> valeur.name();
        };
    }
}

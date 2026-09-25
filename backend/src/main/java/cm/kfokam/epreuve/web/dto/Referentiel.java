package cm.kfokam.epreuve.web.dto;

import java.util.List;

import cm.kfokam.epreuve.domaine.Promotion;

/**
 * Données de référence et libellés d'énumérations destinés à l'interface.
 *
 * Décision H9 : l'absence d'authentification (Q1) nous oblige à fournir des
 * sélecteurs — « qui suis-je ? ». Ces listes ne sont ni des EF ni des RG :
 * c'est de la plomberie d'interface, assumée et documentée dans le CDC.
 */
public final class Referentiel {

    private Referentiel() {
    }

    /** Une promotion proposée dans le sélecteur du formateur. */
    public record PromotionVue(Long id, String nom) {

        public static PromotionVue de(Promotion promotion) {
            return new PromotionVue(promotion.getId(), promotion.getNom());
        }
    }

    /** Un étudiant proposé dans le sélecteur « choisir mon nom ». */
    public record EtudiantVue(Long id, String nom, Long promotionId) {
    }

    /** Valeurs possibles d'un filtre, pour que le frontend n'invente rien. */
    public record EnumerationVue(String valeur, String libelle) {
    }

    /** Réponse de GET /api/referentiel : tout ce qu'il faut pour amorcer l'IHM. */
    public record ReferentielVue(
            List<PromotionVue> promotions,
            List<EtudiantVue> etudiants,
            List<EnumerationVue> statutsExercice,
            List<EnumerationVue> statutsSession,
            List<EnumerationVue> statutsRelecture) {
    }
}

package cm.kfokam.epreuve.web.dto;

import java.time.Instant;

import cm.kfokam.epreuve.domaine.Exercice;
import cm.kfokam.epreuve.domaine.Relecture;

/**
 * Objets d'échange de {@code /api/exercices}.
 *
 * Le relecteur est exposé en aperçu (nom, statut) quand il existe : le
 * formateur doit pouvoir dire « untel doit relire untel » (Q16), et l'auteur
 * doit savoir que son lien n'est plus modifiable. L'identité du relecteur
 * n'est pas masquée — aucune question du client ne demande l'anonymat
 * de la relecture, seules les notes du tableau agrégé restent anonymes.
 */
public final class ExerciceDto {

    private ExerciceDto() {
    }

    /** Corps de requête du dépôt imposé : {sessionId, etudiantId, lien}. */
    public record Depot(Long sessionId, Long etudiantId, String lien) {
    }

    /** Corps de requête du remplacement de lien (EF6). */
    public record Lien(String lien) {
    }

    /** Aperçu du relecteur associé à un exercice. */
    public record RelecteurVue(Long relectureId, Long etudiantId, String nom, String statut) {
    }

    /** Exercice tel que renvoyé à l'interface. */
    public record Reponse(Long id, Long sessionId, Long etudiantId, String etudiantNom,
                          String lien, String statut, Instant deposeAt, RelecteurVue relecture) {

        public static Reponse de(Exercice exercice) {
            return de(exercice, null);
        }

        public static Reponse de(Exercice exercice, Relecture relecture) {
            RelecteurVue vue = relecture == null
                    ? null
                    : new RelecteurVue(
                            relecture.getId(),
                            relecture.getRelecteur().getId(),
                            relecture.getRelecteur().getNom(),
                            relecture.getStatut().name());
            return new Reponse(
                    exercice.getId(),
                    exercice.getSession().getId(),
                    exercice.getEtudiant().getId(),
                    exercice.getEtudiant().getNom(),
                    exercice.getLien(),
                    exercice.getStatut().name(),
                    exercice.getDeposeAt(),
                    vue);
        }
    }
}

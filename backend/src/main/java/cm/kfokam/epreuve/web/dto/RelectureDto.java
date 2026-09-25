package cm.kfokam.epreuve.web.dto;

import java.time.Instant;

import cm.kfokam.epreuve.domaine.Relecture;
import cm.kfokam.epreuve.domaine.StatutRelecture;

/**
 * Objets d'échange de {@code /api/relectures}.
 *
 * {@code note} est un {@code Integer} : {@code null} tant que la relecture
 * n'est pas rendue. Le frontend affiche donc « — » et non « 0 », ce qui
 * serait un contresens (un 0 est une note reçue, pas une absence de note).
 */
public final class RelectureDto {

    private RelectureDto() {
    }

    /** Corps de requête de l'opération imposée n°4 : {relectureId}. */
    public record Demarrage(Long relectureId) {
    }

    /** Corps de requête de l'opération imposée n°5 : {note, commentaire}. */
    public record Note(Integer note, String commentaire) {
    }

    /** Relecture telle que renvoyée à l'interface. */
    public record Reponse(Long id, Long exerciceId, Long relecteurId, String relecteurNom,
                          Long auteurId, String auteurNom, Long sessionId,
                          String lien, Instant deposeAt,
                          String statut, Instant demarreeAt,
                          Integer note, String commentaire, Instant rendueAt) {

        public static Reponse de(Relecture relecture) {
            var exercice = relecture.getExercice();
            return new Reponse(
                    relecture.getId(),
                    exercice.getId(),
                    relecture.getRelecteur().getId(),
                    relecture.getRelecteur().getNom(),
                    exercice.getEtudiant().getId(),
                    exercice.getEtudiant().getNom(),
                    exercice.getSession().getId(),
                    exercice.getLien(),
                    exercice.getDeposeAt(),
                    relecture.getStatut().name(),
                    relecture.getDemarreeAt(),
                    relecture.getNote(),
                    relecture.getCommentaire(),
                    relecture.getRendueAt());
        }

        /** Le contrat attend un statut textuel dont les valeurs sont connues. */
        public static String[] statutsPossibles() {
            return new String[]{
                    StatutRelecture.ASSIGNEE.name(),
                    StatutRelecture.EN_COURS.name(),
                    StatutRelecture.RENDUE.name()
            };
        }
    }
}

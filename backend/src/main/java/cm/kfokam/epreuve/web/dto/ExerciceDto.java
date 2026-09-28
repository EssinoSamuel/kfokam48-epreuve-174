package cm.kfokam.epreuve.web.dto;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import cm.kfokam.epreuve.domaine.Exercice;
import cm.kfokam.epreuve.domaine.Relecture;
import cm.kfokam.epreuve.domaine.StatutRelecture;

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
    public record Depot(
            @NotNull(message = "L'identifiant de session est obligatoire.")
            Long sessionId,

            @NotNull(message = "L'identifiant étudiant est obligatoire.")
            Long etudiantId,

            @NotBlank(message = "Le lien d'exercice est obligatoire.")
            @Size(max = 2048, message = "Le lien ne peut dépasser 2048 caractères.")
            String lien
    ) {
    }

    /** Corps de requête du remplacement de lien (EF6). */
    public record Lien(
            @NotBlank(message = "Le nouveau lien est obligatoire.")
            @Size(max = 2048, message = "Le lien ne peut dépasser 2048 caractères.")
            String lien
    ) {
    }

    /**
     * Aperçu d'une relecture pour le formateur : on ne montre QUE le statut.
     * Le nom du relecteur n'est jamais exposé à l'étudiant relu (RG11, Q8).
     */
    public record RelecteurVue(Long relectureId, Long etudiantId, String statut) {
    }

    /**
     * Exercice tel que renvoyé à l'interface.
     *
     * <p><b>Moyenne des deux relectures</b> (enveloppe étape 3) : depuis la
     * migration {@code V3}, un exercice est relu par deux pairs. La note
     * affichée est la moyenne des notes rendues, et non celle d'un relecteur
     * unique. Tant que les deux relectures ne sont pas rendues, la note est
     * marquée <em>provisoire</em>.
     *
     * <p>Le calcul est fait côté serveur : le frontend ne fait aucun calcul
     * (contrainte F3 du sujet).
     *
     * @param relectures   les deux relectures (liste vide si non assignées)
     * @param moyenne      moyenne des notes rendues, {@code null} si aucune
     * @param notesRendues nombre de notes effectivement rendues
     * @param statutNote   {@code PROVISOIRE} ou {@code DEFINITIVE}
     */
    public record Reponse(Long id, Long sessionId, Long etudiantId, String etudiantNom,
                          String lien, String statut, Instant deposeAt,
                          Double moyenne, Integer notesRendues, String statutNote,
                          List<CommentaireVue> commentaires,
                          List<RelecteurVue> relecteurs) {

        /** Un commentaire rendu, sans l'identité du relecteur (RG11 / Q8). */
        public record CommentaireVue(String commentaire) {
        }

        public static Reponse de(Exercice exercice) {
            return de(exercice, List.of());
        }

        /**
         * Construit la réponse à partir de l'exercice et de ses relectures.
         *
         * @param relectures relectures déjà chargées (évite le lazy loading
         */
        public static Reponse de(Exercice exercice, List<Relecture> relectures) {
            List<Integer> notes = relectures.stream()
                    .filter(r -> r.getStatut() == StatutRelecture.RENDUE)
                    .map(Relecture::getNote)
                    .filter(java.util.Objects::nonNull)
                    .toList();

            Double moyenne = null;
            if (!notes.isEmpty()) {
                double somme = notes.stream().mapToInt(Integer::intValue).sum();
                moyenne = Math.round((somme / notes.size()) * 100.0) / 100.0;
            }
            // Deux notes rendues = définitive ; sinon provisoire (enveloppe étape 3).
            String statutNote = notes.size() >= 2 ? "DEFINITIVE" : "PROVISOIRE";

            List<CommentaireVue> commentaires = relectures.stream()
                    .filter(r -> r.getStatut() == StatutRelecture.RENDUE)
                    .map(Relecture::getCommentaire)
                    .filter(java.util.Objects::nonNull)
                    .map(CommentaireVue::new)
                    .toList();

            List<RelecteurVue> vues = relectures.stream()
                    .map(r -> new RelecteurVue(
                            r.getId(),
                            r.getRelecteur().getId(),
                            r.getStatut().name()))
                    .toList();

            return new Reponse(
                    exercice.getId(),
                    exercice.getSession().getId(),
                    exercice.getEtudiant().getId(),
                    exercice.getEtudiant().getNom(),
                    exercice.getLien(),
                    exercice.getStatut().name(),
                    exercice.getDeposeAt(),
                    moyenne,
                    notes.size(),
                    statutNote,
                    commentaires,
                    vues);
        }
    }
}

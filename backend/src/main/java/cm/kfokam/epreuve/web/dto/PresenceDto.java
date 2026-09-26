package cm.kfokam.epreuve.web.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import cm.kfokam.epreuve.domaine.Presence;
import cm.kfokam.epreuve.domaine.SourcePresence;

/**
 * Objets d'échange de {@code /api/presences}.
 *
 * Entités jamais exposées : seuls ces DTO sortent en JSON, ce qui évite
 * toute fuite de structure interne et toute boucle de sérialisation
 * (contrainte B3).
 *
 * Annotations de validation (contrainte B4) : un corps vide ou partiel
 * est rejeté en 400 REQUETE_INVALIDE par ErreurHandler avant tout service.
 */
public final class PresenceDto {

    private PresenceDto() {
    }

    /** Corps de requête de l'opération imposée : {code, etudiantId}. */
    public record Requete(
            @NotBlank(message = "Le code de session est obligatoire.")
            @Size(max = 12, message = "Le code ne peut dépasser 12 caractères.")
            String code,

            @NotNull(message = "L'identifiant étudiant est obligatoire.")
            Long etudiantId
    ) {
    }

    /** Corps de requête de l'ajout manuel par le formateur (EF3). */
    public record RequeteFormateur(
            @NotNull(message = "L'identifiant de session est obligatoire.")
            Long sessionId,

            @NotNull(message = "L'identifiant étudiant est obligatoire.")
            Long etudiantId
    ) {
    }

    /** Corps de requête pour POST /api/sessions/{id}/presences : {etudiantId}. */
    public record RequeteFormateurSession(
            @NotNull(message = "L'identifiant étudiant est obligatoire.")
            Long etudiantId
    ) {
    }


    /** Réponse de création de présence. */
    public record Reponse(Long id, Long sessionId, Long etudiantId, String etudiantNom,
                          String source, Instant marqueeAt) {

        public static Reponse de(Presence presence) {
            return new Reponse(
                    presence.getId(),
                    presence.getSession().getId(),
                    presence.getEtudiant().getId(),
                    presence.getEtudiant().getNom(),
                    presence.getSource().name(),
                    presence.getMarqueeAt());
        }
    }
}

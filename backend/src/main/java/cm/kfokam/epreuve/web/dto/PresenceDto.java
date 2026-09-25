package cm.kfokam.epreuve.web.dto;

import java.time.Instant;

import cm.kfokam.epreuve.domaine.Presence;
import cm.kfokam.epreuve.domaine.SourcePresence;

/**
 * Objets d'échange de {@code /api/presences}.
 *
 * Entités jamais exposées : seuls ces DTO sortent en JSON, ce qui évite
 * toute fuite de structure interne et toute boucle de sérialisation
 * (contrainte B3).
 */
public final class PresenceDto {

    private PresenceDto() {
    }

    /** Corps de requête de l'opération imposée : {code, etudiantId}. */
    public record Requete(String code, Long etudiantId) {
    }

    /** Corps de requête de l'ajout manuel par le formateur (EF3). */
    public record RequeteFormateur(Long sessionId, Long etudiantId) {
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

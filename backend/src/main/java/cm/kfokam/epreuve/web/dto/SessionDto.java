package cm.kfokam.epreuve.web.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import cm.kfokam.epreuve.domaine.Session;

/**
 * Objets d'échange de {@code /api/sessions}.
 *
 * Le code est renvoyé par l'API — c'est le formateur qui le dicte à voix
 * haute. Aucun calcul d'expiration n'est laissé au frontend : le statut
 * « code encore valable » se déduit de {@code expirationAt}, exposé en UTC
 * (ISO-8601), comme partout ailleurs.
 */
public final class SessionDto {

    private SessionDto() {
    }

    /** Corps de requête d'ouverture : {titre, promotionId}. */
    public record Ouverture(
            @NotBlank(message = "Le titre est obligatoire.")
            @Size(max = 160, message = "Le titre ne peut dépasser 160 caractères.")
            String titre,

            @NotNull(message = "L'identifiant de promotion est obligatoire.")
            Long promotionId
    ) {
    }

    /** Session telle que renvoyée à l'interface. */
    public record Reponse(Long id, String titre, Long promotionId, String code,
                          Instant ouvertureAt, Instant expirationAt, String statut,
                          Instant clotureeAt) {

        public static Reponse de(Session session) {
            return new Reponse(
                    session.getId(),
                    session.getTitre(),
                    session.getPromotion().getId(),
                    session.getCode(),
                    session.getOuvertureAt(),
                    session.getExpirationAt(),
                    session.getStatut().name(),
                    session.getClotureeAt());
        }
    }
}

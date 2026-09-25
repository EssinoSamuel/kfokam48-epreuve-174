package cm.kfokam.epreuve.web.erreur;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Corps d'erreur imposé par le contrat : exactement {@code {code, message}}.
 * Toute erreur de l'API, sans exception, sort sous cette forme (contrainte B4).
 */
@JsonPropertyOrder({"code", "message"})
public record ReponseErreur(String code, String message) {

    public static ReponseErreur de(CodeErreur erreur) {
        return new ReponseErreur(erreur.name(), erreur.message());
    }

    public static ReponseErreur de(CodeErreur erreur, String message) {
        return new ReponseErreur(erreur.name(), message);
    }
}

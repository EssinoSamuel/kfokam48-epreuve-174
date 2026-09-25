package cm.kfokam.epreuve.service;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.stereotype.Component;

import cm.kfokam.epreuve.web.erreur.CodeErreur;
import cm.kfokam.epreuve.web.erreur.ErreurMetierException;

/**
 * Validation du lien d'exercice (400 LIEN_INVALIDE du contrat).
 *
 * Le client ne précise pas le format attendu ; le contrat déclare
 * {@code type: string, format: uri}. On reste donc permissif sur le schéma
 * (http, https, ssh, git…) mais strict sur le minimum : une URL absolue
 * exploitable, non vide, sans espaces.
 */
@Component
public class ValidateurLien {

    private static final int LONGUEUR_MAX = 2048;

    public String normaliser(String lien) {
        if (lien == null || lien.isBlank()) {
            throw new ErreurMetierException(CodeErreur.LIEN_INVALIDE,
                    "Le lien est obligatoire.");
        }
        String nettoye = lien.trim();
        if (nettoye.length() > LONGUEUR_MAX) {
            throw new ErreurMetierException(CodeErreur.LIEN_INVALIDE,
                    "Le lien dépasse " + LONGUEUR_MAX + " caractères.");
        }
        if (nettoye.contains(" ")) {
            throw new ErreurMetierException(CodeErreur.LIEN_INVALIDE,
                    "Le lien ne doit pas contenir d'espace.");
        }
        try {
            URI uri = new URI(nettoye);
            if (!uri.isAbsolute() || uri.getScheme() == null || uri.getHost() == null) {
                throw new ErreurMetierException(CodeErreur.LIEN_INVALIDE,
                        "Le lien doit être une URL absolue, par exemple https://github.com/…");
            }
            return uri.toString();
        } catch (URISyntaxException e) {
            throw new ErreurMetierException(CodeErreur.LIEN_INVALIDE,
                    "Le lien n'est pas une URL valide.");
        }
    }
}

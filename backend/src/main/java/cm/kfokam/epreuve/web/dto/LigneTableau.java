package cm.kfokam.epreuve.web.dto;

import java.util.List;

/**
 * Une ligne du tableau récapitulatif (EF10, Q16).
 *
 * Champs strictement issus de Q16 : nom, présences, exercices déposés,
 * moyenne, relectures en attente. {@code moyenne} est calculée par l'API
 * et vaut {@code null} tant qu'aucune note n'a été reçue.
 */
public record LigneTableau(
        Long etudiantId,
        String nom,
        long presences,
        long exercicesDeposes,
        Double moyenne,
        long relecturesEnAttente
) {

    public static LigneTableau de(Long etudiantId, String nom, long presences,
                                  long exercicesDeposes, Double moyenne,
                                  long relecturesEnAttente) {
        return new LigneTableau(etudiantId, nom, presences, exercicesDeposes,
                moyenne, relecturesEnAttente);
    }

    /** Le contrat renvoie un tableau JSON de lignes : la liste est donc la réponse. */
    public static List<LigneTableau> liste(List<LigneTableau> lignes) {
        return lignes;
    }
}

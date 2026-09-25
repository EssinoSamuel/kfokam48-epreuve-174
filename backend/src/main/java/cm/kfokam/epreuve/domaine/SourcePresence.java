package cm.kfokam.epreuve.domaine;

/**
 * Origine d'une présence (Q14, RG9).
 *
 * {@code ETUDIANT} : l'étudiant a saisi le code de la session.
 * {@code FORMATEUR} : le formateur a ajouté la présence à la main,
 * notamment quand l'étudiant n'avait pas de connexion.
 */
public enum SourcePresence {

    ETUDIANT,
    FORMATEUR
}

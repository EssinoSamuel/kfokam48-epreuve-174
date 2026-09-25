package cm.kfokam.epreuve.domaine;

/**
 * État d'une session (décision H2, source Q12).
 *
 * La clôture est une action explicite du formateur, distincte de
 * l'expiration automatique du code de présence (RG1) : le dépôt d'exercice
 * reste possible « le soir même », ce que 15 minutes interdiraient.
 */
public enum StatutSession {

    OUVERTE,
    CLOTUREE
}

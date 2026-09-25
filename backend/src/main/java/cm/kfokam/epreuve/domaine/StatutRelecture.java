package cm.kfokam.epreuve.domaine;

/**
 * Cycle de vie d'une relecture (D4).
 *
 * <ul>
 *   <li>{@code ASSIGNEE} — un relecteur a été tiré au sort, il n'a pas ouvert
 *       l'exercice : le lien peut encore être remplacé (Q13) ;</li>
 *   <li>{@code EN_COURS} — la relecture a démarré : le lien est verrouillé ;</li>
 *   <li>{@code RENDUE} — note et commentaire envoyés : définitif (Q15).</li>
 * </ul>
 */
public enum StatutRelecture {

    ASSIGNEE,
    EN_COURS,
    RENDUE
}

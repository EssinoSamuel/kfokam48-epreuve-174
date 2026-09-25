package cm.kfokam.epreuve.domaine;

/**
 * Cycle de vie d'un exercice déposé (D4).
 *
 * <ul>
 *   <li>{@code DEPOSE} — déposé, sans relecteur assigné (aucun candidat
 *       éligible : décision H6, pas de nouvelle tentative automatique) ;</li>
 *   <li>{@code EN_ATTENTE_DE_RELECTURE} — un relecteur a été tiré au sort ;</li>
 *   <li>{@code RELU} — la note et le commentaire ont été rendus.</li>
 * </ul>
 */
public enum StatutExercice {

    DEPOSE,
    EN_ATTENTE_DE_RELECTURE,
    RELU
}

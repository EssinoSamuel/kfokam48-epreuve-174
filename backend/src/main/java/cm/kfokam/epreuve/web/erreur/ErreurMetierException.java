package cm.kfokam.epreuve.web.erreur;

/**
 * Erreur métier portée jusqu'à la couche web.
 *
 * Un service lève une exception de ce type ; le contrôleur ne l'attrape
 * jamais : {@link ErreurHandler} la convertit en réponse {@code {code, message}}
 * avec le bon code de statut (contrainte B4).
 */
public class ErreurMetierException extends RuntimeException {

    private final CodeErreur codeErreur;

    public ErreurMetierException(CodeErreur codeErreur) {
        this(codeErreur, codeErreur.message());
    }

    public ErreurMetierException(CodeErreur codeErreur, String message) {
        super(message);
        this.codeErreur = codeErreur;
    }

    public CodeErreur codeErreur() {
        return codeErreur;
    }
}

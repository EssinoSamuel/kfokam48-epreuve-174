package cm.kfokam.epreuve.web.erreur;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Gestion centralisée des erreurs (contrainte B4).
 *
 * Aucune stack trace, aucune page d'erreur Spring ne doit atteindre le
 * client : toutes les sorties passent par {@link ReponseErreur}, au format
 * {@code {code, message}} imposé par le contrat.
 */
@RestControllerAdvice
public class ErreurHandler {

    private static final Logger log = LoggerFactory.getLogger(ErreurHandler.class);

    /** Erreur métier explicite levée par un service. */
    @ExceptionHandler(ErreurMetierException.class)
    public ResponseEntity<ReponseErreur> surErreurMetier(ErreurMetierException ex) {
        CodeErreur erreur = ex.codeErreur();
        return ResponseEntity.status(erreur.statut())
                .body(ReponseErreur.de(erreur, ex.getMessage()));
    }

    /**
     * Validation des entrées ({@code @Valid}). On garde le premier message
     * en clair : le client doit comprendre ce qui manque, mais le code
     * stable reste REQUETE_INVALIDE.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ReponseErreur> surValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " : " + e.getDefaultMessage())
                .collect(Collectors.joining(" ; "));
        String message = details.isBlank()
                ? CodeErreur.REQUETE_INVALIDE.message()
                : details;
        return reponse(CodeErreur.REQUETE_INVALIDE, message);
    }

    /** Corps JSON absent, illisible ou mal typé. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ReponseErreur> surCorpsIllisible(HttpMessageNotReadableException ex) {
        return reponse(CodeErreur.REQUETE_INVALIDE, "Le corps de la requête est absent ou illisible.");
    }

    /** Paramètre de requête obligatoire manquant (ex. promotionId). */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ReponseErreur> surParametreManquant(MissingServletRequestParameterException ex) {
        return reponse(CodeErreur.REQUETE_INVALIDE,
                "Paramètre obligatoire manquant : " + ex.getParameterName() + ".");
    }

    /** Paramètre de chemin ou de requête non convertible (ex. id non numérique). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ReponseErreur> surTypeInvalide(MethodArgumentTypeMismatchException ex) {
        return reponse(CodeErreur.REQUETE_INVALIDE,
                "Valeur invalide pour le paramètre : " + ex.getName() + ".");
    }

    /** Un statut de filtre inconnu (ex. ?statut=PAS_UN_STATUT) est une entrée invalide. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ReponseErreur> surArgumentInvalide(IllegalArgumentException ex) {
        log.debug("Argument invalide reçu", ex);
        return reponse(CodeErreur.REQUETE_INVALIDE, ex.getMessage());
    }

    /** URL inexistante : on répond dans le format imposé, pas la page Spring. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ReponseErreur> surRessourceAbsente(NoResourceFoundException ex) {
        return reponse(CodeErreur.RESSOURCE_INTROUVABLE, CodeErreur.RESSOURCE_INTROUVABLE.message());
    }

    /** Filet de sécurité : toute autre exception devient un 500 propre, sans fuite. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ReponseErreur> surErreurInattendue(Exception ex) {
        log.error("Erreur inattendue non gérée", ex);
        return reponse(CodeErreur.ERREUR_INTERNE, CodeErreur.ERREUR_INTERNE.message());
    }

    private ResponseEntity<ReponseErreur> reponse(CodeErreur erreur, String message) {
        return ResponseEntity.status(erreur.statut()).body(ReponseErreur.de(erreur, message));
    }
}

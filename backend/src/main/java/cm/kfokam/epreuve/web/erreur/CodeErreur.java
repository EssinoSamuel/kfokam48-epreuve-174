package cm.kfokam.epreuve.web.erreur;

import org.springframework.http.HttpStatus;

/**
 * Catalogue central des erreurs métier.
 *
 * Chaque constante porte le {@code code} stable attendu dans le corps
 * {@code {code, message}} imposé par le contrat, et le code de statut HTTP
 * à renvoyer. Les codes de statut des cinq opérations imposées sont gelés :
 * ici, RG10 réutilise par exemple le 400 déjà prévu, avec un code métier
 * distinct (TROP_DE_TENTATIVES) — voir CDC section 7.
 */
public enum CodeErreur {

    // --- Opérations imposées (codes de statut gelés par le contrat) ---
    CODE_INCONNU(HttpStatus.BAD_REQUEST, "Aucune session ne correspond à ce code de présence."),
    CODE_EXPIRE(HttpStatus.GONE, "Le code de présence a expiré."),
    DEJA_PRESENT(HttpStatus.CONFLICT, "Cet étudiant est déjà marqué présent pour cette session."),
    LIEN_INVALIDE(HttpStatus.BAD_REQUEST, "Le lien fourni n'est pas une URL exploitable."),
    EXERCICE_DEJA_DEPOSE(HttpStatus.CONFLICT, "Un exercice a déjà été déposé pour cette session."),
    NOTE_INVALIDE(HttpStatus.BAD_REQUEST, "La note doit être un entier compris entre 0 et 20."),
    AUTO_RELECTURE(HttpStatus.FORBIDDEN, "Un étudiant ne peut pas relire son propre exercice."),
    RELECTURE_DEJA_RENDUE(HttpStatus.CONFLICT, "Cette relecture a déjà été rendue : la note est définitive."),
    PROMOTION_INCONNUE(HttpStatus.NOT_FOUND, "Aucune promotion ne correspond à cet identifiant."),

    // --- Zone libre (décisions H2, H3, H7, H9 du cahier des charges) ---
    SESSION_INCONNUE(HttpStatus.NOT_FOUND, "Aucune session ne correspond à cet identifiant."),
    SESSION_DEJA_CLOTUREE(HttpStatus.CONFLICT, "Cette session est déjà clôturée."),
    SESSION_CLOTUREE(HttpStatus.CONFLICT, "La session est clôturée : cette opération n'est plus autorisée."),
    ETUDIANT_INCONNU(HttpStatus.NOT_FOUND, "Aucun étudiant ne correspond à cet identifiant."),
    EXERCICE_INCONNU(HttpStatus.NOT_FOUND, "Aucun exercice ne correspond à cet identifiant."),
    RELECTURE_INCONNUE(HttpStatus.NOT_FOUND, "Aucune relecture ne correspond à cet identifiant."),
    RELECTURE_DEJA_COMMENCEE(HttpStatus.CONFLICT, "La relecture a déjà commencé : le lien est verrouillé."),
    TROP_DE_TENTATIVES(HttpStatus.BAD_REQUEST, "Trop de tentatives : réessayez dans quelques minutes."),

    // --- Erreurs techniques, jamais de stack trace exposée (contrainte B4) ---
    REQUETE_INVALIDE(HttpStatus.BAD_REQUEST, "La requête est mal formée ou incomplète."),
    RESSOURCE_INTROUVABLE(HttpStatus.NOT_FOUND, "La ressource demandée n'existe pas."),
    ERREUR_INTERNE(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur interne est survenue.");

    private final HttpStatus statut;
    private final String message;

    CodeErreur(HttpStatus statut, String message) {
        this.statut = statut;
        this.message = message;
    }

    public HttpStatus statut() {
        return statut;
    }

    public String message() {
        return message;
    }
}

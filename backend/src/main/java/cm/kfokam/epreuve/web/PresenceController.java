package cm.kfokam.epreuve.web;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;


import cm.kfokam.epreuve.domaine.Etudiant;
import cm.kfokam.epreuve.domaine.Presence;
import cm.kfokam.epreuve.domaine.Session;
import cm.kfokam.epreuve.repository.EtudiantRepository;
import cm.kfokam.epreuve.repository.SessionRepository;
import cm.kfokam.epreuve.service.PresenceService;
import cm.kfokam.epreuve.web.dto.PresenceDto;
import cm.kfokam.epreuve.web.erreur.CodeErreur;
import cm.kfokam.epreuve.web.erreur.ErreurMetierException;

import jakarta.validation.Valid;

/**
 * Opération imposée n°1 du contrat : {@code POST /api/presences}.
 *
 * Chemin, verbe, corps de requête et codes de statut sont <b>gelés</b> :
 * 201, 400 (code inconnu — et RG10 via TROP_DE_TENTATIVES), 409, 410.
 * Aucun code de statut supplémentaire n'a été introduit ici.
 *
 * Le contrôleur ne porte aucune règle de gestion : il traduit la requête,
 * appelle le service, et mappe le résultat en réponse HTTP (contrainte B3).
 */
@RestController
@RequestMapping("/api/presences")
public class PresenceController {

    private final PresenceService presenceService;
    private final SessionRepository sessions;
    private final EtudiantRepository etudiants;

    public PresenceController(PresenceService presenceService,
                              SessionRepository sessions,
                              EtudiantRepository etudiants) {
        this.presenceService = presenceService;
        this.sessions = sessions;
        this.etudiants = etudiants;
    }

    @PostMapping
    public ResponseEntity<PresenceDto.Reponse> marquer(@Valid @RequestBody PresenceDto.Requete requete) {
        Etudiant etudiant = etudiants.findById(requete.etudiantId())
                .orElseThrow(() -> new ErreurMetierException(CodeErreur.ETUDIANT_INCONNU));

        Session session = sessions.findByCode(requete.code()).orElse(null);

        // Un code inconnu n'a pas de session à rattacher : le compteur RG10 est
        // alors porté par l'étudiant seul (H7), et la réponse reste le 400
        // imposé, qu'il soit CODE_INCONNU ou TROP_DE_TENTATIVES.
        if (session == null) {
            presenceService.enregistrerCodeInconnu(etudiant);
            throw new ErreurMetierException(CodeErreur.CODE_INCONNU);
        }

        // RG10 : le blocage est vérifié dans le service, avant l'expiration et
        // avant l'unicité. Le 409 éventuel ne consomme pas une tentative.
        Presence presence = presenceService.marquerAvecCode(session, etudiant, true);

        return ResponseEntity.status(HttpStatus.CREATED).body(PresenceDto.Reponse.de(presence));
    }

    /** Zone libre (H3) : le formateur ajoute une présence manuellement par path. */
    @PostMapping("/session/{sessionId}")
    public ResponseEntity<PresenceDto.Reponse> ajouterParSessionPath(
            @PathVariable Long sessionId,
            @Valid @RequestBody PresenceDto.RequeteFormateurSession requete) {
        Session session = sessions.findById(sessionId)
                .orElseThrow(() -> new ErreurMetierException(CodeErreur.SESSION_INCONNUE));
        Etudiant etudiant = etudiants.findById(requete.etudiantId())
                .orElseThrow(() -> new ErreurMetierException(CodeErreur.ETUDIANT_INCONNU));
        Presence presence = presenceService.ajouterParFormateur(session, etudiant);
        return ResponseEntity.status(HttpStatus.CREATED).body(PresenceDto.Reponse.de(presence));
    }

    /** Liste des présents d'une session — renvoie un tableau simple. */
    @org.springframework.web.bind.annotation.GetMapping("/session/{sessionId}")
    public List<PresenceDto.Reponse> parSession(
            @org.springframework.web.bind.annotation.PathVariable Long sessionId) {
        Session session = sessions.findById(sessionId)
                .orElseThrow(() -> new ErreurMetierException(CodeErreur.SESSION_INCONNUE));
        return presenceService.etudiantsPresents(session.getId()).stream()
                .map(e -> new PresenceDto.Reponse(null, session.getId(), e.getId(), e.getNom(),
                        null, null))
                .toList();
    }
}

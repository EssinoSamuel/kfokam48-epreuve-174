package cm.kfokam.epreuve.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cm.kfokam.epreuve.domaine.Session;
import cm.kfokam.epreuve.service.SessionService;
import cm.kfokam.epreuve.service.TableauService;
import cm.kfokam.epreuve.web.dto.SessionDto;

import jakarta.validation.Valid;

/**
 * Sessions : ouverture, clôture, consultations.
 *
 * <ul>
 *   <li><b>EF1</b> — {@code POST /api/sessions} : ouvre et génère un code
 *       valable 15 minutes (Q2) ;</li>
 *   <li><b>EF4 / H2</b> — {@code PATCH /api/sessions/{id}/cloture} : clôture
 *       explicite du formateur (Q12), ajout en zone libre validé au CDC ;</li>
 *   <li><b>EF10</b> — {@code GET /api/sessions/{id}/tableau} : tableau
 *       récapitulatif, moyenne calculée côté API (Q16).</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final TableauService tableauService;

    public SessionController(SessionService sessionService, TableauService tableauService) {
        this.sessionService = sessionService;
        this.tableauService = tableauService;
    }

    /** EF1 : ouverture d'une session avec génération du code de présence. */
    @PostMapping
    public ResponseEntity<SessionDto.Reponse> ouvrir(@Valid @RequestBody SessionDto.Ouverture requete) {
        Session session = sessionService.ouvrir(requete.titre(), requete.promotionId());
        return ResponseEntity.status(HttpStatus.CREATED).body(SessionDto.Reponse.de(session));
    }

    /** EF4 : clôture explicite — borne le dépôt d'exercice (RG7, Q12). */
    @PatchMapping("/{id}/cloture")
    public SessionDto.Reponse cloturer(@PathVariable Long id) {
        return SessionDto.Reponse.de(sessionService.cloturer(id));
    }

    @GetMapping
    public List<SessionDto.Reponse> parPromotion(@RequestParam Long promotionId) {
        return sessionService.parPromotion(promotionId).stream()
                .map(SessionDto.Reponse::de)
                .toList();
    }

    @GetMapping("/{id}")
    public SessionDto.Reponse parId(@PathVariable Long id) {
        return SessionDto.Reponse.de(sessionService.parId(id));
    }

    /** EF10 / Q16 : tableau récapitulatif de la promotion, moyenne incluse. */
    @GetMapping("/{id}/tableau")
    public List<cm.kfokam.epreuve.web.dto.LigneTableau> tableau(@PathVariable Long id) {
        Session session = sessionService.parId(id);
        return tableauService.pourPromotion(session.getPromotion().getId());
    }
}

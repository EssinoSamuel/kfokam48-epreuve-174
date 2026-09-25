package cm.kfokam.epreuve.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cm.kfokam.epreuve.domaine.Relecture;
import cm.kfokam.epreuve.service.RelectureService;
import cm.kfokam.epreuve.web.dto.RelectureDto;

import jakarta.validation.Valid;

/**
 * Relectures : démarrage (opération imposée n°4) et envoi de la note
 * (opération imposée n°5).
 *
 * Codes de statut gelés, strictement respectés :
 * <ul>
 *   <li>{@code POST /api/relectures} → 201, plus les erreurs applicables
 *       (403 AUTO_RELECTURE, 404, 409) ;</li>
 *   <li>{@code POST /api/relectures/{id}/note} → 200, 400 NOTE_INVALIDE,
 *       403, 409 RELECTURE_DEJA_RENDUE.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/relectures")
public class RelectureController {

    private final RelectureService relectureService;

    public RelectureController(RelectureService relectureService) {
        this.relectureService = relectureService;
    }

    /** Opération imposée n°4 : démarrer une relecture (verrouille le lien). */
    @PostMapping
    public ResponseEntity<RelectureDto.Reponse> demarrer(
            @Valid @RequestBody RelectureDto.Demarrage requete) {
        Relecture relecture = relectureService.demarrer(requete.relectureId());
        return ResponseEntity.status(HttpStatus.CREATED).body(RelectureDto.Reponse.de(relecture));
    }

    /** Opération imposée n°5 : envoyer la note et le commentaire — définitif (RG5). */
    @PostMapping("/{id}/note")
    public RelectureDto.Reponse noter(@PathVariable Long id,
                                      @Valid @RequestBody RelectureDto.Note requete) {
        return RelectureDto.Reponse.de(relectureService.rendre(id, requete.note(), requete.commentaire()));
    }

    /** Relectures assignées à un étudiant — écran relecteur (EF7). */
    @GetMapping
    public List<RelectureDto.Reponse> parRelecteur(@RequestParam Long etudiantId) {
        return relectureService.pourRelecteur(etudiantId).stream()
                .map(RelectureDto.Reponse::de)
                .toList();
    }

    @GetMapping("/{id}")
    public RelectureDto.Reponse parId(@PathVariable Long id) {
        return RelectureDto.Reponse.de(relectureService.parId(id));
    }
}

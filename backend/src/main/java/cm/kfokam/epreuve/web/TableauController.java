package cm.kfokam.epreuve.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cm.kfokam.epreuve.service.TableauService;
import cm.kfokam.epreuve.web.dto.LigneTableau;

/**
 * Opération imposée n°5 du contrat : {@code GET /api/tableau?promotionId=...}.
 *
 * Le contrat d'API impose ce chemin exact avec le paramètre {@code promotionId}.
 * La moyenne vient directement du backend et n'est jamais recalculée côté client (F3).
 */
@RestController
@RequestMapping("/api/tableau")
public class TableauController {

    private final TableauService tableauService;

    public TableauController(TableauService tableauService) {
        this.tableauService = tableauService;
    }

    @GetMapping
    public List<LigneTableau> tableau(@RequestParam Long promotionId) {
        return tableauService.pourPromotion(promotionId);
    }
}

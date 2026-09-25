package cm.kfokam.epreuve.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Point de contrôle minimal, sans authentification.
 *
 * Utile au formateur pour vérifier d'un seul appel que le service est bien
 * démarré et la base jointe : {@code GET /api/sante}.
 */
@RestController
@RequestMapping("/api")
public class SanteController {

    @GetMapping("/sante")
    public Map<String, String> sante() {
        return Map.of(
                "statut", "OK",
                "application", "kfokam48-epreuve-backend");
    }
}

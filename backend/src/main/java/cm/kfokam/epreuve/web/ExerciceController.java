package cm.kfokam.epreuve.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cm.kfokam.epreuve.domaine.Exercice;
import cm.kfokam.epreuve.domaine.StatutExercice;
import cm.kfokam.epreuve.repository.RelectureRepository;
import cm.kfokam.epreuve.service.ExerciceService;
import cm.kfokam.epreuve.web.dto.ExerciceDto;

import jakarta.validation.Valid;

/**
 * Exercices : dépôt (opération imposée n°2), remplacement du lien (EF6),
 * consultations.
 *
 * Codes de statut de l'opération imposée, strictement respectés :
 * 201, 400 (LIEN_INVALIDE), 409 (EXERCICE_DEJA_DEPOSE).
 */
@RestController
@RequestMapping("/api/exercices")
public class ExerciceController {

    private final ExerciceService exerciceService;
    private final RelectureRepository relectures;

    public ExerciceController(ExerciceService exerciceService, RelectureRepository relectures) {
        this.exerciceService = exerciceService;
        this.relectures = relectures;
    }

    /** Opération imposée n°2 : dépôt du lien d'un exercice. */
    @PostMapping
    public ResponseEntity<ExerciceDto.Reponse> deposer(@Valid @RequestBody ExerciceDto.Depot requete) {
        Exercice exercice = exerciceService.deposer(requete.sessionId(), requete.etudiantId(), requete.lien());
        return ResponseEntity.status(HttpStatus.CREATED).body(ExerciceDto.Reponse.de(exercice));
    }

    /**
     * EF6 (Q13) : remplacement du lien par son auteur, tant que personne
     * n'a commencé à le relire et que la session n'est pas clôturée.
     * Zone libre : le contrat n'impose aucun code de statut ici.
     */
    @PutMapping("/{id}")
    public ExerciceDto.Reponse remplacer(@PathVariable Long id,
                                         @Valid @RequestBody ExerciceDto.Lien requete) {
        return ExerciceDto.Reponse.de(exerciceService.remplacerLien(id, requete.lien()));
    }

    @GetMapping
    public List<ExerciceDto.Reponse> lister(@RequestParam(required = false) Long sessionId,
                                            @RequestParam(required = false) Long etudiantId,
                                            @RequestParam(required = false) StatutExercice statut) {
        List<Exercice> resultats;
        if (sessionId != null) {
            resultats = exerciceService.parSession(sessionId, statut);
        } else if (etudiantId != null) {
            resultats = exerciceService.parEtudiant(etudiantId);
        } else {
            resultats = List.of();
        }
        return resultats.stream()
                .map(e -> ExerciceDto.Reponse.de(e, e.getRelectureLecture()))
                .toList();
    }

    @GetMapping("/{id}")
    public ExerciceDto.Reponse parId(@PathVariable Long id) {
        Exercice exercice = exerciceService.parId(id);
        return ExerciceDto.Reponse.de(exercice, exercice.getRelectureLecture());
    }
}

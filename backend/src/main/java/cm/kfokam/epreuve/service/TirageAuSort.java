package cm.kfokam.epreuve.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import cm.kfokam.epreuve.domaine.Etudiant;

/**
 * Tirage au sort du relecteur (Q7) : « au hasard, parmi les étudiants
 * présents à cette session ».
 *
 * Deux règles non négociables s'ajoutent au hasard :
 * <ul>
 *   <li><b>RG2</b> — le relecteur n'est jamais l'auteur de l'exercice ;</li>
 *   <li><b>H6</b> — s'il ne reste aucun candidat, on ne force pas :
 *       l'exercice reste sans relecteur, visible du formateur.</li>
 * </ul>
 *
 * La classe est isolée du service pour être testable seule : le test unitaire
 * de RG2 (contrainte B6) injecte ici une liste d'un seul élément, l'auteur
 * lui-même, et vérifie qu'aucun relecteur n'est proposé.
 */
@Component
public class TirageAuSort {

    public Optional<Etudiant> choisir(List<Etudiant> presents, Long idAuteur) {
        if (presents == null || presents.isEmpty()) {
            return Optional.empty();
        }
        List<Etudiant> eligibles = presents.stream()
                .filter(e -> e.getId() != null && !e.getId().equals(idAuteur))
                .toList();
        if (eligibles.isEmpty()) {
            return Optional.empty();
        }
        // Choix uniforme parmi les éligibles : chaque pair présent a la même chance.
        int index = java.util.concurrent.ThreadLocalRandom.current().nextInt(eligibles.size());
        return Optional.of(eligibles.get(index));
    }
}

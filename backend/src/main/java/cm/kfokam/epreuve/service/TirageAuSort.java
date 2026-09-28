package cm.kfokam.epreuve.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

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

    /**
     * Tirage au sort d'un relecteur parmi les étudiants présents (Q7, RG2).
     *
     * <p>Conservé pour les tests unitaires et le cas mono-relecteur.
     */
    public Optional<Etudiant> choisir(List<Etudiant> presents, Long idAuteur) {
        return choisir(presents, idAuteur, 1).stream().findFirst();
    }

    /**
     * Tirage au sort de {@code nombre} relecteurs distincts (enveloppe étape 3).
     *
     * <p>Contraintes respectées :
     * <ul>
     *   <li><b>RG2</b> — l'auteur n'est jamais choisi ;</li>
     *   <li><b>RG12</b> — les relecteurs sont tous différents les uns des
     *       autres : on retire chaque Tirage de la liste des candidats ;</li>
     *   <li><b>Q7</b> — choix uniforme parmi les étudiants présents.</li>
     * </ul>
     *
     * @return jusqu'à {@code nombre} relecteurs, ou une liste plus courte s'il
     *         n'y a pas assez d'éligibles (dégradation assumée : l'exercice
     *         reste Assigné à un seul pair plutôt que de bloquer le dépôt)
     */
    public List<Etudiant> choisir(List<Etudiant> presents, Long idAuteur, int nombre) {
        if (presents == null || presents.isEmpty() || nombre <= 0) {
            return List.of();
        }
        List<Etudiant> eligibles = new ArrayList<>(presents.stream()
                .filter(e -> e.getId() != null && !e.getId().equals(idAuteur))
                .toList());

        List<Etudiant> retenus = new ArrayList<>();
        while (retenus.size() < nombre && !eligibles.isEmpty()) {
            int index = ThreadLocalRandom.current().nextInt(eligibles.size());
            // remove(index) garantit l'unicité du relecteur retenu (RG12).
            retenus.add(eligibles.remove(index));
        }
        return retenus;
    }
}

package cm.kfokam.epreuve.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cm.kfokam.epreuve.domaine.Etudiant;

/**
 * Test unitaire de la règle RG2 (contrainte B6).
 *
 * Règle testée : « un étudiant ne peut jamais relire son propre exercice »
 * (Q5), appliquée lors du tirage au sort du relecteur (Q7).
 *
 * Pourquoi un test unitaire et pas un test d'intégration : la règle est
 * purement logique, elle ne dépend ni de la base ni du web. Un test unitaire
 * s'exécute donc en quelques millisecondes, sur un poste vierge, sans Docker.
 *
 * Le « pourquoi ce test » : RG2 est la règle la plus grave du domaine — un
 * student qui relit son propre travail fausserait toute la moyenne de la
 * promotion. Elle mérite un test explicite et lisible.
 */
class TirageAuSortTest {

    private final TirageAuSort tirageAuSort = new TirageAuSort();

    /** Construit un étudiant simulé avec son identifiant. */
    private Etudiant etudiant(Long id) {
        Etudiant mock = mock(Etudiant.class);
        when(mock.getId()).thenReturn(id);
        return mock;
    }

    @Test
    @DisplayName("RG2 : l'auteur de l'exercice n'est jamais choisi comme relecteur")
    void auteurJamaisRelecteur() {
        Etudiant auteur = etudiant(1L);
        Etudiant pair = etudiant(2L);

        // Cent tirages : le risque qu'un tirage « passe » par hasard
        // sur l'auteur serait une fausse preuve de sécurité.
        for (int i = 0; i < 100; i++) {
            Optional<Etudiant> choisi = tirageAuSort.choisir(List.of(auteur, pair), 1L);
            assertThat(choisi)
                    .as("L'auteur (id=1) ne doit jamais être désigné relecteur")
                    .isPresent()
                    .get()
                    .extracting(Etudiant::getId)
                    .isNotEqualTo(1L);
        }
    }

    @Test
    @DisplayName("H6 : seul l'auteur est présent, aucun relecteur n'est forcé")
    void aucunCandidatQuandSeulLAuteurEstPresent() {
        Etudiant auteur = etudiant(1L);

        Optional<Etudiant> choisi = tirageAuSort.choisir(List.of(auteur), 1L);

        assertThat(choisi)
                .as("Sans candidat éligible, l'exercice reste sans relecteur")
                .isEmpty();
    }

    @Test
    @DisplayName("Q7 : le relecteur est choisi parmi les étudiants présents")
    void relecteurChoisiParmiLesPresents() {
        Etudiant auteur = etudiant(1L);
        Etudiant pair1 = etudiant(2L);
        Etudiant pair2 = etudiant(3L);

        Optional<Etudiant> choisi =
                tirageAuSort.choisir(List.of(auteur, pair1, pair2), 1L);

        assertThat(choisi).isPresent();
        assertThat(choisi.get().getId()).isIn(2L, 3L);
    }

    @Test
    @DisplayName("Session sans aucun étudiant présent : aucun relecteur")
    void aucunPresent() {
        assertThat(tirageAuSort.choisir(List.of(), 1L)).isEmpty();
    }
}

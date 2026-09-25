package cm.kfokam.epreuve.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cm.kfokam.epreuve.domaine.Exercice;
import cm.kfokam.epreuve.domaine.StatutExercice;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

    /** Un seul dépôt par étudiant et par session : base du 409 imposé. */
    Optional<Exercice> findBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    List<Exercice> findBySessionIdOrderByDeposeAtAsc(Long sessionId);

    List<Exercice> findBySessionIdAndStatutOrderByDeposeAtAsc(Long sessionId, StatutExercice statut);

    List<Exercice> findByEtudiantIdOrderByDeposeAtDesc(Long etudiantId);

    @Query("select e from Exercice e join fetch e.session where e.etudiant.id = :etudiantId order by e.deposeAt desc")
    List<Exercice> trouverAvecSessionPourEtudiant(@Param("etudiantId") Long etudiantId);

    long countByEtudiantId(Long etudiantId);
}

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

    /**
     * Exercices d'un étudiant avec leurs relectures déjà chargées.
     * Le fetch join évite la LazyInitializationException : la transaction
     * se ferme avant la sérialisation JSON (contrainte B3, DTO).
     */
    @Query("""
            select distinct e from Exercice e
            left join fetch e.relectureLecture r
            left join fetch r.relecteur
            join fetch e.session
            join fetch e.etudiant
            where e.etudiant.id = :etudiantId
            order by e.deposeAt desc
            """)
    List<Exercice> trouverAvecDetailsPourEtudiant(@Param("etudiantId") Long etudiantId);

    /**
     * Exercices d'une session, détails chargés.
     * Lazy loading impossible après fermeture de la transaction.
     */
    @Query("""
            select distinct e from Exercice e
            left join fetch e.relectureLecture r
            left join fetch r.relecteur
            join fetch e.session
            join fetch e.etudiant
            where e.session.id = :sessionId
              and (:statut is null or e.statut = :statut)
            order by e.deposeAt asc
            """)
    List<Exercice> trouverAvecDetailsPourSession(@Param("sessionId") Long sessionId,
                                                @Param("statut") StatutExercice statut);

    long countByEtudiantId(Long etudiantId);
}

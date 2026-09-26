package cm.kfokam.epreuve.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cm.kfokam.epreuve.domaine.Relecture;
import cm.kfokam.epreuve.domaine.StatutRelecture;

public interface RelectureRepository extends JpaRepository<Relecture, Long> {

    /** Un seul relecteur par exercice (Q6) : la base le garantit aussi. */
    Optional<Relecture> findByExerciceId(Long exerciceId);

    boolean existsByExerciceId(Long exerciceId);

    /** Relectures assignées à un étudiant : écran relecteur (EF7). */
    @Query("""
            select r from Relecture r
            join fetch r.exercice e
            join fetch e.session
            join fetch e.etudiant
            join fetch r.relecteur
            where r.relecteur.id = :etudiantId
            order by r.id asc
            """)
    List<Relecture> trouverPourRelecteur(@Param("etudiantId") Long etudiantId);

    @Query("select count(r) from Relecture r where r.relecteur.id = :etudiantId and r.statut = :statut")
    long compterParRelecteurEtStatut(@Param("etudiantId") Long etudiantId,
                                     @Param("statut") StatutRelecture statut);

    /** Nombre de relectures en attente sur une session (colonne du tableau, Q16). */
    @Query("""
            select count(r) from Relecture r
            where r.exercice.session.id = :sessionId
              and r.statut <> cm.kfokam.epreuve.domaine.StatutRelecture.RENDUE
            """)
    long compterEnAttenteParSession(@Param("sessionId") Long sessionId);

    @Query("""
            select count(r) from Relecture r
            where r.exercice.etudiant.id = :etudiantId
              and r.statut <> cm.kfokam.epreuve.domaine.StatutRelecture.RENDUE
            """)
    long compterEnAttentePourEtudiant(@Param("etudiantId") Long etudiantId);
}

package cm.kfokam.epreuve.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cm.kfokam.epreuve.domaine.TentativeCode;

public interface TentativeCodeRepository extends JpaRepository<TentativeCode, Long> {

    /** Cas « session connue » : rattache le compteur au couple (étudiant, session). */
    Optional<TentativeCode> findByEtudiantIdAndSessionId(Long etudiantId, Long sessionId);

    /** Cas « code inconnu » : aucune session à rattacher (H7). */
    Optional<TentativeCode> findByEtudiantIdAndSessionIsNull(Long etudiantId);

    /**
     * Suppression des compteurs d'une session : utilisé par les tests de RG10
     * qui rejouent plusieurs scénarios de suite.
     */
    @Modifying
    @Query("delete from TentativeCode t where t.session.id = :sessionId")
    void supprimerPourSession(@Param("sessionId") Long sessionId);
}

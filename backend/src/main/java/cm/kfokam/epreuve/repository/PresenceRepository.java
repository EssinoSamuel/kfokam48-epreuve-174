package cm.kfokam.epreuve.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cm.kfokam.epreuve.domaine.Etudiant;
import cm.kfokam.epreuve.domaine.Presence;
import cm.kfokam.epreuve.domaine.Session;

public interface PresenceRepository extends JpaRepository<Presence, Long> {

    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    long countBySessionId(Long sessionId);

    long countByEtudiantId(Long etudiantId);

    /**
     * Étudiants présents à une session : c'est le vivier dans lequel le
     * relecteur est tiré au sort (Q7).
     */
    @Query("select p.etudiant from Presence p where p.session.id = :sessionId order by p.etudiant.id asc")
    List<Etudiant> etudiantsPresents(@Param("sessionId") Long sessionId);

    List<Presence> findBySession(Session session);
}

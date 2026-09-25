package cm.kfokam.epreuve.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cm.kfokam.epreuve.domaine.Session;

public interface SessionRepository extends JpaRepository<Session, Long> {

    /** Recherche par code de présence : le contrat impose le code comme clé (RG1). */
    Optional<Session> findByCode(String code);

    /** Sessions d'une promotion, les plus récentes d'abord (H9, écran formateur). */
    List<Session> findByPromotionIdOrderByOuvertureAtDesc(Long promotionId);

    boolean existsByCode(String code);
}

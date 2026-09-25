package cm.kfokam.epreuve.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cm.kfokam.epreuve.domaine.Etudiant;

public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {

    /** Étudiants d'une promotion, triés par nom : sélecteur « choisir son nom » (Q1). */
    List<Etudiant> findByPromotionIdOrderByNomAsc(Long promotionId);

    @Query("select count(e) from Etudiant e where e.promotion.id = :promotionId")
    long compterParPromotion(@Param("promotionId") Long promotionId);
}

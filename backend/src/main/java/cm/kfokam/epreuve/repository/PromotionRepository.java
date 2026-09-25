package cm.kfokam.epreuve.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cm.kfokam.epreuve.domaine.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    /** Liste triée des promotions : alimente le sélecteur de l'écran formateur (H9). */
    List<Promotion> findAllByOrderByNomAsc();

    Optional<Promotion> findByNom(String nom);
}

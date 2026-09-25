package cm.kfokam.epreuve.domaine;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.util.Objects;

/**
 * Établissement de rattachement des étudiants et des sessions.
 * Donnée préexistante (H1) : injectée par la migration de démonstration,
 * aucune opération de création n'est exposée.
 */
@Entity
@Table(name = "promotion")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, unique = true, length = 120)
    private String nom;

    protected Promotion() {
        // requis par JPA
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    @Override
    public boolean equals(Object autre) {
        if (this == autre) {
            return true;
        }
        if (!(autre instanceof Promotion promotion)) {
            return false;
        }
        return id != null && id.equals(promotion.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

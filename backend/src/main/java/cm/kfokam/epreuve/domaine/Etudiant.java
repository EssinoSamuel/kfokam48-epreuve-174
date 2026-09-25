package cm.kfokam.epreuve.domaine;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * Participant à une session, et relecteur potentiel d'un pair (Q7).
 *
 * Aucun mot de passe, aucun identifiant de connexion : Q1 exclut
 * explicitement l'authentification (voir CDC section 3).
 */
@Entity
@Table(name = "etudiant")
public class Etudiant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, length = 120)
    private String nom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    protected Etudiant() {
        // requis par JPA
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    @Override
    public boolean equals(Object autre) {
        if (this == autre) {
            return true;
        }
        if (!(autre instanceof Etudiant etudiant)) {
            return false;
        }
        return id != null && id.equals(etudiant.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

package cm.kfokam.epreuve.domaine;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

/**
 * Relecture d'un exercice par un pair (Q5 à Q9, Q13, Q15).
 *
 * <ul>
 *   <li>un seul relecteur par exercice (Q6) — unicité portée par la base ;</li>
 *   <li>le relecteur n'est jamais l'auteur (RG2) — garanti par le service,
 *       car la règle croise deux tables ;</li>
 *   <li>la note est un entier de 0 à 20 (RG3) et devient définitive à
 *       l'envoi (RG5, arbitrage de la contradiction Q10/Q15) ;</li>
 *   <li>démarrer la relecture verrouille le lien (Q13, H4).</li>
 * </ul>
 */
@Entity
@Table(name = "relecture")
public class Relecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercice_id", nullable = false)
    private Exercice exercice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "relecteur_id", nullable = false)
    private Etudiant relecteur;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 16)
    private StatutRelecture statut;

    @Column(name = "demarree_at")
    private Instant demarreeAt;

    @Column(name = "note")
    private Integer note;

    @Column(name = "commentaire", length = 2000)
    private String commentaire;

    @Column(name = "rendue_at")
    private Instant rendueAt;

    protected Relecture() {
        // requis par JPA
    }

    /** Le système assigne un relecteur tiré au sort (Q7). */
    public static Relecture assigner(Exercice exercice, Etudiant relecteur) {
        Relecture relecture = new Relecture();
        relecture.exercice = exercice;
        relecture.relecteur = relecteur;
        relecture.statut = StatutRelecture.ASSIGNEE;
        return relecture;
    }

    /**
     * Démarre la relecture (Q13) : à partir de cet instant, l'auteur ne peut
     * plus remplacer le lien.
     */
    public void demarrer(Instant instant) {
        this.statut = StatutRelecture.EN_COURS;
        this.demarreeAt = instant;
    }

    /**
     * Rend la note et le commentaire (Q9). Définitif : la règle RG5 et la
     * contrainte de base refusent tout second envoi.
     */
    public void rendre(int note, String commentaire, Instant instant) {
        this.note = note;
        this.commentaire = commentaire;
        this.rendueAt = instant;
        this.statut = StatutRelecture.RENDUE;
    }

    /** Règle RG2 : on ne relit jamais son propre exercice. */
    public boolean estAutoRelecture() {
        return exercice.getEtudiant().getId() != null
                && exercice.getEtudiant().getId().equals(relecteur.getId());
    }

    public Long getId() {
        return id;
    }

    public Exercice getExercice() {
        return exercice;
    }

    public Etudiant getRelecteur() {
        return relecteur;
    }

    public StatutRelecture getStatut() {
        return statut;
    }

    public Instant getDemarreeAt() {
        return demarreeAt;
    }

    public Integer getNote() {
        return note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public Instant getRendueAt() {
        return rendueAt;
    }

    @Override
    public boolean equals(Object autre) {
        if (this == autre) {
            return true;
        }
        if (!(autre instanceof Relecture relecture)) {
            return false;
        }
        return id != null && id.equals(relecture.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

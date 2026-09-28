package cm.kfokam.epreuve.domaine;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Dépôt du lien d'un exercice par un étudiant, pour une session donnée.
 *
 * Le dépôt n'est subordonné ni à la présence (décision H5, Q12 : possible
 * « le soir même ») ni aux 15 minutes du code : seule la clôture de la
 * session le ferme (RG7).
 */
@Entity
@Table(name = "exercice")
public class Exercice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    /**
     * Relectures de l'exercice. {@code mappedBy} car la clé étrangère est
     * portée par relecture. Depuis la migration V3 (enveloppe étape 3) il y a
     * <b>deux</b> relectures par exercice : le lien passe de 1..1 à 1..N.
     */
    @OneToMany(mappedBy = "exercice", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Relecture> relectureLecture = new ArrayList<>();

    @Column(name = "lien", nullable = false, length = 2048)
    private String lien;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 32)
    private StatutExercice statut;

    @Column(name = "depose_at", nullable = false)
    private Instant deposeAt;

    protected Exercice() {
        // requis par JPA
    }

    public static Exercice deposer(Session session, Etudiant etudiant,
                                   String lien, Instant instant) {
        Exercice exercice = new Exercice();
        exercice.session = session;
        exercice.etudiant = etudiant;
        exercice.lien = lien;
        exercice.statut = StatutExercice.DEPOSE;
        exercice.deposeAt = instant;
        return exercice;
    }

    /** Un relecteur vient d'être tiré au sort (Q7). */
    public void marquerRelecteurAssigne() {
        if (statut == StatutExercice.DEPOSE) {
            this.statut = StatutExercice.EN_ATTENTE_DE_RELECTURE;
        }
    }

    /** La relecture a été rendue (Q15). */
    public void marquerRelu() {
        this.statut = StatutExercice.RELU;
    }

    /**
     * Remplacement du lien par son auteur (Q13, EF6).
     * Le service vérifie au préalable la clôture de la session (RG7) et
     * l'état de la relecture (RG8).
     */
    public void remplacerLien(String nouveauLien) {
        if (nouveauLien == null || nouveauLien.isBlank()) {
            throw new IllegalArgumentException("Le lien ne peut pas être vide.");
        }
        this.lien = nouveauLien;
    }

    public Long getId() {
        return id;
    }

    public Session getSession() {
        return session;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }

    /** Relectures de l'exercice, déjà chargées par les requêtes de lecture. */
    public List<Relecture> getRelectureLecture() {
        return relectureLecture;
    }

    /** Nombre de relectures rendues parmi celles de cet exercice. */
    public int nombreNotesRendues() {
        return (int) relectureLecture.stream()
                .filter(r -> r.getStatut() == StatutRelecture.RENDUE)
                .count();
    }

    public String getLien() {
        return lien;
    }

    public StatutExercice getStatut() {
        return statut;
    }

    public Instant getDeposeAt() {
        return deposeAt;
    }

    @Override
    public boolean equals(Object autre) {
        if (this == autre) {
            return true;
        }
        if (!(autre instanceof Exercice exercice)) {
            return false;
        }
        return id != null && id.equals(exercice.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

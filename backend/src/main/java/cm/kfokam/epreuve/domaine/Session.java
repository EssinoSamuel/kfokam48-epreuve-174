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
 * Session de cours ouverte par le formateur.
 *
 * Deux durées coexistent volontairement :
 * <ul>
 *   <li>{@code expirationAt} borne la <b>validité du code</b> (15 minutes, Q2/RG1) ;</li>
 *   <li>{@code clotureeAt} borne le <b>dépôt d'exercice</b> (Q12/RG7) — les deux
 *       événements sont distincts (décision H2).</li>
 * </ul>
 */
@Entity
@Table(name = "session")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titre", nullable = false, length = 160)
    private String titre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Column(name = "code", nullable = false, unique = true, length = 12)
    private String code;

    @Column(name = "ouverture_at", nullable = false)
    private Instant ouvertureAt;

    @Column(name = "expiration_at", nullable = false)
    private Instant expirationAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 16)
    private StatutSession statut;

    @Column(name = "cloturee_at")
    private Instant clotureeAt;

    protected Session() {
        // requis par JPA
    }

    /**
     * Ouvre une session : le code n'est valable que
     * {@code dureeValiditeMinutes} minutes.
     */
    public static Session ouvrir(String titre, Promotion promotion, String code,
                                 Instant ouverture, int dureeValiditeMinutes) {
        Session session = new Session();
        session.titre = titre;
        session.promotion = promotion;
        session.code = code;
        session.ouvertureAt = ouverture;
        session.expirationAt = ouverture.plusSeconds(dureeValiditeMinutes * 60L);
        session.statut = StatutSession.OUVERTE;
        session.clotureeAt = null;
        return session;
    }

    /** Le code est-il encore valable ? (RG1, Q2) */
    public boolean codeValableA(Instant instant) {
        return !instant.isAfter(expirationAt);
    }

    public boolean estCloturee() {
        return statut == StatutSession.CLOTUREE;
    }

    /**
     * Clôture explicite demandée par le formateur (H2, Q12).
     *
     * @throws IllegalStateException si la session est déjà clôturée
     */
    public void cloturer(Instant instant) {
        if (estCloturee()) {
            throw new IllegalStateException("La session est déjà clôturée.");
        }
        this.statut = StatutSession.CLOTUREE;
        this.clotureeAt = instant;
    }

    public Long getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public String getCode() {
        return code;
    }

    public Instant getOuvertureAt() {
        return ouvertureAt;
    }

    public Instant getExpirationAt() {
        return expirationAt;
    }

    public StatutSession getStatut() {
        return statut;
    }

    public Instant getClotureeAt() {
        return clotureeAt;
    }

    @Override
    public boolean equals(Object autre) {
        if (this == autre) {
            return true;
        }
        if (!(autre instanceof Session session)) {
            return false;
        }
        return id != null && id.equals(session.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

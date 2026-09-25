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
 * Présence d'un étudiant à une session.
 *
 * Contrainte d'unicité (session, étudiant) portée par la base : c'est elle
 * qui garantit le 409 DEJA_PRESENT imposé par le contrat.
 */
@Entity
@Table(name = "presence")
public class Presence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 16)
    private SourcePresence source;

    @Column(name = "marquee_at", nullable = false)
    private Instant marqueeAt;

    protected Presence() {
        // requis par JPA
    }

    public static Presence marquer(Session session, Etudiant etudiant,
                                   SourcePresence source, Instant instant) {
        Presence presence = new Presence();
        presence.session = session;
        presence.etudiant = etudiant;
        presence.source = source;
        presence.marqueeAt = instant;
        return presence;
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

    public SourcePresence getSource() {
        return source;
    }

    public Instant getMarqueeAt() {
        return marqueeAt;
    }

    @Override
    public boolean equals(Object autre) {
        if (this == autre) {
            return true;
        }
        if (!(autre instanceof Presence presence)) {
            return false;
        }
        return id != null && id.equals(presence.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

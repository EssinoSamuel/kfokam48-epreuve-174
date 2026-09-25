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

import java.time.Instant;
import java.util.Objects;

/**
 * Compteur d'échecs de saisie du code de présence (RG10, décision H7).
 *
 * Q4 demande de bloquer deux minutes après cinq erreurs. Le compteur est
 * <b>persisté</b> et porté par le couple (étudiant, session) : les erreurs
 * commises sur le code d'une session ne bloquent pas l'étudiant sur une autre.
 *
 * {@code session} est nullable : une tentative sur un code <b>inconnu</b> ne
 * peut être rattachée à aucune session. Deux index uniques partiels en base
 * couvrent les deux cas (voir migration V1).
 */
@Entity
@Table(name = "tentative_code")
public class TentativeCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;

    @Column(name = "echecs", nullable = false)
    private int echecs;

    @Column(name = "bloque_jusqua")
    private Instant bloqueJusqua;

    protected TentativeCode() {
        // requis par JPA
    }

    public static TentativeCode pour(Etudiant etudiant, Session session) {
        TentativeCode tentative = new TentativeCode();
        tentative.etudiant = etudiant;
        tentative.session = session;
        tentative.echecs = 0;
        tentative.bloqueJusqua = null;
        return tentative;
    }

    /** Le couple (étudiant, session) est-il bloqué à cet instant ? */
    public boolean estBloqueA(Instant instant) {
        return bloqueJusqua != null && instant.isBefore(bloqueJusqua);
    }

    /**
     * Enregistre un échec et arme le blocage si le seuil est atteint.
     *
     * @return vrai si ce nouvel échec a provoqué le blocage
     */
    public boolean enregistrerEchec(int seuil, Instant instant, int dureeBlocageMinutes) {
        this.echecs = this.echecs + 1;
        if (this.echecs >= seuil) {
            this.bloqueJusqua = instant.plusSeconds(dureeBlocageMinutes * 60L);
            return true;
        }
        return false;
    }

    /**
     * Remise à zéro après une présence réussie, ou après expiration du
     * blocage (décision H7).
     */
    public void reinitialiser() {
        this.echecs = 0;
        this.bloqueJusqua = null;
    }

    public Long getId() {
        return id;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }

    public Session getSession() {
        return session;
    }

    public int getEchecs() {
        return echecs;
    }

    public Instant getBloqueJusqua() {
        return bloqueJusqua;
    }

    @Override
    public boolean equals(Object autre) {
        if (this == autre) {
            return true;
        }
        if (!(autre instanceof TentativeCode tentative)) {
            return false;
        }
        return id != null && id.equals(tentative.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

package cm.kfokam.epreuve.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cm.kfokam.epreuve.config.ReglesMetierProperties;
import cm.kfokam.epreuve.domaine.Promotion;
import cm.kfokam.epreuve.domaine.Session;
import cm.kfokam.epreuve.repository.PromotionRepository;
import cm.kfokam.epreuve.repository.SessionRepository;
import cm.kfokam.epreuve.web.erreur.CodeErreur;
import cm.kfokam.epreuve.web.erreur.ErreurMetierException;

/**
 * Ouverture et clôture des sessions.
 *
 * <ul>
 *   <li><b>EF1</b> — ouvrir une session et produire un code de présence
 *       valable 15 minutes (Q2) ;</li>
 *   <li><b>EF4 / RG7</b> — clôturer explicitement une session (Q12),
 *       opération distincte de l'expiration du code (décision H2).</li>
 * </ul>
 */
@Service
public class SessionService {

    /** Alphabet sans caractères ambigus (pas de O/0, I/1) : le code est dicté à l'oral. */
    private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int LONGUEUR_CODE = 6;
    private static final int TENTATIVES_CODE_UNIQUE = 20;

    private final SessionRepository sessions;
    private final PromotionRepository promotions;
    private final ReglesMetierProperties regles;
    private final Clock horloge;
    private final SecureRandom aleatoire = new SecureRandom();

    public SessionService(SessionRepository sessions,
                          PromotionRepository promotions,
                          ReglesMetierProperties regles,
                          Clock horloge) {
        this.sessions = sessions;
        this.promotions = promotions;
        this.regles = regles;
        this.horloge = horloge;
    }

    /** EF1 : ouvre une session pour une promotion et génère un code unique. */
    @Transactional
    public Session ouvrir(String titre, Long promotionId) {
        Promotion promotion = promotions.findById(promotionId)
                .orElseThrow(() -> new ErreurMetierException(CodeErreur.PROMOTION_INCONNUE));

        Session session = Session.ouvrir(
                titre,
                promotion,
                genererCodeUnique(),
                horloge.instant(),
                regles.code().validiteMinutes());
        return sessions.save(session);
    }

    /** EF4 / RG7 : clôture explicite demandée par le formateur. */
    @Transactional
    public Session cloturer(Long sessionId) {
        Session session = sessions.findById(sessionId)
                .orElseThrow(() -> new ErreurMetierException(CodeErreur.SESSION_INCONNUE));
        if (session.estCloturee()) {
            throw new ErreurMetierException(CodeErreur.SESSION_DEJA_CLOTUREE);
        }
        session.cloturer(Instant.now(horloge));
        return session;
    }

    @Transactional(readOnly = true)
    public Session parId(Long sessionId) {
        return sessions.findById(sessionId)
                .orElseThrow(() -> new ErreurMetierException(CodeErreur.SESSION_INCONNUE));
    }

    @Transactional(readOnly = true)
    public List<Session> parPromotion(Long promotionId) {
        if (!promotions.existsById(promotionId)) {
            throw new ErreurMetierException(CodeErreur.PROMOTION_INCONNUE);
        }
        return sessions.findByPromotionIdOrderByOuvertureAtDesc(promotionId);
    }

    private String genererCodeUnique() {
        for (int essai = 0; essai < TENTATIVES_CODE_UNIQUE; essai++) {
            String code = code();
            if (!sessions.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException(
                "Impossible de générer un code de session unique après "
                        + TENTATIVES_CODE_UNIQUE + " tentatives.");
    }

    private String code() {
        StringBuilder sb = new StringBuilder(LONGUEUR_CODE);
        for (int i = 0; i < LONGUEUR_CODE; i++) {
            sb.append(ALPHABET[aleatoire.nextInt(ALPHABET.length)]);
        }
        return sb.toString();
    }
}

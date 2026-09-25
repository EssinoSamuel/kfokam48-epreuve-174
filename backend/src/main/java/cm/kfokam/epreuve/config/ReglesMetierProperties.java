package cm.kfokam.epreuve.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Paramètres métier issus de {@code application.yml}.
 *
 * Les valeurs de Q2 (15 minutes) et Q4 (5 échecs, 2 minutes) sont
 * paramétrables plutôt que codées en dur : les tests peuvent les
 * raccourcir sans attendre réellement deux minutes (cf. RG1, RG10).
 *
 * @param code       validité du code de présence
 * @param tentatives limitation du tâtonnement sur le code
 */
@ConfigurationProperties(prefix = "kfokam48")
public record ReglesMetierProperties(
        Code code,
        Tentatives tentatives
) {

    public record Code(int validiteMinutes) {
    }

    public record Tentatives(int max, int blocageMinutes) {
    }
}

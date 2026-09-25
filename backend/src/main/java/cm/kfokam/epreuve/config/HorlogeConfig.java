package cm.kfokam.epreuve.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Horloge injectable.
 *
 * Les règles de gestion de ce projet sont toutes temporelles : validité de
 * 15 minutes du code (RG1), blocage de 2 minutes après cinq échecs (RG10).
 * En injectant un {@link Clock}, les tests peuvent se placer à un instant
 * précis au lieu d'attendre réellement — ce qui rend les tests RG1 et RG10
 * exécutables sur un poste vierge en quelques millisecondes (contrainte B6).
 */
@Configuration
public class HorlogeConfig {

    @Bean
    public Clock horloge() {
        return Clock.systemDefaultZone();
    }
}

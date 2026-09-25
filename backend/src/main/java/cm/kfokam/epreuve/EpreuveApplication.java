package cm.kfokam.epreuve;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Point d'entrée du backend KFOKAM48.
 *
 * Architecture en couches strictes (contrainte B3 du sujet) :
 * Controller → Service → Repository, aucune entité exposée en JSON,
 * erreurs centralisées par un {@code @RestControllerAdvice}.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class EpreuveApplication {

    public static void main(String[] args) {
        SpringApplication.run(EpreuveApplication.class, args);
    }
}

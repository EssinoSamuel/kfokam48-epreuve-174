package cm.kfokam.epreuve.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test d'intégration (contrainte B6) et preuve du bug de l'enveloppe — étape 3.
 *
 * <b>Bug :</b> l'écran formateur appelle l'ajout manuel de présence sur
 * {@code POST /api/sessions/{id}/presences}, chemin imposé par le contrat
 * (décision H3). Le backend n'exposait que {@code POST /api/presences/session/{id}} :
 * l'appel renvoyait 404 « La ressource demandée n'existe pas » et le formateur
 * ne pouvaitmarké aucun étudiant manuellement (Q14).
 *
 * <b>Ce test échoue avant le correctif</b> (404 au lieu de 201) et passe après :
 * c'est exactement la preuve attendue par le jury — que le bug existait et qu'il
 * a été corrigé.
 *
 * Profil H2 : aucun Docker, aucune base locale requise (contrainte B6).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PresenceFormateurIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String json(Object valeur) throws Exception {
        return objectMapper.writeValueAsString(valeur);
    }

    @Test
    @DisplayName("BUG (issue #16) : l'ajout manuel doit répondre 201 sur le chemin du contrat")
    void ajoutManuelSurCheminDuContrat() throws Exception {
        mockMvc.perform(post("/api/sessions/1/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(java.util.Map.of("etudiantId", 12))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.source").value("FORMATEUR"))
                .andExpect(jsonPath("$.sessionId").value(1));
    }

    @Test
    @DisplayName("BUG (issue #16) : le doublon reste refusé en 409 DEJA_PRESENT")
    void doublonResteRefuse() throws Exception {
        // On enregistre d'abord, puis on réessaie : le second appel doit être
        // refusé, et non créer une seconde ligne.
        mockMvc.perform(post("/api/sessions/1/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(java.util.Map.of("etudiantId", 11))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/sessions/1/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(java.util.Map.of("etudiantId", 11))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DEJA_PRESENT"));
    }

    @Test
    @DisplayName("BUG (issue #16) : un étudiant inconnu renvoie 404 ETUDIANT_INCONNU")
    void etudiantInconnu() throws Exception {
        mockMvc.perform(post("/api/sessions/1/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(java.util.Map.of("etudiantId", 999999))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ETUDIANT_INCONNU"));
    }
}

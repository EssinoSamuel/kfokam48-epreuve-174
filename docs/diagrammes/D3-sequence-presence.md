# D3 — Séquence : marquer sa présence

**Format :** Mermaid (`sequenceDiagram`). Correspondance exigée avec les codes HTTP du
contrat imposé : `201` (nominal) · `400 CODE_INCONNU` · `400 TROP_DE_TENTATIVES` (RG10) ·
`409 DEJA_PRESENT` · `410 CODE_EXPIRE`.

```mermaid
sequenceDiagram
    autonumber
    actor E as Étudiant
    participant F as Frontend (écran étudiant)
    participant C as PresenceController
    participant S as PresenceService
    participant R as PresenceRepository
    participant DB as Base de données

    E->>F: saisit le code de présence
    F->>C: POST /api/presences { code, etudiantId }
    C->>S: marquerPresence(code, etudiantId)

    S->>R: charger les compteurs d'échecs (étudiant, puis session)
    R->>DB: SELECT tentative_code …
    DB-->>R: compteurs
    alt compteur bloqué (RG10, Q4)
        S-->>C: TropDeTentativesException
        C-->>F: 400 { code: "TROP_DE_TENTATIVES" }
    else tentative autorisée
        S->>R: trouver la session par code
        R->>DB: SELECT session WHERE code = ?
        DB-->>R: session ou rien
        alt code inconnu
            S->>R: +1 échec (étudiant, session inconnue)
            S-->>C: CodeInconnuException
            C-->>F: 400 { code: "CODE_INCONNU" }
        else code expiré (Q2, RG1)
            S->>R: +1 échec (étudiant, session)
            S-->>C: CodeExpireException
            C-->>F: 410 { code: "CODE_EXPIRE" }
        else étudiant déjà présent
            S-->>C: DejaPresentException
            C-->>F: 409 { code: "DEJA_PRESENT" }
        else cas nominal
            S->>R: enregistrer présence (source = ETUDIANT)
            R->>DB: INSERT presence
            S->>R: remettre les échecs à zéro
            S-->>C: Presence
            C-->>F: 201 { id, sessionId, etudiantId, source }
        end
    end
```

## Lecture du diagramme

- Le **blocage RG10** est vérifié avant tout enregistrement : d'abord le compteur de
  l'étudiant (échecs sur codes inconnus), puis le compteur du couple (étudiant, session)
  quand la session est identifiable. Au 5e échec, tout nouvel essai reçoit
  `400 TROP_DE_TENTATIVES` pendant deux minutes.
- `410 CODE_EXPIRE` traite Q2/RG1. La clôture de session **ne conditionne pas** la
  présence : décision H10 écartée, Q3 est couvert par RG1 (voir CDC section 7).
- L'ajout manuel par le formateur ne passe pas par ce flux : il utilise
  `POST /api/sessions/{id}/presences` (H3, Q14) et produit `source = FORMATEUR`.
- Les codes de statut et le format d'erreur sont ceux du contrat imposé ; seule la
  valeur métier `TROP_DE_TENTATIVES` est ajoutée dans le corps, comme prévu par RG10.

# D4 — États-transitions (bonus) : exercice et relecture

**Format :** Mermaid (`stateDiagram-v2`). Diagramme bonus (+3 points) : il complète D2
et documente Q13 (verrouillage du lien) et Q15 (note définitive).

## Cycle de vie d'un exercice (déposé → en attente de relecture → relu)

```mermaid
stateDiagram-v2
    [*] --> DEPOSE : dépôt du lien (EF5, Q12)
    DEPOSE --> EN_ATTENTE_DE_RELECTURE : un relecteur est assigné au hasard (EF7, Q7)
    DEPOSE --> DEPOSE : aucun étudiant éligible — reste visible « en attente » (H6, Q11)
    EN_ATTENTE_DE_RELECTURE --> EN_ATTENTE_DE_RELECTURE : lien remplacé (Should, Q13)
    EN_ATTENTE_DE_RELECTURE --> RELU : le relecteur envoie sa note (EF8, Q15)
    RELU --> [*]
```

Notes :

- le remplacement du lien (Q13) ne change pas le statut mais reste borné : session non
  clôturée (RG7) **et** relecture ni commencée ni rendue (RG8) ;
- sans candidat relecteur (H6), l'exercice demeure `DEPOSE` — c'est le cas que le
  formateur doit voir « en attente » dans son suivi (Q11).

## Cycle de vie d'une relecture (supporte Q13)

```mermaid
stateDiagram-v2
    [*] --> ASSIGNEE : exercice déposé, relecteur tiré au hasard (Q7)
    ASSIGNEE --> EN_COURS : le relecteur démarre — le lien est verrouillé (H4, Q13)
    ASSIGNEE --> RENDUE : note envoyée directement (EF8)
    EN_COURS --> RENDUE : note et commentaire envoyés — définitif (Q15, RG5)
    RENDUE --> [*]
```

Notes :

- un envoi après `RENDUE` est refusé : `409 RELECTURE_DEJA_RENDUE` (contrat imposé, Q15) ;
- un démarrage après `EN_COURS` est refusé : `409 RELECTURE_DEJA_COMMENCEE` (H4) ;
- `ASSIGNEE → RENDUE` est permis : un relecteur peut rendre directement sans démarrer.

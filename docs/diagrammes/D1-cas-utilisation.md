# D1 — Diagramme de cas d'utilisation

**Format :** PlantUML — le sujet autorise « Mermaid ou PlantUML, en texte ». Mermaid n'a
pas de notation « cas d'utilisation » ; PlantUML en propose une formelle (acteurs,
ellipses, `<<include>>`, `<<extend>>`, généralisation). Le source reste du texte pur,
versionné et diffusable en `diff`.

## Comment le lire

- **Acteurs** — le **Formateur** anime la session ; l'**Étudiant** y participe ; le
  **Relecteur** n'est pas un compte distinct : c'est un étudiant dans un rôle temporaire,
  pour un exercice donné (flèche de généralisation `Relecteur → Étudiant`, Q7).
- **Cas d'utilisation** — chaque ellipse est un objectif utilisateur vérifiable, relié aux
  exigences `EFx` du cahier des charges (traçabilité en bas de page).
- `<<include>>` — comportement systématiquement déclenché par le cas de base
  (déposer un exercice déclenche l'assignation d'un relecteur, EF7).
- `<<extend>>` — comportement conditionnel (remplacer le lien étend le dépôt, seulement
  tant que la relecture n'a pas commencé et que la session n'est pas clôturée, EF6/Q13).
- Le **Système** est un acteur secondaire : il agit sans intervention humaine quand le
  client le dit explicitement (Q7 : « Le système, au hasard »). Les règles purement
  automatiques (RG1, RG10) sont en notes, pas en cas d'utilisation : ce ne sont pas des
  objectifs utilisateur.

```plantuml
@startuml D1-cas-utilisation
left to right direction
skinparam actorStyle awesome

actor "Formateur" as Formateur
actor "Étudiant" as Etudiant
actor "Relecteur" as Relecteur
actor "Système" as Systeme

' Le relecteur est un étudiant dans un rôle temporaire (CDC section 2, Q7)
Etudiant <|-- Relecteur

rectangle "Suivi de présence, exercices et relectures" {

  usecase "Ouvrir une session et obtenir un code (EF1 · Q2)" as UC1
  usecase "Ajouter une présence manuellement (EF3 · Q14)" as UC3
  usecase "Clôturer une session (EF4 · Q12)" as UC4
  usecase "Consulter le tableau de sa promotion (EF10 · Q16)" as UC10

  usecase "Marquer sa présence avec un code (EF2 · Q2)" as UC2
  usecase "Déposer le lien de son exercice (EF5 · Q12)" as UC5
  usecase "Remplacer le lien de son exercice (EF6 · Q13 — Should)" as UC6
  usecase "Consulter sa note et son commentaire (EF9 · Q8)" as UC9

  usecase "Consulter l'exercice assigné" as UC7b
  usecase "Rendre une note et un commentaire (EF8 · Q9 · Q15)" as UC7

  usecase "Assigner un relecteur au hasard (EF7 · Q6 · Q7)" as UC8
}

Formateur --> UC1
Formateur --> UC3
Formateur --> UC4
Formateur --> UC10

Etudiant --> UC2
Etudiant --> UC5
Etudiant --> UC9

Relecteur --> UC7b
Relecteur --> UC7

UC6 .> UC5 : <<extend>>
UC5 ..> UC8 : <<include>>
Systeme --> UC8

note right of UC2
  RG1 · Q2 : le code expire 15 min après l'ouverture.
  RG10 · Q4 : après 5 codes erronés, le couple
  (étudiant, session) est bloqué 2 min.
  Q3 : aucune présence après la fin de session.
end note

note bottom of UC5
  RG7 · Q12 : dépôt accepté jusqu'à la clôture de la session.
  RG6 · Q11 : sans relecture rendue, l'exercice reste « en attente ».
end note

note bottom of UC7
  RG5 · Q15 : une relecture rendue est définitive
  (Q10 écartée — contradiction tranchée, CDC section 7).
end note

note right of UC8
  Q5 / RG2 : jamais l'auteur de l'exercice.
  Q6 / RG4 : un seul relecteur, choisi parmi
  les étudiants présents à la session.
end note

@enduml
```

## Traçabilité cas d'utilisation → exigences

| Cas d'utilisation | Exigence | Sources client |
|---|---|---|
| Ouvrir une session et obtenir un code | EF1 | Q2 |
| Marquer sa présence avec un code | EF2 | Q2, Q3, Q4 |
| Ajouter une présence manuellement | EF3 | Q14 |
| Clôturer une session | EF4 | Q12 |
| Déposer le lien de son exercice | EF5 | Q12 |
| Remplacer le lien de son exercice (Should) | EF6 | Q13 |
| Assigner un relecteur au hasard | EF7 | Q6, Q7 |
| Rendre une note et un commentaire | EF8 | Q9, Q15 |
| Consulter sa note et son commentaire | EF9 | Q8 |
| Consulter le tableau de sa promotion | EF10 | Q11, Q16 |

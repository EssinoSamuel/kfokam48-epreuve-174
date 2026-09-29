# Backlog — état au jalon `v1.0`

Le backlog vit sur GitHub (`https://github.com/EssinoSamuel/kfokam48-epreuve-174/issues`).
Ce fichier en donne l'**état trié** au moment de la livraison : ce qui est livré, ce qui
est sacrifié, ce qui reste. C'est la re-priorisation demandée à l'étape 4.

## Livré — 16 tickets

| # | Ticket | Priorité | État |
|---|---|---|---|
| 1 | Le formateur ouvre une session pour sa promotion | Must | Livré |
| 2 | Le formateur peut consulter les sessions de sa promotion | Should | Livré |
| 3 | Un étudiant marque sa présence avec le code de la session | Must | Livré |
| 4 | Le formateur ajoute une présence manuellement | Must | Livré |
| 5 | Le formateur clôture une session | Must | Livré |
| 6 | Un étudiant dépose le lien de son exercice | Must | Livré |
| 8 | Un relecteur démarre sa relecture | Must | Livré |
| 9 | Un relecteur rend sa note et son commentaire | Must | Livré |
| 10 | Le formateur consulte le tableau récapitulatif de sa promotion | Must | Livré |
| 11 | Le formateur consulte le détail des exercices d'une session | Should | Livré |
| 12 | Un utilisateur consulte la liste des promotions | Could | Livré |
| 13 | Un étudiant choisit son nom dans la liste de sa promotion | Must | Livré |
| 14 | Un étudiant consulte ses notes et commentaires reçus | Must | Livré |
| 15 | Un relecteur retrouve les relectures qui lui sont assignées | Must | Livré |
| 16 | **Bug** — le formateur ne peut ajouter aucune présence à la main (404) | **Must (corrigé)** | Livré, prouvé par test |
| 17 | **Évolution** — deux relecteurs par exercice, moyenne des deux | **Must (arrivé tard)** | Livré |

## Sacrifié — 1 ticket

| # | Ticket | Priorité | Décision |
|---|---|---|---|
| 7 | Un étudiant remplace le lien de son exercice déposé (EF6) | Should | **Sorti du périmètre** |

**Pourquoi :** exigence *Should*, hors du cœur noté. Le remplacement n'est légitime que
tant que personne n'a commencé à relire ; l'exiger aurait obligé à figer le premier
relecteur, ce qui contredit frontalement la nouvelle règle « chaque exercice est relu par
deux pairs différents » (RG4/RG12, issue 17). Les deux exigences ne peuvent pas tenir
ensemble : j'ai sacrifié celle qui n'était pas obligatoire.

**Conséquences assumées (détail en `docs/CAHIER_DES_CHARGES.md` §7, C1–C4) :** l'auteur
ne peut plus corriger un lien erroné après dépôt — il doit redéposer, ce que la règle
`409 EXERCICE_DEJA_DEPOSE` interdit. C'est une régression connue et acceptée.

## Reporté — 0 ticket

Aucun ticket reporté. Le reste du backlog est soldé.

---

## Ce que je ferais ensuite, dans cet ordre

1. **EF6** (ticket 7) — le rendre compatible avec deux relecteurs : autoriser le
   remplacement tant qu'**aucune** des deux relectures n'a démarré, au lieu de bloquer
   dès la première.
2. **PR liées aux issues** — le travail de l'étape 3 a été fusionné en local : les deux
   branches (`fix/bug-presence-formateur`, `feat/relecture-par-deux-pairs`) sont poussées
   et la séparation est visible dans l'historique, mais aucune pull request ne les
   rattache aux tickets. À faire en amont la prochaine fois, pas après coup.
3. **Clore les tickets livrés** un par un, en commentaire du commit correspondant.

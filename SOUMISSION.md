# Soumission — Épreuve finale fullstack KFOKAM48

> Vérifie les deux liens depuis une fenêtre de navigation privée, puis téléverse ce
> fichier sur la plateforme **avant 18h00**. Sans ce dépôt sur la plateforme, rien
> n'est rendu.

---

## Candidat

| | |
|---|---|
| Nom et prénom(s) | ESSINO Samuel |
| Matricule | 174 |
| Centre | Yaoundé |
| Compte GitHub | EssinoSamuel |

## Projet

| | |
|---|---|
| Dépôt (public) | https://github.com/EssinoSamuel/kfokam48-epreuve-174 |
| Commit final — hash complet, 40 caractères | `268c12685c15e1e6548f92d134eb17b65361893e` — version finale **v1.0** (`[JALON] v1.0` puis CHANGELOG), relevé le 28/09/2026 |
| Branche | `main` |

## Épreuve Git — étape 5

Aucun second dépôt : l'**étape 5 (épreuve `git-lab`) a été annulée** par l'encadrement.
Aucun dépôt `kfokam48-gitlab-174` n'est requis, et aucun n'est rendu.

## Technique

| | |
|---|---|
| Frontend utilisé | React + Vite |
| Backend | Java 21 LTS · Spring Boot · Maven (wrapper `mvnw` commité) |
| Base de données | PostgreSQL 16 — `docker compose` (port hôte **5434**, 5432 occupé) + migrations Flyway |
| Commandes de démarrage | `docker compose up -d` puis `cd backend && mvnw spring-boot:run` puis `cd frontend && npm install && npm run dev` — détail dans le `README.md` |

## Ce que j'ai livré

L'application couvre le parcours complet : le formateur ouvre une session et diffuse un
code, ajoute une présence à la main et clôture ; l'étudiant marque sa présence et dépose
le lien de son exercice ; **chaque exercice est relu par deux pairs distincts** et l'API
rend la moyenne des deux notes, marquée `PROVISOIRE` tant qu'un seul relecteur a rendu, la
vue étudiant n'exposant jamais l'identité des relecteurs (RG11). Le formateur suit le tout
dans un tableau récapitulatif. Les 5 opérations imposées du contrat sont respectées à la
lettre (chemins, verbes, codes de statut, erreur `{code, message}`), et le schéma est
versionné par Flyway (`V1` à `V3`), testé pour de vrai sur PostgreSQL.

Ce qui ne fonctionne pas ou reste incomplet : le **backlog restant n'a pas été trié**
(re-priorisation des issues *Should* / *Could* non faite faute de temps) ; l'**étape 5
(épreuve Git) a été annulée** par l'encadrement, aucun dépôt `git-lab` n'est donc rendu.

Ce qui a été volontairement laissé de côté : **EF6, le remplacement du lien d'exercice**
par son auteur. C'était une exigence *Should*, et la garder aurait obligé à figer le
premier relecteur — ce qui contredit la nouvelle règle « deux pairs par exercice ». Le
sacrifice est écrit et justifié dans `docs/CAHIER_DES_CHARGES.md` §7, avec les quatre
conséquences du changement de besoin (C1–C4).

Ce qui a été corrigé : le bug signalé par le client (**issue #16**) — le formateur ne
pouvait ajouter aucune présence à la main, le chemin `POST /api/sessions/{id}/presences`
imposé par le contrat répondant 404. Reproduit par un test qui échouait avant d'être
corrigé, dans une branche dédiée, puis repassé au vert.

---

## Avant de téléverser, vérifie

- [ ] Mes deux dépôts sont **publics** et s'ouvrent en navigation privée
- [ ] Les deux hash font bien **40 caractères** et existent sur GitHub
- [ ] Tout mon travail est **poussé** — `git status` est propre sur les deux dépôts
- [ ] Mon `README` a été testé depuis un clone vierge, dans un dossier vide
- [ ] Mon `JOURNAL.md` et mon cahier des charges sont dans `docs/`
- [ ] Les trois commits `[JALON]` sont poussés et dans le bon ordre

---

**Déclaration.** J'ai réalisé ce travail seul. Les outils d'IA étaient autorisés sans
restriction et je les ai utilisés ; mon journal indique où et comment j'ai vérifié leurs
réponses. Mes dépôts resteront publics et inchangés jusqu'à la publication des résultats.

Signature : ESSINO Samuel · Date : ______________________

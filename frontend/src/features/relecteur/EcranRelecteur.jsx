import { Carte } from '../../components/ui.jsx'
import { SelecteurPromotion, SelecteurEtudiant } from '../../components/selecteurs.jsx'

/** Ecran relecteur — Phase 5. Le relecteur est un etudiant (Q7). */
export default function EcranRelecteur() {
  return (
    <>
      <header className="page-entete">
        <div>
          <h1 className="page-entete__titre">Espace relecteur</h1>
          <p className="page-entete__description">
            Consulter les exercices assignés, démarrer la relecture et envoyer une note.
          </p>
        </div>
      </header>
      <div className="grille grille--2">
        <Carte titre="Mon identité" description="Le relecteur est choisi parmi les étudiants présents (Q7).">
          <SelecteurPromotion />
          <SelecteurEtudiant />
        </Carte>
        <Carte titre="À venir — Phase 5">
          Liste des relectures assignées, démarrage (verrouillage du lien), notation de 0 à 20
          et commentaire seront construits dans cette phase.
        </Carte>
      </div>
    </>
  )
}


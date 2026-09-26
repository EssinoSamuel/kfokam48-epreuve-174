import { Carte } from '../../components/ui.jsx'
import { SelecteurPromotion } from '../formateur/EcranFormateur.jsx'

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
        <Carte titre="Promotion">
          <SelecteurPromotion />
        </Carte>
        <Carte titre="À venir — Phase 6">
          Liste des relectures assignées, démarrage (verrouillage du lien), notation de 0 à 20
          et commentaire seront construits à cette phase.
        </Carte>
      </div>
    </>
  )
}

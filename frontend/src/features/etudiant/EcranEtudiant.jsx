import { Carte } from '../../components/ui.jsx'
import { SelecteurPromotion } from '../formateur/EcranFormateur.jsx'

export default function EcranEtudiant() {
  return (
    <>
      <header className="page-entete">
        <div>
          <h1 className="page-entete__titre">Espace étudiant</h1>
          <p className="page-entete__description">
            Choisir son nom, marquer sa présence, déposer un exercice et consulter sa note.
          </p>
        </div>
      </header>
      <div className="grille grille--2">
        <Carte titre="Promotion">
          <SelecteurPromotion />
        </Carte>
        <Carte titre="À venir — Phase 5">
          Sélection du nom, saisie du code de présence, dépôt et remplacement du lien,
          consultation de la note et du commentaire seront construits à cette phase.
        </Carte>
      </div>
    </>
  )
}

import { Carte, Alerte } from '../../components/ui.jsx'
import { SelecteurPromotion, SelecteurEtudiant } from '../../components/selecteurs.jsx'

/**
 * Ecran etudiant — Phase 4.
 * Identite sans authentification (Q1) : promotion puis nom dans la liste.
 */
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
        <Carte titre="Mon identité" description="Aucun mot de passe n’est demandé (Q1).">
          <SelecteurPromotion />
          <SelecteurEtudiant />
        </Carte>
        <Carte titre="À venir — Phase 4">
          Saisie du code de présence, dépôt et remplacement du lien, consultation de la note
          et du commentaire seront construits dans cette phase.
        </Carte>
      </div>
    </>
  )
}


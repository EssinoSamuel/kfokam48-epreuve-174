import { Carte } from '../../components/ui.jsx'
import { SelecteurPromotion, SelecteurEtudiant } from '../../components/selecteurs.jsx'
import { BlocPresence } from './blocPresence.jsx'
import { BlocDepot, BlocMesExercices } from './blocExercices.jsx'

/**
 * Ecran etudiant : identite → presence → exercice → consultation (Q1, Q12, Q13, Q8).
 * Parcours volontairement simple pour rester utilisable sur mobile (F2).
 */
export default function EcranEtudiant() {
  return (
    <>
      <header className="page-entete">
        <div>
          <h1 className="page-entete__titre">Espace étudiant</h1>
          <p className="page-entete__description">
            Choisissez votre nom, marquez votre présence, déposez votre exercice et consultez
            votre note.
          </p>
        </div>
      </header>

      <div className="grille grille--2" style={{ marginBottom: '1rem' }}>
        <Carte
          titre="Qui êtes-vous ?"
          description="Aucun mot de passe : on simule l’identité, ce n’est pas une authentification (Q1)."
        >
          <SelecteurPromotion />
          <SelecteurEtudiant />
        </Carte>
        <BlocPresence />
      </div>

      <div className="grille grille--2">
        <BlocDepot />
        <BlocMesExercices />
      </div>
    </>
  )
}

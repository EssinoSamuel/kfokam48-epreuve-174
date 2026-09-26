import { Carte } from '../../components/ui.jsx'
import { SelecteurPromotion, SelecteurEtudiant } from '../../components/selecteurs.jsx'
import { CartePresence } from './CartePresence.jsx'
import { CarteDepotExercice, CarteRemplacerLien } from './CarteExercice.jsx'
import CarteMesExercices from './CarteMesExercices.jsx'
import { useApp } from '../../context/AppContext.jsx'
import { useRequete } from '../../hooks/useRequete.js'
import { exercicesApi } from '../../api/index.js'

/**
 * Ecran etudiant — parcours complet (Phase 4).
 * Identite → presence → depot → consultation (EF2, EF5, EF6, EF9).
 */
export default function EcranEtudiant() {
  const { etudiantId } = useApp()
  const { donnees: exercices } = useRequete(
    etudiantId ? () => exercicesApi.parEtudiant(etudiantId) : null,
    [etudiantId],
    [],
  )

  return (
    <>
      <header className="page-entete">
        <div>
          <h1 className="page-entete__titre">Espace étudiant</h1>
          <p className="page-entete__description">
            Choisissez votre nom, marquez votre présence et déposez votre exercice.
          </p>
        </div>
      </header>

      <div className="grille grille--2" style={{ marginBottom: 'var(--espace-5)' }}>
        <Carte titre="Mon identité" description="Aucun mot de passe n’est demandé (Q1).">
          <SelecteurPromotion />
          <SelecteurEtudiant />
        </Carte>
        <CartePresence />
      </div>

      <div className="grille grille--2" style={{ marginBottom: 'var(--espace-5)' }}>
        <CarteDepotExercice />
        <CarteRemplacerLien exercices={exercices ?? []} />
      </div>

      <CarteMesExercices />
    </>
  )
}



import { Carte } from '../../components/ui.jsx'
import { SelecteurPromotion } from '../../components/selecteurs.jsx'
import { BlocSession, BlocPresenceManuelle } from './blocs.jsx'
import { BlocTableau } from './BlocTableau.jsx'

/**
 * Ecran formateur (F2) : promotion → ouverture de session → code →
 * presences → tableau → cloture (EF1, EF3, EF4, EF10).
 */
export default function EcranFormateur() {
  return (
    <>
      <header className="page-entete">
        <div>
          <h1 className="page-entete__titre">Espace formateur</h1>
          <p className="page-entete__description">
            Ouvrez une session, suivez les présences et consultez le tableau de la promotion.
          </p>
        </div>
      </header>

      <div className="grille grille--2" style={{ marginBottom: '1rem' }}>
        <Carte titre="Promotion de travail" description="Toutes les sessions et étudiants affichés dépendent de ce choix.">
          <SelecteurPromotion />
        </Carte>
        <BlocSession />
      </div>

      <div className="grille grille--2" style={{ marginBottom: '1rem' }}>
        <BlocPresenceManuelle />
      </div>

      <BlocTableau />
    </>
  )
}

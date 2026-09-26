import { Carte, Bouton, Chargement, EtatVide } from '../../components/ui.jsx'
import { SelecteurPromotion, SelecteurEtudiant } from '../../components/selecteurs.jsx'
import { useApp } from '../../context/AppContext.jsx'
import { useRequete } from '../../hooks/useRequete.js'
import { relecturesApi } from '../../api/index.js'
import { CarteRelecture } from './CarteRelecture.jsx'

/** Ecran relecteur : identite → liste → demarrage → notation (F2). */
export default function EcranRelecteur() {
  const { etudiantId } = useApp()
  const { donnees, chargement, recharger } = useRequete(
    () => (etudiantId ? relecturesApi.pourEtudiant(etudiantId) : Promise.resolve([])),
    [etudiantId],
  )

  const liste = donnees ?? []

  return (
    <>
      <header className="page-entete">
        <div>
          <h1 className="page-entete__titre">Espace relecteur</h1>
          <p className="page-entete__description">
            Consultez les exercices qui vous sont assignés, puis envoyez votre note. Une relecture
            rendue est définitive.
          </p>
        </div>
      </header>

      <div className="grille grille--2" style={{ marginBottom: '1rem' }}>
        <Carte
          titre="Mon identité de relecteur"
          description="Le système désigne les relecteurs parmi les étudiants présents (Q7)."
        >
          <SelecteurPromotion />
          <SelecteurEtudiant />
        </Carte>
        <Carte titre="Comment ça marche ?">
          <ol style={{ paddingLeft: '1.25rem', margin: 0, color: '#64748b' }}>
            <li>Ouvrez l’exercice pour lire le travail du pair.</li>
            <li>
              Cliquez sur « Démarrer la relecture » : son lien se verrouille et l’auteur ne peut
              plus le remplacer (Q13).
            </li>
            <li>Notez de 0 à 20 et laissez un commentaire.</li>
            <li>Envoyez : la note devient définitive (Q15).</li>
          </ol>
        </Carte>
      </div>

      <Carte
        titre="Mes relectures"
        actions={
          <Bouton variante="secondaire" petit onClick={recharger}>
            Actualiser
          </Bouton>
        }
      >
        {!etudiantId ? (
          <EtatVide titre="Choisissez votre nom">
            Vos relectures assignées s’afficheront ici.
          </EtatVide>
        ) : chargement ? (
          <Chargement message="Chargement de vos relectures…" />
        ) : liste.length === 0 ? (
          <EtatVide titre="Aucune relecture assignée">
            Le système vous désignera lorsqu’un pair déposera un exercice.
          </EtatVide>
        ) : (
          <ul className="pile" style={{ listStyle: 'none', margin: 0, padding: 0 }}>
            {liste.map((relecture) => (
              <CarteRelecture key={relecture.id} relecture={relecture} />
            ))}
          </ul>
        )}
      </Carte>
    </>
  )
}

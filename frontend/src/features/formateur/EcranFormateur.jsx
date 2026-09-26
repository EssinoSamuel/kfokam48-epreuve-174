import { useEffect, useState } from 'react'
import { Carte, Chargement, Alerte } from '../../components/ui.jsx'
import { Champ } from '../../components/formulaires.jsx'
import { useApp } from '../../context/AppContext.jsx'
import { promotionsApi } from '../../api/index.js'

/** Selecteur de promotion partage par les trois roles (donnees V2). */
export function SelecteurPromotion() {
  const { promotionId, setPromotionId, setEtudiantId, setEtudiantNom } = useApp()
  const [promotions, setPromotions] = useState([])
  const [chargement, setChargement] = useState(true)
  const [erreur, setErreur] = useState(null)

  useEffect(() => {
    let actif = true
    promotionsApi
      .lister()
      .then((donnees) => {
        if (!actif) return
        setPromotions(donnees)
        if (donnees.length > 0 && promotionId === null) {
          setPromotionId(donnees[0].id)
        }
      })
      .catch((e) => actif && setErreur(e.message))
      .finally(() => actif && setChargement(false))
    return () => {
      actif = false
    }
  }, [promotionId, setPromotionId])

  if (chargement) return <Chargement message="Chargement des promotions…" />
  if (erreur) return <Alerte variante="danger">{erreur}</Alerte>

  return (
    <Champ
      label="Promotion"
      id="selecteur-promotion"
      aide="Toutes les sessions et étudiants de cette promotion seront affichés."
    >
      <select
        id="selecteur-promotion"
        className="selection"
        value={promotionId ?? ''}
        onChange={(event) => {
          setPromotionId(Number(event.target.value))
          setEtudiantId(null)
          setEtudiantNom('')
        }}
      >
        {promotions.map((promotion) => (
          <option key={promotion.id} value={promotion.id}>
            {promotion.nom}
          </option>
        ))}
      </select>
    </Champ>
  )
}

export default function EcranFormateur() {
  return (
    <>
      <header className="page-entete">
        <div>
          <h1 className="page-entete__titre">Espace formateur</h1>
          <p className="page-entete__description">
            Ouvrir une session, suivre les présences et consulter le tableau de la promotion.
          </p>
        </div>
      </header>
      <div className="grille grille--2">
        <Carte titre="Promotion de travail">
          <SelecteurPromotion />
        </Carte>
        <Carte titre="À venir — Phase 4">
          Ouverture de session, code de présence, ajout manuel de présence, clôture et
          tableau récapitulatif seront construits à cette phase.
        </Carte>
      </div>
    </>
  )
}

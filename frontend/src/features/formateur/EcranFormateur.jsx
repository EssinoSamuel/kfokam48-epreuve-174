import { useEffect, useState } from 'react'
import { Carte, Chargement, Alerte } from '../../components/ui.jsx'
import { Champ } from '../../components/formulaires.jsx'
import { SelecteurPromotion } from '../../components/selecteurs.jsx'
import { useApp } from '../../context/AppContext.jsx'

/**
 * Ecran formateur — Phase 4 en cours de construction.
 * La promotion et la session sont deja reellement interrogees sur l'API :
 * la preuve que la couche F3 fonctionne.
 */
export default function EcranFormateur() {
  const { promotionId } = useApp()
  const [etudiants, setEtudiants] = useState([])
  const [chargement, setChargement] = useState(true)
  const [erreur, setErreur] = useState(null)

  useEffect(() => {
    if (!promotionId) return
    let actif = true
    setChargement(true)
    import('../../api/index.js').then(({ promotionsApi }) => {
      return promotionsApi.etudiants(promotionId)
    })
      .then((donnees) => actif && setEtudiants(donnees))
      .catch((e) => actif && setErreur(e.message))
      .finally(() => actif && setChargement(false))
    return () => {
      actif = false
    }
  }, [promotionId])

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
        <Carte titre="Promotion de travail" description="Données de démonstration V2">
          <SelecteurPromotion />
          {chargement ? <Chargement message="Chargement des étudiants…" /> : null}
          {erreur ? <Alerte variante="danger">{erreur}</Alerte> : null}
          {!chargement && !erreur ? (
            <p className="carte__description">
              {etudiants.length} étudiant{etudiants.length > 1 ? 's' : ''} inscrit
              {etudiants.length > 1 ? 's' : ''} dans cette promotion.
            </p>
          ) : null}
        </Carte>
        <Carte titre="À venir — Phase 4">
          Ouverture de session, code de présence, ajout manuel de présence, clôture et
          tableau récapitulatif seront construits dans cette phase.
        </Carte>
      </div>
    </>
  )
}


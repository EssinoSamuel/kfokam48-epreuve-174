import { useEffect, useState } from 'react'
import { Champ } from './formulaires.jsx'
import { Chargement, Alerte } from './ui.jsx'
import { useApp } from '../context/AppContext.jsx'
import { promotionsApi, sessionsApi } from '../api/index.js'

/**
 * Selecteurs partages par les trois roles.
 *
 * Q1 exclut l'authentification : l'etudiant se choisit dans une liste
 * renvoyee par l'API. Ces composants interrogent reellement le backend —
 * c'est la demonstration que la couche F3 (client + services) fonctionne.
 */

/** Liste des promotions ; alimente le contexte global. */
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
    <Champ label="Promotion" id="selecteur-promotion">
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

/** Liste des etudiants de la promotion courante (Q1). */
export function SelecteurEtudiant({ requis = true }) {
  const { promotionId, etudiantId, setEtudiantId, setEtudiantNom } = useApp()
  const [etudiants, setEtudiants] = useState([])
  const [chargement, setChargement] = useState(false)
  const [erreur, setErreur] = useState(null)

  useEffect(() => {
    if (!promotionId) return
    let actif = true
    setChargement(true)
    setErreur(null)
    promotionsApi
      .etudiants(promotionId)
      .then((donnees) => actif && setEtudiants(donnees))
      .catch((e) => actif && setErreur(e.message))
      .finally(() => actif && setChargement(false))
    return () => {
      actif = false
    }
  }, [promotionId])

  if (chargement) return <Chargement message="Chargement des étudiants…" />
  if (erreur) return <Alerte variante="danger">{erreur}</Alerte>

  return (
    <Champ
      label="Mon nom"
      id="selecteur-etudiant"
      aide="Aucun mot de passe n’est demandé (Q1) : on simule l’identité, ce n’est pas une authentification."
    >
      <select
        id="selecteur-etudiant"
        className="selection"
        value={etudiantId ?? ''}
        disabled={!requis || etudiants.length === 0}
        onChange={(event) => {
          const id = Number(event.target.value)
          setEtudiantId(id)
          setEtudiantNom(etudiants.find((e) => e.id === id)?.nom ?? '')
        }}
      >
        <option value="">— Choisir —</option>
        {etudiants.map((etudiant) => (
          <option key={etudiant.id} value={etudiant.id}>
            {etudiant.nom}
          </option>
        ))}
      </select>
    </Champ>
  )
}

/** Liste des sessions de la promotion courante. */
export function SelecteurSession({ valeur, onChange, libelle = 'Session' }) {
  const { promotionId } = useApp()
  const [sessions, setSessions] = useState([])
  const [chargement, setChargement] = useState(false)
  const [erreur, setErreur] = useState(null)

  useEffect(() => {
    if (!promotionId) return
    let actif = true
    setChargement(true)
    sessionsApi
      .lister(promotionId)
      .then((donnees) => actif && setSessions(donnees))
      .catch((e) => actif && setErreur(e.message))
      .finally(() => actif && setChargement(false))
    return () => {
      actif = false
    }
  }, [promotionId])

  if (chargement) return <Chargement message="Chargement des sessions…" />
  if (erreur) return <Alerte variante="danger">{erreur}</Alerte>

  return (
    <Champ label={libelle} id="selecteur-session">
      <select
        id="selecteur-session"
        className="selection"
        value={valeur ?? ''}
        onChange={(event) => onChange(event.target.value ? Number(event.target.value) : null)}
      >
        <option value="">— Choisir —</option>
        {sessions.map((session) => (
          <option key={session.id} value={session.id}>
            {session.titre} ({session.statut === 'OUVERTE' ? 'ouverte' : 'clôturée'})
          </option>
        ))}
      </select>
    </Champ>
  )
}

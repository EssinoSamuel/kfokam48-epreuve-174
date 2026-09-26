import { createContext, useContext, useMemo, useState } from 'react'

/**
 * Contexte d'identite et de navigation.
 *
 * Le client exclut explicitement toute authentification (Q1) : il n'y a donc
 * pas de login. On simule simplement « je suis etudiant X » ou « j'agis en
 * tant que formateur » pour alimenter les selecteurs et conserver le contexte
 * de navigation. Ce n'est PAS une authentification.
 */
const ContexteApp = createContext(null)

const ROLES = [
  { cle: 'formateur', libelle: 'Formateur' },
  { cle: 'etudiant', libelle: 'Étudiant' },
  { cle: 'relecteur', libelle: 'Relecteur' },
]

export function FournisseurApp({ children }) {
  const [role, setRole] = useState('formateur')
  const [promotionId, setPromotionId] = useState(null)
  const [etudiantId, setEtudiantId] = useState(null)
  const [etudiantNom, setEtudiantNom] = useState('')
  const [sessionCouranteId, setSessionCouranteId] = useState(null)

  const valeur = useMemo(
    () => ({
      role,
      setRole,
      roles: ROLES,
      promotionId,
      setPromotionId,
      etudiantId,
      setEtudiantId,
      etudiantNom,
      setEtudiantNom,
      sessionCouranteId,
      setSessionCouranteId,
    }),
    [role, promotionId, etudiantId, etudiantNom, sessionCouranteId],
  )

  return <ContexteApp.Provider value={valeur}>{children}</ContexteApp.Provider>
}

export function useApp() {
  const contexte = useContext(ContexteApp)
  if (!contexte) {
    throw new Error('useApp doit être utilisé dans FournisseurApp')
  }
  return contexte
}

import { useCallback, useEffect, useRef, useState } from 'react'

/**
 * Hook d'appel a l'API : centralise les etats de l'interface.
 *
 * Pourquoi un hook plutot qu'une repetition dans chaque ecran :
 * - un seul endroit porte `chargement` et `erreur` (regle du prompt) ;
 * - les boutons se desactivent de la meme facon partout ;
 * - plus de code duplique, donc moins de divergences.
 *
 * Usage :
 *   const { donnees, chargement, erreur, recharger, definirDonnees } = useRequete(
 *     promotionsApi.lister, [promotionId], null,
 *   )
 */
export function useRequete(chargeur, dependances = [], valeurInitiale = null) {
  const [donnees, setDonnees] = useState(valeurInitiale)
  const [chargement, setChargement] = useState(false)
  const [erreur, setErreur] = useState(null)
  const [touche, setTouche] = useState(0)
  const actif = useRef(true)

  useEffect(() => {
    return () => {
      actif.current = false
    }
  }, [])

  useEffect(() => {
    if (typeof chargeur !== 'function') return
    let vivant = true
    setChargement(true)
    setErreur(null)

    Promise.resolve()
      .then(() => chargeur())
      .then((resultat) => {
        if (vivant) setDonnees(resultat)
      })
      .catch((e) => {
        if (vivant) setErreur(e?.message ?? 'Une erreur est survenue.')
      })
      .finally(() => {
        if (vivant) setChargement(false)
      })

    return () => {
      vivant = false
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...dependances, touche])

  const recharger = useCallback(() => setTouche((v) => v + 1), [])

  return {
    donnees,
    setDonnees,
    chargement,
    erreur,
    recharger,
  }
}

/**
 * Etat d'un formulaire : envoi en cours, message de succes, message d'erreur.
 * Separé de useRequete car un envoi n'est pas une simple relecture.
 */
export function useEnvoi() {
  const [envoi, setEnvoi] = useState(false)
  const [succes, setSucces] = useState(null)
  const [erreur, setErreur] = useState(null)

  const executer = useCallback(async (action, messageSucces = 'C’est fait.') => {
    setEnvoi(true)
    setSucces(null)
    setErreur(null)
    try {
      const resultat = await action()
      setSucces(messageSucces)
      return resultat
    } catch (e) {
      setErreur(e?.message ?? 'Une erreur est survenue.')
      return null
    } finally {
      setEnvoi(false)
    }
  }, [])

  const reinitialiser = useCallback(() => {
    setSucces(null)
    setErreur(null)
  }, [])

  return { envoi, succes, erreur, executer, reinitialiser }
}

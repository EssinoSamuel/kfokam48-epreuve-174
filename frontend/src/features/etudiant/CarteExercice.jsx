import { useState } from 'react'
import { Carte, Bouton, Alerte } from '../../components/ui.jsx'
import { Champ } from '../../components/formulaires.jsx'
import { SelecteurSession } from '../../components/selecteurs.jsx'
import { useApp } from '../../context/AppContext.jsx'
import { useEnvoi } from '../../hooks/useRequete.js'
import { exercicesApi } from '../../api/index.js'

/**
 * Depot d'un exercice (EF5, Q12) et remplacement du lien (EF6, Q13).
 *
 * Le depot est possible tant que la session n'est pas cloturee — meme
 * le soir, sans avoir ete present (decision H5). Le remplacement est
 * refuse par le backend si la relecture a commence (RG8) : on affiche
 * simplement le message qu'il renvoie.
 */
export function CarteDepotExercice() {
  const { etudiantId } = useApp()
  const { envoi, succes, erreur, executer, reinitialiser } = useEnvoi()
  const [sessionId, setSessionId] = useState(null)
  const [lien, setLien] = useState('')

  const desactive = envoi || !etudiantId || !sessionId

  async function soumettre(event) {
    event.preventDefault()
    reinitialiser()
    const resultat = await executer(
      () => exercicesApi.deposer(sessionId, etudiantId, lien.trim()),
      'Exercice déposé. Un relecteur a été désigné si possible.',
    )
    if (resultat) setLien('')
  }

  return (
    <Carte
      titre="Déposer mon exercice"
      description="Le lien reste modifiable tant que la session n’est pas clôturée et qu’aucune relecture n’a commencé."
    >
      {!etudiantId ? (
        <Alerte variante="info">Choisissez d’abord votre nom ci-contre.</Alerte>
      ) : null}

      <form onSubmit={soumettre}>
        <SelecteurSession valeur={sessionId} onChange={setSessionId} libelle="Session concernée" />

        <Champ
          label="Lien de mon exercice"
          id="lien-exercice"
          aide="Adresse complète de votre dépôt (https://…)."
        >
          <input
            id="lien-exercice"
            className="saisie"
            type="url"
            value={lien}
            onChange={(event) => setLien(event.target.value)}
            placeholder="https://github.com/…"
            disabled={desactive}
            aria-describedby="lien-exercice-aide"
          />
        </Champ>

        <Bouton type="submit" charge={envoi} disabled={desactive || lien.trim() === ''}>
          Déposer mon exercice
        </Bouton>
      </form>

      {succes ? <Alerte variante="succes">{succes}</Alerte> : null}
      {erreur ? <Alerte variante="danger">{erreur}</Alerte> : null}
    </Carte>
  )
}

/** Remplacement du lien d'un exercice deja depose (EF6, Q13). */
export function CarteRemplacerLien({ exercices }) {
  const { envoi, succes, erreur, executer } = useEnvoi()
  const [exerciceId, setExerciceId] = useState('')
  const [lien, setLien] = useState('')

  const remplacables = exercices.filter((e) => e.statut !== 'RELU')
  const desactive = envoi || !exerciceId || lien.trim() === ''

  async function soumettre(event) {
    event.preventDefault()
    const resultat = await executer(
      () => exercicesApi.remplacerLien(Number(exerciceId), lien.trim()),
      'Lien mis à jour.',
    )
    if (resultat) setLien('')
  }

  return (
    <Carte titre="Remplacer le lien" description="Refusé si la relecture a déjà commencé.">
      {remplacables.length === 0 ? (
        <p className="carte__description">Aucun exercice en attente ne peut être modifié.</p>
      ) : (
        <form onSubmit={soumettre}>
          <Champ label="Exercice" id="exercice-a-remplacer">
            <select
              id="exercice-a-remplacer"
              className="selection"
              value={exerciceId}
              onChange={(event) => setExerciceId(event.target.value)}
            >
              <option value="">— Choisir —</option>
              {remplacables.map((exercice) => (
                <option key={exercice.id} value={exercice.id}>
                  Exercice #{exercice.id}
                </option>
              ))}
            </select>
          </Champ>

          <Champ label="Nouveau lien" id="nouveau-lien">
            <input
              id="nouveau-lien"
              className="saisie"
              type="url"
              value={lien}
              onChange={(event) => setLien(event.target.value)}
              placeholder="https://github.com/…"
              disabled={desactive}
            />
          </Champ>

          <Bouton variante="secondaire" type="submit" charge={envoi} disabled={desactive}>
            Remplacer le lien
          </Bouton>
        </form>
      )}

      {succes ? <Alerte variante="succes">{succes}</Alerte> : null}
      {erreur ? <Alerte variante="danger">{erreur}</Alerte> : null}
    </Carte>
  )
}

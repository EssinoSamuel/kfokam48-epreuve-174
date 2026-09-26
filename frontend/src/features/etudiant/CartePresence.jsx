import { useState } from 'react'
import { Carte, Bouton, Alerte } from '../../components/ui.jsx'
import { Champ } from '../../components/formulaires.jsx'
import { useApp } from '../../context/AppContext.jsx'
import { useEnvoi } from '../../hooks/useRequete.js'
import { presencesApi } from '../../api/index.js'
import { formaterDate } from '../../utils/libelles.js'

/**
 * Marquer sa presence avec un code (EF2, RG1, RG10).
 *
 * Regle de conception : le frontend n'invente aucune regle metier. Il
 * envoie le code, affiche la reponse telle quelle. Le blocage apres
 * cinq tentatives (RG10) est arbitre par le backend : ici on ne fait que
 * montrer le message qu'il renvoie.
 */
export function CartePresence() {
  const { etudiantId, etudiantNom } = useApp()
  const { envoi, succes, erreur, executer, reinitialiser } = useEnvoi()
  const [code, setCode] = useState('')
  const [presence, setPresence] = useState(null)

  const desactive = envoi || !etudiantId

  async function soumettre(event) {
    event.preventDefault()
    reinitialiser()
    const resultat = await executer(
      () => presencesApi.marquer(code.trim().toUpperCase(), etudiantId),
      'Présence enregistrée.',
    )
    if (resultat) {
      setPresence(resultat)
      setCode('')
    }
  }

  return (
    <Carte
      titre="Marquer ma présence"
      description="Saisissez le code de présence donné par le formateur."
    >
      {!etudiantId ? (
        <Alerte variante="info">Choisissez d’abord votre nom ci-contre.</Alerte>
      ) : null}

      <form onSubmit={soumettre}>
        <Champ
          label="Code de présence"
          id="code-presence"
          aide="Six caractères, valable 15 minutes après l’ouverture de la session (Q2)."
        >
          <input
            id="code-presence"
            className="saisie saisie--code"
            value={code}
            onChange={(event) => setCode(event.target.value)}
            placeholder="ABC123"
            maxLength={12}
            disabled={desactive}
            autoComplete="off"
            aria-describedby="code-presence-aide"
          />
        </Champ>

        <Bouton type="submit" charge={envoi} disabled={desactive || code.trim().length === 0}>
          Marquer ma présence
        </Bouton>
      </form>

      {succes ? <Alerte variante="succes">{succes}</Alerte> : null}
      {erreur ? <Alerte variante="danger">{erreur}</Alerte> : null}

      {presence ? (
        <div className="meta-liste" style={{ marginTop: 'var(--espace-4)' }}>
          <div className="meta-liste__ligne">
            <span className="meta-liste__cle">Étudiant</span>
            <span className="meta-liste__valeur">{presence.etudiantNom ?? etudiantNom}</span>
          </div>
          <div className="meta-liste__ligne">
            <span className="meta-liste__cle">Marquée le</span>
            <span className="meta-liste__valeur">{formaterDate(presence.marqueeAt)}</span>
          </div>
        </div>
      ) : null}
    </Carte>
  )
}

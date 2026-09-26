import { useState } from 'react'
import { Carte, Bouton, Badge, Alerte, Chargement, EtatVide } from '../../components/ui.jsx'
import { Champ } from '../../components/formulaires.jsx'
import { SelecteurPromotion, SelecteurEtudiant, SelecteurSession } from '../../components/selecteurs.jsx'
import { useApp } from '../../context/AppContext.jsx'
import { useRequete, useEnvoi } from '../../hooks/useRequete.js'
import { presencesApi, exercicesApi } from '../../api/index.js'
import { LIBELLES_STATUT_EXERCICE, formaterDate, formaterMoyenne } from '../../utils/libelles.js'

/**
 * Parcours etudiant : identite → presence → exercice → consultation.
 *
 * Regle F3 : aucune regle metier ici. Le frontend affiche ce que l'API
 * renvoie ; les refus (code expire, trop de tentatives, session cloturee)
 * sont traduits par la couche API, pas devines ici.
 */

/** Bloc 1 : marquage de la presence avec le code de la session. */
export function BlocPresence() {
  const { etudiantId } = useApp()
  const [code, setCode] = useState('')
  const { envoi, succes, erreur, executer, reinitialiser } = useEnvoi()

  const envoyer = () => {
    reinitialiser()
    executer(
      () => presencesApi.marquer(code.trim(), etudiantId),
      'Présence enregistrée. Votre présence est confirmée pour cette session.',
    )
  }

  return (
    <Carte titre="Marquer ma présence" description="Saisissez le code affiché par le formateur.">
      <Champ
        label="Code de présence"
        id="code-presence"
        aide="Le code est valable quinze minutes après l’ouverture de la session (Q2)."
      >
        <input
          id="code-presence"
          className="saisie saisie--code"
          value={code}
          maxLength={12}
          autoComplete="off"
          disabled={!etudiantId || envoi}
          onChange={(e) => setCode(e.target.value.toUpperCase())}
          placeholder="KF48A1"
        />
      </Champ>

      {succes ? <Alerte variante="succes">{succes}</Alerte> : null}
      {erreur ? <Alerte variante="danger">{erreur}</Alerte> : null}

      <Bouton onClick={envoyer} disabled={!etudiantId || code.trim().length === 0 || envoi} charge={envoi}>
        Marquer ma présence
      </Bouton>
      {!etudiantId ? (
        <p className="champ__aide" style={{ marginTop: '0.75rem' }}>
          Choisissez d’abord votre nom ci-dessus.
        </p>
      ) : null}
    </Carte>
  )
}

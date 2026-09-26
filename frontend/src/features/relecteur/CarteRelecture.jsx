import { useState } from 'react'
import { Carte, Bouton, Badge, Alerte } from '../../components/ui.jsx'
import { Champ } from '../../components/formulaires.jsx'
import { useEnvoi } from '../../hooks/useRequete.js'
import { relecturesApi } from '../../api/index.js'
import { LIBELLES_STATUT_RELECTURE, formaterDate } from '../../utils/libelles.js'

/**
 * Carte d'une relecture assignee : consulter → demarrer → noter (Q7, Q13, Q9, Q15).
 *
 * Le verrouillage du lien est decide par le backend (H4) : le bouton
 * « Demarrer » appelle l'API, ce n'est pas une regle frontend.
 */
export function CarteRelecture({ relecture }) {
  const [note, setNote] = useState('')
  const [commentaire, setCommentaire] = useState('')
  const { envoi, succes, erreur, executer, reinitialiser } = useEnvoi()

  const statut = LIBELLES_STATUT_RELECTURE[relecture.statut] ?? {
    texte: relecture.statut,
    variante: 'neutre',
  }
  const demarrable = relecture.statut === 'ASSIGNEE'
  const notable = relecture.statut === 'ASSIGNEE' || relecture.statut === 'EN_COURS'

  const demarrer = () => {
    reinitialiser()
    executer(
      () => relecturesApi.demarrer(relecture.id),
      'Relecture démarrée. L’auteur ne peut plus remplacer son lien (Q13).',
    )
  }

  const envoyer = () => {
    reinitialiser()
    executer(
      () => relecturesApi.envoyer(relecture.id, Number(note), commentaire.trim()),
      'Relecture envoyée. La note est définitive (Q15).',
    )
  }

  return (
    <li className="carte" style={{ padding: '1rem' }}>
      <div className="carte__entete" style={{ marginBottom: '0.5rem' }}>
        <div>
          <strong>Exercice #{relecture.exerciceId}</strong>
          <div className="champ__aide">
            Auteur : {relecture.auteurNom} · Déposé le {formaterDate(relecture.deposeAt)}
          </div>
        </div>
        <Badge variante={statut.variante}>{statut.texte}</Badge>
      </div>

      <div className="rangee" style={{ marginBottom: '0.75rem' }}>
        <a
          href={relecture.lien}
          target="_blank"
          rel="noreferrer"
          className="bouton bouton--secondaire petit"
        >
          Ouvrir l’exercice
        </a>
        {demarrable ? (
          <Bouton petit variante="secondaire" onClick={demarrer} charge={envoi}>
            Démarrer la relecture
          </Bouton>
        ) : null}
      </div>

      {relecture.statut === 'RENDUE' ? (
        <Alerte variante="succes">
          Relecture rendue le {formaterDate(relecture.rendueAt)} — note {relecture.note}/20.
          {relecture.commentaire ? ` Commentaire : ${relecture.commentaire}` : ''}
        </Alerte>
      ) : null}

      {notable ? (
        <div>
          <Champ label="Note sur 20" id={`note-${relecture.id}`} aide="Nombre entier de 0 à 20 (Q9).">
            <input
              id={`note-${relecture.id}`}
              className="saisie"
              type="number"
              min="0"
              max="20"
              step="1"
              value={note}
              disabled={envoi}
              onChange={(e) => setNote(e.target.value)}
              style={{ maxWidth: '10rem' }}
            />
          </Champ>
          <Champ label="Commentaire" id={`commentaire-${relecture.id}`}>
            <textarea
              id={`commentaire-${relecture.id}`}
              className="zone-texte"
              value={commentaire}
              disabled={envoi}
              onChange={(e) => setCommentaire(e.target.value)}
              placeholder="Points forts, axes d’amélioration, conseils…"
            />
          </Champ>
        </div>
      ) : null}

      {succes ? <Alerte variante="succes">{succes}</Alerte> : null}
      {erreur ? <Alerte variante="danger">{erreur}</Alerte> : null}

      {notable ? (
        <Bouton
          onClick={envoyer}
          disabled={envoi || note === '' || Number(note) < 0 || Number(note) > 20}
          charge={envoi}
        >
          Envoyer ma relecture
        </Bouton>
      ) : null}
    </li>
  )
}

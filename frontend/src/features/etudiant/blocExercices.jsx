import { useState } from 'react'
import { Carte, Bouton, Badge, Alerte, Chargement, EtatVide } from '../../components/ui.jsx'
import { Champ } from '../../components/formulaires.jsx'
import { SelecteurSession } from '../../components/selecteurs.jsx'
import { useApp } from '../../context/AppContext.jsx'
import { useRequete, useEnvoi } from '../../hooks/useRequete.js'
import { exercicesApi } from '../../api/index.js'
import { LIBELLES_STATUT_EXERCICE, formaterDate, formaterMoyenne } from '../../utils/libelles.js'

/** Bloc : depot du lien d'exercice (Q12) et remplacement du lien (Q13). */
export function BlocDepot() {
  const { etudiantId } = useApp()
  const [sessionId, setSessionId] = useState(null)
  const [lien, setLien] = useState('')
  const { envoi, succes, erreur, executer, reinitialiser } = useEnvoi()

  const envoyer = () => {
    reinitialiser()
    executer(
      () => exercicesApi.deposer(sessionId, etudiantId, lien.trim()),
      'Exercice déposé. Un relecteur va être désigné automatiquement.',
    )
  }

  return (
    <Carte
      titre="Déposer mon exercice"
      description="Possible jusqu’à la clôture de la session (Q12) — même le soir même."
    >
      <SelecteurSession
        valeur={sessionId}
        onChange={(id) => {
          setSessionId(id)
          reinitialiser()
        }}
        libelle="Session concernée"
      />
      <Champ
        label="Lien de mon exercice"
        id="lien-exercice"
        aide="Adresse complète du dépôt : https://github.com/…"
      >
        <input
          id="lien-exercice"
          className="saisie"
          value={lien}
          disabled={!etudiantId || !sessionId || envoi}
          onChange={(e) => setLien(e.target.value)}
          placeholder="https://github.com/mon-pseudonyme/mon-exercice"
        />
      </Champ>

      {succes ? <Alerte variante="succes">{succes}</Alerte> : null}
      {erreur ? <Alerte variante="danger">{erreur}</Alerte> : null}

      <Bouton
        onClick={envoyer}
        disabled={!etudiantId || !sessionId || lien.trim().length === 0 || envoi}
        charge={envoi}
      >
        Déposer mon exercice
      </Bouton>
    </Carte>
  )
}

/** Bloc : mes exercices, leur statut, la note et le commentaire reçus (Q8). */
export function BlocMesExercices() {
  const { etudiantId } = useApp()
  const { donnees, chargement, recharger } = useRequete(
    () => (etudiantId ? exercicesApi.parEtudiant(etudiantId) : Promise.resolve([])),
    [etudiantId],
  )

  if (!etudiantId) {
    return (
      <Carte titre="Mes exercices">
        <EtatVide titre="Choisissez votre nom">Vos dépôts et vos notes s’afficheront ici.</EtatVide>
      </Carte>
    )
  }
  if (chargement) {
    return (
      <Carte titre="Mes exercices">
        <Chargement message="Chargement de vos exercices…" />
      </Carte>
    )
  }

  const liste = donnees ?? []

  return (
    <Carte
      titre="Mes exercices"
      description="Le nom du relecteur n’est jamais affiché (Q8) : seule la note compte."
      actions={
        <Bouton variante="secondaire" petit onClick={recharger}>
          Actualiser
        </Bouton>
      }
    >
      {liste.length === 0 ? (
        <EtatVide titre="Aucun exercice déposé">
          Déposez un premier lien pour lancer la relecture.
        </EtatVide>
      ) : (
        <ul className="pile" style={{ listStyle: 'none', margin: 0, padding: 0 }}>
          {liste.map((exercice) => {
            const statut = LIBELLES_STATUT_EXERCICE[exercice.statut] ?? {
              texte: exercice.statut,
              variante: 'neutre',
            }
            // La note est provisoire tant que les deux pairs n'ont pas rendu.
            const noteProvisoire = exercice.statutNote === 'PROVISOIRE'
            return (
              <li key={exercice.id} className="carte" style={{ padding: '1rem' }}>
                <div className="carte__entete" style={{ marginBottom: '0.5rem' }}>
                  <a href={exercice.lien} target="_blank" rel="noreferrer">
                    Voir mon dépôt
                  </a>
                  <div className="rangee" style={{ gap: '0.5rem' }}>
                    {noteProvisoire ? (
                      <Badge variante="alerte">Note provisoire</Badge>
                    ) : null}
                    <Badge variante={statut.variante}>{statut.texte}</Badge>
                  </div>
                </div>
                <div className="meta-liste">
                  <div className="meta-liste__ligne">
                    <span className="meta-liste__cle">Déposé le</span>
                    <span className="meta-liste__valeur">{formaterDate(exercice.deposeAt)}</span>
                  </div>
                  <div className="meta-liste__ligne">
                    <span className="meta-liste__cle">Note reçue</span>
                    <span className="meta-liste__valeur">
                      {exercice.moyenne === null || exercice.moyenne === undefined ? (
                        <span className="vide-ou-tiret">Pas encore notée</span>
                      ) : (
                        formaterMoyenne(exercice.moyenne)
                      )}
                    </span>
                  </div>
                  {/* Note provisoire : un seul des deux pairs a encore répondu. */}
                  {exercice.statutNote === 'PROVISOIRE' && exercice.notesRendues > 0 ? (
                    <div className="meta-liste__ligne">
                      <span className="meta-liste__cle">En attente</span>
                      <span className="meta-liste__valeur">
                        Note provisoire : {exercice.notesRendues} relecture(s) sur 2
                      </span>
                    </div>
                  ) : null}
                  {(exercice.commentaires ?? []).map((item, index) => (
                    <div className="meta-liste__ligne" key={index}>
                      <span className="meta-liste__cle">Commentaire</span>
                      <span className="meta-liste__valeur">{item.commentaire}</span>
                    </div>
                  ))}
                </div>
              </li>
            )
          })}
        </ul>
      )}
    </Carte>
  )
}

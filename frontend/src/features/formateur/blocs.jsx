import { useState } from 'react'
import { Carte, Bouton, Badge, Alerte, Chargement, EtatVide } from '../../components/ui.jsx'
import { Champ } from '../../components/formulaires.jsx'
import { SelecteurPromotion, SelecteurEtudiant, SelecteurSession } from '../../components/selecteurs.jsx'
import { useApp } from '../../context/AppContext.jsx'
import { useRequete, useEnvoi } from '../../hooks/useRequete.js'
import { sessionsApi, presencesApi } from '../../api/index.js'
import { LIBELLES_STATUT_SESSION, formaterDate } from '../../utils/libelles.js'

/** Bloc : ouverture d'une session et affichage du code (EF1, Q2). */
export function BlocSession() {
  const { promotionId, sessionCouranteId, setSessionCouranteId } = useApp()
  const [titre, setTitre] = useState('')
  const { envoi, succes, erreur, executer, reinitialiser } = useEnvoi()
  const { donnees, recharger } = useRequete(
    () => (promotionId ? sessionsApi.lister(promotionId) : Promise.resolve([])),
    [promotionId],
  )

  const sessions = donnees ?? []
  const courante = sessions.find((s) => s.id === sessionCouranteId) ?? null

  const ouvrir = () => {
    reinitialiser()
    executer(async () => {
      const session = await sessionsApi.creer(titre.trim(), promotionId)
      setSessionCouranteId(session.id)
      recharger()
      return session
    })
  }

  const cloturer = () => {
    if (!courante) return
    reinitialiser()
    executer(async () => {
      await sessionsApi.cloturer(courante.id)
      recharger()
    }, 'Session clôturée : plus aucun dépôt ni remplacement de lien n’est accepté (Q12).')
  }

  return (
    <Carte
      titre="Sessions de la promotion"
      description="Le code généré est valable 15 minutes (Q2). La clôture, elle, est une action séparée (Q12)."
    >
      <Champ label="Titre de la session" id="titre-session">
        <input
          id="titre-session"
          className="saisie"
          value={titre}
          disabled={!promotionId || envoi}
          onChange={(e) => setTitre(e.target.value)}
          placeholder="Session 3 — Tests automatisés"
        />
      </Champ>

      {succes ? <Alerte variante="succes">{succes}</Alerte> : null}
      {erreur ? <Alerte variante="danger">{erreur}</Alerte> : null}

      <Bouton onClick={ouvrir} disabled={!promotionId || titre.trim().length === 0 || envoi} charge={envoi}>
        Ouvrir la session
      </Bouton>

      {courante ? (
        <div style={{ marginTop: '1rem' }}>
          <div className="code-session">{courante.code}</div>
          <div className="meta-liste">
            <div className="meta-liste__ligne">
              <span className="meta-liste__cle">Titre</span>
              <span className="meta-liste__valeur">{courante.titre}</span>
            </div>
            <div className="meta-liste__ligne">
              <span className="meta-liste__cle">Expire le</span>
              <span className="meta-liste__valeur">{formaterDate(courante.expirationAt)}</span>
            </div>
            <div className="meta-liste__ligne">
              <span className="meta-liste__cle">État</span>
              <span className="meta-liste__valeur">
                {LIBELLES_STATUT_SESSION[courante.statut]?.texte ?? courante.statut}
              </span>
            </div>
          </div>
          {courante.statut === 'OUVERTE' ? (
            <Bouton variante="danger" onClick={cloturer} charge={envoi} style={{ marginTop: '0.75rem' }}>
              Clôturer la session
            </Bouton>
          ) : null}
        </div>
      ) : null}

      {sessions.length > 0 ? (
        <ul className="pile" style={{ listStyle: 'none', margin: '1rem 0 0', padding: 0 }}>
          {sessions.map((session) => (
            <li key={session.id} className="rangee" style={{ justifyContent: 'space-between' }}>
              <span>{session.titre}</span>
              <Badge variante={LIBELLES_STATUT_SESSION[session.statut]?.variante ?? 'neutre'}>
                {LIBELLES_STATUT_SESSION[session.statut]?.texte ?? session.statut}
              </Badge>
            </li>
          ))}
        </ul>
      ) : null}
    </Carte>
  )
}

/** Bloc : ajout manuel d'une presence (EF3, Q14, RG9). */
export function BlocPresenceManuelle() {
  const { etudiantId, sessionCouranteId } = useApp()
  const { envoi, succes, erreur, executer, reinitialiser } = useEnvoi()

  const ajouter = () => {
    reinitialiser()
    executer(
      () => presencesApi.ajouterParFormateur(sessionCouranteId, etudiantId),
      'Présence ajoutée. Elle apparaîtra comme « ajoutée par le formateur ».',
    )
  }

  return (
    <Carte
      titre="Ajouter une présence manuellement"
      description="Quand un étudiant a un souci de connexion (Q14). La présence est marquée comme ajoutée par le formateur (RG9)."
    >
      <SelecteurEtudiant />
      {succes ? <Alerte variante="succes">{succes}</Alerte> : null}
      {erreur ? <Alerte variante="danger">{erreur}</Alerte> : null}
      <Bouton
        onClick={ajouter}
        disabled={!etudiantId || !sessionCouranteId || envoi}
        charge={envoi}
        variante="secondaire"
      >
        Ajouter la présence
      </Bouton>
      {!sessionCouranteId ? (
        <p className="champ__aide" style={{ marginTop: '0.75rem' }}>
          Ouvrez d’abord une session ci-contre.
        </p>
      ) : null}
    </Carte>
  )
}

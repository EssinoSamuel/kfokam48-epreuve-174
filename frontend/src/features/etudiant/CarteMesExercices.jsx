import { Carte, Badge, Chargement, EtatVide, Alerte, Bouton } from '../../components/ui.jsx'
import { Tableau } from '../../components/formulaires.jsx'
import { useApp } from '../../context/AppContext.jsx'
import { useRequete } from '../../hooks/useRequete.js'
import { exercicesApi } from '../../api/index.js'
import {
  LIBELLES_STATUT_EXERCICE,
  formaterDate,
} from '../../utils/libelles.js'

/**
 * Mes exercices (EF9, RG6, RG11).
 *
 * La note vient de l'API : si elle est null, on affiche « Note non
 * disponible » et jamais 0 — 0 serait une vraie note. Le nom du
 * relecteur n'est jamais renvoyé par cette lecture (Q8) : rien à masquer
 * côté interface.
 */
export default function CarteMesExercices() {
  const { etudiantId } = useApp()
  const { donnees, chargement, erreur, recharger } = useRequete(
    etudiantId ? () => exercicesApi.parEtudiant(etudiantId) : null,
    [etudiantId],
    [],
  )

  if (!etudiantId) {
    return (
      <Carte titre="Mes exercices">
        <Alerte variante="info">Choisissez d’abord votre nom pour voir vos dépôts.</Alerte>
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

  if (erreur) {
    return (
      <Carte titre="Mes exercices">
        <Alerte variante="danger">{erreur}</Alerte>
        <Bouton variante="secondaire" onClick={recharger}>
          Réessayer
        </Bouton>
      </Carte>
    )
  }

  if (donnees.length === 0) {
    return (
      <Carte titre="Mes exercices">
        <EtatVide titre="Aucun dépôt pour le moment">
          Déposez un exercice pour qu’un pair puisse le relire.
        </EtatVide>
      </Carte>
    )
  }

  return (
    <Carte
      titre="Mes exercices"
      description="Vos dépôts, leur état et la note reçue."
      actions={<Bouton variante="secondaire" petit onClick={recharger}>Actualiser</Bouton>}
    >
      <Tableau
        colonnes={[
          { titre: 'Exercice' },
          { titre: 'Lien' },
          { titre: 'Statut' },
          { titre: 'Note' },
          { titre: 'Commentaire' },
        ]}
      >
        {donnees.map((exercice) => {
          const libelle = LIBELLES_STATUT_EXERCICE[exercice.statut] ?? {
            texte: exercice.statut,
            variante: 'neutre',
          }
          return (
            <tr key={exercice.id}>
              <td>
                #{exercice.id}
                <div className="vide-ou-tiret">{formaterDate(exercice.deposeAt)}</div>
              </td>
              <td>
                <a href={exercice.lien} target="_blank" rel="noreferrer">
                  Ouvrir le dépôt
                </a>
              </td>
              <td>
                <Badge variante={libelle.variante}>{libelle.texte}</Badge>
              </td>
              <td>
                {exercice.note === null || exercice.note === undefined ? (
                  <span className="vide-ou-tiret">Note non disponible</span>
                ) : (
                  <strong>{exercice.note}/20</strong>
                )}
              </td>
              <td>
                {exercice.commentaire ? (
                  exercice.commentaire
                ) : (
                  <span className="vide-ou-tiret">Aucun commentaire</span>
                )}
              </td>
            </tr>
          )
        })}
      </Tableau>
      <p className="carte__description" style={{ marginTop: 'var(--espace-3)' }}>
        Le nom du relecteur n’est pas communiqué (Q8) : seule la note et le commentaire
        vous sont transmis.
      </p>
    </Carte>
  )
}

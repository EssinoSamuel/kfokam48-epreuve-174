import { useState } from 'react'
import { Carte, Bouton, Chargement, EtatVide } from '../../components/ui.jsx'
import { Tableau } from '../../components/formulaires.jsx'
import { useApp } from '../../context/AppContext.jsx'
import { useRequete } from '../../hooks/useRequete.js'
import { tableauApi } from '../../api/index.js'
import { formaterMoyenne } from '../../utils/libelles.js'

/**
 * Tableau recapitulatif de la promotion (EF10, Q16).
 *
 * Contrainte F3 : la moyenne est une responsabilite du backend. On affiche
 * exactement la valeur recue ; si elle est null on affiche « — », jamais 0,
 * car 0 est une vraie note.
 */
export function BlocTableau() {
  const { promotionId } = useApp()
  const [declencheur, setDeclencheur] = useState(0)
  const { donnees, chargement, erreur, recharger } = useRequete(
    () => (promotionId ? tableauApi.charger(promotionId) : Promise.resolve([])),
    [promotionId, declencheur],
  )

  const lignes = donnees ?? []

  return (
    <Carte
      titre="Tableau récapitulatif de la promotion"
      description="Par étudiant : présence, dépôts, moyenne des notes reçues et relectures à faire (Q16)."
      actions={
        <Bouton variante="secondaire" petit onClick={() => { recharger(); setDeclencheur((v) => v + 1) }}>
          Actualiser
        </Bouton>
      }
    >
      {!promotionId ? (
        <EtatVide titre="Choisissez une promotion">Le tableau s’affichera ici.</EtatVide>
      ) : chargement ? (
        <Chargement message="Chargement du tableau…" />
      ) : erreur ? (
        <EtatVide titre="Tableau indisponible">{erreur}</EtatVide>
      ) : lignes.length === 0 ? (
        <EtatVide titre="Aucun étudiant">Cette promotion ne contient encore aucun étudiant.</EtatVide>
      ) : (
        <Tableau
          colonnes={[
            { titre: 'Étudiant' },
            { titre: 'Présences', numerique: true },
            { titre: 'Exercices déposés', numerique: true },
            { titre: 'Moyenne', numerique: true },
            { titre: 'Relectures à faire', numerique: true },
          ]}
        >
          {lignes.map((ligne) => (
            <tr key={ligne.etudiantId}>
              <td>{ligne.nom}</td>
              <td className="numeral">{ligne.presences}</td>
              <td className="numeral">{ligne.exercicesDeposes}</td>
              <td className="numeral">
                {ligne.moyenne === null || ligne.moyenne === undefined ? (
                  <span className="vide-ou-tiret">—</span>
                ) : (
                  formaterMoyenne(ligne.moyenne)
                )}
              </td>
              <td className="numeral">{ligne.relecturesEnAttente}</td>
            </tr>
          ))}
        </Tableau>
      )}
    </Carte>
  )
}

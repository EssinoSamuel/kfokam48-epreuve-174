/**
 * Client HTTP unique de l'application (contrainte F3).
 *
 * Pourquoi un seul point de sortie :
 * - aucun composant ne connait l'URL, le verbe ou le format d'erreur ;
 * - les erreurs {code, message} du backend sont normalisees en une seule
 *   fois, sans duplication dans chaque ecran ;
 * - la base de l'API vient de l'environnement (VITE_API_BASE_URL).
 */

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

/** Erreur applicative transportant le code metier du backend. */
export class ErreurApi extends Error {
  constructor(message, { code = null, statut = null } = {}) {
    super(message)
    this.name = 'ErreurApi'
    this.code = code
    this.statut = statut
  }
}

/**
 * Messages plus explicites que le message brut pour les codes les plus
 * frequents. Le backend reste la source de verite : on n'invente aucune
 * regle, on reformule ce qu'il a dit.
 */
const MESSAGES_AFFICHAGE = {
  CODE_INCONNU: 'Le code saisi ne correspond à aucune session.',
  CODE_EXPIRE: 'Le code de présence a expiré.',
  TROP_DE_TENTATIVES: 'Trop de tentatives incorrectes. Réessayez après le délai de blocage.',
  DEJA_PRESENT: 'Votre présence est déjà enregistrée.',
  NOTE_INVALIDE: 'La note doit être un entier compris entre 0 et 20.',
  AUTO_RELECTURE: 'Un étudiant ne peut pas relire son propre exercice.',
  RELECTURE_DEJA_RENDUE: 'Cette relecture a déjà été rendue : la note est définitive.',
  RELECTURE_DEJA_COMMENCEE: 'La relecture a commencé : le lien n’est plus modifiable.',
  SESSION_CLOTUREE: 'La session est clôturée : cette action n’est plus possible.',
  SESSION_DEJA_CLOTUREE: 'Cette session est déjà clôturée.',
  EXERCICE_DEJA_DEPOSE: 'Un exercice a déjà été déposé pour cette session.',
  LIEN_INVALIDE: 'Le lien fourni n’est pas une URL valide.',
  ETUDIANT_INCONNU: 'Étudiant introuvable.',
  SESSION_INCONNUE: 'Session introuvable.',
  PROMOTION_INCONNUE: 'Promotion introuvable.',
}

/** Message a afficher a l'utilisateur pour un code donne. */
export function messagePourCode(code, messageBackend) {
  if (!code) return messageBackend || 'Une erreur est survenue.'
  return MESSAGES_AFFICHAGE[code] || messageBackend || 'Une erreur est survenue.'
}

/** Construit l'URL complete a partir du chemin et des query params. */
function construireUrl(chemin, params) {
  const url = new URL(`${BASE_URL}${chemin}`)
  if (params) {
    Object.entries(params).forEach(([cle, valeur]) => {
      if (valeur !== undefined && valeur !== null && valeur !== '') {
        url.searchParams.append(cle, valeur)
      }
    })
  }
  return url.toString()
}

/** Execute un appel HTTP et normalise la reponse ou l'echec. */
async function appeler(method, chemin, { corps, params } = {}) {
  let reponse
  try {
    reponse = await fetch(construireUrl(chemin, params), {
      method,
      headers: corps ? { 'Content-Type': 'application/json' } : undefined,
      body: corps ? JSON.stringify(corps) : undefined,
    })
  } catch (erreur) {
    // Reseau indisponible ou backend arrete : message dedie, pas de stack trace.
    throw new ErreurApi(
      'Impossible de joindre le serveur. Vérifiez que le backend est démarré.',
      { code: 'RESEAU_INDISPONIBLE' },
    )
  }

  if (reponse.status === 204) return null

  const texte = await reponse.text()
  let donnees = null
  if (texte) {
    try {
      donnees = JSON.parse(texte)
    } catch {
      donnees = null
    }
  }

  if (!reponse.ok) {
    const code = donnees?.code ?? null
    throw new ErreurApi(messagePourCode(code, donnees?.message), {
      code,
      statut: reponse.status,
    })
  }

  return donnees
}

export const apiClient = {
  get: (chemin, params) => appeler('GET', chemin, { params }),
  post: (chemin, corps, params) => appeler('POST', chemin, { corps, params }),
  put: (chemin, corps) => appeler('PUT', chemin, { corps }),
  patch: (chemin) => appeler('PATCH', chemin),
}

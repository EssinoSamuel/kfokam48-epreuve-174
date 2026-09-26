/** Libelles français des statuts renvoyes par le backend. */
export const LIBELLES_STATUT_EXERCICE = {
  DEPOSE: { texte: 'Déposé', variante: 'neutre' },
  EN_ATTENTE_DE_RELECTURE: { texte: 'En attente de relecture', variante: 'alerte' },
  RELU: { texte: 'Relu', variante: 'succes' },
}

export const LIBELLES_STATUT_SESSION = {
  OUVERTE: { texte: 'Ouverte', variante: 'succes' },
  CLOTUREE: { texte: 'Clôturée', variante: 'neutre' },
}

export const LIBELLES_STATUT_RELECTURE = {
  ASSIGNEE: { texte: 'Assignée', variante: 'info' },
  EN_COURS: { texte: 'En cours', variante: 'alerte' },
  RENDUE: { texte: 'Rendue', variante: 'succes' },
}

export const LIBELLES_SOURCE_PRESENCE = {
  ETUDIANT: 'Marquée par l’étudiant',
  FORMATEUR: 'Ajoutée par le formateur',
}

/** Formate une date ISO renvoyee par l'API en heure locale lisible. */
export function formaterDate(iso) {
  if (!iso) return null
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) return null
  return date.toLocaleString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

/** Formate une moyenne : null devient un tiret, jamais 0 (contrainte F3). */
export function formaterMoyenne(moyenne) {
  if (moyenne === null || moyenne === undefined) return '—'
  return `${moyenne}/20`
}

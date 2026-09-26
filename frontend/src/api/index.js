/**
 * Services API par domaine metier (contrainte F3).
 *
 * Les composants appellent uniquement ces fonctions : ils ne connaissent
 * ni les chemins, ni les verbes, ni le format des erreurs.
 */
import { apiClient } from './client'

/** Promotions, etudiants et referentiel d'interface (Q1, H9). */
export const promotionsApi = {
  lister: () => apiClient.get('/api/promotions'),
  etudiants: (promotionId) => apiClient.get('/api/etudiants', { promotionId }),
  referentiel: (promotionId) => apiClient.get('/api/referentiel', { promotionId }),
}

/** Sessions : ouverture, consultation, cloture. */
export const sessionsApi = {
  creer: (titre, promotionId) => apiClient.post('/api/sessions', { titre, promotionId }),
  lister: (promotionId) => apiClient.get('/api/sessions', { promotionId }),
  parId: (id) => apiClient.get(`/api/sessions/${id}`),
  cloturer: (id) => apiClient.patch(`/api/sessions/${id}/cloture`),
  exercices: (id, statut) => apiClient.get(`/api/sessions/${id}/exercices`, { statut }),
}

/** Presences : marquage par code et ajout manuel par le formateur. */
export const presencesApi = {
  marquer: (code, etudiantId) => apiClient.post('/api/presences', { code, etudiantId }),
  ajouterParFormateur: (sessionId, etudiantId) =>
    apiClient.post(`/api/sessions/${sessionId}/presences`, { etudiantId }),
  listeParSession: (sessionId) => apiClient.get(`/api/presences/session/${sessionId}`),
}

/** Exercices : depot, remplacement du lien, lectures. */
export const exercicesApi = {
  deposer: (sessionId, etudiantId, lien) =>
    apiClient.post('/api/exercices', { sessionId, etudiantId, lien }),
  remplacerLien: (id, lien) => apiClient.put(`/api/exercices/${id}`, { lien }),
  parEtudiant: (etudiantId) => apiClient.get(`/api/etudiants/${etudiantId}/exercices`),
  parId: (id) => apiClient.get(`/api/exercices/${id}`),
}

/** Relectures : liste, demarrage, notation. */
export const relecturesApi = {
  pourEtudiant: (etudiantId) => apiClient.get(`/api/etudiants/${etudiantId}/relectures`),
  parId: (id) => apiClient.get(`/api/relectures/${id}`),
  demarrer: (id) => apiClient.post(`/api/relectures/${id}/demarrer`),
  envoyer: (id, note, commentaire) =>
    apiClient.post(`/api/relectures/${id}`, { note, commentaire }),
}

/** Tableau recapitulatif du formateur (Q16). */
export const tableauApi = {
  charger: (promotionId) => apiClient.get('/api/tableau', { promotionId }),
}

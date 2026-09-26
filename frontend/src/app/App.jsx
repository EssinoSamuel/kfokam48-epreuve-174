import { useState } from 'react'
import { FournisseurApp, useApp } from '../context/AppContext.jsx'
import EcranFormateur from '../features/formateur/EcranFormateur.jsx'
import EcranEtudiant from '../features/etudiant/EcranEtudiant.jsx'
import EcranRelecteur from '../features/relecteur/EcranRelecteur.jsx'

/**
 * Coquille de l'application : navigation entre les trois roles.
 * Aucun routage externe (react-router) : trois onglets suffisent et
 * evitent une dependance de plus (regle de non-sur-architecture).
 */
function Coquille() {
  const { role, setRole, roles } = useApp()

  const ecrans = {
    formateur: <EcranFormateur />,
    etudiant: <EcranEtudiant />,
    relecteur: <EcranRelecteur />,
  }

  return (
    <>
      <nav className="navigation" aria-label="Navigation principale">
        <div className="navigation__interieur">
          <div className="navigation__marque">
            KFOKAM48<span>.Suivi</span>
          </div>
          <div className="navigation__onglets" role="tablist" aria-label="Choisir un rôle">
            {roles.map((entree) => (
              <button
                key={entree.cle}
                type="button"
                role="tab"
                className="onglet"
                aria-selected={role === entree.cle}
                onClick={() => setRole(entree.cle)}
              >
                {entree.libelle}
              </button>
            ))}
          </div>
        </div>
      </nav>
      <main className="conteneur">{ecrans[role]}</main>
    </>
  )
}

export default function App() {
  return (
    <FournisseurApp>
      <Coquille />
    </FournisseurApp>
  )
}

/** Champs de formulaire avec label, aide et message d'erreur relies (accessibilite). */
export function Champ({ label, id, aide, erreur, children }) {
  return (
    <div className="champ">
      <label className="champ__etiquette" htmlFor={id}>
        {label}
      </label>
      {children}
      {aide ? (
        <span className="champ__aide" id={`${id}-aide`}>
          {aide}
        </span>
      ) : null}
      {erreur ? (
        <span className="champ__erreur" id={`${id}-erreur`} role="alert">
          {erreur}
        </span>
      ) : null}
    </div>
  )
}

/** Enveloppe de tableau scrollable : evite la casse du layout sur mobile. */
export function Tableau({ colonnes, children }) {
  return (
    <div className="tableau-conteneur">
      <table className="tableau">
        <thead>
          <tr>
            {colonnes.map((colonne) => (
              <th key={colonne} className={colonne.numerique ? 'numeral' : undefined}>
                {colonne.titre}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>{children}</tbody>
      </table>
    </div>
  )
}

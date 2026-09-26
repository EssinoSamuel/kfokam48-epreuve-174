/**
 * Composants de base reutilisables (design system).
 *
 * Ils encapsulent uniquement de la presentation : aucune regle metier ici.
 */
export function Bouton({ variante = 'principal', petit = false, charge = false, children, ...props }) {
  const classes = ['bouton', `bouton--${variante}`, petit ? 'bouton--petit' : '']
    .filter(Boolean)
    .join(' ')
  return (
    <button className={classes} disabled={props.disabled || charge} {...props}>
      {charge ? 'En cours…' : children}
    </button>
  )
}

export function Badge({ variante = 'neutre', children }) {
  return <span className={`badge badge--${variante}`}>{children}</span>
}

export function Alerte({ variante = 'info', titre, children }) {
  return (
    <div className={`alerte alerte--${variante}`} role="status" aria-live="polite">
      <div>
        {titre ? <strong>{titre} </strong> : null}
        {children}
      </div>
    </div>
  )
}

export function Chargement({ message = 'Chargement…' }) {
  return (
    <div className="chargement" role="status" aria-live="polite">
      <span className="rotation" aria-hidden="true" />
      <span>{message}</span>
    </div>
  )
}

export function EtatVide({ titre, children }) {
  return (
    <div className="etat-vide">
      {titre ? <div className="etat-vide__titre">{titre}</div> : null}
      {children}
    </div>
  )
}

export function Carte({ titre, badge, description, children, actions }) {
  return (
    <section className="carte">
      {(titre || actions) && (
        <header className="carte__entete">
          {titre ? <h3 className="carte__titre">{titre}</h3> : <span />}
          {actions}
        </header>
      )}
      {description ? <p className="carte__description">{description}</p> : null}
      {badge}
      {children}
    </section>
  )
}

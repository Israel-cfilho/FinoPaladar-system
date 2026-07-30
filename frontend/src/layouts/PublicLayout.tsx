import { Link, NavLink, Outlet } from "react-router-dom"
import { useCart } from "@/hooks/useCart"

function CartIcon({ count }: { count: number }) {
  return (
    <span className="relative inline-flex">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
        <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z" />
        <path d="M3 6h18M16 10a4 4 0 0 1-8 0" />
      </svg>
      {count > 0 && (
        <span className="absolute -right-2 -top-2 flex h-5 min-w-[20px] items-center justify-center rounded-full bg-primary px-1 text-[11px] font-semibold text-primary-foreground">
          {count}
        </span>
      )}
    </span>
  )
}

export function PublicLayout() {
  const { totalItens } = useCart()

  const linkClass = ({ isActive }: { isActive: boolean }) =>
    `text-sm font-medium transition-colors hover:text-primary ${isActive ? "text-primary" : "text-foreground"}`

  return (
    <div className="flex min-h-screen flex-col bg-background">
      <header className="sticky top-0 z-40 border-b border-border bg-surface/90 backdrop-blur">
        <div className="mx-auto flex h-16 w-full max-w-6xl items-center justify-between gap-4 px-4">
          <Link to="/" className="flex items-center gap-2">
            <span className="flex h-9 w-9 items-center justify-center rounded-full bg-primary text-primary-foreground font-serif text-lg font-semibold">
              F
            </span>
            <span className="font-serif text-xl font-semibold text-foreground">Fino Paladar</span>
          </Link>

          <nav className="flex items-center gap-5">
            <NavLink to="/" end className={linkClass}>
              Cardápio
            </NavLink>
            <NavLink to="/acompanhar" className={linkClass}>
              Acompanhar pedido
            </NavLink>
            <Link
              to="/carrinho"
              className="relative text-foreground transition-colors hover:text-primary"
              aria-label={`Carrinho com ${totalItens} itens`}
            >
              <CartIcon count={totalItens} />
            </Link>
          </nav>
        </div>
      </header>

      <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-8">
        <Outlet />
      </main>

      <footer className="border-t border-border bg-surface">
        <div className="mx-auto flex w-full max-w-6xl flex-col items-center justify-between gap-2 px-4 py-6 text-sm text-muted sm:flex-row">
          <span>&copy; {new Date().getFullYear()} Fino Paladar. Todos os direitos reservados.</span>
          <Link to="/admin/login" className="transition-colors hover:text-primary">
            Área administrativa
          </Link>
        </div>
      </footer>
    </div>
  )
}

import { useState } from "react"
import { NavLink, Outlet, useNavigate } from "react-router-dom"
import { useAuth } from "@/hooks/useAuth"
import { Button } from "@/components/ui"

interface NavItem {
  to: string
  label: string
  icon: JSX.Element
}

const navItems: NavItem[] = [
  {
    to: "/admin",
    label: "Dashboard",
    icon: (
      <path d="M3 13h8V3H3zM13 21h8V3h-8zM3 21h8v-6H3z" />
    ),
  },
  {
    to: "/admin/produtos",
    label: "Produtos",
    icon: <path d="M20 7 12 3 4 7l8 4 8-4ZM4 7v10l8 4 8-4V7M12 11v10" />,
  },
  {
    to: "/admin/disponibilidade",
    label: "Disponibilidade",
    icon: <path d="M8 2v4M16 2v4M3 10h18M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2Z" />,
  },
  {
    to: "/admin/pedidos",
    label: "Pedidos",
    icon: <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4ZM3 6h18M16 10a4 4 0 0 1-8 0" />,
  },
  {
    to: "/admin/venda-manual",
    label: "Venda manual",
    icon: <path d="M12 5v14M5 12h14" />,
  },
  {
    to: "/admin/relatorios",
    label: "Relatórios",
    icon: <path d="M3 3v18h18M18 17V9M13 17V5M8 17v-3" />,
  },
]

export function AdminLayout() {
  const { admin, logout } = useAuth()
  const navigate = useNavigate()
  const [mobileOpen, setMobileOpen] = useState(false)

  const handleLogout = () => {
    logout()
    navigate("/admin/login", { replace: true })
  }

  const navClass = ({ isActive }: { isActive: boolean }) =>
    `flex items-center gap-3 rounded px-3 py-2.5 text-sm font-medium transition-colors ${
      isActive ? "bg-primary text-primary-foreground" : "text-foreground/80 hover:bg-background"
    }`

  const sidebar = (
    <nav className="flex flex-1 flex-col gap-1 p-3">
      {navItems.map((item) => (
        <NavLink
          key={item.to}
          to={item.to}
          end={item.to === "/admin"}
          className={navClass}
          onClick={() => setMobileOpen(false)}
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
            {item.icon}
          </svg>
          {item.label}
        </NavLink>
      ))}
    </nav>
  )

  return (
    <div className="flex min-h-screen bg-background">
      {/* Sidebar desktop */}
      <aside className="hidden w-64 flex-col border-r border-border bg-surface lg:flex">
        <div className="flex h-16 items-center gap-2 border-b border-border px-5">
          <span className="flex h-8 w-8 items-center justify-center rounded-full bg-primary text-primary-foreground font-serif font-semibold">
            F
          </span>
          <span className="font-serif text-lg font-semibold">Fino Paladar</span>
        </div>
        {sidebar}
        <div className="border-t border-border p-4">
          <p className="truncate text-sm font-medium text-foreground">{admin?.nome ?? "Administrador"}</p>
          <p className="truncate text-xs text-muted">{admin?.email}</p>
          <Button variant="outline" size="sm" className="mt-3 w-full" onClick={handleLogout}>
            Sair
          </Button>
        </div>
      </aside>

      {/* Mobile drawer */}
      {mobileOpen && (
        <div className="fixed inset-0 z-50 lg:hidden">
          <div className="absolute inset-0 bg-foreground/40" onClick={() => setMobileOpen(false)} />
          <aside className="relative flex h-full w-64 flex-col bg-surface">
            <div className="flex h-16 items-center justify-between border-b border-border px-5">
              <span className="font-serif text-lg font-semibold">Fino Paladar</span>
              <button onClick={() => setMobileOpen(false)} aria-label="Fechar menu">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M18 6 6 18M6 6l12 12" />
                </svg>
              </button>
            </div>
            {sidebar}
            <div className="border-t border-border p-4">
              <Button variant="outline" size="sm" className="w-full" onClick={handleLogout}>
                Sair
              </Button>
            </div>
          </aside>
        </div>
      )}

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="flex h-16 items-center justify-between border-b border-border bg-surface px-4 lg:hidden">
          <button onClick={() => setMobileOpen(true)} aria-label="Abrir menu">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M3 12h18M3 6h18M3 18h18" />
            </svg>
          </button>
          <span className="font-serif text-lg font-semibold">Fino Paladar</span>
          <Button variant="ghost" size="sm" onClick={handleLogout}>
            Sair
          </Button>
        </header>

        <main className="flex-1 p-4 sm:p-6 lg:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

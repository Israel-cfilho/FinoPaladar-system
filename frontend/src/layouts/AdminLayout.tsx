import { Link, NavLink, Outlet } from 'react-router-dom';
import { LOJA_INFO } from '@/services/mockData';

const adminNavItems = [
  { to: '/admin', label: 'Dashboard', end: true },
  { to: '/admin/produtos', label: 'Produtos' },
  { to: '/admin/pedidos', label: 'Pedidos' },
];

export function AdminLayout() {
  return (
    <div className="flex min-h-screen bg-cream-50">
      <aside className="hidden w-64 flex-shrink-0 border-r border-cream-200 bg-cream-100 lg:block">
        <div className="border-b border-cream-200 px-6 py-6">
          <p className="font-serif text-lg font-semibold text-caramel-700">
            {LOJA_INFO.nome}
          </p>
          <p className="text-xs text-ink-700/60">Painel administrativo</p>
        </div>
        <nav className="flex flex-col gap-1 p-4">
          {adminNavItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) =>
                `rounded-lg px-4 py-2.5 text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-cream-200 text-caramel-700'
                    : 'text-ink-700 hover:bg-cream-200 hover:text-caramel-700'
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="flex flex-1 flex-col">
        <header className="border-b border-cream-200 bg-cream-50 px-4 py-4 lg:px-8">
          <div className="flex items-center justify-between">
            <p className="font-serif text-lg text-caramel-700 lg:hidden">
              {LOJA_INFO.nome}
            </p>
            <Link
              to="/"
              className="text-sm text-caramel-600 hover:text-caramel-700"
            >
              Ver loja
            </Link>
          </div>
        </header>
        <main className="flex-1 p-4 lg:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

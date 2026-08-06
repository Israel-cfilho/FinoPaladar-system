import { Link } from 'react-router-dom';
import { LOJA_INFO } from '@/services/mockData';

interface PublicHeaderProps {
  cartItemCount?: number;
}

export function PublicHeader({ cartItemCount = 0 }: PublicHeaderProps) {
  return (
    <header className="sticky top-0 z-50 border-b border-cream-200 bg-cream-50/95 backdrop-blur-sm">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-4 sm:px-6">
        <Link to="/" className="group flex flex-col">
          <span className="font-serif text-xl font-semibold text-caramel-700 transition-colors group-hover:text-caramel-600 sm:text-2xl">
            {LOJA_INFO.nome}
          </span>
          <span className="hidden text-xs text-ink-700/70 sm:block">
            Bolos de rolo artesanais
          </span>
        </Link>

        <nav className="flex items-center gap-4 sm:gap-6">
          <Link
            to="/catalogo"
            className="text-sm font-medium text-ink-700 transition-colors hover:text-caramel-600"
          >
            Catálogo
          </Link>
          <Link
            to="/carrinho"
            className="relative rounded-full bg-caramel-600 px-4 py-2 text-sm font-medium text-cream-50 transition-colors hover:bg-caramel-700"
          >
            Carrinho
            {cartItemCount > 0 && (
              <span className="absolute -right-1 -top-1 flex h-5 w-5 items-center justify-center rounded-full bg-rose-400 text-xs font-bold text-white">
                {cartItemCount}
              </span>
            )}
          </Link>
        </nav>
      </div>
    </header>
  );
}

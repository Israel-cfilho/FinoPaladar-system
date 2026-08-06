import { Link } from 'react-router-dom';
import { LOJA_INFO } from '@/services/mockData';

export function PublicFooter() {
  return (
    <footer className="mt-auto border-t border-cream-200 bg-cream-100">
      <div className="mx-auto max-w-6xl px-4 py-10 sm:px-6">
        <div className="flex flex-col gap-6 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="font-serif text-lg font-semibold text-caramel-700">
              {LOJA_INFO.nome}
            </p>
            <p className="mt-1 max-w-md text-sm text-ink-700/80">
              {LOJA_INFO.slogan}
            </p>
          </div>
          <div className="text-sm text-ink-700/70">
            <p>
              {LOJA_INFO.cidade}, {LOJA_INFO.estado}
            </p>
            <Link
              to="/admin/login"
              className="mt-2 inline-block text-caramel-600 hover:text-caramel-700"
            >
              Área administrativa
            </Link>
          </div>
        </div>
        <p className="mt-8 text-center text-xs text-ink-700/50">
          © {new Date().getFullYear()} {LOJA_INFO.nome}. Todos os direitos reservados.
        </p>
      </div>
    </footer>
  );
}

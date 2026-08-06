import type { ProdutoPublico } from '@/types';
import { formatCurrency, formatWeight } from '@/utils/format';

interface ProdutoCardProps {
  produto: ProdutoPublico;
}

export function ProdutoCard({ produto }: ProdutoCardProps) {
  const indisponivel = produto.quantidadeDisponivel === 0;

  return (
    <article className="group overflow-hidden rounded-2xl border border-cream-200 bg-white shadow-sm transition-shadow hover:shadow-md">
      <div className="relative aspect-[4/3] overflow-hidden bg-cream-100">
        <div className="flex h-full items-center justify-center bg-gradient-to-br from-cream-100 to-cream-200">
          <span className="font-serif text-4xl text-caramel-400/50">🍰</span>
        </div>
        {indisponivel && (
          <span className="absolute left-3 top-3 rounded-full bg-ink-800/80 px-3 py-1 text-xs font-medium text-cream-50">
            Indisponível
          </span>
        )}
      </div>

      <div className="p-4 sm:p-5">
        <h3 className="font-serif text-lg font-semibold text-ink-900">
          {produto.nome}
        </h3>
        <p className="mt-1 text-sm text-ink-700/70">
          Peso médio: {formatWeight(produto.pesoMedioGramas)}
        </p>
        <div className="mt-4 flex items-center justify-between">
          <span className="text-lg font-semibold text-caramel-700">
            {formatCurrency(produto.preco)}
          </span>
          {!indisponivel && (
            <span className="text-xs text-sage-500">
              {produto.quantidadeDisponivel} disponíveis
            </span>
          )}
        </div>
      </div>
    </article>
  );
}

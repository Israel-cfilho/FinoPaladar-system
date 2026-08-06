import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ProdutoCard } from '@/components/ProdutoCard';
import { Button } from '@/components/ui/Button';
import { listarProdutosPublicos } from '@/services/produtoService';
import { LOJA_INFO } from '@/services/mockData';
import type { ProdutoPublico } from '@/types';

export function HomePage() {
  const [produtos, setProdutos] = useState<ProdutoPublico[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    listarProdutosPublicos()
      .then(setProdutos)
      .finally(() => setLoading(false));
  }, []);

  const destaques = produtos.slice(0, 3);

  return (
    <>
      <section className="relative overflow-hidden bg-gradient-to-b from-cream-100 to-cream-50">
        <div className="mx-auto max-w-6xl px-4 py-16 sm:px-6 sm:py-24">
          <div className="max-w-2xl">
            <p className="mb-3 text-sm font-medium uppercase tracking-wider text-caramel-500">
              Artesanal · Familiar · Feito com carinho
            </p>
            <h1 className="font-serif text-4xl font-semibold leading-tight text-ink-900 sm:text-5xl">
              Bolos de rolo que trazem o sabor de casa
            </h1>
            <p className="mt-5 text-lg leading-relaxed text-ink-700/80">
              {LOJA_INFO.slogan} Peça online e finalize seu pedido pelo WhatsApp.
            </p>
            <div className="mt-8 flex flex-col gap-3 sm:flex-row">
              <Button to="/catalogo">Ver catálogo</Button>
              <Button to="/catalogo" variant="outline">
                Fazer pedido
              </Button>
            </div>
          </div>
        </div>
        <div
          className="pointer-events-none absolute -right-20 -top-20 h-64 w-64 rounded-full bg-rose-100/60 blur-3xl"
          aria-hidden
        />
      </section>

      <section className="mx-auto max-w-6xl px-4 py-16 sm:px-6">
        <div className="mb-10 flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h2 className="font-serif text-2xl font-semibold text-ink-900 sm:text-3xl">
              Nossos destaques
            </h2>
            <p className="mt-2 text-ink-700/70">
              Sabores tradicionais preparados diariamente.
            </p>
          </div>
          <Link
            to="/catalogo"
            className="text-sm font-medium text-caramel-600 hover:text-caramel-700"
          >
            Ver todos →
          </Link>
        </div>

        {loading ? (
          <p className="text-center text-ink-700/60">Carregando produtos...</p>
        ) : (
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {destaques.map((produto) => (
              <ProdutoCard key={produto.id} produto={produto} />
            ))}
          </div>
        )}
      </section>

      <section className="border-t border-cream-200 bg-cream-100">
        <div className="mx-auto max-w-6xl px-4 py-16 sm:px-6">
          <div className="grid gap-8 sm:grid-cols-3">
            <div className="text-center sm:text-left">
              <span className="text-2xl">🌾</span>
              <h3 className="mt-3 font-serif text-lg font-semibold text-ink-900">
                Ingredientes selecionados
              </h3>
              <p className="mt-2 text-sm text-ink-700/70">
                Receitas de família com ingredientes de qualidade.
              </p>
            </div>
            <div className="text-center sm:text-left">
              <span className="text-2xl">👩‍🍳</span>
              <h3 className="mt-3 font-serif text-lg font-semibold text-ink-900">
                Produção artesanal
              </h3>
              <p className="mt-2 text-sm text-ink-700/70">
                Cada bolo é enrolado à mão, com todo o cuidado.
              </p>
            </div>
            <div className="text-center sm:text-left">
              <span className="text-2xl">📱</span>
              <h3 className="mt-3 font-serif text-lg font-semibold text-ink-900">
                Pedido fácil
              </h3>
              <p className="mt-2 text-sm text-ink-700/70">
                Monte seu carrinho e confirme pelo WhatsApp.
              </p>
            </div>
          </div>
        </div>
      </section>
    </>
  );
}

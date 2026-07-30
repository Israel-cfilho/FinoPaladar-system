import { useCallback, useEffect, useMemo, useState } from "react"
import { extractErrorMessage } from "@/services/api"
import { produtoService } from "@/services/produtoService"
import { useCart } from "@/hooks/useCart"
import { ProductCard } from "@/components/ProductCard"
import { EmptyState, ErrorMessage, Input, Loading } from "@/components/ui"
import type { ProdutoPublicoResponse } from "@/types"

export function HomePage() {
  const { addItem, items } = useCart()
  const [produtos, setProdutos] = useState<ProdutoPublicoResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [busca, setBusca] = useState("")

  const carregar = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await produtoService.listarPublicos()
      setProdutos(data)
    } catch (err) {
      setError(extractErrorMessage(err, "Não foi possível carregar o cardápio."))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void carregar()
  }, [carregar])

  const filtrados = useMemo(() => {
    const termo = busca.trim().toLowerCase()
    if (!termo) return produtos
    return produtos.filter((p) => p.nome.toLowerCase().includes(termo))
  }, [produtos, busca])

  const quantidadeNoCarrinho = useCallback(
    (produtoId: number) => items.find((i) => i.produtoId === produtoId)?.quantidade ?? 0,
    [items],
  )

  return (
    <div className="flex flex-col gap-8">
      <section className="rounded-lg border border-border bg-surface px-6 py-8 sm:px-10 sm:py-12">
        <p className="text-sm font-medium uppercase tracking-wide text-primary">Fino Paladar</p>
        <h1 className="mt-2 max-w-2xl font-serif text-3xl font-semibold text-foreground text-balance sm:text-4xl">
          Sabores artesanais, feitos com carinho para você
        </h1>
        <p className="mt-3 max-w-xl text-muted leading-relaxed">
          Escolha seus produtos favoritos, monte seu pedido e receba em casa ou retire no balcão. Simples,
          rápido e delicioso.
        </p>
      </section>

      <section className="flex flex-col gap-5">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <h2 className="font-serif text-2xl font-semibold text-foreground">Cardápio</h2>
          <div className="w-full sm:max-w-xs">
            <Input
              type="search"
              placeholder="Buscar produto..."
              value={busca}
              onChange={(e) => setBusca(e.target.value)}
              aria-label="Buscar produto"
            />
          </div>
        </div>

        {loading ? (
          <Loading label="Carregando cardápio..." />
        ) : error ? (
          <ErrorMessage message={error} onRetry={() => void carregar()} />
        ) : filtrados.length === 0 ? (
          <EmptyState
            title={busca ? "Nenhum produto encontrado" : "Cardápio indisponível"}
            description={
              busca
                ? "Tente buscar por outro nome de produto."
                : "No momento não há produtos disponíveis. Volte em breve!"
            }
          />
        ) : (
          <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {filtrados.map((produto) => (
              <ProductCard
                key={produto.id}
                produto={produto}
                onAdd={addItem}
                jaNoCarrinho={quantidadeNoCarrinho(produto.id)}
              />
            ))}
          </div>
        )}
      </section>
    </div>
  )
}

import { Link, useNavigate } from "react-router-dom"
import { useCart } from "@/hooks/useCart"
import { resolveImageUrl } from "@/services/api"
import { Button, Card, EmptyState } from "@/components/ui"
import { formatCurrency, formatWeight } from "@/utils/format"

export function CartPage() {
  const { items, subtotalEstimado, updateQuantidade, removeItem, clear } = useCart()
  const navigate = useNavigate()

  if (items.length === 0) {
    return (
      <div className="mx-auto max-w-2xl">
        <h1 className="mb-6 font-serif text-2xl font-semibold text-foreground">Seu carrinho</h1>
        <EmptyState
          title="Seu carrinho está vazio"
          description="Adicione produtos do cardápio para começar seu pedido."
          action={
            <Button onClick={() => navigate("/")}>Ver cardápio</Button>
          }
        />
      </div>
    )
  }

  return (
    <div className="mx-auto flex max-w-4xl flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="font-serif text-2xl font-semibold text-foreground">Seu carrinho</h1>
        <button
          onClick={clear}
          className="text-sm font-medium text-muted transition-colors hover:text-danger"
        >
          Limpar carrinho
        </button>
      </div>

      <div className="flex flex-col gap-4">
        {items.map((item) => {
          const imageUrl = resolveImageUrl(item.imagem)
          return (
            <Card key={item.produtoId} className="flex gap-4 p-4">
              <div className="h-20 w-20 flex-shrink-0 overflow-hidden rounded bg-background">
                {imageUrl ? (
                  <img src={imageUrl || "/placeholder.svg"} alt={item.nome} className="h-full w-full object-cover" />
                ) : (
                  <div className="flex h-full w-full items-center justify-center text-muted">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.2">
                      <rect x="3" y="3" width="18" height="18" rx="2" />
                      <circle cx="9" cy="9" r="2" />
                      <path d="m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21" />
                    </svg>
                  </div>
                )}
              </div>

              <div className="flex min-w-0 flex-1 flex-col justify-between gap-2">
                <div className="flex items-start justify-between gap-2">
                  <div className="min-w-0">
                    <h3 className="truncate font-medium text-foreground">{item.nome}</h3>
                    <p className="text-sm text-muted">
                      {formatCurrency(item.preco)} &middot; {formatWeight(item.pesoMedioGramas)}
                    </p>
                  </div>
                  <button
                    onClick={() => removeItem(item.produtoId)}
                    className="text-muted transition-colors hover:text-danger"
                    aria-label={`Remover ${item.nome}`}
                  >
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
                      <path d="M3 6h18M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2m3 0v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6" />
                    </svg>
                  </button>
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center rounded border border-border">
                    <button
                      onClick={() => updateQuantidade(item.produtoId, item.quantidade - 1)}
                      disabled={item.quantidade <= 1}
                      className="flex h-9 w-9 items-center justify-center text-foreground disabled:opacity-40"
                      aria-label="Diminuir quantidade"
                    >
                      &minus;
                    </button>
                    <span className="w-10 text-center text-sm font-medium">{item.quantidade}</span>
                    <button
                      onClick={() => updateQuantidade(item.produtoId, item.quantidade + 1)}
                      disabled={item.quantidade >= item.quantidadeDisponivel}
                      className="flex h-9 w-9 items-center justify-center text-foreground disabled:opacity-40"
                      aria-label="Aumentar quantidade"
                    >
                      +
                    </button>
                  </div>
                  <span className="font-semibold text-foreground">
                    {formatCurrency(item.preco * item.quantidade)}
                  </span>
                </div>
              </div>
            </Card>
          )
        })}
      </div>

      <Card className="p-5">
        <div className="flex items-center justify-between">
          <span className="text-muted">Subtotal estimado</span>
          <span className="text-lg font-semibold text-foreground">{formatCurrency(subtotalEstimado)}</span>
        </div>
        <p className="mt-1 text-xs text-muted">
          Os valores finais, incluindo taxa de entrega, serão calculados e confirmados na finalização do pedido.
        </p>
        <div className="mt-4 flex flex-col gap-3 sm:flex-row">
          <Button variant="outline" className="sm:flex-1" onClick={() => navigate("/")}>
            Continuar comprando
          </Button>
          <Button className="sm:flex-1" onClick={() => navigate("/checkout")}>
            Finalizar pedido
          </Button>
        </div>
      </Card>

      <p className="text-center text-sm text-muted">
        Quer acompanhar um pedido existente?{" "}
        <Link to="/acompanhar" className="font-medium text-primary hover:underline">
          Clique aqui
        </Link>
      </p>
    </div>
  )
}

import { Button, Card } from "@/components/ui"
import { resolveImageUrl } from "@/services/api"
import type { ProdutoPublicoResponse } from "@/types"
import { formatCurrency, formatWeight } from "@/utils/format"

interface ProductCardProps {
  produto: ProdutoPublicoResponse
  onAdd: (produto: ProdutoPublicoResponse) => void
  jaNoCarrinho?: number
}

export function ProductCard({ produto, onAdd, jaNoCarrinho = 0 }: ProductCardProps) {
  const imageUrl = resolveImageUrl(produto.imagem)
  const esgotado = produto.quantidadeDisponivel <= 0
  const atingiuLimite = jaNoCarrinho >= produto.quantidadeDisponivel

  return (
    <Card className="flex flex-col overflow-hidden">
      <div className="relative aspect-[4/3] w-full overflow-hidden bg-background">
        {imageUrl ? (
          <img
            src={imageUrl || "/placeholder.svg"}
            alt={produto.nome}
            className="h-full w-full object-cover"
            loading="lazy"
          />
        ) : (
          <div className="flex h-full w-full items-center justify-center text-muted">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.2">
              <rect x="3" y="3" width="18" height="18" rx="2" />
              <circle cx="9" cy="9" r="2" />
              <path d="m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21" />
            </svg>
          </div>
        )}
        {esgotado && (
          <div className="absolute inset-0 flex items-center justify-center bg-foreground/50">
            <span className="rounded-full bg-surface px-3 py-1 text-sm font-semibold text-foreground">
              Esgotado
            </span>
          </div>
        )}
      </div>

      <div className="flex flex-1 flex-col gap-3 p-4">
        <div className="flex-1">
          <h3 className="font-serif text-lg font-semibold leading-tight text-foreground text-pretty">
            {produto.nome}
          </h3>
          <p className="mt-1 text-sm text-muted">Peso médio: {formatWeight(produto.pesoMedioGramas)}</p>
        </div>

        <div className="flex items-end justify-between gap-2">
          <div>
            <p className="text-xl font-semibold text-foreground">{formatCurrency(produto.preco)}</p>
            {!esgotado && (
              <p className="text-xs text-muted">{produto.quantidadeDisponivel} disponíveis</p>
            )}
          </div>
          <Button
            size="sm"
            onClick={() => onAdd(produto)}
            disabled={esgotado || atingiuLimite}
          >
            {esgotado ? "Esgotado" : atingiuLimite ? "No carrinho" : "Adicionar"}
          </Button>
        </div>
      </div>
    </Card>
  )
}

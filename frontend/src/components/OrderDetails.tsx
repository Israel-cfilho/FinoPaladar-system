import { Card } from "@/components/ui"
import { StatusBadge } from "@/components/StatusBadge"
import type { PedidoEnderecoResponse, PedidoResponse } from "@/types"
import {
  canalVendaLabels,
  cidadeLabels,
  formaPagamentoLabels,
  formatCurrency,
  formatDate,
  formatWeight,
  tipoRecebimentoLabels,
} from "@/utils/format"

function EnderecoBlock({ endereco }: { endereco: PedidoEnderecoResponse }) {
  const linhas: string[] = []
  if (endereco.tipoEndereco === "CONDOMINIO") {
    if (endereco.condominio) linhas.push(`Condomínio ${endereco.condominio}`)
    const ql = [endereco.quadra && `Quadra ${endereco.quadra}`, endereco.lote && `Lote ${endereco.lote}`]
      .filter(Boolean)
      .join(" · ")
    if (ql) linhas.push(ql)
  } else {
    const rua = [endereco.rua, endereco.numero].filter(Boolean).join(", ")
    if (rua) linhas.push(rua)
    if (endereco.bairro) linhas.push(endereco.bairro)
    if (endereco.complemento) linhas.push(endereco.complemento)
    if (endereco.pontoReferencia) linhas.push(`Ref.: ${endereco.pontoReferencia}`)
  }
  if (endereco.cidade) linhas.push(cidadeLabels[endereco.cidade])

  return (
    <div className="text-sm text-foreground">
      {linhas.length > 0 ? (
        linhas.map((linha, i) => <p key={i}>{linha}</p>)
      ) : (
        <p className="text-muted">Endereço não informado</p>
      )}
    </div>
  )
}

function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex flex-col">
      <span className="text-xs uppercase tracking-wide text-muted">{label}</span>
      <span className="text-sm font-medium text-foreground">{value}</span>
    </div>
  )
}

export function OrderDetails({ pedido }: { pedido: PedidoResponse }) {
  return (
    <div className="flex flex-col gap-5">
      <Card className="flex flex-col gap-4 p-5">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <p className="text-xs uppercase tracking-wide text-muted">Pedido</p>
            <p className="font-serif text-xl font-semibold text-foreground">{pedido.codigo}</p>
          </div>
          <StatusBadge status={pedido.status} />
        </div>

        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
          <InfoRow label="Cliente" value={pedido.cliente} />
          <InfoRow label="Telefone" value={pedido.telefone} />
          <InfoRow label="Data" value={formatDate(pedido.data)} />
          <InfoRow label="Pagamento" value={formaPagamentoLabels[pedido.formaPagamento]} />
          <InfoRow label="Recebimento" value={tipoRecebimentoLabels[pedido.tipoRecebimento]} />
          {pedido.canalVenda && <InfoRow label="Canal" value={canalVendaLabels[pedido.canalVenda]} />}
        </div>

        {pedido.observacao && (
          <div className="rounded border border-border bg-background p-3 text-sm">
            <span className="font-medium text-foreground">Observação: </span>
            <span className="text-muted">{pedido.observacao}</span>
          </div>
        )}
      </Card>

      <Card className="flex flex-col gap-3 p-5">
        <h3 className="font-serif text-lg font-semibold text-foreground">Itens</h3>
        <ul className="flex flex-col divide-y divide-border">
          {pedido.itens.map((item) => (
            <li key={item.id} className="flex items-center justify-between gap-3 py-2.5">
              <div className="min-w-0">
                <p className="truncate font-medium text-foreground">
                  {item.quantidade}x {item.nomeProduto}
                </p>
                <p className="text-xs text-muted">
                  {formatCurrency(item.precoUnitario)} · {formatWeight(item.pesoMedioGramas)}
                </p>
              </div>
              <span className="font-medium text-foreground">{formatCurrency(item.subtotal)}</span>
            </li>
          ))}
        </ul>

        <div className="flex flex-col gap-1 border-t border-border pt-3 text-sm">
          <div className="flex justify-between">
            <span className="text-muted">Valor dos produtos</span>
            <span className="text-foreground">{formatCurrency(pedido.valorProdutos)}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted">Taxa de entrega</span>
            <span className="text-foreground">{formatCurrency(pedido.taxaEntrega)}</span>
          </div>
          <div className="flex justify-between border-t border-border pt-2 text-base font-semibold">
            <span className="text-foreground">Total</span>
            <span className="text-foreground">{formatCurrency(pedido.valorTotal)}</span>
          </div>
        </div>
      </Card>

      {pedido.enderecoEntrega && (
        <Card className="flex flex-col gap-2 p-5">
          <h3 className="font-serif text-lg font-semibold text-foreground">Endereço de entrega</h3>
          <EnderecoBlock endereco={pedido.enderecoEntrega} />
        </Card>
      )}
    </div>
  )
}

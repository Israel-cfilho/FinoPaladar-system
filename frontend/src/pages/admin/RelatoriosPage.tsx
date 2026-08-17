import { useCallback, useEffect, useState, type FormEvent } from "react"
import { relatorioService } from "@/services/relatorioService"
import { extractErrorMessage } from "@/services/api"
import { Button, Card, ErrorMessage, Field, Input, Loading } from "@/components/ui"
import { cidadeLabels, formatCurrency, formatDate } from "@/utils/format"
import type {
  CidadeMaisPedidosResponse,
  ProdutoVendidoResponse,
  RelatorioFaturamentoResponse,
  TicketMedioResponse,
} from "@/types"

function firstDayOfMonth(): string {
  const now = new Date()
  return new Date(now.getFullYear(), now.getMonth(), 1).toISOString().slice(0, 10)
}

function today(): string {
  return new Date().toISOString().slice(0, 10)
}

export function RelatoriosPage() {
  const [dataInicial, setDataInicial] = useState(firstDayOfMonth())
  const [dataFinal, setDataFinal] = useState(today())

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [faturamento, setFaturamento] = useState<RelatorioFaturamentoResponse | null>(null)
  const [ticket, setTicket] = useState<TicketMedioResponse | null>(null)
  const [produtos, setProdutos] = useState<ProdutoVendidoResponse[]>([])
  const [cidades, setCidades] = useState<CidadeMaisPedidosResponse[]>([])

  const carregar = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const [fat, tk, prods, cids] = await Promise.all([
        relatorioService.faturamentoPeriodo(dataInicial, dataFinal),
        relatorioService.ticketMedio(dataInicial, dataFinal),
        relatorioService.produtosMaisVendidos(dataInicial, dataFinal, 10),
        relatorioService.cidadeMaisPedidos(dataInicial, dataFinal),
      ])
      setFaturamento(fat)
      setTicket(tk)
      setProdutos(prods)
      setCidades(cids)
    } catch (err) {
      setError(extractErrorMessage(err, "Não foi possível carregar os relatórios."))
    } finally {
      setLoading(false)
    }
  }, [dataInicial, dataFinal])

  useEffect(() => {
    carregar()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    carregar()
  }

  const maxProdutoQtd = produtos.reduce((max, p) => Math.max(max, p.quantidadeVendida), 0)
  const maxCidadeQtd = cidades.reduce((max, c) => Math.max(max, c.quantidadePedidos), 0)

  return (
    <div className="mx-auto max-w-5xl">
      <header className="mb-6">
        <h1 className="font-serif text-2xl font-semibold text-foreground">Relatórios</h1>
        <p className="text-sm text-muted">Analise o desempenho de vendas por período.</p>
      </header>

      <Card className="mb-6 p-5">
        <form onSubmit={handleSubmit} className="flex flex-wrap items-end gap-4">
          <Field label="Data inicial" htmlFor="dataInicial" className="min-w-40 flex-1">
            <Input id="dataInicial" type="date" value={dataInicial} onChange={(e) => setDataInicial(e.target.value)} />
          </Field>
          <Field label="Data final" htmlFor="dataFinal" className="min-w-40 flex-1">
            <Input id="dataFinal" type="date" value={dataFinal} onChange={(e) => setDataFinal(e.target.value)} />
          </Field>
          <Button type="submit" loading={loading}>
            Gerar relatório
          </Button>
        </form>
      </Card>

      {error && (
        <div className="mb-6">
          <ErrorMessage message={error} />
        </div>
      )}

      {loading ? (
        <Loading label="Gerando relatórios..." />
      ) : (
        <div className="flex flex-col gap-6">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <Card className="p-5">
              <p className="text-sm text-muted">Faturamento no período</p>
              <p className="mt-1 font-serif text-3xl font-semibold text-foreground">
                {formatCurrency(faturamento?.valorTotal ?? 0)}
              </p>
              {faturamento && (
                <p className="mt-1 text-xs text-muted">
                  {formatDate(faturamento.dataInicial)} — {formatDate(faturamento.dataFinal)}
                </p>
              )}
            </Card>
            <Card className="p-5">
              <p className="text-sm text-muted">Ticket médio</p>
              <p className="mt-1 font-serif text-3xl font-semibold text-foreground">
                {formatCurrency(ticket?.valor ?? 0)}
              </p>
              <p className="mt-1 text-xs text-muted">Valor médio por pedido</p>
            </Card>
          </div>

          <Card className="p-5">
            <h2 className="mb-4 font-serif text-lg font-semibold text-foreground">Produtos mais vendidos</h2>
            {produtos.length === 0 ? (
              <p className="text-sm text-muted">Nenhuma venda no período selecionado.</p>
            ) : (
              <ul className="flex flex-col gap-3">
                {produtos.map((p) => (
                  <li key={p.produtoId} className="flex flex-col gap-1">
                    <div className="flex items-baseline justify-between gap-3 text-sm">
                      <span className="font-medium text-foreground">{p.nomeProduto}</span>
                      <span className="text-muted">
                        {p.quantidadeVendida} un · {formatCurrency(p.valorVendido)}
                      </span>
                    </div>
                    <div className="h-2 overflow-hidden rounded-full bg-border">
                      <div
                        className="h-full rounded-full bg-primary"
                        style={{ width: maxProdutoQtd ? `${(p.quantidadeVendida / maxProdutoQtd) * 100}%` : "0%" }}
                      />
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </Card>

          <Card className="p-5">
            <h2 className="mb-4 font-serif text-lg font-semibold text-foreground">Pedidos por cidade</h2>
            {cidades.length === 0 ? (
              <p className="text-sm text-muted">Nenhum pedido no período selecionado.</p>
            ) : (
              <ul className="flex flex-col gap-3">
                {cidades.map((c) => (
                  <li key={c.cidade} className="flex flex-col gap-1">
                    <div className="flex items-baseline justify-between gap-3 text-sm">
                      <span className="font-medium text-foreground">{cidadeLabels[c.cidade]}</span>
                      <span className="text-muted">{c.quantidadePedidos} pedidos</span>
                    </div>
                    <div className="h-2 overflow-hidden rounded-full bg-border">
                      <div
                        className="h-full rounded-full bg-accent"
                        style={{ width: maxCidadeQtd ? `${(c.quantidadePedidos / maxCidadeQtd) * 100}%` : "0%" }}
                      />
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </Card>
        </div>
      )}
    </div>
  )
}

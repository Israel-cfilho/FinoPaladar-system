import { useCallback, useEffect, useState, type FormEvent } from "react"
import { useNavigate, useParams } from "react-router-dom"
import { pedidoService } from "@/services/pedidoService"
import { extractErrorMessage } from "@/services/api"
import { OrderDetails } from "@/components/OrderDetails"
import { StatusBadge } from "@/components/StatusBadge"
import { Button, Card, ErrorMessage, Field, Input, Loading } from "@/components/ui"
import type { HistoricoStatusResponse, PedidoResponse } from "@/types"
import { formatDate } from "@/utils/format"

export function TrackOrderPage() {
  const { codigo: codigoParam } = useParams<{ codigo: string }>()
  const navigate = useNavigate()

  const [codigo, setCodigo] = useState(codigoParam ?? "")
  const [pedido, setPedido] = useState<PedidoResponse | null>(null)
  const [historico, setHistorico] = useState<HistoricoStatusResponse[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const buscar = useCallback(async (cod: string) => {
    const codigoLimpo = cod.trim()
    if (!codigoLimpo) return
    setLoading(true)
    setError(null)
    setPedido(null)
    setHistorico([])
    try {
      const [pedidoData, historicoData] = await Promise.all([
        pedidoService.buscarPorCodigo(codigoLimpo),
        pedidoService.listarHistorico(codigoLimpo).catch(() => [] as HistoricoStatusResponse[]),
      ])
      setPedido(pedidoData)
      setHistorico(historicoData)
    } catch (err) {
      setError(extractErrorMessage(err, "Pedido não encontrado. Verifique o código e tente novamente."))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    if (codigoParam) void buscar(codigoParam)
  }, [codigoParam, buscar])

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    navigate(`/acompanhar/${codigo.trim()}`)
    void buscar(codigo)
  }

  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-6">
      <div>
        <h1 className="font-serif text-2xl font-semibold text-foreground">Acompanhar pedido</h1>
        <p className="mt-1 text-muted">Digite o código do seu pedido para ver o status atual.</p>
      </div>

      <Card className="p-5">
        <form onSubmit={handleSubmit} className="flex flex-col gap-3 sm:flex-row sm:items-end">
          <Field label="Código do pedido" htmlFor="codigo" className="flex-1">
            <Input
              id="codigo"
              value={codigo}
              onChange={(e) => setCodigo(e.target.value)}
              placeholder="Ex.: PED-2024-0001"
              required
            />
          </Field>
          <Button type="submit" loading={loading} className="sm:w-auto">
            Buscar
          </Button>
        </form>
      </Card>

      {loading && <Loading label="Buscando pedido..." />}
      {error && <ErrorMessage message={error} />}

      {pedido && !loading && (
        <>
          <OrderDetails pedido={pedido} />

          {historico.length > 0 && (
            <Card className="flex flex-col gap-4 p-5">
              <h3 className="font-serif text-lg font-semibold text-foreground">Histórico do pedido</h3>
              <ol className="flex flex-col gap-4">
                {historico.map((evento) => (
                  <li key={evento.id} className="flex gap-3">
                    <div className="mt-1 flex flex-col items-center">
                      <span className="h-2.5 w-2.5 rounded-full bg-primary" />
                    </div>
                    <div className="flex flex-1 flex-col gap-1">
                      <StatusBadge status={evento.status} />
                      <span className="text-xs text-muted">{formatDate(evento.dataHora)}</span>
                    </div>
                  </li>
                ))}
              </ol>
            </Card>
          )}
        </>
      )}
    </div>
  )
}

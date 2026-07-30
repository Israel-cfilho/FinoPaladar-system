import { useState, type FormEvent } from "react"
import { pedidoService } from "@/services/pedidoService"
import { StatusPedido, type PedidoResponse } from "@/types"
import { Button, Card, Field, Input, Select, Loading, ErrorMessage } from "@/components/ui"
import { statusLabels } from "@/utils/format"
import { StatusBadge } from "@/components/StatusBadge"
import { OrderDetails } from "@/components/OrderDetails"

const statusOptions = Object.values(StatusPedido)

export default function PedidosPage() {
  const [codigo, setCodigo] = useState("")
  const [pedido, setPedido] = useState<PedidoResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [novoStatus, setNovoStatus] = useState<StatusPedido>(StatusPedido.ACEITO)
  const [updating, setUpdating] = useState(false)

  async function handleSearch(e: FormEvent) {
    e.preventDefault()
    const code = codigo.trim()
    if (!code) return
    setLoading(true)
    setError(null)
    setPedido(null)
    try {
      const result = await pedidoService.buscarPorCodigo(code)
      setPedido(result)
      setNovoStatus(result.status)
    } catch {
      setError("Pedido não encontrado. Verifique o código informado.")
    } finally {
      setLoading(false)
    }
  }

  async function handleUpdateStatus() {
    if (!pedido) return
    setUpdating(true)
    try {
      const updated = await pedidoService.alterarStatus(pedido.id, novoStatus)
      setPedido(updated)
      setNovoStatus(updated.status)
    } catch (err) {
      const message =
        (err as { response?: { data?: { mensagem?: string } } })?.response?.data?.mensagem ??
        "Não foi possível atualizar o status."
      window.alert(message)
    } finally {
      setUpdating(false)
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="font-serif text-2xl font-bold text-foreground">Pedidos</h1>
        <p className="text-sm text-muted">Busque um pedido pelo código para acompanhar e atualizar o status.</p>
      </div>

      <Card className="p-5">
        <form onSubmit={handleSearch} className="flex flex-col gap-4 sm:flex-row sm:items-end">
          <Field label="Código do pedido" htmlFor="codigo" className="flex-1">
            <Input
              id="codigo"
              placeholder="Ex.: FP-2026-0001"
              value={codigo}
              onChange={(e) => setCodigo(e.target.value)}
            />
          </Field>
          <Button type="submit" loading={loading}>
            Buscar
          </Button>
        </form>
      </Card>

      {loading && <Loading />}
      {error && <ErrorMessage message={error} />}

      {pedido && (
        <div className="flex flex-col gap-6">
          <Card className="flex flex-col gap-4 p-5">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <p className="text-sm text-muted">Status atual</p>
                <div className="mt-1">
                  <StatusBadge status={pedido.status} />
                </div>
              </div>
            </div>
            <div className="flex flex-col gap-3 border-t border-border pt-4 sm:flex-row sm:items-end">
              <Field label="Novo status" htmlFor="novoStatus" className="flex-1">
                <Select
                  id="novoStatus"
                  value={novoStatus}
                  onChange={(e) => setNovoStatus(e.target.value as StatusPedido)}
                >
                  {statusOptions.map((s) => (
                    <option key={s} value={s}>
                      {statusLabels[s]}
                    </option>
                  ))}
                </Select>
              </Field>
              <Button
                onClick={handleUpdateStatus}
                loading={updating}
                disabled={novoStatus === pedido.status}
              >
                Atualizar status
              </Button>
            </div>
          </Card>

          <OrderDetails pedido={pedido} />
        </div>
      )}
    </div>
  )
}

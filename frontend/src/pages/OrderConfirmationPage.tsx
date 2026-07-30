import { useCallback, useEffect, useState } from "react"
import { Link, useLocation, useParams } from "react-router-dom"
import { pedidoService } from "@/services/pedidoService"
import { extractErrorMessage } from "@/services/api"
import { OrderDetails } from "@/components/OrderDetails"
import { Button, Card, ErrorMessage, Loading } from "@/components/ui"
import type { PedidoResponse } from "@/types"

export function OrderConfirmationPage() {
  const { codigo } = useParams<{ codigo: string }>()
  const location = useLocation()
  const pedidoFromState = (location.state as { pedido?: PedidoResponse } | null)?.pedido ?? null

  const [pedido, setPedido] = useState<PedidoResponse | null>(pedidoFromState)
  const [loading, setLoading] = useState(!pedidoFromState)
  const [error, setError] = useState<string | null>(null)

  const carregar = useCallback(async () => {
    if (!codigo) return
    setLoading(true)
    setError(null)
    try {
      const data = await pedidoService.buscarPorCodigo(codigo)
      setPedido(data)
    } catch (err) {
      setError(extractErrorMessage(err, "Não foi possível carregar o pedido."))
    } finally {
      setLoading(false)
    }
  }, [codigo])

  useEffect(() => {
    if (!pedidoFromState) void carregar()
  }, [pedidoFromState, carregar])

  if (loading) return <Loading label="Carregando pedido..." />
  if (error) return <ErrorMessage message={error} onRetry={() => void carregar()} />
  if (!pedido) return null

  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-6">
      <Card className="flex flex-col items-center gap-3 p-6 text-center">
        <span className="flex h-14 w-14 items-center justify-center rounded-full bg-accent/10 text-accent">
          <svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M20 6 9 17l-5-5" />
          </svg>
        </span>
        <h1 className="font-serif text-2xl font-semibold text-foreground">Pedido recebido!</h1>
        <p className="text-muted">
          Seu pedido <strong className="text-foreground">{pedido.codigo}</strong> foi registrado com sucesso.
        </p>
        {pedido.mensagem && (
          <p className="rounded border border-border bg-background px-4 py-3 text-sm text-foreground">
            {pedido.mensagem}
          </p>
        )}
        {pedido.linkWhatsApp && (
          <a href={pedido.linkWhatsApp} target="_blank" rel="noopener noreferrer" className="w-full sm:w-auto">
            <Button size="lg" className="w-full bg-accent hover:opacity-90">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12.04 2c-5.46 0-9.91 4.45-9.91 9.91 0 1.75.46 3.45 1.32 4.95L2 22l5.25-1.38a9.9 9.9 0 0 0 4.79 1.22h.01c5.46 0 9.91-4.45 9.91-9.91 0-2.65-1.03-5.14-2.9-7.01A9.82 9.82 0 0 0 12.04 2Zm5.8 14.16c-.24.68-1.42 1.32-1.95 1.37-.53.05-1.02.24-3.42-.71-2.9-1.14-4.73-4.11-4.87-4.3-.14-.19-1.16-1.54-1.16-2.94s.73-2.08 1-2.37c.26-.29.56-.36.75-.36.19 0 .38.01.54.02.17.01.4-.07.63.48.24.56.82 1.94.89 2.08.07.14.12.31.02.5-.09.19-.14.31-.28.48-.14.17-.29.37-.42.5-.14.14-.28.29-.12.57.16.28.71 1.17 1.53 1.9 1.05.94 1.94 1.23 2.22 1.37.28.14.44.12.6-.07.17-.19.69-.8.87-1.08.18-.28.36-.23.6-.14.24.09 1.55.73 1.81.86.26.14.44.21.5.33.06.11.06.66-.18 1.34Z" />
              </svg>
              Enviar pedido pelo WhatsApp
            </Button>
          </a>
        )}
      </Card>

      <OrderDetails pedido={pedido} />

      <div className="flex flex-col gap-3 sm:flex-row">
        <Link to={`/acompanhar/${pedido.codigo}`} className="sm:flex-1">
          <Button variant="outline" className="w-full">
            Acompanhar pedido
          </Button>
        </Link>
        <Link to="/" className="sm:flex-1">
          <Button className="w-full">Voltar ao cardápio</Button>
        </Link>
      </div>
    </div>
  )
}

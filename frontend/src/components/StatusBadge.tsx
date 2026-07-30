import { Badge } from "@/components/ui"
import { StatusPedido } from "@/types"
import { statusLabels } from "@/utils/format"

const statusStyles: Record<StatusPedido, string> = {
  [StatusPedido.AGUARDANDO_CONFIRMACAO]: "bg-amber-100 text-amber-800",
  [StatusPedido.ACEITO]: "bg-blue-100 text-blue-800",
  [StatusPedido.EM_PREPARACAO]: "bg-indigo-100 text-indigo-800",
  [StatusPedido.PRONTO_PARA_RETIRADA]: "bg-cyan-100 text-cyan-800",
  [StatusPedido.SAIU_PARA_ENTREGA]: "bg-purple-100 text-purple-800",
  [StatusPedido.ENTREGUE]: "bg-green-100 text-green-800",
  [StatusPedido.CANCELADO]: "bg-red-100 text-red-800",
}

export function StatusBadge({ status }: { status: StatusPedido }) {
  return <Badge className={statusStyles[status]}>{statusLabels[status]}</Badge>
}

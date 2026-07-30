import { api } from "./api"
import type {
  HistoricoStatusResponse,
  PedidoRequest,
  PedidoResponse,
  StatusPedido,
  VendaManualRequest,
} from "@/types"

export const pedidoService = {
  async criarPedido(payload: PedidoRequest): Promise<PedidoResponse> {
    const { data } = await api.post<PedidoResponse>("/api/pedidos", payload)
    return data
  },

  async buscarPorCodigo(codigo: string): Promise<PedidoResponse> {
    const { data } = await api.get<PedidoResponse>(`/api/pedidos/${codigo}`)
    return data
  },

  async listarHistorico(codigo: string): Promise<HistoricoStatusResponse[]> {
    const { data } = await api.get<HistoricoStatusResponse[]>(`/api/pedidos/${codigo}/historico`)
    return data
  },

  async alterarStatus(id: number, status: StatusPedido): Promise<PedidoResponse> {
    const { data } = await api.patch<PedidoResponse>(`/api/admin/pedidos/${id}/status`, { status })
    return data
  },

  async registrarVendaManual(payload: VendaManualRequest): Promise<PedidoResponse> {
    const { data } = await api.post<PedidoResponse>("/api/admin/pedidos/vendas-manuais", payload)
    return data
  },
}

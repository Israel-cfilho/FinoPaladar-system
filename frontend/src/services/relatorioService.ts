import { api } from "./api"
import type {
  CidadeMaisPedidosResponse,
  ProdutoVendidoResponse,
  RelatorioFaturamentoResponse,
  TicketMedioResponse,
} from "@/types"

export const relatorioService = {
  async faturamentoPeriodo(dataInicial: string, dataFinal: string): Promise<RelatorioFaturamentoResponse> {
    const { data } = await api.get<RelatorioFaturamentoResponse>("/api/admin/relatorios/faturamento", {
      params: { dataInicial, dataFinal },
    })
    return data
  },

  async produtosMaisVendidos(
    dataInicial: string,
    dataFinal: string,
    limite = 10,
  ): Promise<ProdutoVendidoResponse[]> {
    const { data } = await api.get<ProdutoVendidoResponse[]>("/api/admin/relatorios/produtos-mais-vendidos", {
      params: { dataInicial, dataFinal, limite },
    })
    return data
  },

  async ticketMedio(dataInicial: string, dataFinal: string): Promise<TicketMedioResponse> {
    const { data } = await api.get<TicketMedioResponse>("/api/admin/relatorios/ticket-medio", {
      params: { dataInicial, dataFinal },
    })
    return data
  },

  async cidadeMaisPedidos(dataInicial: string, dataFinal: string): Promise<CidadeMaisPedidosResponse[]> {
    const { data } = await api.get<CidadeMaisPedidosResponse[]>("/api/admin/relatorios/cidade-mais-pedidos", {
      params: { dataInicial, dataFinal },
    })
    return data
  },
}

import { api } from "./api"
import type { DisponibilidadeProdutoRequest, DisponibilidadeProdutoResponse } from "@/types"

export const disponibilidadeService = {
  async listar(): Promise<DisponibilidadeProdutoResponse[]> {
    const { data } = await api.get<DisponibilidadeProdutoResponse[]>("/api/admin/disponibilidade")
    return data
  },

  async criar(payload: DisponibilidadeProdutoRequest): Promise<DisponibilidadeProdutoResponse> {
    const { data } = await api.post<DisponibilidadeProdutoResponse>("/api/admin/disponibilidade", payload)
    return data
  },

  async atualizar(
    id: number,
    payload: DisponibilidadeProdutoRequest,
  ): Promise<DisponibilidadeProdutoResponse> {
    const { data } = await api.put<DisponibilidadeProdutoResponse>(`/api/admin/disponibilidade/${id}`, payload)
    return data
  },

  async excluir(id: number): Promise<void> {
    await api.delete(`/api/admin/disponibilidade/${id}`)
  },
}

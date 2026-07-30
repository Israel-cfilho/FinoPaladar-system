import { api } from "./api"
import type { ProdutoPublicoResponse, ProdutoRequest, ProdutoResponse } from "@/types"

export const produtoService = {
  async listarPublicos(): Promise<ProdutoPublicoResponse[]> {
    const { data } = await api.get<ProdutoPublicoResponse[]>("/api/produtos")
    return data
  },

  async buscarPublico(id: number): Promise<ProdutoPublicoResponse> {
    const { data } = await api.get<ProdutoPublicoResponse>(`/api/produtos/${id}`)
    return data
  },

  async listarAdmin(): Promise<ProdutoResponse[]> {
    const { data } = await api.get<ProdutoResponse[]>("/api/admin/produtos")
    return data
  },

  async criar(payload: ProdutoRequest): Promise<ProdutoResponse> {
    const { data } = await api.post<ProdutoResponse>("/api/admin/produtos", payload)
    return data
  },

  async atualizar(id: number, payload: ProdutoRequest): Promise<ProdutoResponse> {
    const { data } = await api.put<ProdutoResponse>(`/api/admin/produtos/${id}`, payload)
    return data
  },

  async excluir(id: number): Promise<void> {
    await api.delete(`/api/admin/produtos/${id}`)
  },

  async uploadImagem(id: number, arquivo: File): Promise<ProdutoResponse> {
    const formData = new FormData()
    formData.append("imagem", arquivo)
    const { data } = await api.post<ProdutoResponse>(`/api/admin/produtos/${id}/imagem`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    })
    return data
  },
}

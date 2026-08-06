import type { ProdutoPublico } from '@/types';
import { produtosMock } from './mockData';

const USE_MOCK = import.meta.env.VITE_USE_MOCK !== 'false';

export async function listarProdutosPublicos(): Promise<ProdutoPublico[]> {
  if (USE_MOCK) {
    return Promise.resolve(produtosMock.filter((p) => p.quantidadeDisponivel >= 0));
  }

  const { api } = await import('./api');
  const { data } = await api.get<ProdutoPublico[]>('/produtos');
  return data;
}

export async function buscarProdutoPublico(id: number): Promise<ProdutoPublico | undefined> {
  if (USE_MOCK) {
    return Promise.resolve(produtosMock.find((p) => p.id === id));
  }

  const { api } = await import('./api');
  const { data } = await api.get<ProdutoPublico>(`/produtos/${id}`);
  return data;
}

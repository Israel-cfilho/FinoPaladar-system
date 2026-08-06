export type StatusPedido =
  | 'AGUARDANDO_CONFIRMACAO'
  | 'ACEITO'
  | 'EM_PREPARACAO'
  | 'PRONTO_PARA_RETIRADA'
  | 'SAIU_PARA_ENTREGA'
  | 'ENTREGUE'
  | 'CANCELADO';

export type FormaPagamento = 'PIX' | 'DINHEIRO' | 'CARTAO';

export type CanalVenda = 'ONLINE' | 'MANUAL';

export interface Produto {
  id: number;
  nome: string;
  descricao: string;
  preco: number;
  pesoMedioGramas: number;
  imagem: string;
  quantidadeDisponivel: number;
  ativo: boolean;
}

export interface ProdutoPublico {
  id: number;
  nome: string;
  preco: number;
  pesoMedioGramas: number;
  imagem: string;
  quantidadeDisponivel: number;
}

export interface ItemCarrinho {
  produto: ProdutoPublico;
  quantidade: number;
}

export interface PedidoResumo {
  codigo: string;
  status: StatusPedido;
  total: number;
  criadoEm: string;
}

export interface ApiError {
  message: string;
  status: number;
}

export interface LoginCredentials {
  email: string;
  senha: string;
}

export interface AuthResponse {
  token: string;
  nome: string;
}

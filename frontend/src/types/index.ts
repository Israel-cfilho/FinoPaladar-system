// Enums (as const objects to stay erasable-safe under strict TS)

export const FormaPagamento = {
  PIX: "PIX",
  DINHEIRO: "DINHEIRO",
} as const
export type FormaPagamento = (typeof FormaPagamento)[keyof typeof FormaPagamento]

export const TipoRecebimento = {
  RETIRADA: "RETIRADA",
  ENTREGA: "ENTREGA",
} as const
export type TipoRecebimento = (typeof TipoRecebimento)[keyof typeof TipoRecebimento]

export const Cidade = {
  BANANEIRAS: "BANANEIRAS",
  SOLANEA: "SOLANEA",
} as const
export type Cidade = (typeof Cidade)[keyof typeof Cidade]

export const StatusPedido = {
  AGUARDANDO_CONFIRMACAO: "AGUARDANDO_CONFIRMACAO",
  ACEITO: "ACEITO",
  EM_PREPARACAO: "EM_PREPARACAO",
  PRONTO_PARA_RETIRADA: "PRONTO_PARA_RETIRADA",
  SAIU_PARA_ENTREGA: "SAIU_PARA_ENTREGA",
  ENTREGUE: "ENTREGUE",
  CANCELADO: "CANCELADO",
} as const
export type StatusPedido = (typeof StatusPedido)[keyof typeof StatusPedido]

export const CanalVenda = {
  WHATSAPP: "WHATSAPP",
  TELEFONE: "TELEFONE",
  PRESENCIAL: "PRESENCIAL",
} as const
export type CanalVenda = (typeof CanalVenda)[keyof typeof CanalVenda]

// Produtos

export interface ProdutoPublicoResponse {
  id: number
  nome: string
  preco: number
  pesoMedioGramas: number
  imagem: string | null
  quantidadeDisponivel: number
}

export interface ProdutoResponse {
  id: number
  nome: string
  descricao: string | null
  preco: number
  pesoMedioGramas: number
  imagem: string | null
  ativo: boolean
}

export interface ProdutoRequest {
  nome: string
  descricao: string
  preco: number
  pesoMedioGramas: number
  imagem?: string | null
  ativo: boolean
}

// Disponibilidade

export interface DisponibilidadeProdutoRequest {
  produtoId: number
  quantidadeDisponivel: number
  dataInicial: string
  dataFinal: string
}

export interface DisponibilidadeProdutoResponse {
  id: number
  produtoId: number
  produtoNome: string
  quantidadeDisponivel: number
  dataInicial: string
  dataFinal: string
}

// Auth

export interface LoginRequest {
  email: string
  senha: string
}

export interface AdministradorAutenticadoResponse {
  id: number
  nome: string
  email: string
}

export interface LoginResponse {
  token: string
  tipo: string
  expiraEm: string
  administrador: AdministradorAutenticadoResponse
}

// Pedidos

export interface PedidoEnderecoRequest {
  cidade?: Cidade | null
  tipoEndereco?: string | null
  condominio?: string | null
  quadra?: string | null
  lote?: string | null
  bairro?: string | null
  rua?: string | null
  numero?: string | null
  complemento?: string | null
  pontoReferencia?: string | null
}

export interface PedidoEnderecoResponse {
  cidade: Cidade | null
  tipoEndereco: string | null
  condominio: string | null
  quadra: string | null
  lote: string | null
  bairro: string | null
  rua: string | null
  numero: string | null
  complemento: string | null
  pontoReferencia: string | null
}

export interface PedidoItemRequest {
  produtoId: number
  quantidade: number
}

export interface PedidoItemResponse {
  id: number
  produtoId: number
  nomeProduto: string
  precoUnitario: number
  pesoMedioGramas: number
  quantidade: number
  subtotal: number
}

export interface PedidoRequest {
  cliente: string
  telefone: string
  formaPagamento: FormaPagamento
  tipoRecebimento: TipoRecebimento
  observacao?: string | null
  enderecoEntrega?: PedidoEnderecoRequest | null
  itens: PedidoItemRequest[]
}

export interface VendaManualRequest extends PedidoRequest {
  canalVenda: CanalVenda
}

export interface PedidoResponse {
  id: number
  codigo: string
  cliente: string
  telefone: string
  status: StatusPedido
  formaPagamento: FormaPagamento
  tipoRecebimento: TipoRecebimento
  canalVenda: CanalVenda | null
  valorProdutos: number
  taxaEntrega: number
  valorTotal: number
  observacao: string | null
  data: string
  enderecoEntrega: PedidoEnderecoResponse | null
  itens: PedidoItemResponse[]
  mensagem: string | null
  linkWhatsApp: string | null
}

export interface PedidoStatusRequest {
  status: StatusPedido
}

export interface HistoricoStatusResponse {
  id: number
  status: StatusPedido
  dataHora: string
}

// Dashboard e relatórios

export interface DashboardResumoResponse {
  pedidosHoje: number
  pedidosEmAberto: number
  pedidosEntregues: number
  pedidosCancelados: number
  valorVendidoHoje: number
}

export interface RelatorioFaturamentoResponse {
  dataInicial: string
  dataFinal: string
  valorTotal: number
}

export interface ProdutoVendidoResponse {
  produtoId: number
  nomeProduto: string
  quantidadeVendida: number
  valorVendido: number
}

export interface TicketMedioResponse {
  dataInicial: string
  dataFinal: string
  valor: number
}

export interface CidadeMaisPedidosResponse {
  cidade: Cidade
  quantidadePedidos: number
}

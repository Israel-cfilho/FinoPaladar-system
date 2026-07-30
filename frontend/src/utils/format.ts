import {
  CanalVenda,
  Cidade,
  FormaPagamento,
  StatusPedido,
  TipoRecebimento,
} from "@/types"

export function formatCurrency(value: number | null | undefined): string {
  const n = typeof value === "number" ? value : 0
  return n.toLocaleString("pt-BR", { style: "currency", currency: "BRL" })
}

export function formatWeight(grams: number | null | undefined): string {
  if (!grams) return "-"
  if (grams >= 1000) return `${(grams / 1000).toLocaleString("pt-BR", { maximumFractionDigits: 2 })} kg`
  return `${grams} g`
}

export function formatDate(value: string | null | undefined): string {
  if (!value) return "-"
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  // Datas simples (yyyy-MM-dd) não têm horário.
  if (/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    return date.toLocaleDateString("pt-BR", { timeZone: "UTC" })
  }
  return date.toLocaleString("pt-BR")
}

export function todayISO(): string {
  return new Date().toISOString().slice(0, 10)
}

export function daysAgoISO(days: number): string {
  const d = new Date()
  d.setDate(d.getDate() - days)
  return d.toISOString().slice(0, 10)
}

export const statusLabels: Record<StatusPedido, string> = {
  [StatusPedido.AGUARDANDO_CONFIRMACAO]: "Aguardando confirmação",
  [StatusPedido.ACEITO]: "Aceito",
  [StatusPedido.EM_PREPARACAO]: "Em preparação",
  [StatusPedido.PRONTO_PARA_RETIRADA]: "Pronto para retirada",
  [StatusPedido.SAIU_PARA_ENTREGA]: "Saiu para entrega",
  [StatusPedido.ENTREGUE]: "Entregue",
  [StatusPedido.CANCELADO]: "Cancelado",
}

export const formaPagamentoLabels: Record<FormaPagamento, string> = {
  [FormaPagamento.PIX]: "PIX",
  [FormaPagamento.DINHEIRO]: "Dinheiro",
}

export const tipoRecebimentoLabels: Record<TipoRecebimento, string> = {
  [TipoRecebimento.RETIRADA]: "Retirada",
  [TipoRecebimento.ENTREGA]: "Entrega",
}

export const cidadeLabels: Record<Cidade, string> = {
  [Cidade.BANANEIRAS]: "Bananeiras",
  [Cidade.SOLANEA]: "Solânea",
}

export const canalVendaLabels: Record<CanalVenda, string> = {
  [CanalVenda.WHATSAPP]: "WhatsApp",
  [CanalVenda.TELEFONE]: "Telefone",
  [CanalVenda.PRESENCIAL]: "Presencial",
}

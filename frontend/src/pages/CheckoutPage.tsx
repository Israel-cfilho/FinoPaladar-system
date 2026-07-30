import { useState, type FormEvent } from "react"
import { useNavigate } from "react-router-dom"
import { useCart } from "@/hooks/useCart"
import { pedidoService } from "@/services/pedidoService"
import { extractErrorMessage } from "@/services/api"
import { Button, Card, ErrorMessage, Field, Input, Select, Textarea } from "@/components/ui"
import { AddressFields, type EnderecoState, emptyEndereco } from "@/components/AddressFields"
import {
  FormaPagamento,
  TipoRecebimento,
  type PedidoEnderecoRequest,
  type PedidoRequest,
} from "@/types"
import { formaPagamentoLabels, formatCurrency, tipoRecebimentoLabels } from "@/utils/format"

function buildEndereco(endereco: EnderecoState): PedidoEnderecoRequest {
  if (endereco.tipoEndereco === "CONDOMINIO") {
    return {
      cidade: endereco.cidade || null,
      tipoEndereco: "CONDOMINIO",
      condominio: endereco.condominio || null,
      quadra: endereco.quadra || null,
      lote: endereco.lote || null,
    }
  }
  return {
    cidade: endereco.cidade || null,
    tipoEndereco: "RUA",
    bairro: endereco.bairro || null,
    rua: endereco.rua || null,
    numero: endereco.numero || null,
    complemento: endereco.complemento || null,
    pontoReferencia: endereco.pontoReferencia || null,
  }
}

export function CheckoutPage() {
  const { items, subtotalEstimado, clear } = useCart()
  const navigate = useNavigate()

  const [cliente, setCliente] = useState("")
  const [telefone, setTelefone] = useState("")
  const [formaPagamento, setFormaPagamento] = useState<FormaPagamento>(FormaPagamento.PIX)
  const [tipoRecebimento, setTipoRecebimento] = useState<TipoRecebimento>(TipoRecebimento.RETIRADA)
  const [observacao, setObservacao] = useState("")
  const [endereco, setEndereco] = useState<EnderecoState>(emptyEndereco)

  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  if (items.length === 0 && !submitting) {
    return (
      <div className="mx-auto max-w-2xl text-center">
        <h1 className="mb-4 font-serif text-2xl font-semibold text-foreground">Checkout</h1>
        <p className="text-muted">Seu carrinho está vazio.</p>
        <Button className="mt-4" onClick={() => navigate("/")}>
          Ver cardápio
        </Button>
      </div>
    )
  }

  const isEntrega = tipoRecebimento === TipoRecebimento.ENTREGA

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    setError(null)
    setSubmitting(true)

    const payload: PedidoRequest = {
      cliente: cliente.trim(),
      telefone: telefone.trim(),
      formaPagamento,
      tipoRecebimento,
      observacao: observacao.trim() || null,
      enderecoEntrega: isEntrega ? buildEndereco(endereco) : null,
      // O frontend envia somente produtoId e quantidade; valores são calculados no backend.
      itens: items.map((i) => ({ produtoId: i.produtoId, quantidade: i.quantidade })),
    }

    try {
      const pedido = await pedidoService.criarPedido(payload)
      clear()
      navigate(`/pedido/${pedido.codigo}`, { state: { pedido } })
    } catch (err) {
      setError(extractErrorMessage(err, "Não foi possível criar o pedido. Verifique os dados e tente novamente."))
      setSubmitting(false)
    }
  }

  return (
    <div className="mx-auto max-w-4xl">
      <h1 className="mb-6 font-serif text-2xl font-semibold text-foreground">Finalizar pedido</h1>

      <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-6 lg:grid-cols-[1fr_320px]">
        <div className="flex flex-col gap-6">
          <Card className="flex flex-col gap-4 p-5">
            <h2 className="font-serif text-lg font-semibold text-foreground">Seus dados</h2>
            <Field label="Nome completo" htmlFor="cliente" required>
              <Input
                id="cliente"
                value={cliente}
                onChange={(e) => setCliente(e.target.value)}
                required
                maxLength={150}
                placeholder="Como devemos te chamar?"
              />
            </Field>
            <Field label="Telefone / WhatsApp" htmlFor="telefone" required>
              <Input
                id="telefone"
                value={telefone}
                onChange={(e) => setTelefone(e.target.value)}
                required
                placeholder="(83) 99999-9999"
                inputMode="tel"
              />
            </Field>
          </Card>

          <Card className="flex flex-col gap-4 p-5">
            <h2 className="font-serif text-lg font-semibold text-foreground">Pagamento e recebimento</h2>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <Field label="Forma de pagamento" htmlFor="formaPagamento" required>
                <Select
                  id="formaPagamento"
                  value={formaPagamento}
                  onChange={(e) => setFormaPagamento(e.target.value as FormaPagamento)}
                >
                  {Object.values(FormaPagamento).map((fp) => (
                    <option key={fp} value={fp}>
                      {formaPagamentoLabels[fp]}
                    </option>
                  ))}
                </Select>
              </Field>
              <Field label="Tipo de recebimento" htmlFor="tipoRecebimento" required>
                <Select
                  id="tipoRecebimento"
                  value={tipoRecebimento}
                  onChange={(e) => setTipoRecebimento(e.target.value as TipoRecebimento)}
                >
                  {Object.values(TipoRecebimento).map((tr) => (
                    <option key={tr} value={tr}>
                      {tipoRecebimentoLabels[tr]}
                    </option>
                  ))}
                </Select>
              </Field>
            </div>
          </Card>

          {isEntrega && (
            <Card className="flex flex-col gap-4 p-5">
              <h2 className="font-serif text-lg font-semibold text-foreground">Endereço de entrega</h2>
              <AddressFields value={endereco} onChange={setEndereco} required />
            </Card>
          )}

          <Card className="flex flex-col gap-4 p-5">
            <h2 className="font-serif text-lg font-semibold text-foreground">Observações</h2>
            <Field label="Alguma observação para o pedido?" htmlFor="observacao">
              <Textarea
                id="observacao"
                value={observacao}
                onChange={(e) => setObservacao(e.target.value)}
                placeholder="Ex.: sem cebola, troco para R$ 50,00..."
              />
            </Field>
          </Card>
        </div>

        <div className="flex flex-col gap-4 lg:sticky lg:top-24 lg:self-start">
          <Card className="flex flex-col gap-4 p-5">
            <h2 className="font-serif text-lg font-semibold text-foreground">Resumo</h2>
            <ul className="flex flex-col gap-2">
              {items.map((item) => (
                <li key={item.produtoId} className="flex justify-between gap-2 text-sm">
                  <span className="text-muted">
                    {item.quantidade}x {item.nome}
                  </span>
                  <span className="text-foreground">{formatCurrency(item.preco * item.quantidade)}</span>
                </li>
              ))}
            </ul>
            <div className="border-t border-border pt-3">
              <div className="flex justify-between">
                <span className="text-muted">Subtotal estimado</span>
                <span className="font-semibold text-foreground">{formatCurrency(subtotalEstimado)}</span>
              </div>
              <p className="mt-1 text-xs text-muted">
                A taxa de entrega e o valor total serão confirmados pela loja.
              </p>
            </div>

            {error && <ErrorMessage message={error} />}

            <Button type="submit" size="lg" loading={submitting} className="w-full">
              Confirmar pedido
            </Button>
          </Card>
        </div>
      </form>
    </div>
  )
}

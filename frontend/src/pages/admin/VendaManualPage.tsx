import { useEffect, useMemo, useState, type FormEvent } from "react"
import { produtoService } from "@/services/produtoService"
import { pedidoService } from "@/services/pedidoService"
import { extractErrorMessage } from "@/services/api"
import { Button, Card, ErrorMessage, Field, Input, Loading, Select, Textarea } from "@/components/ui"
import { AddressFields, type EnderecoState, emptyEndereco } from "@/components/AddressFields"
import { OrderDetails } from "@/components/OrderDetails"
import {
  CanalVenda,
  FormaPagamento,
  TipoRecebimento,
  type PedidoEnderecoRequest,
  type ProdutoResponse,
  type PedidoResponse,
  type VendaManualRequest,
} from "@/types"
import {
  canalVendaLabels,
  formaPagamentoLabels,
  formatCurrency,
  tipoRecebimentoLabels,
} from "@/utils/format"

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

interface ItemState {
  produtoId: number
  quantidade: number
}

export function VendaManualPage() {
  const [produtos, setProdutos] = useState<ProdutoResponse[]>([])
  const [loadingProdutos, setLoadingProdutos] = useState(true)

  const [cliente, setCliente] = useState("")
  const [telefone, setTelefone] = useState("")
  const [canalVenda, setCanalVenda] = useState<CanalVenda>(CanalVenda.WHATSAPP)
  const [formaPagamento, setFormaPagamento] = useState<FormaPagamento>(FormaPagamento.PIX)
  const [tipoRecebimento, setTipoRecebimento] = useState<TipoRecebimento>(TipoRecebimento.RETIRADA)
  const [observacao, setObservacao] = useState("")
  const [endereco, setEndereco] = useState<EnderecoState>(emptyEndereco)
  const [itens, setItens] = useState<ItemState[]>([])

  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [criado, setCriado] = useState<PedidoResponse | null>(null)

  useEffect(() => {
    produtoService
      .listarAdmin()
      .then(setProdutos)
      .catch(() => setProdutos([]))
      .finally(() => setLoadingProdutos(false))
  }, [])

  const produtosMap = useMemo(() => {
    const map = new Map<number, ProdutoResponse>()
    produtos.forEach((p) => map.set(p.id, p))
    return map
  }, [produtos])

  const subtotal = useMemo(
    () =>
      itens.reduce((sum, item) => {
        const produto = produtosMap.get(item.produtoId)
        return sum + (produto ? produto.preco * item.quantidade : 0)
      }, 0),
    [itens, produtosMap],
  )

  const isEntrega = tipoRecebimento === TipoRecebimento.ENTREGA

  const addItem = () => {
    const disponivel = produtos.find((p) => !itens.some((i) => i.produtoId === p.id))
    if (!disponivel) return
    setItens((prev) => [...prev, { produtoId: disponivel.id, quantidade: 1 }])
  }

  const updateItem = (index: number, patch: Partial<ItemState>) =>
    setItens((prev) => prev.map((item, i) => (i === index ? { ...item, ...patch } : item)))

  const removeItem = (index: number) => setItens((prev) => prev.filter((_, i) => i !== index))

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    setError(null)

    if (itens.length === 0) {
      setError("Adicione ao menos um item ao pedido.")
      return
    }

    setSubmitting(true)
    const payload: VendaManualRequest = {
      cliente: cliente.trim(),
      telefone: telefone.trim(),
      canalVenda,
      formaPagamento,
      tipoRecebimento,
      observacao: observacao.trim() || null,
      enderecoEntrega: isEntrega ? buildEndereco(endereco) : null,
      itens: itens.map((i) => ({ produtoId: i.produtoId, quantidade: i.quantidade })),
    }

    try {
      const pedido = await pedidoService.registrarVendaManual(payload)
      setCriado(pedido)
      setCliente("")
      setTelefone("")
      setObservacao("")
      setEndereco(emptyEndereco)
      setItens([])
    } catch (err) {
      setError(extractErrorMessage(err, "Não foi possível registrar a venda. Verifique os dados."))
    } finally {
      setSubmitting(false)
    }
  }

  if (loadingProdutos) return <Loading label="Carregando produtos..." />

  return (
    <div className="mx-auto max-w-4xl">
      <header className="mb-6">
        <h1 className="font-serif text-2xl font-semibold text-foreground">Venda manual</h1>
        <p className="text-sm text-muted">Registre pedidos recebidos por WhatsApp, telefone ou presencialmente.</p>
      </header>

      {criado && (
        <Card className="mb-6 flex flex-col gap-4 border-primary/40 p-5">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <h2 className="font-serif text-lg font-semibold text-foreground">
              Venda registrada — {criado.codigo}
            </h2>
            <Button variant="ghost" onClick={() => setCriado(null)}>
              Registrar outra
            </Button>
          </div>
          <OrderDetails pedido={criado} />
        </Card>
      )}

      <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-6 lg:grid-cols-[1fr_320px]">
        <div className="flex flex-col gap-6">
          <Card className="flex flex-col gap-4 p-5">
            <h2 className="font-serif text-lg font-semibold text-foreground">Cliente</h2>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <Field label="Nome" htmlFor="cliente" required>
                <Input id="cliente" value={cliente} onChange={(e) => setCliente(e.target.value)} required maxLength={150} />
              </Field>
              <Field label="Telefone" htmlFor="telefone" required>
                <Input
                  id="telefone"
                  value={telefone}
                  onChange={(e) => setTelefone(e.target.value)}
                  required
                  inputMode="tel"
                  placeholder="(83) 99999-9999"
                />
              </Field>
            </div>
          </Card>

          <Card className="flex flex-col gap-4 p-5">
            <div className="flex items-center justify-between">
              <h2 className="font-serif text-lg font-semibold text-foreground">Itens</h2>
              <Button type="button" variant="secondary" onClick={addItem} disabled={itens.length >= produtos.length}>
                Adicionar item
              </Button>
            </div>
            {itens.length === 0 ? (
              <p className="text-sm text-muted">Nenhum item adicionado.</p>
            ) : (
              <ul className="flex flex-col gap-3">
                {itens.map((item, index) => {
                  const produto = produtosMap.get(item.produtoId)
                  return (
                    <li key={index} className="grid grid-cols-[1fr_88px_auto] items-end gap-3">
                      <Field label="Produto" htmlFor={`item-${index}`}>
                        <Select
                          id={`item-${index}`}
                          value={item.produtoId}
                          onChange={(e) => updateItem(index, { produtoId: Number(e.target.value) })}
                        >
                          {produtos.map((p) => (
                            <option
                              key={p.id}
                              value={p.id}
                              disabled={p.id !== item.produtoId && itens.some((i) => i.produtoId === p.id)}
                            >
                              {p.nome} — {formatCurrency(p.preco)}
                            </option>
                          ))}
                        </Select>
                      </Field>
                      <Field label="Qtd." htmlFor={`qtd-${index}`}>
                        <Input
                          id={`qtd-${index}`}
                          type="number"
                          min={1}
                          value={item.quantidade}
                          onChange={(e) => updateItem(index, { quantidade: Math.max(1, Number(e.target.value)) })}
                        />
                      </Field>
                      <Button type="button" variant="ghost" onClick={() => removeItem(index)} className="mb-1">
                        Remover
                      </Button>
                      {produto && (
                        <span className="col-span-3 -mt-2 text-xs text-muted">
                          Subtotal do item: {formatCurrency(produto.preco * item.quantidade)}
                        </span>
                      )}
                    </li>
                  )
                })}
              </ul>
            )}
          </Card>

          <Card className="flex flex-col gap-4 p-5">
            <h2 className="font-serif text-lg font-semibold text-foreground">Pagamento e recebimento</h2>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
              <Field label="Canal de venda" htmlFor="canalVenda" required>
                <Select id="canalVenda" value={canalVenda} onChange={(e) => setCanalVenda(e.target.value as CanalVenda)}>
                  {Object.values(CanalVenda).map((cv) => (
                    <option key={cv} value={cv}>
                      {canalVendaLabels[cv]}
                    </option>
                  ))}
                </Select>
              </Field>
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
              <Field label="Recebimento" htmlFor="tipoRecebimento" required>
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
            <Field label="Observação" htmlFor="observacao">
              <Textarea id="observacao" value={observacao} onChange={(e) => setObservacao(e.target.value)} />
            </Field>
          </Card>
        </div>

        <div className="flex flex-col gap-4 lg:sticky lg:top-24 lg:self-start">
          <Card className="flex flex-col gap-4 p-5">
            <h2 className="font-serif text-lg font-semibold text-foreground">Resumo</h2>
            <div className="flex justify-between">
              <span className="text-muted">Subtotal produtos</span>
              <span className="font-semibold text-foreground">{formatCurrency(subtotal)}</span>
            </div>
            <p className="text-xs text-muted">A taxa de entrega e o total final são calculados pelo backend.</p>
            {error && <ErrorMessage message={error} />}
            <Button type="submit" size="lg" loading={submitting} className="w-full">
              Registrar venda
            </Button>
          </Card>
        </div>
      </form>
    </div>
  )
}

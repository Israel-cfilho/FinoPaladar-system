import { useRef, useState, type FormEvent } from "react"
import useSWR from "swr"
import { produtoService } from "@/services/produtoService"
import type { ProdutoRequest, ProdutoResponse } from "@/types"
import { Button, Card, Field, Input, Textarea, Modal, Loading, ErrorMessage, EmptyState, Badge } from "@/components/ui"
import { formatCurrency, formatWeight } from "@/utils/format"

const emptyForm: ProdutoRequest = {
  nome: "",
  descricao: "",
  preco: 0,
  pesoMedioGramas: 0,
  ativo: true,
}

export default function ProdutosPage() {
  const { data, error, isLoading, mutate } = useSWR("admin-produtos", () => produtoService.listarAdmin())

  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<ProdutoResponse | null>(null)
  const [form, setForm] = useState<ProdutoRequest>(emptyForm)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  function openCreate() {
    setEditing(null)
    setForm(emptyForm)
    setFormError(null)
    setModalOpen(true)
  }

  function openEdit(produto: ProdutoResponse) {
    setEditing(produto)
    setForm({
      nome: produto.nome,
      descricao: produto.descricao ?? "",
      preco: produto.preco,
      pesoMedioGramas: produto.pesoMedioGramas,
      ativo: produto.ativo,
    })
    setFormError(null)
    setModalOpen(true)
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setSaving(true)
    setFormError(null)
    try {
      if (editing) {
        await produtoService.atualizar(editing.id, form)
      } else {
        await produtoService.criar(form)
      }
      await mutate()
      setModalOpen(false)
    } catch (err) {
      const message =
        (err as { response?: { data?: { mensagem?: string } } })?.response?.data?.mensagem ??
        "Não foi possível salvar o produto."
      setFormError(message)
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(produto: ProdutoResponse) {
    if (!window.confirm(`Excluir o produto "${produto.nome}"?`)) return
    await produtoService.excluir(produto.id)
    await mutate()
  }

  async function handleUpload(produto: ProdutoResponse, file: File) {
    await produtoService.uploadImagem(produto.id, file)
    await mutate()
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="font-serif text-2xl font-bold text-foreground">Produtos</h1>
          <p className="text-sm text-muted">Cadastre e gerencie os produtos do cardápio.</p>
        </div>
        <Button onClick={openCreate}>Novo produto</Button>
      </div>

      {isLoading && <Loading />}
      {error && <ErrorMessage message="Não foi possível carregar os produtos." onRetry={() => mutate()} />}

      {data && data.length === 0 && (
        <EmptyState
          title="Nenhum produto cadastrado"
          description="Comece adicionando o primeiro produto ao cardápio."
          action={<Button onClick={openCreate}>Novo produto</Button>}
        />
      )}

      {data && data.length > 0 && (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
          {data.map((produto) => (
            <Card key={produto.id} className="flex flex-col overflow-hidden">
              <div className="flex aspect-video items-center justify-center bg-background">
                {produto.imagem ? (
                  <img
                    src={produto.imagem || "/placeholder.svg"}
                    alt={produto.nome}
                    className="h-full w-full object-cover"
                  />
                ) : (
                  <span className="text-sm text-muted">Sem imagem</span>
                )}
              </div>
              <div className="flex flex-1 flex-col gap-2 p-4">
                <div className="flex items-start justify-between gap-2">
                  <h3 className="font-medium text-foreground">{produto.nome}</h3>
                  <Badge className={produto.ativo ? "bg-accent/10 text-accent" : "bg-muted/15 text-muted"}>
                    {produto.ativo ? "Ativo" : "Inativo"}
                  </Badge>
                </div>
                {produto.descricao && <p className="text-sm text-muted line-clamp-2">{produto.descricao}</p>}
                <div className="mt-1 flex items-center justify-between text-sm">
                  <span className="font-semibold text-primary">{formatCurrency(produto.preco)}</span>
                  <span className="text-muted">{formatWeight(produto.pesoMedioGramas)}</span>
                </div>
                <div className="mt-auto flex flex-wrap gap-2 pt-3">
                  <Button size="sm" variant="outline" onClick={() => openEdit(produto)}>
                    Editar
                  </Button>
                  <label className="inline-flex">
                    <input
                      ref={fileInputRef}
                      type="file"
                      accept="image/*"
                      className="hidden"
                      onChange={(e) => {
                        const file = e.target.files?.[0]
                        if (file) void handleUpload(produto, file)
                        e.target.value = ""
                      }}
                    />
                    <span className="inline-flex h-9 cursor-pointer items-center rounded border border-border bg-surface px-3 text-sm font-medium text-foreground hover:bg-background">
                      Imagem
                    </span>
                  </label>
                  <Button size="sm" variant="danger" onClick={() => handleDelete(produto)}>
                    Excluir
                  </Button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editing ? "Editar produto" : "Novo produto"}
        footer={
          <>
            <Button variant="outline" onClick={() => setModalOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit" form="produto-form" loading={saving}>
              Salvar
            </Button>
          </>
        }
      >
        <form id="produto-form" onSubmit={handleSubmit} className="flex flex-col gap-4">
          <Field label="Nome" htmlFor="nome" required>
            <Input
              id="nome"
              value={form.nome}
              onChange={(e) => setForm({ ...form, nome: e.target.value })}
              required
            />
          </Field>
          <Field label="Descrição" htmlFor="descricao">
            <Textarea
              id="descricao"
              value={form.descricao}
              onChange={(e) => setForm({ ...form, descricao: e.target.value })}
            />
          </Field>
          <div className="grid grid-cols-2 gap-4">
            <Field label="Preço (R$)" htmlFor="preco" required>
              <Input
                id="preco"
                type="number"
                min="0"
                step="0.01"
                value={form.preco}
                onChange={(e) => setForm({ ...form, preco: Number(e.target.value) })}
                required
              />
            </Field>
            <Field label="Peso médio (g)" htmlFor="peso" required>
              <Input
                id="peso"
                type="number"
                min="0"
                step="1"
                value={form.pesoMedioGramas}
                onChange={(e) => setForm({ ...form, pesoMedioGramas: Number(e.target.value) })}
                required
              />
            </Field>
          </div>
          <label className="flex items-center gap-2 text-sm text-foreground">
            <input
              type="checkbox"
              checked={form.ativo}
              onChange={(e) => setForm({ ...form, ativo: e.target.checked })}
              className="h-4 w-4 rounded border-border"
            />
            Produto ativo
          </label>
          {formError && <p className="rounded-md bg-danger/10 px-3 py-2 text-sm text-danger">{formError}</p>}
        </form>
      </Modal>
    </div>
  )
}

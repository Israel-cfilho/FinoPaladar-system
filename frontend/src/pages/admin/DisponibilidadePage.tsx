import { useState, type FormEvent } from "react"
import useSWR from "swr"
import { disponibilidadeService } from "@/services/disponibilidadeService"
import { produtoService } from "@/services/produtoService"
import type { DisponibilidadeProdutoRequest, DisponibilidadeProdutoResponse } from "@/types"
import { Button, Card, Field, Input, Select, Modal, Loading, ErrorMessage, EmptyState } from "@/components/ui"
import { formatDate, todayISO } from "@/utils/format"

function makeEmptyForm(): DisponibilidadeProdutoRequest {
  return {
    produtoId: 0,
    quantidadeDisponivel: 1,
    dataInicial: todayISO(),
    dataFinal: todayISO(),
  }
}

export default function DisponibilidadePage() {
  const { data, error, isLoading, mutate } = useSWR("admin-disponibilidade", () =>
    disponibilidadeService.listar(),
  )
  const { data: produtos } = useSWR("admin-produtos", () => produtoService.listarAdmin())

  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<DisponibilidadeProdutoResponse | null>(null)
  const [form, setForm] = useState<DisponibilidadeProdutoRequest>(makeEmptyForm())
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)

  function openCreate() {
    setEditing(null)
    setForm(makeEmptyForm())
    setFormError(null)
    setModalOpen(true)
  }

  function openEdit(item: DisponibilidadeProdutoResponse) {
    setEditing(item)
    setForm({
      produtoId: item.produtoId,
      quantidadeDisponivel: item.quantidadeDisponivel,
      dataInicial: item.dataInicial,
      dataFinal: item.dataFinal,
    })
    setFormError(null)
    setModalOpen(true)
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!form.produtoId) {
      setFormError("Selecione um produto.")
      return
    }
    setSaving(true)
    setFormError(null)
    try {
      if (editing) {
        await disponibilidadeService.atualizar(editing.id, form)
      } else {
        await disponibilidadeService.criar(form)
      }
      await mutate()
      setModalOpen(false)
    } catch (err) {
      const message =
        (err as { response?: { data?: { mensagem?: string } } })?.response?.data?.mensagem ??
        "Não foi possível salvar a disponibilidade."
      setFormError(message)
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(item: DisponibilidadeProdutoResponse) {
    if (!window.confirm(`Excluir a disponibilidade de "${item.produtoNome}"?`)) return
    await disponibilidadeService.excluir(item.id)
    await mutate()
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="font-serif text-2xl font-bold text-foreground">Disponibilidade</h1>
          <p className="text-sm text-muted">Defina os estoques por período para cada produto.</p>
        </div>
        <Button onClick={openCreate}>Nova disponibilidade</Button>
      </div>

      {isLoading && <Loading />}
      {error && <ErrorMessage message="Não foi possível carregar a lista." onRetry={() => mutate()} />}

      {data && data.length === 0 && (
        <EmptyState
          title="Nenhuma disponibilidade cadastrada"
          description="Cadastre quantidades disponíveis por período para liberar produtos no cardápio."
          action={<Button onClick={openCreate}>Nova disponibilidade</Button>}
        />
      )}

      {data && data.length > 0 && (
        <Card className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border text-left text-muted">
                <th className="px-4 py-3 font-medium">Produto</th>
                <th className="px-4 py-3 font-medium">Quantidade</th>
                <th className="px-4 py-3 font-medium">Início</th>
                <th className="px-4 py-3 font-medium">Fim</th>
                <th className="px-4 py-3 font-medium text-right">Ações</th>
              </tr>
            </thead>
            <tbody>
              {data.map((item) => (
                <tr key={item.id} className="border-b border-border last:border-0">
                  <td className="px-4 py-3 font-medium text-foreground">{item.produtoNome}</td>
                  <td className="px-4 py-3 text-foreground">{item.quantidadeDisponivel}</td>
                  <td className="px-4 py-3 text-muted">{formatDate(item.dataInicial)}</td>
                  <td className="px-4 py-3 text-muted">{formatDate(item.dataFinal)}</td>
                  <td className="px-4 py-3">
                    <div className="flex justify-end gap-2">
                      <Button size="sm" variant="outline" onClick={() => openEdit(item)}>
                        Editar
                      </Button>
                      <Button size="sm" variant="danger" onClick={() => handleDelete(item)}>
                        Excluir
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editing ? "Editar disponibilidade" : "Nova disponibilidade"}
        footer={
          <>
            <Button variant="outline" onClick={() => setModalOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit" form="disp-form" loading={saving}>
              Salvar
            </Button>
          </>
        }
      >
        <form id="disp-form" onSubmit={handleSubmit} className="flex flex-col gap-4">
          <Field label="Produto" htmlFor="produtoId" required>
            <Select
              id="produtoId"
              value={form.produtoId || ""}
              onChange={(e) => setForm({ ...form, produtoId: Number(e.target.value) })}
              required
            >
              <option value="" disabled>
                Selecione um produto
              </option>
              {produtos?.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.nome}
                </option>
              ))}
            </Select>
          </Field>
          <Field label="Quantidade disponível" htmlFor="quantidade" required>
            <Input
              id="quantidade"
              type="number"
              min="0"
              step="1"
              value={form.quantidadeDisponivel}
              onChange={(e) => setForm({ ...form, quantidadeDisponivel: Number(e.target.value) })}
              required
            />
          </Field>
          <div className="grid grid-cols-2 gap-4">
            <Field label="Data inicial" htmlFor="dataInicial" required>
              <Input
                id="dataInicial"
                type="date"
                value={form.dataInicial}
                onChange={(e) => setForm({ ...form, dataInicial: e.target.value })}
                required
              />
            </Field>
            <Field label="Data final" htmlFor="dataFinal" required>
              <Input
                id="dataFinal"
                type="date"
                value={form.dataFinal}
                onChange={(e) => setForm({ ...form, dataFinal: e.target.value })}
                required
              />
            </Field>
          </div>
          {formError && <p className="rounded-md bg-danger/10 px-3 py-2 text-sm text-danger">{formError}</p>}
        </form>
      </Modal>
    </div>
  )
}

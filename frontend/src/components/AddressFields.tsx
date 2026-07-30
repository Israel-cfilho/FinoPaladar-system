import { Field, Input, Select } from "@/components/ui"
import { Cidade } from "@/types"
import { cidadeLabels } from "@/utils/format"

export type TipoEndereco = "CONDOMINIO" | "RUA"

export interface EnderecoState {
  cidade: Cidade | ""
  tipoEndereco: TipoEndereco
  condominio: string
  quadra: string
  lote: string
  bairro: string
  rua: string
  numero: string
  complemento: string
  pontoReferencia: string
}

export const emptyEndereco: EnderecoState = {
  cidade: "",
  tipoEndereco: "RUA",
  condominio: "",
  quadra: "",
  lote: "",
  bairro: "",
  rua: "",
  numero: "",
  complemento: "",
  pontoReferencia: "",
}

interface AddressFieldsProps {
  value: EnderecoState
  onChange: (value: EnderecoState) => void
  required?: boolean
}

export function AddressFields({ value, onChange, required }: AddressFieldsProps) {
  const set = <K extends keyof EnderecoState>(key: K, v: EnderecoState[K]) =>
    onChange({ ...value, [key]: v })

  const isCondominio = value.tipoEndereco === "CONDOMINIO"

  return (
    <div className="flex flex-col gap-4">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Field label="Cidade" htmlFor="cidade" required={required}>
          <Select
            id="cidade"
            value={value.cidade}
            onChange={(e) => set("cidade", e.target.value as Cidade | "")}
            required={required}
          >
            <option value="">Selecione...</option>
            {Object.values(Cidade).map((c) => (
              <option key={c} value={c}>
                {cidadeLabels[c]}
              </option>
            ))}
          </Select>
        </Field>
        <Field label="Tipo de endereço" htmlFor="tipoEndereco" required={required}>
          <Select
            id="tipoEndereco"
            value={value.tipoEndereco}
            onChange={(e) => set("tipoEndereco", e.target.value as TipoEndereco)}
          >
            <option value="RUA">Rua</option>
            <option value="CONDOMINIO">Condomínio</option>
          </Select>
        </Field>
      </div>

      {isCondominio ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field label="Condomínio" htmlFor="condominio" required={required} className="sm:col-span-2">
            <Input
              id="condominio"
              value={value.condominio}
              onChange={(e) => set("condominio", e.target.value)}
              required={required}
              maxLength={120}
            />
          </Field>
          <Field label="Quadra" htmlFor="quadra">
            <Input id="quadra" value={value.quadra} onChange={(e) => set("quadra", e.target.value)} maxLength={50} />
          </Field>
          <Field label="Lote" htmlFor="lote">
            <Input id="lote" value={value.lote} onChange={(e) => set("lote", e.target.value)} maxLength={50} />
          </Field>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field label="Bairro" htmlFor="bairro" required={required}>
            <Input
              id="bairro"
              value={value.bairro}
              onChange={(e) => set("bairro", e.target.value)}
              required={required}
              maxLength={120}
            />
          </Field>
          <Field label="Rua" htmlFor="rua" required={required}>
            <Input
              id="rua"
              value={value.rua}
              onChange={(e) => set("rua", e.target.value)}
              required={required}
              maxLength={150}
            />
          </Field>
          <Field label="Número" htmlFor="numero">
            <Input id="numero" value={value.numero} onChange={(e) => set("numero", e.target.value)} maxLength={30} />
          </Field>
          <Field label="Complemento" htmlFor="complemento">
            <Input
              id="complemento"
              value={value.complemento}
              onChange={(e) => set("complemento", e.target.value)}
              maxLength={150}
            />
          </Field>
          <Field label="Ponto de referência" htmlFor="pontoReferencia" className="sm:col-span-2">
            <Input
              id="pontoReferencia"
              value={value.pontoReferencia}
              onChange={(e) => set("pontoReferencia", e.target.value)}
              maxLength={150}
            />
          </Field>
        </div>
      )}
    </div>
  )
}

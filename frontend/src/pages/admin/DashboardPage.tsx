import useSWR from "swr"
import { dashboardService } from "@/services/dashboardService"
import { Card, Loading, ErrorMessage } from "@/components/ui"
import { formatCurrency } from "@/utils/format"

function StatCard({
  label,
  value,
  accent,
}: {
  label: string
  value: string
  accent?: string
}) {
  return (
    <Card className="p-5">
      <p className="text-sm text-muted">{label}</p>
      <p className={`mt-2 font-serif text-3xl font-bold ${accent ?? "text-foreground"}`}>{value}</p>
    </Card>
  )
}

export default function DashboardPage() {
  const { data, error, isLoading, mutate } = useSWR("dashboard-resumo", () =>
    dashboardService.resumo(),
  )

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="font-serif text-2xl font-bold text-foreground">Dashboard</h1>
        <p className="text-sm text-muted">Resumo das vendas de hoje.</p>
      </div>

      {isLoading && <Loading />}
      {error && <ErrorMessage message="Não foi possível carregar o resumo." onRetry={() => mutate()} />}

      {data && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <StatCard label="Pedidos hoje" value={String(data.pedidosHoje)} accent="text-primary" />
          <StatCard label="Valor vendido hoje" value={formatCurrency(data.valorVendidoHoje)} accent="text-accent" />
          <StatCard label="Pedidos em aberto" value={String(data.pedidosEmAberto)} />
          <StatCard label="Pedidos entregues" value={String(data.pedidosEntregues)} accent="text-accent" />
          <StatCard label="Pedidos cancelados" value={String(data.pedidosCancelados)} accent="text-danger" />
        </div>
      )}
    </div>
  )
}

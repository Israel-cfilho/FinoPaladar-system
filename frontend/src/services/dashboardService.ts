import { api } from "./api"
import type { DashboardResumoResponse } from "@/types"

export const dashboardService = {
  async resumo(): Promise<DashboardResumoResponse> {
    const { data } = await api.get<DashboardResumoResponse>("/api/admin/dashboard")
    return data
  },
}

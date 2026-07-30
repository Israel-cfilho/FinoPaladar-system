import { api } from "./api"
import type { LoginRequest, LoginResponse } from "@/types"

export const authService = {
  async login(payload: LoginRequest): Promise<LoginResponse> {
    const { data } = await api.post<LoginResponse>("/api/auth/login", payload)
    return data
  },
}

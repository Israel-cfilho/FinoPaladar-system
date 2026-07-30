import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from "react"
import { TOKEN_KEY } from "@/services/api"
import { authService } from "@/services/authService"
import type { AdministradorAutenticadoResponse, LoginRequest } from "@/types"

const ADMIN_KEY = "finopaladar_admin"

interface AuthContextValue {
  token: string | null
  admin: AdministradorAutenticadoResponse | null
  isAuthenticated: boolean
  login: (payload: LoginRequest) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

function readStoredAdmin(): AdministradorAutenticadoResponse | null {
  try {
    const raw = localStorage.getItem(ADMIN_KEY)
    return raw ? (JSON.parse(raw) as AdministradorAutenticadoResponse) : null
  } catch {
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY))
  const [admin, setAdmin] = useState<AdministradorAutenticadoResponse | null>(() => readStoredAdmin())

  const login = useCallback(async (payload: LoginRequest) => {
    const response = await authService.login(payload)
    localStorage.setItem(TOKEN_KEY, response.token)
    localStorage.setItem(ADMIN_KEY, JSON.stringify(response.administrador))
    setToken(response.token)
    setAdmin(response.administrador)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(ADMIN_KEY)
    setToken(null)
    setAdmin(null)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({ token, admin, isAuthenticated: Boolean(token), login, logout }),
    [token, admin, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error("useAuth deve ser usado dentro de <AuthProvider>")
  return ctx
}

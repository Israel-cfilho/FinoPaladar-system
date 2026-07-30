import axios from "axios"

const baseURL = import.meta.env.VITE_API_URL ?? "http://localhost:8080"

export const TOKEN_KEY = "finopaladar_token"

export const api = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
  },
})

// Interceptor: envia Authorization: Bearer {token} quando existir um token salvo.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Interceptor de resposta: em caso de 401 em rota administrativa, limpa o token.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status
    const url: string = error?.config?.url ?? ""
    if (status === 401 && url.includes("/admin")) {
      localStorage.removeItem(TOKEN_KEY)
    }
    return Promise.reject(error)
  },
)

// Extrai uma mensagem de erro amigável a partir de uma resposta do backend.
export function extractErrorMessage(error: unknown, fallback = "Ocorreu um erro. Tente novamente."): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as
      | { message?: string; erro?: string; error?: string; detail?: string }
      | undefined
    return data?.message ?? data?.erro ?? data?.error ?? data?.detail ?? error.message ?? fallback
  }
  if (error instanceof Error) return error.message
  return fallback
}

// Resolve a URL de exibição de uma imagem retornada pela API.
export function resolveImageUrl(imagem: string | null | undefined): string | null {
  if (!imagem) return null
  if (imagem.startsWith("http://") || imagem.startsWith("https://")) return imagem
  return `${baseURL}${imagem.startsWith("/") ? "" : "/"}${imagem}`
}

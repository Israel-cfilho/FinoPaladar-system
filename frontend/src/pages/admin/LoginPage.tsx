import { useState, type FormEvent } from "react"
import { useNavigate, useLocation } from "react-router-dom"
import { useAuth } from "../../hooks/useAuth"
import { Button, Input, Field, Card } from "../../components/ui"

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = (location.state as { from?: string })?.from ?? "/admin/dashboard"

  const [email, setEmail] = useState("")
  const [senha, setSenha] = useState("")
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await login({ email, senha })
      navigate(from, { replace: true })
    } catch (err) {
      const message =
        (err as { response?: { data?: { mensagem?: string } } })?.response?.data?.mensagem ??
        "Email ou senha invalidos."
      setError(message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-background px-4">
      <div className="w-full max-w-md">
        <div className="mb-8 text-center">
          <h1 className="font-serif text-3xl font-bold text-primary">Fino Paladar</h1>
          <p className="mt-1 text-sm text-muted">Painel administrativo</p>
        </div>
        <Card className="p-8">
          <form onSubmit={handleSubmit} className="flex flex-col gap-5">
            <Field label="Email" htmlFor="email" required>
              <Input
                id="email"
                type="email"
                autoComplete="username"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </Field>
            <Field label="Senha" htmlFor="senha" required>
              <Input
                id="senha"
                type="password"
                autoComplete="current-password"
                value={senha}
                onChange={(e) => setSenha(e.target.value)}
                required
              />
            </Field>
            {error && (
              <p className="rounded-md bg-danger/10 px-3 py-2 text-sm text-danger" role="alert">
                {error}
              </p>
            )}
            <Button type="submit" loading={loading} className="w-full">
              Entrar
            </Button>
          </form>
        </Card>
      </div>
    </div>
  )
}

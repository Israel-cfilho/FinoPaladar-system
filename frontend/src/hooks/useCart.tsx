import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react"
import type { ProdutoPublicoResponse } from "@/types"

export interface CartItem {
  produtoId: number
  nome: string
  preco: number
  pesoMedioGramas: number
  imagem: string | null
  quantidade: number
  quantidadeDisponivel: number
}

interface CartContextValue {
  items: CartItem[]
  totalItens: number
  subtotalEstimado: number
  addItem: (produto: ProdutoPublicoResponse, quantidade?: number) => void
  updateQuantidade: (produtoId: number, quantidade: number) => void
  removeItem: (produtoId: number) => void
  clear: () => void
}

const CartContext = createContext<CartContextValue | undefined>(undefined)
const CART_KEY = "finopaladar_cart"

function readStoredCart(): CartItem[] {
  try {
    const raw = localStorage.getItem(CART_KEY)
    return raw ? (JSON.parse(raw) as CartItem[]) : []
  } catch {
    return []
  }
}

export function CartProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<CartItem[]>(() => readStoredCart())

  useEffect(() => {
    localStorage.setItem(CART_KEY, JSON.stringify(items))
  }, [items])

  const addItem = useCallback((produto: ProdutoPublicoResponse, quantidade = 1) => {
    setItems((prev) => {
      const existing = prev.find((i) => i.produtoId === produto.id)
      const max = produto.quantidadeDisponivel
      if (existing) {
        const novaQtd = Math.min(existing.quantidade + quantidade, max)
        return prev.map((i) => (i.produtoId === produto.id ? { ...i, quantidade: novaQtd } : i))
      }
      return [
        ...prev,
        {
          produtoId: produto.id,
          nome: produto.nome,
          preco: produto.preco,
          pesoMedioGramas: produto.pesoMedioGramas,
          imagem: produto.imagem,
          quantidade: Math.min(quantidade, max) || 1,
          quantidadeDisponivel: max,
        },
      ]
    })
  }, [])

  const updateQuantidade = useCallback((produtoId: number, quantidade: number) => {
    setItems((prev) =>
      prev.map((i) =>
        i.produtoId === produtoId
          ? { ...i, quantidade: Math.max(1, Math.min(quantidade, i.quantidadeDisponivel)) }
          : i,
      ),
    )
  }, [])

  const removeItem = useCallback((produtoId: number) => {
    setItems((prev) => prev.filter((i) => i.produtoId !== produtoId))
  }, [])

  const clear = useCallback(() => setItems([]), [])

  const totalItens = useMemo(() => items.reduce((sum, i) => sum + i.quantidade, 0), [items])
  const subtotalEstimado = useMemo(
    () => items.reduce((sum, i) => sum + i.preco * i.quantidade, 0),
    [items],
  )

  const value = useMemo<CartContextValue>(
    () => ({ items, totalItens, subtotalEstimado, addItem, updateQuantidade, removeItem, clear }),
    [items, totalItens, subtotalEstimado, addItem, updateQuantidade, removeItem, clear],
  )

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}

export function useCart(): CartContextValue {
  const ctx = useContext(CartContext)
  if (!ctx) throw new Error("useCart deve ser usado dentro de <CartProvider>")
  return ctx
}

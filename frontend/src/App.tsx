import { Navigate, Route, Routes } from "react-router-dom"
import { AuthProvider } from "@/hooks/useAuth"
import { CartProvider } from "@/hooks/useCart"
import { ProtectedRoute } from "@/components/ProtectedRoute"
import { PublicLayout } from "@/layouts/PublicLayout"
import { AdminLayout } from "@/layouts/AdminLayout"

import { HomePage } from "@/pages/HomePage"
import { CartPage } from "@/pages/CartPage"
import { CheckoutPage } from "@/pages/CheckoutPage"
import { OrderConfirmationPage } from "@/pages/OrderConfirmationPage"
import { TrackOrderPage } from "@/pages/TrackOrderPage"

import { LoginPage } from "@/pages/admin/LoginPage"
import { DashboardPage } from "@/pages/admin/DashboardPage"
import { ProdutosPage } from "@/pages/admin/ProdutosPage"
import { DisponibilidadePage } from "@/pages/admin/DisponibilidadePage"
import { PedidosPage } from "@/pages/admin/PedidosPage"
import { VendaManualPage } from "@/pages/admin/VendaManualPage"
import { RelatoriosPage } from "@/pages/admin/RelatoriosPage"

export default function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <Routes>
          {/* Área pública */}
          <Route element={<PublicLayout />}>
            <Route path="/" element={<HomePage />} />
            <Route path="/carrinho" element={<CartPage />} />
            <Route path="/checkout" element={<CheckoutPage />} />
            <Route path="/pedido/:codigo" element={<OrderConfirmationPage />} />
            <Route path="/acompanhar" element={<TrackOrderPage />} />
            <Route path="/acompanhar/:codigo" element={<TrackOrderPage />} />
          </Route>

          {/* Login admin */}
          <Route path="/admin/login" element={<LoginPage />} />

          {/* Área administrativa protegida */}
          <Route element={<ProtectedRoute />}>
            <Route element={<AdminLayout />}>
              <Route path="/admin" element={<DashboardPage />} />
              <Route path="/admin/produtos" element={<ProdutosPage />} />
              <Route path="/admin/disponibilidade" element={<DisponibilidadePage />} />
              <Route path="/admin/pedidos" element={<PedidosPage />} />
              <Route path="/admin/venda-manual" element={<VendaManualPage />} />
              <Route path="/admin/relatorios" element={<RelatoriosPage />} />
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </CartProvider>
    </AuthProvider>
  )
}

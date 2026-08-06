import { Navigate, Route, Routes } from 'react-router-dom';
import { AdminLayout } from '@/layouts/AdminLayout';
import { PublicLayout } from '@/layouts/PublicLayout';
import { AdminDashboardPage } from '@/pages/admin/AdminDashboardPage';
import { AdminLoginPage } from '@/pages/admin/AdminLoginPage';
import { CatalogoPage } from '@/pages/public/CatalogoPage';
import { HomePage } from '@/pages/public/HomePage';

export function AppRoutes() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        <Route index element={<HomePage />} />
        <Route path="catalogo" element={<CatalogoPage />} />
      </Route>

      <Route path="admin/login" element={<AdminLoginPage />} />

      <Route path="admin" element={<AdminLayout />}>
        <Route index element={<AdminDashboardPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

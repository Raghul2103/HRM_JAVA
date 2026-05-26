import React from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Sidebar from './components/layout/Sidebar';
import Navbar from './components/layout/Navbar';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import WorkerManagement from './pages/WorkerManagement';
import SiteManagement from './pages/SiteManagement';
import AttendanceDashboard from './pages/AttendanceDashboard';
import AttendanceHistory from './pages/AttendanceHistory';
import OvertimeSummary from './pages/OvertimeSummary';
import ActiveWorkers from './pages/ActiveWorkers';
import { KeyRound } from 'lucide-react';
import './index.css';

// Protected Route component
const ProtectedRoute = ({ children, allowedRoles }) => {
  const { isAuthenticated, user } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
};

// Layout Wrapper
const AppLayout = ({ children, title }) => {
  const [isSidebarOpen, setIsSidebarOpen] = React.useState(false);

  return (
    <div className="flex h-screen bg-slate-50 overflow-hidden">
      <Sidebar isOpen={isSidebarOpen} onClose={() => setIsSidebarOpen(false)} />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Navbar title={title} onMenuToggle={() => setIsSidebarOpen(true)} />
        <main className="flex-1 overflow-y-auto px-4 py-6 md:px-8 md:py-8">
          {children}
        </main>
      </div>
    </div>
  );
};

// Unauthorized Page
const Unauthorized = () => {
  return (
    <div className="min-h-screen bg-slate-950 flex flex-col justify-center items-center px-4 relative overflow-hidden">
      <div className="absolute top-0 -left-4 w-96 h-96 bg-rose-500/10 rounded-full blur-3xl pointer-events-none" />
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-8 max-w-md text-center shadow-2xl relative z-10 space-y-6">
        <div className="inline-flex p-4 bg-rose-500/10 text-rose-500 border border-rose-500/20 rounded-2xl shadow-lg animate-bounce">
          <KeyRound className="w-10 h-10" />
        </div>
        <div className="space-y-2">
          <h2 className="text-2xl font-extrabold text-white tracking-tight">Access Restricted</h2>
          <p className="text-sm text-slate-400">
            You do not possess the necessary credentials to view this page. Contact administration if you believe this is an error.
          </p>
        </div>
        <button
          onClick={() => window.location.href = '/'}
          className="w-full bg-slate-800 hover:bg-slate-700 text-white font-semibold py-2.5 px-4 rounded-xl text-sm transition-all border border-slate-700 cursor-pointer"
        >
          Return to Dashboard
        </button>
      </div>
    </div>
  );
};

const App = () => {
  const allRoles = ['ROLE_ADMIN', 'ROLE_HR', 'ROLE_SITE_SUPERVISOR', 'ROLE_PAYROLL_OPERATOR', 'ROLE_SITE_MANAGER'];

  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/unauthorized" element={<Unauthorized />} />

          <Route path="/" element={
            <ProtectedRoute allowedRoles={allRoles}>
              <AppLayout title="Dashboard Metrics"><Dashboard /></AppLayout>
            </ProtectedRoute>
          } />

          <Route path="/workers" element={
            <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_HR']}>
              <AppLayout title="Worker Profiles Directory"><WorkerManagement /></AppLayout>
            </ProtectedRoute>
          } />

          <Route path="/sites" element={
            <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_HR']}>
              <AppLayout title="Site Directories"><SiteManagement /></AppLayout>
            </ProtectedRoute>
          } />

          <Route path="/attendance" element={
            <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_SITE_SUPERVISOR']}>
              <AppLayout title="Real-Time Attendance Control"><AttendanceDashboard /></AppLayout>
            </ProtectedRoute>
          } />

          <Route path="/active-workers" element={
            <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_SITE_SUPERVISOR', 'ROLE_SITE_MANAGER']}>
              <AppLayout title="Active On-Site Crews"><ActiveWorkers /></AppLayout>
            </ProtectedRoute>
          } />

          <Route path="/history" element={
            <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_HR', 'ROLE_SITE_MANAGER']}>
              <AppLayout title="Attendance History Ledger"><AttendanceHistory /></AppLayout>
            </ProtectedRoute>
          } />

          <Route path="/overtime" element={
            <ProtectedRoute allowedRoles={['ROLE_ADMIN', 'ROLE_PAYROLL_OPERATOR', 'ROLE_SITE_MANAGER']}>
              <AppLayout title="Overtime Ledgers &amp; Settlements"><OvertimeSummary /></AppLayout>
            </ProtectedRoute>
          } />

          {/* Catch-all */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
};

createRoot(document.getElementById('root')).render(<App />);

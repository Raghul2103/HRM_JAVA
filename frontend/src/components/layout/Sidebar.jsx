import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { 
  Users, 
  MapPin, 
  ClipboardCheck, 
  Clock, 
  History, 
  DollarSign, 
  LayoutDashboard,
  LogOut,
  X
} from 'lucide-react';

const Sidebar = ({ isOpen, onClose }) => {
  const { user, logout } = useAuth();

  if (!user) return null;

  const links = [
    {
      to: '/',
      label: 'Dashboard',
      icon: LayoutDashboard,
      roles: ['ROLE_ADMIN', 'ROLE_HR', 'ROLE_SITE_SUPERVISOR', 'ROLE_PAYROLL_OPERATOR', 'ROLE_SITE_MANAGER']
    },
    {
      to: '/workers',
      label: 'Workers',
      icon: Users,
      roles: ['ROLE_ADMIN', 'ROLE_HR']
    },
    {
      to: '/sites',
      label: 'Sites',
      icon: MapPin,
      roles: ['ROLE_ADMIN', 'ROLE_HR']
    },
    {
      to: '/attendance',
      label: 'Attendance Panel',
      icon: ClipboardCheck,
      roles: ['ROLE_ADMIN', 'ROLE_SITE_SUPERVISOR']
    },
    {
      to: '/active-workers',
      label: 'Active Workers',
      icon: Clock,
      roles: ['ROLE_ADMIN', 'ROLE_SITE_SUPERVISOR', 'ROLE_SITE_MANAGER']
    },
    {
      to: '/history',
      label: 'Attendance History',
      icon: History,
      roles: ['ROLE_ADMIN', 'ROLE_HR', 'ROLE_SITE_MANAGER']
    },
    {
      to: '/overtime',
      label: 'Overtime & Payouts',
      icon: DollarSign,
      roles: ['ROLE_ADMIN', 'ROLE_PAYROLL_OPERATOR', 'ROLE_SITE_MANAGER']
    }
  ];

  return (
    <>
      {/* Mobile Sidebar Backdrop Overlay */}
      {isOpen && (
        <div 
          className="fixed inset-0 bg-slate-950/60 z-40 md:hidden backdrop-blur-sm transition-opacity duration-300"
          onClick={onClose}
        />
      )}
      
      <aside 
        className={`fixed md:static inset-y-0 left-0 w-64 bg-slate-900 text-slate-100 flex flex-col h-full border-r border-slate-800 z-50 transform transition-transform duration-300 ease-in-out md:translate-x-0 ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="p-6 border-b border-slate-800 flex items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="bg-sky-500 text-white p-2 rounded-lg font-bold shadow-md shadow-sky-500/20">
              HR
            </div>
            <div>
              <h1 className="font-bold text-lg leading-none tracking-tight">Workforce</h1>
              <span className="text-xs text-slate-400">Construction HRMS</span>
            </div>
          </div>
          {/* Mobile close button */}
          <button 
            onClick={onClose}
            className="md:hidden p-1 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>
        
        <nav className="flex-1 px-4 py-6 space-y-1 overflow-y-auto">
          {links.map((link) => {
            if (!link.roles.includes(user.role)) return null;
            const Icon = link.icon;
            return (
              <NavLink
                key={link.to}
                to={link.to}
                onClick={onClose} // Auto-close on mobile when link is clicked
                className={({ isActive }) =>
                  `flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition-all ${
                    isActive
                      ? 'bg-sky-500 text-white shadow-lg shadow-sky-500/20'
                      : 'text-slate-400 hover:bg-slate-800 hover:text-slate-100'
                  }`
                }
              >
                <Icon className="w-5 h-5" />
                {link.label}
              </NavLink>
            );
          })}
        </nav>

        <div className="p-4 border-t border-slate-800">
          <button
            onClick={() => {
              onClose();
              logout();
            }}
            className="flex items-center gap-3 w-full px-4 py-3 rounded-lg text-sm font-medium text-rose-400 hover:bg-rose-950/20 hover:text-rose-300 transition-all"
          >
            <LogOut className="w-5 h-5" />
            Logout
          </button>
        </div>
      </aside>
    </>
  );
};

export default Sidebar;

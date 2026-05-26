import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { User as UserIcon, Menu } from 'lucide-react';

const Navbar = ({ title, onMenuToggle }) => {
  const { user } = useAuth();

  if (!user) return null;

  const roleLabels = {
    'ROLE_ADMIN': 'System Admin',
    'ROLE_HR': 'HR Manager',
    'ROLE_SITE_SUPERVISOR': 'Site Supervisor',
    'ROLE_PAYROLL_OPERATOR': 'Payroll Operator',
    'ROLE_SITE_MANAGER': 'Site Manager'
  };

  const roleBadgeColors = {
    'ROLE_ADMIN': 'bg-purple-100 text-purple-700 border-purple-200',
    'ROLE_HR': 'bg-emerald-100 text-emerald-700 border-emerald-200',
    'ROLE_SITE_SUPERVISOR': 'bg-amber-100 text-amber-700 border-amber-200',
    'ROLE_PAYROLL_OPERATOR': 'bg-blue-100 text-blue-700 border-blue-200',
    'ROLE_SITE_MANAGER': 'bg-sky-100 text-sky-700 border-sky-200'
  };

  return (
    <header className="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-4 md:px-8 shadow-sm">
      <div className="flex items-center gap-3">
        {/* Toggle Menu Button for Mobile */}
        <button 
          onClick={onMenuToggle}
          className="md:hidden p-2 text-slate-600 hover:text-slate-900 rounded-lg hover:bg-slate-100 transition-colors"
          title="Toggle Navigation Menu"
        >
          <Menu className="w-5 h-5" />
        </button>
        <h2 className="text-lg md:text-xl font-semibold text-slate-800 tracking-tight truncate max-w-[150px] sm:max-w-xs md:max-w-none">
          {title || 'Dashboard'}
        </h2>
      </div>
      
      <div className="flex items-center gap-2 md:gap-4">
        <div className="text-right">
          <div className="text-sm font-medium text-slate-700 truncate max-w-[100px] sm:max-w-none">{user.username}</div>
          <div className="text-[10px] text-slate-400">
            <span className={`px-2 py-0.5 rounded-full border font-semibold tracking-wider uppercase ${roleBadgeColors[user.role] || 'bg-slate-100 text-slate-700'}`}>
              {roleLabels[user.role] || user.role}
            </span>
          </div>
        </div>
        <div className="w-8 h-8 md:w-10 md:h-10 rounded-full bg-slate-100 border border-slate-200 flex items-center justify-center text-slate-600 shadow-inner">
          <UserIcon className="w-4 h-4 md:w-5 md:h-5" />
        </div>
      </div>
    </header>
  );
};

export default Navbar;

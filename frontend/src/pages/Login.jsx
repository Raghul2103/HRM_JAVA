import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ShieldAlert, LogIn } from 'lucide-react';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const { login, error, loading } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!username || !password) return;
    const success = await login(username, password);
    if (success) {
      navigate('/');
    }
  };

  const handleQuickLogin = (uname, pass) => {
    setUsername(uname);
    setPassword(pass);
  };

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col justify-center py-12 sm:px-6 lg:px-8 relative overflow-hidden">
      {/* Background blobs for premium styling */}
      <div className="absolute top-0 -left-4 w-96 h-96 bg-sky-500/10 rounded-full blur-3xl" />
      <div className="absolute bottom-0 right-0 w-96 h-96 bg-purple-500/10 rounded-full blur-3xl" />

      <div className="sm:mx-auto sm:w-full sm:max-w-md relative z-10">
        <div className="flex justify-center">
          <div className="bg-gradient-to-tr from-sky-500 to-indigo-600 text-white p-3.5 rounded-2xl shadow-xl shadow-sky-500/20 font-black text-2xl tracking-wider">
            HRMS
          </div>
        </div>
        <h2 className="mt-6 text-center text-3xl font-extrabold text-white tracking-tight">
          Workforce Portal
        </h2>
        <p className="mt-2 text-center text-sm text-slate-400">
          Construction HR &amp; Attendance Settlements
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md relative z-10">
        <div className="bg-slate-900/80 backdrop-blur-md py-8 px-4 shadow-2xl border border-slate-800 sm:rounded-2xl sm:px-10">
          <form className="space-y-6" onSubmit={handleSubmit}>
            {error && (
              <div className="bg-rose-500/10 border border-rose-500/20 rounded-xl p-4 flex items-start gap-3">
                <ShieldAlert className="w-5 h-5 text-rose-400 shrink-0 mt-0.5" />
                <div className="text-sm text-rose-300 font-medium">{error}</div>
              </div>
            )}

            <div>
              <label htmlFor="username" className="block text-sm font-semibold text-slate-300">
                Username
              </label>
              <input
                id="username"
                name="username"
                type="text"
                required
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="mt-1.5 block w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-sky-500 focus:border-sky-500 transition-all text-sm"
                placeholder="Enter operator username"
              />
            </div>

            <div>
              <label htmlFor="password" className="block text-sm font-semibold text-slate-300">
                Password
              </label>
              <input
                id="password"
                name="password"
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="mt-1.5 block w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-sky-500 focus:border-sky-500 transition-all text-sm"
                placeholder="••••••••"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full flex justify-center items-center gap-2 bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-white px-4 py-3 rounded-xl text-sm font-semibold shadow-lg shadow-sky-500/25 focus:outline-none focus:ring-2 focus:ring-sky-500 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? (
                'Authenticating...'
              ) : (
                <>
                  <LogIn className="w-4 h-4" />
                  Sign In
                </>
              )}
            </button>
          </form>

          {/* Quick Logins for Seeding */}
          <div className="mt-8 pt-6 border-t border-slate-800">
            <span className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-3">
              Quick Operator Logins
            </span>
            <div className="grid grid-cols-2 gap-2 text-xs">
              <button
                onClick={() => handleQuickLogin('admin', 'admin123')}
                className="bg-slate-950 hover:bg-slate-800/80 text-slate-300 py-2 px-2.5 rounded-lg border border-slate-800 text-left transition-all"
              >
                <strong>Admin:</strong> admin/admin123
              </button>
              <button
                onClick={() => handleQuickLogin('hr_user', 'hr123')}
                className="bg-slate-950 hover:bg-slate-800/80 text-slate-300 py-2 px-2.5 rounded-lg border border-slate-800 text-left transition-all"
              >
                <strong>HR:</strong> hr_user/hr123
              </button>
              <button
                onClick={() => handleQuickLogin('supervisor', 'super123')}
                className="bg-slate-950 hover:bg-slate-800/80 text-slate-300 py-2 px-2.5 rounded-lg border border-slate-800 text-left transition-all"
              >
                <strong>Supervisor:</strong> supervisor/super123
              </button>
              <button
                onClick={() => handleQuickLogin('payroll', 'pay123')}
                className="bg-slate-950 hover:bg-slate-800/80 text-slate-300 py-2 px-2.5 rounded-lg border border-slate-800 text-left transition-all"
              >
                <strong>Payroll:</strong> payroll/pay123
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;

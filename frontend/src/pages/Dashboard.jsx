import React, { useState, useEffect } from 'react';
import client from '../api/client';
import { useAuth } from '../context/AuthContext';
import { 
  Users, 
  MapPin, 
  Activity, 
  ShieldCheck, 
  AlertTriangle 
} from 'lucide-react';

const Dashboard = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState({
    totalWorkers: 0,
    activeWorkersCount: 0,
    totalSites: 0,
    flaggedLogsCount: 0
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDashboardStats = async () => {
      try {
        setLoading(true);
        // Load workers
        let workersRes = { data: { data: [] } };
        try {
          workersRes = await client.get('/api/workers');
        } catch (e) {
          console.warn("Workers fetch skipped or restricted:", e.message);
        }

        // Load sites
        let sitesRes = { data: { data: [] } };
        try {
          sitesRes = await client.get('/api/sites');
        } catch (e) {
          console.warn("Sites fetch skipped or restricted:", e.message);
        }

        // Load active workers
        let activeRes = { data: { data: [] } };
        try {
          activeRes = await client.get('/api/attendance/active');
        } catch (e) {
          console.warn("Active workers fetch skipped or restricted:", e.message);
        }

        // Load logs for checking flagged ones
        let logsRes = { data: { data: { content: [] } } };
        try {
          logsRes = await client.get('/api/attendance/log?page=0&size=100');
        } catch (e) {
          console.warn("Logs fetch skipped or restricted:", e.message);
        }

        const workersList = workersRes.data?.data || [];
        const sitesList = sitesRes.data?.data || [];
        const activeList = activeRes.data?.data || [];
        const logsList = logsRes.data?.data?.content || [];

        setStats({
          totalWorkers: workersList.length,
          activeWorkersCount: activeList.length,
          totalSites: sitesList.length,
          flaggedLogsCount: logsList.filter(l => l.flagged).length
        });
      } catch (err) {
        console.error('Error fetching dashboard stats', err);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardStats();
  }, [user]);

  const cards = [
    {
      title: 'Active On-Site',
      value: stats.activeWorkersCount,
      desc: 'Workers currently clocked in',
      icon: Activity,
      color: 'from-sky-500 to-sky-600',
      shadow: 'shadow-sky-500/20'
    },
    {
      title: 'Total Workforce',
      value: stats.totalWorkers,
      desc: 'Registered worker profiles',
      icon: Users,
      color: 'from-indigo-500 to-indigo-600',
      shadow: 'shadow-indigo-500/20'
    },
    {
      title: 'Construction Sites',
      value: stats.totalSites,
      desc: 'Total active work sites',
      icon: MapPin,
      color: 'from-emerald-500 to-emerald-600',
      shadow: 'shadow-emerald-500/20'
    },
    {
      title: 'Flagged Shifts',
      value: stats.flaggedLogsCount,
      desc: 'Shifts exceeding 16 hours',
      icon: AlertTriangle,
      color: 'from-rose-500 to-rose-600',
      shadow: 'shadow-rose-500/20'
    }
  ];

  return (
    <div className="space-y-8">
      {/* Welcome Card */}
      <div className="bg-gradient-to-r from-slate-900 via-slate-800 to-slate-900 rounded-2xl p-8 border border-slate-700 shadow-xl relative overflow-hidden">
        <div className="absolute top-0 right-0 w-64 h-64 bg-sky-500/10 rounded-full blur-3xl pointer-events-none" />
        <div className="relative z-10 space-y-2">
          <div className="flex items-center gap-2 text-sky-400">
            <ShieldCheck className="w-5 h-5" />
            <span className="text-sm font-semibold tracking-wider uppercase">System Active</span>
          </div>
          <h1 className="text-3xl font-bold text-white leading-tight">
            Welcome back, {user?.username}!
          </h1>
          <p className="text-slate-400 max-w-xl">
            Monitor real-time shifts, run monthly settlements, and manage daily operations from your control panel.
          </p>
        </div>
      </div>

      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="bg-white border border-slate-200 rounded-2xl p-6 h-32 animate-pulse space-y-4">
              <div className="h-4 bg-slate-200 rounded w-1/3" />
              <div className="h-8 bg-slate-200 rounded w-1/2" />
            </div>
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {cards.map((card) => {
            const Icon = card.icon;
            return (
              <div 
                key={card.title}
                className="bg-white border border-slate-100 rounded-2xl p-6 shadow-sm hover:shadow-md transition-all flex items-start justify-between relative overflow-hidden group"
              >
                <div className="space-y-2">
                  <span className="text-sm font-medium text-slate-400">{card.title}</span>
                  <div className="text-3xl font-bold text-slate-800 tracking-tight">{card.value}</div>
                  <span className="text-xs text-slate-400 block">{card.desc}</span>
                </div>
                <div className={`p-3.5 rounded-xl bg-gradient-to-tr ${card.color} text-white shadow-lg ${card.shadow} group-hover:scale-105 transition-all`}>
                  <Icon className="w-6 h-6" />
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default Dashboard;

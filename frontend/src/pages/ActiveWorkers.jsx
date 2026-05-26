import React, { useState, useEffect } from 'react';
import client from '../api/client';
import { Users, Clock, MapPin } from 'lucide-react';

const ActiveWorkers = () => {
  const [activeWorkers, setActiveWorkers] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchActive = async () => {
    setLoading(true);
    try {
      const res = await client.get('/api/attendance/active');
      setActiveWorkers(res.data.data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchActive();
  }, []);

  return (
    <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm flex flex-col">
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-2">
          <div className="p-2 bg-emerald-100 text-emerald-600 rounded-lg">
            <Users className="w-5 h-5" />
          </div>
          <h3 className="font-bold text-slate-800 text-lg">Active Clocked-In Crews</h3>
        </div>
        <span className="bg-sky-50 text-sky-600 px-3 py-1 rounded-full text-xs font-semibold uppercase border border-sky-100 tracking-wider">
          {activeWorkers.length} Live On Site
        </span>
      </div>

      {loading ? (
        <div className="p-8 text-center text-slate-400">Loading live list...</div>
      ) : activeWorkers.length === 0 ? (
        <div className="flex-1 flex flex-col items-center justify-center py-12 text-center text-slate-400">
          <Clock className="w-12 h-12 text-slate-200 mb-3" />
          <p className="font-semibold text-slate-500">No active shifts currently.</p>
          <p className="text-xs text-slate-400 mt-1">Supervisors will clock in crews at the site.</p>
        </div>
      ) : (
        <div className="overflow-x-auto flex-1">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-100">
                <th className="px-6 py-3.5 text-xs font-bold text-slate-400 uppercase tracking-wider">Worker</th>
                <th className="px-6 py-3.5 text-xs font-bold text-slate-400 uppercase tracking-wider">Site</th>
                <th className="px-6 py-3.5 text-xs font-bold text-slate-400 uppercase tracking-wider">Clocked In At</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {activeWorkers.map((aw) => (
                <tr key={aw.workerId} className="hover:bg-slate-50/50 transition-all text-sm">
                  <td className="px-6 py-3.5">
                    <div className="font-semibold text-slate-800">{aw.workerName}</div>
                    <div className="text-[10px] text-slate-400 font-mono tracking-tight uppercase">
                      {aw.designation} &bull; {aw.workerPhone}
                    </div>
                  </td>
                  <td className="px-6 py-3.5 text-slate-600 font-medium">
                    <span className="flex items-center gap-1">
                      <MapPin className="w-3.5 h-3.5 text-slate-400" />
                      {aw.siteName}
                    </span>
                  </td>
                  <td className="px-6 py-3.5 text-slate-500 font-mono text-xs">
                    {new Date(aw.clockIn).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default ActiveWorkers;

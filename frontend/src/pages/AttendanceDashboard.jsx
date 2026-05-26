import React, { useState, useEffect } from 'react';
import client from '../api/client';
import { ClipboardCheck, Users, MapPin, Play, LogOut, CheckCircle, ShieldAlert } from 'lucide-react';

const AttendanceDashboard = () => {
  const [activeWorkers, setActiveWorkers] = useState([]);
  const [workers, setWorkers] = useState([]);
  const [sites, setSites] = useState([]);
  const [loading, setLoading] = useState(false);

  // Form State
  const [selectedWorkerId, setSelectedWorkerId] = useState('');
  const [selectedSiteId, setSelectedSiteId] = useState('');
  const [message, setMessage] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  const fetchActiveWorkers = async () => {
    try {
      const res = await client.get('/api/attendance/active');
      setActiveWorkers(res.data.data);
    } catch (e) {
      console.error('Error fetching active workers', e);
    }
  };

  const fetchWorkerAndSiteOptions = async () => {
    try {
      const [workersRes, sitesRes] = await Promise.all([
        client.get('/api/workers'),
        client.get('/api/sites')
      ]);
      setWorkers(workersRes.data.data.filter(w => w.active));
      setSites(sitesRes.data.data.filter(s => s.active));
    } catch (e) {
      console.error('Error fetching dropdown choices', e);
    }
  };

  const loadData = async () => {
    setLoading(true);
    await Promise.all([fetchActiveWorkers(), fetchWorkerAndSiteOptions()]);
    setLoading(false);
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleClockIn = async (e) => {
    e.preventDefault();
    setMessage('');
    setErrorMsg('');

    if (!selectedWorkerId || !selectedSiteId) {
      setErrorMsg('Please select a worker and a site.');
      return;
    }

    try {
      const res = await client.post('/api/attendance/clock-in', {
        workerId: parseInt(selectedWorkerId),
        siteId: parseInt(selectedSiteId)
      });
      setMessage('Worker clocked in successfully!');
      setSelectedWorkerId('');
      setSelectedSiteId('');
      fetchActiveWorkers();
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to clock in worker';
      setErrorMsg(msg);
    }
  };

  const handleClockOut = async (workerId) => {
    setMessage('');
    setErrorMsg('');
    try {
      const res = await client.post('/api/attendance/clock-out', {
        workerId
      });
      const log = res.data.data;
      let successMsg = `Worker clocked out. Total hours: ${log.totalHours}h.`;
      if (log.overtimeHours > 0) {
        successMsg += ` Overtime: ${log.overtimeHours}h recorded.`;
      }
      if (log.flagged) {
        successMsg += ' WARNING: Shift exceeded 16 hours and is FLAGGED.';
      }
      setMessage(successMsg);
      fetchActiveWorkers();
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to clock out worker';
      setErrorMsg(msg);
    }
  };

  // Determine workers who are NOT clocked in
  const clockedInIds = activeWorkers.map(aw => aw.workerId);
  const inactiveWorkers = workers.filter(w => !clockedInIds.includes(w.id));

  return (
    <div className="space-y-8">
      {/* Notifications */}
      {(message || errorMsg) && (
        <div className="space-y-2 max-w-4xl">
          {message && (
            <div className="bg-emerald-500/10 border border-emerald-500/20 text-emerald-700 rounded-xl p-4 flex gap-3 text-sm font-semibold animate-fadeIn">
              <CheckCircle className="w-5 h-5 text-emerald-500 shrink-0" />
              <div>{message}</div>
            </div>
          )}
          {errorMsg && (
            <div className="bg-rose-500/10 border border-rose-500/20 text-rose-700 rounded-xl p-4 flex gap-3 text-sm font-semibold animate-fadeIn">
              <ShieldAlert className="w-5 h-5 text-rose-500 shrink-0" />
              <div>{errorMsg}</div>
            </div>
          )}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left Form: Clock In */}
        <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm h-fit">
          <div className="flex items-center gap-2 mb-6">
            <div className="p-2 bg-sky-100 text-sky-600 rounded-lg">
              <ClipboardCheck className="w-5 h-5" />
            </div>
            <h3 className="font-bold text-slate-800 text-lg">Clock-In Shift Entry</h3>
          </div>

          <form onSubmit={handleClockIn} className="space-y-5">
            <div>
              <label className="block text-xs font-bold text-slate-500 uppercase tracking-wide mb-1.5">
                Select Worker
              </label>
              <div className="relative">
                <Users className="absolute left-3.5 top-3 w-4 h-4 text-slate-400" />
                <select
                  value={selectedWorkerId}
                  onChange={(e) => setSelectedWorkerId(e.target.value)}
                  className="w-full bg-slate-50 border border-slate-200 rounded-xl pl-10 pr-4 py-2.5 focus:outline-none focus:ring-2 focus:ring-sky-500 text-sm"
                >
                  <option value="">-- Choose Worker --</option>
                  {inactiveWorkers.map(w => (
                    <option key={w.id} value={w.id}>
                      {w.name} ({w.designation} - {w.phone})
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-500 uppercase tracking-wide mb-1.5">
                Select Work Site
              </label>
              <div className="relative">
                <MapPin className="absolute left-3.5 top-3 w-4 h-4 text-slate-400" />
                <select
                  value={selectedSiteId}
                  onChange={(e) => setSelectedSiteId(e.target.value)}
                  className="w-full bg-slate-50 border border-slate-200 rounded-xl pl-10 pr-4 py-2.5 focus:outline-none focus:ring-2 focus:ring-sky-500 text-sm"
                >
                  <option value="">-- Choose Site --</option>
                  {sites.map(s => (
                    <option key={s.id} value={s.id}>
                      {s.siteName} ({s.location})
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <button
              type="submit"
              className="w-full flex items-center justify-center gap-2 bg-gradient-to-r from-sky-500 to-indigo-600 hover:from-sky-400 hover:to-indigo-500 text-white font-semibold py-3 rounded-xl text-sm shadow-lg shadow-sky-500/25 transition-all cursor-pointer"
            >
              <Play className="w-4 h-4 fill-white" />
              Clock In Worker
            </button>
          </form>
        </div>

        {/* Right Active List (Redis) */}
        <div className="lg:col-span-2 bg-white border border-slate-200 rounded-2xl p-6 shadow-sm flex flex-col">
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
              <ClipboardCheck className="w-12 h-12 text-slate-200 mb-3" />
              <p className="font-semibold text-slate-500">No active shifts currently.</p>
              <p className="text-xs text-slate-400 mt-1">Clock in workers to start tracking.</p>
            </div>
          ) : (
            <div className="overflow-x-auto flex-1">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-slate-50 border-b border-slate-100">
                    <th className="px-4 py-3 text-xs font-bold text-slate-400 uppercase tracking-wider">Worker</th>
                    <th className="px-4 py-3 text-xs font-bold text-slate-400 uppercase tracking-wider">Site</th>
                    <th className="px-4 py-3 text-xs font-bold text-slate-400 uppercase tracking-wider">Clocked In At</th>
                    <th className="px-4 py-3 text-xs font-bold text-slate-400 uppercase tracking-wider text-right">Checkout</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {activeWorkers.map((aw) => (
                    <tr key={aw.workerId} className="hover:bg-slate-50/50 transition-all text-sm">
                      <td className="px-4 py-3.5">
                        <div className="font-semibold text-slate-800">{aw.workerName}</div>
                        <div className="text-[10px] text-slate-400 font-mono tracking-tight uppercase">
                          {aw.designation} &bull; {aw.workerPhone}
                        </div>
                      </td>
                      <td className="px-4 py-3.5 text-slate-600 font-medium">
                        {aw.siteName}
                      </td>
                      <td className="px-4 py-3.5 text-slate-500 font-mono text-xs">
                        {new Date(aw.clockIn).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </td>
                      <td className="px-4 py-3.5 text-right">
                        <button
                          onClick={() => handleClockOut(aw.workerId)}
                          className="inline-flex items-center gap-1 bg-rose-50 hover:bg-rose-100 text-rose-600 hover:text-rose-700 border border-rose-100 font-semibold px-3 py-1.5 rounded-lg text-xs transition-all cursor-pointer"
                        >
                          <LogOut className="w-3.5 h-3.5" />
                          Clock Out
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default AttendanceDashboard;

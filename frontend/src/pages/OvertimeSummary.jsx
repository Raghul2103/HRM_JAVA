import React, { useState, useEffect } from 'react';
import client from '../api/client';
import { useAuth } from '../context/AuthContext';
import { DollarSign, Calendar, Users, CheckCircle, ShieldAlert, Award } from 'lucide-react';

const OvertimeSummary = () => {
  const { user } = useAuth();
  const [workers, setWorkers] = useState([]);
  const [selectedWorkerId, setSelectedWorkerId] = useState('');
  
  // Default to previous month
  const [selectedMonth, setSelectedMonth] = useState(() => {
    const d = new Date();
    d.setMonth(d.getMonth() - 1);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    return `${y}-${m}`;
  });

  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(false);
  const [settling, setSettling] = useState(false);
  const [message, setMessage] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  const fetchWorkers = async () => {
    try {
      const res = await client.get('/api/workers');
      setWorkers(res.data.data);
    } catch (e) {
      console.error(e);
    }
  };

  const fetchSummary = async () => {
    if (!selectedWorkerId || !selectedMonth) {
      setSummary(null);
      return;
    }
    setLoading(true);
    setMessage('');
    setErrorMsg('');
    try {
      const res = await client.get(`/api/overtime/summary/${selectedWorkerId}?month=${selectedMonth}`);
      setSummary(res.data.data);
    } catch (e) {
      console.error('Error fetching overtime summary', e);
      setSummary(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWorkers();
  }, []);

  useEffect(() => {
    fetchSummary();
  }, [selectedWorkerId, selectedMonth]);

  const handleSettle = async () => {
    if (!selectedWorkerId || !selectedMonth) return;
    if (!window.confirm(`Are you sure you want to settle all overtime entries for this worker for the month of ${selectedMonth}?`)) {
      return;
    }

    setSettling(true);
    setMessage('');
    setErrorMsg('');
    try {
      const res = await client.post(`/api/overtime/settle/${selectedWorkerId}?month=${selectedMonth}`);
      setMessage(`Overtime settled successfully! Total payout amount of ₹${res.data.data.toFixed(2)} finalized. Worker will receive SMS notification.`);
      fetchSummary();
    } catch (err) {
      console.error('Error settling overtime', err);
      const msg = err.response?.data?.message || 'Failed to settle overtime.';
      setErrorMsg(msg);
    } finally {
      setSettling(false);
    }
  };

  // Determine if settlement is allowed
  const currentMonth = new Date().toISOString().substring(0, 7);
  const isPastMonth = selectedMonth < currentMonth;
  const hasPendingEntries = summary && summary.settlementStatus === 'PENDING' && summary.totalOvertimeHours > 0;
  const isPayrollOperator = user?.role === 'ROLE_ADMIN' || user?.role === 'ROLE_PAYROLL_OPERATOR';
  const canSettle = isPastMonth && hasPendingEntries && isPayrollOperator;

  return (
    <div className="space-y-6">
      {/* Selection Panel */}
      <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm">
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 items-end">
          <div>
            <label className="block text-xs font-bold text-slate-500 uppercase tracking-wide mb-1.5 flex items-center gap-1">
              <Users className="w-3.5 h-3.5 text-slate-400" /> Choose Worker
            </label>
            <select
              value={selectedWorkerId}
              onChange={(e) => setSelectedWorkerId(e.target.value)}
              className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-sky-500"
            >
              <option value="">-- Choose Worker --</option>
              {workers.map(w => (
                <option key={w.id} value={w.id}>{w.name} ({w.designation})</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-500 uppercase tracking-wide mb-1.5 flex items-center gap-1">
              <Calendar className="w-3.5 h-3.5 text-slate-400" /> Settlement Month
            </label>
            <input
              type="month"
              value={selectedMonth}
              onChange={(e) => setSelectedMonth(e.target.value)}
              className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-sky-500"
            />
          </div>

          <div className="text-slate-400 text-xs py-2">
            * Capped at 60 hours per month. Only past months are eligible for settlement.
          </div>
        </div>
      </div>

      {/* Notifications */}
      {message && (
        <div className="bg-emerald-500/10 border border-emerald-500/20 text-emerald-700 rounded-xl p-4 flex gap-3 text-sm font-semibold animate-fadeIn max-w-4xl">
          <CheckCircle className="w-5 h-5 text-emerald-500 shrink-0" />
          <div>{message}</div>
        </div>
      )}
      {errorMsg && (
        <div className="bg-rose-500/10 border border-rose-500/20 text-rose-700 rounded-xl p-4 flex gap-3 text-sm font-semibold animate-fadeIn max-w-4xl">
          <ShieldAlert className="w-5 h-5 text-rose-500 shrink-0" />
          <div>{errorMsg}</div>
        </div>
      )}

      {loading ? (
        <div className="bg-white border border-slate-200 rounded-2xl p-8 text-center animate-pulse">
          Loading overtime summary...
        </div>
      ) : !selectedWorkerId ? (
        <div className="bg-white border border-slate-100 rounded-2xl p-12 text-center text-slate-400 font-medium shadow-sm">
          Please select a worker to view their overtime ledger.
        </div>
      ) : !summary || summary.breakdown.length === 0 ? (
        <div className="bg-white border border-slate-100 rounded-2xl p-12 text-center text-slate-400 font-medium shadow-sm">
          No overtime records found for this worker in {selectedMonth}.
        </div>
      ) : (
        <div className="space-y-6">
          {/* Summary Cards & Settlement Action */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="bg-white border border-slate-100 rounded-2xl p-6 shadow-sm flex flex-col justify-between">
              <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">Total Overtime Hours</span>
              <div className="text-3xl font-bold text-slate-800 my-2">{summary.totalOvertimeHours.toFixed(2)}h</div>
              <span className="text-[10px] text-slate-400 block font-medium">Capping applied at 60h max</span>
            </div>

            <div className="bg-white border border-slate-100 rounded-2xl p-6 shadow-sm flex flex-col justify-between">
              <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">Total Payout Amount</span>
              <div className="text-3xl font-bold text-indigo-600 my-2">₹{summary.totalPayoutAmount.toFixed(2)}</div>
              <span className="text-[10px] text-slate-400 block font-medium">1.5x first 2h &bull; 2x beyond</span>
            </div>

            <div className="bg-white border border-slate-100 rounded-2xl p-6 shadow-sm flex flex-col justify-between">
              <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">Settlement Status</span>
              <div className="my-2">
                {summary.settlementStatus === 'SETTLED' ? (
                  <span className="inline-flex items-center gap-1.5 text-xs font-bold text-emerald-600 bg-emerald-50 px-3 py-1 rounded-full border border-emerald-100">
                    <CheckCircle className="w-4 h-4" /> FULLY SETTLED
                  </span>
                ) : (
                  <span className="inline-flex items-center gap-1.5 text-xs font-bold text-amber-600 bg-amber-50 px-3 py-1 rounded-full border border-amber-100">
                    <ShieldAlert className="w-4 h-4" /> PENDING
                  </span>
                )}
              </div>
              <div>
                {canSettle ? (
                  <button
                    onClick={handleSettle}
                    disabled={settling}
                    className="w-full bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 text-white font-semibold py-2 px-4 rounded-xl text-xs shadow-md shadow-emerald-500/10 transition-all cursor-pointer text-center"
                  >
                    {settling ? 'Settling Ledger...' : 'Settle Overtime Now'}
                  </button>
                ) : (
                  <span className="text-[10px] text-slate-400 block font-medium">
                    {!isPastMonth 
                      ? 'Current month cannot be settled until complete.' 
                      : !isPayrollOperator 
                        ? 'Payroll credentials required to settle.' 
                        : 'Settlement complete for this month.'
                    }
                  </span>
                )}
              </div>
            </div>
          </div>

          {/* Breakdown Entries */}
          <div className="bg-white border border-slate-100 rounded-2xl overflow-hidden shadow-sm">
            <div className="px-6 py-4 border-b border-slate-100 flex items-center gap-2">
              <Award className="w-5 h-5 text-indigo-500" />
              <h4 className="font-bold text-slate-800 text-sm">Detailed Overtime Logs for {selectedMonth}</h4>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-slate-50 border-b border-slate-100">
                    <th className="px-6 py-3.5 text-xs font-bold text-slate-400 uppercase tracking-wider">Date</th>
                    <th className="px-6 py-3.5 text-xs font-bold text-slate-400 uppercase tracking-wider">Overtime Hours</th>
                    <th className="px-6 py-3.5 text-xs font-bold text-slate-400 uppercase tracking-wider">Hourly Rate Applied</th>
                    <th className="px-6 py-3.5 text-xs font-bold text-slate-400 uppercase tracking-wider">Payout Amount</th>
                    <th className="px-6 py-3.5 text-xs font-bold text-slate-400 uppercase tracking-wider">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {summary.breakdown.map((entry) => (
                    <tr key={entry.id} className="hover:bg-slate-50/50 transition-all text-sm">
                      <td className="px-6 py-3.5 text-slate-700 font-semibold">{entry.date}</td>
                      <td className="px-6 py-3.5 font-semibold text-slate-800">{entry.overtimeHours.toFixed(2)}h</td>
                      <td className="px-6 py-3.5 text-slate-500">₹{entry.overtimeRateApplied.toFixed(2)}/h</td>
                      <td className="px-6 py-3.5 font-bold text-slate-800">₹{entry.amount.toFixed(2)}</td>
                      <td className="px-6 py-3.5">
                        {entry.settlementStatus === 'SETTLED' ? (
                          <span className="text-[10px] font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-100">SETTLED</span>
                        ) : (
                          <span className="text-[10px] font-bold text-amber-600 bg-amber-50 px-2 py-0.5 rounded border border-amber-100">PENDING</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default OvertimeSummary;

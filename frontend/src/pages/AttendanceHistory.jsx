import React, { useState, useEffect } from 'react';
import client from '../api/client';
import { Search, ChevronLeft, ChevronRight, Calendar, AlertOctagon, RefreshCw } from 'lucide-react';

const AttendanceHistory = () => {
  const [logs, setLogs] = useState([]);
  const [workers, setWorkers] = useState([]);
  const [loading, setLoading] = useState(false);
  
  // Filter States
  const [selectedWorkerId, setSelectedWorkerId] = useState('');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  
  // Pagination States
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(15);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [hasNext, setHasNext] = useState(false);
  const [hasPrevious, setHasPrevious] = useState(false);

  const fetchWorkersList = async () => {
    try {
      const res = await client.get('/api/workers');
      setWorkers(res.data.data);
    } catch (e) {
      console.error(e);
    }
  };

  const fetchLogs = async () => {
    setLoading(true);
    try {
      let url = `/api/attendance/log?page=${page}&size=${size}`;
      if (selectedWorkerId) url += `&workerId=${selectedWorkerId}`;
      if (fromDate) url += `&from=${fromDate}`;
      if (toDate) url += `&to=${toDate}`;

      const res = await client.get(url);
      const data = res.data.data;
      setLogs(data.content || []);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
      setHasNext(data.hasNext || false);
      setHasPrevious(data.hasPrevious || false);
    } catch (e) {
      console.error('Error fetching logs', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWorkersList();
  }, []);

  useEffect(() => {
    fetchLogs();
  }, [page, selectedWorkerId, fromDate, toDate]);

  const handleClearFilters = () => {
    setSelectedWorkerId('');
    setFromDate('');
    setToDate('');
    setPage(0);
  };

  return (
    <div className="space-y-6">
      {/* Filters Panel */}
      <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 items-end">
          <div>
            <label className="block text-xs font-bold text-slate-500 uppercase tracking-wide mb-1.5">
              Filter by Worker
            </label>
            <select
              value={selectedWorkerId}
              onChange={(e) => { setSelectedWorkerId(e.target.value); setPage(0); }}
              className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-sky-500"
            >
              <option value="">-- All Workers --</option>
              {workers.map(w => (
                <option key={w.id} value={w.id}>{w.name} ({w.phone})</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-500 uppercase tracking-wide mb-1.5 flex items-center gap-1">
              <Calendar className="w-3.5 h-3.5 text-slate-400" /> Start Date
            </label>
            <input
              type="date"
              value={fromDate}
              onChange={(e) => { setFromDate(e.target.value); setPage(0); }}
              className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-sky-500"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-500 uppercase tracking-wide mb-1.5 flex items-center gap-1">
              <Calendar className="w-3.5 h-3.5 text-slate-400" /> End Date
            </label>
            <input
              type="date"
              value={toDate}
              onChange={(e) => { setToDate(e.target.value); setPage(0); }}
              className="w-full bg-slate-50 border border-slate-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-sky-500"
            />
          </div>

          <button
            onClick={handleClearFilters}
            className="flex items-center justify-center gap-2 border border-slate-200 hover:bg-slate-50 text-slate-700 font-semibold px-4 py-2 text-sm rounded-xl transition-all cursor-pointer h-10"
          >
            <RefreshCw className="w-4 h-4" />
            Reset Filters
          </button>
        </div>
      </div>

      {/* Logs Table */}
      {loading ? (
        <div className="bg-white border border-slate-200 rounded-2xl p-8 text-center animate-pulse">
          Loading history logs...
        </div>
      ) : logs.length === 0 ? (
        <div className="bg-white border border-slate-100 rounded-2xl p-12 text-center text-slate-400 font-medium shadow-sm">
          No attendance logs found matching the filters.
        </div>
      ) : (
        <div className="bg-white border border-slate-100 rounded-2xl overflow-hidden shadow-sm flex flex-col">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-100">
                  <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Worker</th>
                  <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Site</th>
                  <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Clock In</th>
                  <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Clock Out</th>
                  <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Shift Hours</th>
                  <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Overtime</th>
                  <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Flagged</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {logs.map((log) => (
                  <tr key={log.id} className="hover:bg-slate-50/50 transition-all text-sm">
                    <td className="px-6 py-4">
                      <div className="font-semibold text-slate-800">{log.workerName}</div>
                      <div className="text-[10px] text-slate-400 uppercase font-semibold">
                        {log.workerDesignation} &bull; {log.workerPhone}
                      </div>
                    </td>
                    <td className="px-6 py-4 text-slate-600 font-medium">{log.siteName}</td>
                    <td className="px-6 py-4 text-slate-500 font-mono text-xs">
                      {new Date(log.clockIn).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })}
                    </td>
                    <td className="px-6 py-4 text-slate-500 font-mono text-xs">
                      {log.clockOut 
                        ? new Date(log.clockOut).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })
                        : <span className="text-emerald-600 font-bold bg-emerald-50 px-2 py-0.5 rounded border border-emerald-100">ON SHIFT</span>
                      }
                    </td>
                    <td className="px-6 py-4 text-slate-700 font-medium">
                      {log.totalHours ? `${log.totalHours.toFixed(2)}h` : '-'}
                    </td>
                    <td className="px-6 py-4 font-semibold text-indigo-600">
                      {log.overtimeHours && log.overtimeHours > 0 ? `${log.overtimeHours.toFixed(2)}h` : '0h'}
                    </td>
                    <td className="px-6 py-4">
                      {log.flagged ? (
                        <span className="inline-flex items-center gap-1 text-xs font-bold text-rose-600 bg-rose-50 px-2 py-0.5 rounded-full border border-rose-100 animate-pulse">
                          <AlertOctagon className="w-3.5 h-3.5" /> &gt; 16h Shift
                        </span>
                      ) : (
                        <span className="text-slate-400 text-xs font-medium">Normal</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Pagination Controls */}
          <div className="p-4 bg-slate-50 border-t border-slate-100 flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500">
              Showing Page {page + 1} of {totalPages} ({totalElements} logs total)
            </span>
            <div className="flex gap-2">
              <button
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={!hasPrevious}
                className="p-1.5 border border-slate-200 bg-white rounded-lg hover:bg-slate-50 disabled:opacity-50 transition-all cursor-pointer"
              >
                <ChevronLeft className="w-5 h-5 text-slate-600" />
              </button>
              <button
                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                disabled={!hasNext}
                className="p-1.5 border border-slate-200 bg-white rounded-lg hover:bg-slate-50 disabled:opacity-50 transition-all cursor-pointer"
              >
                <ChevronRight className="w-5 h-5 text-slate-600" />
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AttendanceHistory;

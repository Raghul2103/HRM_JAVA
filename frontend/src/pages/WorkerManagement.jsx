import React, { useState, useEffect } from 'react';
import client from '../api/client';
import { Plus, Search, Edit2, Check, X, ShieldAlert } from 'lucide-react';

const WorkerManagement = () => {
  const [workers, setWorkers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  
  // Form State
  const [showForm, setShowForm] = useState(false);
  const [editId, setEditId] = useState(null);
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [designation, setDesignation] = useState('MASON');
  const [dailyWageRate, setDailyWageRate] = useState('');
  const [active, setActive] = useState(true);
  const [formError, setFormError] = useState('');

  const fetchWorkers = async () => {
    setLoading(true);
    try {
      const res = await client.get('/api/workers');
      setWorkers(res.data.data);
    } catch (e) {
      console.error('Error fetching workers', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWorkers();
  }, []);

  const resetForm = () => {
    setName('');
    setPhone('');
    setDesignation('MASON');
    setDailyWageRate('');
    setActive(true);
    setEditId(null);
    setShowForm(false);
    setFormError('');
  };

  const handleEdit = (worker) => {
    setEditId(worker.id);
    setName(worker.name);
    setPhone(worker.phone);
    setDesignation(worker.designation);
    setDailyWageRate(worker.dailyWageRate.toString());
    setActive(worker.active);
    setShowForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');

    if (!name || !phone || !dailyWageRate) {
      setFormError('Please fill out all required fields.');
      return;
    }

    const payload = {
      name,
      phone,
      designation,
      dailyWageRate: parseFloat(dailyWageRate),
      active
    };

    try {
      if (editId) {
        await client.put(`/api/workers/${editId}`, payload);
      } else {
        await client.post('/api/workers', payload);
      }
      resetForm();
      fetchWorkers();
    } catch (err) {
      console.error('Error saving worker', err);
      const msg = err.response?.data?.message || 'Error occurred while saving the worker profile.';
      setFormError(msg);
    }
  };

  const handleDeactivate = async (id) => {
    if (!window.confirm('Are you sure you want to deactivate this worker?')) return;
    try {
      await client.delete(`/api/workers/${id}`);
      fetchWorkers();
    } catch (e) {
      alert(e.response?.data?.message || 'Failed to deactivate worker');
    }
  };

  const filteredWorkers = workers.filter(worker => 
    worker.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    worker.phone.includes(searchTerm)
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div className="relative flex-1 w-full max-w-md">
          <Search className="absolute left-4 top-3.5 w-5 h-5 text-slate-400" />
          <input
            type="text"
            placeholder="Search workers by name or phone..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full bg-white border border-slate-200 rounded-xl pl-12 pr-4 py-3 focus:outline-none focus:ring-2 focus:ring-sky-500 transition-all text-sm shadow-sm"
          />
        </div>
        <button
          onClick={() => { resetForm(); setShowForm(true); }}
          className="flex items-center gap-2 bg-sky-500 hover:bg-sky-600 text-white font-semibold px-4 py-3 rounded-xl text-sm shadow-md shadow-sky-500/10 transition-all cursor-pointer w-full sm:w-auto justify-center"
        >
          <Plus className="w-5 h-5" />
          Add New Worker
        </button>
      </div>

      {showForm && (
        <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm max-w-2xl animate-fadeIn">
          <div className="flex justify-between items-center mb-6">
            <h3 className="font-bold text-slate-800 text-lg">
              {editId ? 'Edit Worker Profile' : 'Add New Worker Profile'}
            </h3>
            <button onClick={resetForm} className="text-slate-400 hover:text-slate-600">
              <X className="w-6 h-6" />
            </button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {formError && (
              <div className="bg-rose-500/10 border border-rose-500/20 text-rose-700 rounded-xl p-4 flex gap-3 text-sm font-medium">
                <ShieldAlert className="w-5 h-5 text-rose-500 shrink-0" />
                <div>{formError}</div>
              </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-bold text-slate-500 uppercase tracking-wide mb-1.5">
                  Full Name *
                </label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="w-full border border-slate-200 rounded-xl px-4 py-2.5 focus:outline-none focus:ring-2 focus:ring-sky-500 text-sm"
                  placeholder="e.g. Rajesh Kumar"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-500 uppercase tracking-wide mb-1.5">
                  Phone Number *
                </label>
                <input
                  type="text"
                  required
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  className="w-full border border-slate-200 rounded-xl px-4 py-2.5 focus:outline-none focus:ring-2 focus:ring-sky-500 text-sm"
                  placeholder="e.g. 9876543210"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-500 uppercase tracking-wide mb-1.5">
                  Designation *
                </label>
                <select
                  value={designation}
                  onChange={(e) => setDesignation(e.target.value)}
                  className="w-full border border-slate-200 rounded-xl px-4 py-2.5 focus:outline-none focus:ring-2 focus:ring-sky-500 text-sm"
                >
                  <option value="MASON">Mason</option>
                  <option value="ELECTRICIAN">Electrician</option>
                  <option value="PLUMBER">Plumber</option>
                  <option value="SUPERVISOR">Supervisor</option>
                  <option value="HELPER">Helper</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-500 uppercase tracking-wide mb-1.5">
                  Daily Wage Rate (₹) *
                </label>
                <input
                  type="number"
                  required
                  step="0.01"
                  value={dailyWageRate}
                  onChange={(e) => setDailyWageRate(e.target.value)}
                  className="w-full border border-slate-200 rounded-xl px-4 py-2.5 focus:outline-none focus:ring-2 focus:ring-sky-500 text-sm"
                  placeholder="e.g. 500"
                />
              </div>
            </div>

            <div className="flex items-center gap-2 py-2">
              <input
                type="checkbox"
                id="active"
                checked={active}
                onChange={(e) => setActive(e.target.checked)}
                className="w-4 h-4 text-sky-500 border-slate-300 rounded focus:ring-sky-500"
              />
              <label htmlFor="active" className="text-sm font-semibold text-slate-600 select-none">
                Mark Profile as Active
              </label>
            </div>

            <div className="flex justify-end gap-3 pt-2">
              <button
                type="button"
                onClick={resetForm}
                className="border border-slate-200 hover:bg-slate-50 text-slate-700 font-semibold px-4 py-2.5 rounded-xl text-sm"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="bg-sky-500 hover:bg-sky-600 text-white font-semibold px-5 py-2.5 rounded-xl text-sm shadow-md shadow-sky-500/10"
              >
                {editId ? 'Save Changes' : 'Create Profile'}
              </button>
            </div>
          </form>
        </div>
      )}

      {loading ? (
        <div className="bg-white border border-slate-200 rounded-2xl p-8 text-center animate-pulse">
          Loading worker directory...
        </div>
      ) : filteredWorkers.length === 0 ? (
        <div className="bg-white border border-slate-100 rounded-2xl p-12 text-center text-slate-400 font-medium shadow-sm">
          No worker profiles found. Add profiles to start.
        </div>
      ) : (
        <div className="bg-white border border-slate-100 rounded-2xl overflow-hidden shadow-sm">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-100">
                  <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Name</th>
                  <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Phone</th>
                  <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Designation</th>
                  <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Daily Wage</th>
                  <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Status</th>
                  <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filteredWorkers.map((worker) => (
                  <tr key={worker.id} className="hover:bg-slate-50/50 transition-all">
                    <td className="px-6 py-4 font-semibold text-slate-800">{worker.name}</td>
                    <td className="px-6 py-4 text-slate-600">{worker.phone}</td>
                    <td className="px-6 py-4">
                      <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold uppercase tracking-wider bg-slate-100 text-slate-600 border border-slate-200">
                        {worker.designation}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-slate-700 font-medium">₹{worker.dailyWageRate.toFixed(2)}</td>
                    <td className="px-6 py-4">
                      {worker.active ? (
                        <span className="inline-flex items-center gap-1.5 text-xs font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-full border border-emerald-100">
                          <Check className="w-3.5 h-3.5" /> Active
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1.5 text-xs font-bold text-slate-400 bg-slate-50 px-2 py-0.5 rounded-full border border-slate-100">
                          <X className="w-3.5 h-3.5" /> Inactive
                        </span>
                      )}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="flex justify-end gap-2">
                        <button
                          onClick={() => handleEdit(worker)}
                          className="p-2 hover:bg-slate-100 text-slate-500 hover:text-slate-800 rounded-lg transition-all"
                          title="Edit Profile"
                        >
                          <Edit2 className="w-4 h-4" />
                        </button>
                        {worker.active && (
                          <button
                            onClick={() => handleDeactivate(worker.id)}
                            className="p-2 hover:bg-rose-50 text-rose-500 hover:text-rose-700 rounded-lg transition-all"
                            title="Deactivate worker"
                          >
                            <X className="w-4 h-4" />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

export default WorkerManagement;

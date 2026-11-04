import React, { useEffect, useState } from 'react';
import apiClient from '../../apiClient';
import { toast } from 'react-toastify';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { useTranslation } from 'react-i18next';
const getDefaultDateRange = () => {
  const pad = (n) => String(n).padStart(2, '0');
  
  const now = new Date();
  const oneMonthAgo = new Date();
  oneMonthAgo.setMonth(oneMonthAgo.getMonth() - 1);

  const format = (d) => 
    `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;

  return {
    from: format(oneMonthAgo), 
    to: format(now)            
  };
};
function SalesReports() {
  const { t } = useTranslation();
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [summary, setSummary] = useState({ totalRevenue: 0, totalSold: 0, totalEvents: 0 });
  const [dateRange, setDateRange] = useState(() => getDefaultDateRange());
  
 const fetchReports = async (filters = {}) => {
    setLoading(true);
      try {        
        const params = {};
      if (filters.from) params.from = filters.from;
      if (filters.to) params.to = filters.to;

      // Filtre var mı kontrolü (Başlangıç ve Bitiş doluysa filtre var demektir)
      const hasFilter = filters.from && filters.to;

        const response = await apiClient.get("/api/reports/sales/full", { params });
        const data = response.data;
        setReports(data);

        const totalRev = data.reduce((acc, item) => {  
        const val = hasFilter ? item.revenueRange : item.revenueAllTime;
        return acc + (val || 0);
      }, 0);

      const totalTix = data.reduce((acc, item) => {
        const val = hasFilter ? item.soldInRange : item.soldAllTime;
        return acc + (val || 0);
      }, 0);
        
        setSummary({
          totalRevenue: totalRev,
          totalSold: totalTix,
          totalEvents: data.length
        });

      } catch (error) {
        console.error("Rapor hatası:", error);
        toast.error(t('toasts.reportError'));
      } finally {
        setLoading(false);
      }
    };
  useEffect(() => {
    fetchReports(dateRange);
  }, []);
  const handleFilter = (e) => {
    e.preventDefault();
    fetchReports(dateRange);
    toast.info(t('toasts.reportFiltered'));
  };
  const handleClear = () => {
    setDateRange({ from: "", to: "" });
    fetchReports({}); 
    toast.info(t('toasts.filterCleared'));
  };

  // Dosya indirme fonksiyonu (CSV veya PDF)
  const handleDownload = async (type) => {
    try {
      const endpoint = type === 'pdf' ? '/api/reports/sales/full.pdf' : '/api/reports/sales/full.csv';
      
      const params = {};
      if (dateRange.from) params.from = dateRange.from;
      if (dateRange.to) params.to = dateRange.to;

      const response = await apiClient.get(endpoint, { 
        params, 
        responseType: 'blob' 
      });     
  
      const mimeType = type === 'pdf' ? 'application/pdf' : 'text/csv';
      const blob = new Blob([response.data], { type: mimeType });
   
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      
            link.setAttribute('download', `satis_raporu.${type}`);
      
      document.body.appendChild(link);
      link.click();
      
      
      link.remove();
      window.URL.revokeObjectURL(url);

      toast.success(t('toasts.downloadSuccess', { type: type.toUpperCase() }));
    } catch (error) {
      console.error(error);
      toast.error(t('toasts.downloadError'));
    }
  };

  if (loading) return <div className="text-center text-white mt-5">{t('app.loading')}</div>;

 return (
    <div>   
      <div className="card bg-dark border-secondary mb-5 p-4 shadow-lg">     
        <h6 className="text-uppercase text-secondary fw-bold mb-3" style={{ letterSpacing: '1px' }}>
          🗓️ {t('admin.reports.filters')}
        </h6>

        <form onSubmit={handleFilter}>
          <div className="row g-4">
            <div className="col-md-4">
              <label className="text-secondary small mb-2 d-block">{t('admin.reports.startDate')}</label>
              <input 
                type="datetime-local" 
                className="form-control bg-secondary text-white border-0 py-2" 
                value={dateRange.from}
                onChange={(e) => setDateRange({...dateRange, from: e.target.value})}
              />
            </div>
            <div className="col-md-4">
              <label className="text-secondary small mb-2 d-block">{t('admin.reports.endDate')}</label>
              <input 
                type="datetime-local" 
                className="form-control bg-secondary text-white border-0 py-2"
                value={dateRange.to}
                onChange={(e) => setDateRange({...dateRange, to: e.target.value})}
              />
            </div>
            <div className="col-md-4 d-flex align-items-end gap-2">
              <button type="submit" className="btn btn-primary flex-grow-1 py-2">
                🔍 {t('admin.reports.filterBtn')}
              </button>
              {(dateRange.from || dateRange.to) && 
                <button type="button" onClick={handleClear} className="btn btn-outline-secondary py-2">
                  {t('admin.reports.clearBtn', 'Temizle')}
                </button>
              }
            </div>
          </div>      

          {/* Grafik */}
          {reports.length > 0 && (
            <div className="card bg-dark border-secondary mb-4 mt-5 p-3 shadow">
              <h5 className="text-white mb-4">📊 {t('admin.reports.chartTitle')}</h5>
              <div style={{ width: '100%', height: 300 }}>
                <ResponsiveContainer>
                  <BarChart data={reports}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#444" />
                    <XAxis dataKey="title" stroke="#ccc" tick={{fontSize: 12}} />
                    <YAxis stroke="#ccc" />
                    <Tooltip 
                      contentStyle={{ backgroundColor: '#222', borderColor: '#555', color: '#fff' }}
                      itemStyle={{ color: '#fff' }}
                    />
                    <Legend />
                    <Bar 
                      dataKey={(dateRange.from && dateRange.to) ? "soldInRange" : "soldAllTime"}
                      name={(dateRange.from && dateRange.to) ? t('admin.reports.periodSales') : t('admin.reports.totalSales')}
                      fill="#0d6efd"
                      radius={[4, 4, 0, 0]}
                    />
                    <Bar 
                      dataKey="remaining" 
                      name={t('admin.reports.stockLeft')} 
                      fill="#dc3545" 
                      radius={[4, 4, 0, 0]} 
                    />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>
          )}
        </form>
      </div>

      {/* Özet Kartları */}
      <div className="row g-3 mb-4">
        <div className="col-md-4">
          <div className="card bg-success text-white h-100">
            <div className="card-body text-center">
              <h6 className="card-title opacity-75">{t('admin.reports.periodRev')}</h6>
              <h2 className="fw-bold text-white">{summary.totalRevenue.toLocaleString('tr-TR')} ₺</h2>
            </div>
          </div>
        </div>
        
        <div className="col-md-4">
          <div className="card bg-primary text-white h-100">
            <div className="card-body text-center">
              <h6 className="card-title opacity-75">{t('admin.reports.periodSales')}</h6>
              <h2 className="fw-bold text-white">{summary.totalSold} {t('admin.add.pcs', 'Adet')}</h2>
            </div>
          </div>
        </div>
        
        <div className="col-md-4">
          <div className="card bg-warning text-white h-100">
            <div className="card-body text-center">
              <h6 className="card-title opacity-75">{t('admin.reports.reportEvents')}</h6>
              <h2 className="fw-bold text-white">{summary.totalEvents}</h2>
            </div>
          </div>
        </div>
      </div>

      {/* İndirme Butonları */}
      <div className="d-flex justify-content-end mb-3 gap-2">
        <button onClick={() => handleDownload('csv')} className="btn btn-outline-info btn-sm">
          📥 {t('admin.reports.dlExcel')}
        </button>
        <button onClick={() => handleDownload('pdf')} className="btn btn-outline-danger btn-sm">
          📑 {t('admin.reports.dlPdf')}
        </button>
      </div>

      {/* Tablo */}
      <div className="table-responsive">
        <table className="table table-dark table-striped table-hover border-secondary align-middle">
          <thead>
            <tr>
              <th>{t('admin.reports.eventName')}</th>
              <th className="text-end">{t('admin.add.capacity')}</th>
              <th className="text-end">{t('admin.reports.periodSales')}</th>
              <th className="text-end">{t('admin.reports.periodInc')}</th>
              <th className="text-end">{t('admin.reports.allTime')}</th>
              <th className="text-end">{t('admin.reports.allTimeInc')}</th>                
            </tr>
          </thead>
          <tbody>
            {reports.map((row) => (
              <tr key={row.eventId}>
                <td className="text-info fw-bold">{row.title}</td>
                <td className="text-end">{row.totalSeats}</td>
                <td className="text-end text-warning fw-bold">{row.soldInRange}</td>                    
                <td className="text-end text-success fw-bold">
                  {row.revenueRange ? row.revenueRange.toLocaleString('tr-TR') : 0} ₺
                </td>
                <td className="text-end text-white">{row.soldAllTime}</td>
                <td className="text-end text-white">
                  {row.revenueAllTime ? row.revenueAllTime.toLocaleString('tr-TR') : 0} ₺                          
                </td>
              </tr>
            ))}
            {reports.length === 0 && (
              <tr><td colSpan="6" className="text-center text-muted py-4">{t('modals.noData')}</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default SalesReports;
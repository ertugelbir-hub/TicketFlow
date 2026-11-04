import React, { useEffect, useState } from 'react';
import apiClient from '../../apiClient';
import { useTranslation } from 'react-i18next';

function EventAnalysisModal({ eventId, onClose }) {
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const { t } = useTranslation();

  useEffect(() => {
    const fetchAnalysis = async () => {
      try {
        
        const response = await apiClient.get("/api/reports/sales/alltime", {
          params: { eventId }
        });
        setReport(response.data);
      } catch (error) {
        console.error("Analiz hatası:", error);
      } finally {
        setLoading(false);
      }
    };

    if (eventId) fetchAnalysis();
  }, [eventId]);

  return (
    <div className="modal show d-block" style={{ backgroundColor: "rgba(0,0,0,0.8)" }}>
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content bg-dark text-white border-info">
          
          <div className="modal-header border-secondary">
            <h5 className="modal-title text-info">📈 {t('modals.analysisTitle')}</h5>
            <button type="button" className="btn-close btn-close-white" onClick={onClose}></button>
          </div>

          <div className="modal-body text-center">
            {loading ? (
              <div className="spinner-border text-info my-4" role="status"></div>
            ) : report ? (
              <div>
                <h4 className="mb-4">{report.title}</h4>
                
                <div className="row g-3">
                  {}
                  <div className="col-12">
                    <div className="p-3 border border-success rounded bg-success bg-opacity-10">
                      <small className="text-success fw-bold">{t('modals.totalRevenue')}</small>
                      <h2 className="mb-0 text-success">{report.revenue.toLocaleString('tr-TR')} ₺</h2>
                    </div>
                  </div>

                  {}
                  <div className="col-6">
                    <div className="p-3 border border-secondary rounded">
                      <small className="text-secondary">{t('modals.soldTickets')}</small>
                      <h3>{report.sold}</h3>
                    </div>
                  </div>

                  {}
                  <div className="col-6">
                    <div className="p-3 border border-secondary rounded">
                      <small className="text-secondary">{t('modals.remainingSeats')}</small>
                      <h3>{report.remaining} / {report.totalSeats}</h3>
                    </div>
                  </div>
                  
                  <div className="col-12 mt-3">
                    <div className="progress" style={{height: '25px'}}>
                      <div 
                        className="progress-bar bg-info" 
                        role="progressbar" 
                        style={{ width: `${(report.sold / report.totalSeats) * 100}%` }}
                      >
                        {t('modals.occupancy', { rate: Math.round((report.sold / report.totalSeats) * 100) })}
                      </div>
                    </div>
                  </div>

                </div>
              </div>
            ) : (
              <p className="text-danger">{t('modals.noData')}</p>
            )}
          </div>

          <div className="modal-footer border-secondary">
            <button type="button" className="btn btn-secondary" onClick={onClose}>{t('modals.close')}</button>
          </div>

        </div>
      </div>
    </div>
  );
}

export default EventAnalysisModal;
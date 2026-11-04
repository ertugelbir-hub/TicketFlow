import React from 'react';
import { downloadPdfTicket } from '../../utils/pdfHelper';
import { useTranslation } from 'react-i18next';

function PurchaseSuccessModal({ ticketData, onClose, onGoToTickets }) {
  const ticketId = Array.isArray(ticketData) ? ticketData[0]?.id : ticketData?.id;
  const { t } = useTranslation();

  const handleDownload = () => {
    if (ticketId) {
      downloadPdfTicket(ticketId);
    } else if (onGoToTickets) {
      onGoToTickets();
    }
  };

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      width: '100%',
      height: '100%',
      backgroundColor: 'rgba(0, 0, 0, 0.88)',
      backdropFilter: 'blur(8px)',
      zIndex: 9999,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '20px'
    }}>
      <div 
        className="card text-center p-4 p-md-5 bg-dark text-white border-0 shadow-lg position-relative"
        style={{
          maxWidth: '480px',
          width: '100%',
          borderRadius: '24px',
          background: '#0f172a',
          border: '1px solid rgba(56, 189, 248, 0.35)',
          boxShadow: '0 25px 60px rgba(0, 0, 0, 0.85), 0 0 35px rgba(56, 189, 248, 0.2)'
        }}
      >
        <div className="mb-3">
          <span style={{ fontSize: '4.2rem', filter: 'drop-shadow(0 0 20px rgba(34, 197, 94, 0.6))' }}>
            🎉
          </span>
        </div>

        <h2 className="fw-bold text-white mb-2" style={{ letterSpacing: '-0.5px' }}>
          {t('successModal.title')}
        </h2>
        <p className="text-secondary small mb-4">
          {t('successModal.desc')}
        </p>

        <div 
          className="p-3 rounded-3 mb-4 d-flex align-items-center justify-content-center gap-3 text-start"
          style={{
            backgroundColor: 'rgba(56, 189, 248, 0.08)',
            border: '1px dashed rgba(56, 189, 248, 0.4)'
          }}
        >
          <span className="fs-1">🎟️</span>
          <div>
            <div className="fw-bold text-info" style={{ fontSize: '0.95rem' }}>
              {ticketId ? t('successModal.ticketNo', { id: ticketId }) : t('successModal.success')}
            </div>
            <small className="text-muted d-block" style={{ fontSize: '0.78rem' }}>
              {t('successModal.hint')}
            </small>
          </div>
        </div>

        <div className="d-flex flex-column gap-2">
          <button 
            className="btn btn-info btn-lg fw-bold text-dark w-100 py-3 shadow-sm d-flex align-items-center justify-content-center gap-2"
            style={{ 
              borderRadius: '12px',
              background: 'linear-gradient(135deg, #38bdf8 0%, #0ea5e9 100%)',
              border: 'none'
            }}
            onClick={handleDownload}
          >
            <span>📄</span> {t('successModal.downloadPdf')}
          </button>

          <button 
            className="btn btn-outline-light w-100 py-2 fw-semibold"
            style={{ borderRadius: '12px', borderColor: 'rgba(255, 255, 255, 0.2)' }}
            onClick={onClose}
          >
            {t('successModal.continueShopping')}
          </button>
        </div>

      </div>
    </div>
  );
}

export default PurchaseSuccessModal;
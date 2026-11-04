import React, { useEffect, useState } from 'react';
import apiClient from "../../apiClient";
import { toast } from 'react-toastify';
import { getErrorMessage } from "../../utils/errorHelper";
import { useTranslation } from 'react-i18next';
import { downloadPdfTicket } from '../../utils/pdfHelper';
function MyTickets({ onBack }) {
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const { t } = useTranslation();

  
   const fetchMyTickets = async () => {
    try {
      const response = await apiClient.get("/api/tickets/my");
      setTickets(response.data);
    } catch (err) {
      console.error("Biletler çekilemedi:", err);
      toast.error(t('toasts.ticketsLoadError'));
    } finally {
      setLoading(false);
    }
  };
     useEffect(() => {
     fetchMyTickets();
    }, []);
   const handleCancel = async (ticketId) => {
    if (!window.confirm(t('toasts.confirmAction'))) return;

    try {
      await apiClient.delete(`/api/tickets/${ticketId}`);
      toast.success(t('toasts.ticketCancelled'));
         
      fetchMyTickets();
    } catch (error) {
      console.error("İptal hatası:", error);
      const msg = getErrorMessage(error);
      toast.error(`❌ ${msg}`);
    }
  };


  return (
    <div className="container text-white">
      <div className="d-flex align-items-center mb-4">
        <button onClick={onBack} className="btn btn-outline-secondary me-3">
         {t('common.back')}
        </button>
        <h2 className="mb-0">{t('app.myTickets')}</h2>
      </div>

      {loading && <div className="text-center text-secondary">{t('myTicketsPage.searching')}</div>}     
            
      {!loading && tickets.length === 0 && (
        <div className="text-center py-5 text-muted border border-secondary rounded">
          <p className="fs-5">{t('myTicketsPage.empty')}</p>
          <button onClick={onBack} className="btn btn-primary">{t('myTicketsPage.goSelect')}</button>
        </div>
      )}

      <div className="row g-3">
        {tickets.map((ticket) => (
          <div key={ticket.id} className="col-md-6 col-lg-4">
            <div className="card bg-dark border-secondary text-white h-100 shadow">
              {/* Header */}
              <div className="card-header border-secondary d-flex justify-content-between align-items-center">
                <span className="badge bg-success">{t('myTicketsPage.purchased')}</span>
                <small className="text-muted">{t('myTicketsPage.ticketId')}{ticket.id}</small>
              </div>
              {/* Body */}
              <div className="card-body d-flex flex-column">
                <h5 className="card-title text-info">
                  {ticket.event ? ticket.event.title : t('myTicketsPage.noEventInfo')}
                </h5>
                <p className="card-text small text-secondary mb-2">                 
                  {t('myTicketsPage.user')} {ticket.username}
                </p>
                
               {ticket.event && (
                   <div className="mb-3 small text-light opacity-75"> 
                    <div className="mb-1">📍 {ticket.event.city}</div>
                    <div>📅 {new Date(ticket.event.dateTime).toLocaleString("tr-TR")}</div>
                   </div>
                )}

                <div className="mt-auto border-top border-secondary pt-3">

               {/* FİYAT HESAPLAMA ALANI */}
  <div className="d-flex justify-content-between align-items-end mb-3">
      {/* SOL: Adet ve Birim Fiyat Bilgisi */}
      <div className="d-flex flex-column">
          <span className="fw-bold text-white fs-5">{ticket.quantity} {t('myTicketsPage.quantity')}</span>
          <small className="text-muted">
             ({t('myTicketsPage.perPiece')} {ticket.price ? ticket.price : ticket.event.price} ₺)
          </small>
      </div>

      {/* SAĞ: Kocaman Toplam Fiyat (Adet * Fiyat) */}
      <div className="text-end">
          <span className="text-secondary small d-block">{t('myTicketsPage.totalPaid')}</span>
          <span className="text-warning fw-bold display-6" style={{ fontSize: '1.8rem' }}>             
              { (ticket.quantity * (ticket.price || ticket.event.price)).toFixed(2) } ₺
          </span>
      </div>
  </div>
                {/*PDF İndir ve İptal Butonları */}
<div className="d-flex gap-2">
  <button 
    onClick={() => downloadPdfTicket(ticket.id)} 
    className="btn btn-outline-info w-50 btn-sm d-flex align-items-center justify-content-center gap-1 shadow-sm"
    title="QR Kodlu Bilet PDF'ini İndir"
  >
    <span>📄</span> <span>{t('myTicketsPage.downloadPdf', 'PDF İndir')}</span>
  </button>

  <button 
    onClick={() => handleCancel(ticket.id)} 
    className="btn btn-outline-danger w-50 btn-sm"
  >
    {t('myTicketsPage.cancelBtn')}
  </button>
</div>
             </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default MyTickets;
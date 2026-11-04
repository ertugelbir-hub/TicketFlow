import React, { useEffect, useState } from 'react';
import apiClient from '../../apiClient';
import { downloadPdfTicket } from '../../utils/pdfHelper';
import { toast } from 'react-toastify';
import { useTranslation } from 'react-i18next';

function TicketManager() {
  const [tickets, setTickets] = useState([]);
  const { t } = useTranslation(); 
  const fetchTickets = async () => {
    try {
     
      const res = await apiClient.get("/api/tickets");
      setTickets(res.data);
    } catch (error) {
      console.error("Biletler çekilemedi", error);
    }
  };

  useEffect(() => { fetchTickets(); }, []);

  const handleDelete = async (id) => {
    if(!window.confirm(t('toasts.confirmAction'))) return;
    try {
      await apiClient.delete(`/api/tickets/${id}`);
      toast.success(t('toasts.ticketCancelled'));
      fetchTickets(); // Listeyi yenile
    } catch (error) {
      toast.error(t('toasts.ticketCancelError'));
    }
  };

  return (
    <div className="table-responsive">
      <h4 className="text-warning mb-3">🎫 {t('admin.tickets.title', 'Satılan Tüm Biletler')}</h4>
      <table className="table table-dark table-hover border-secondary align-middle">
        <thead>
          <tr>
            <th>ID</th>
            <th>{t('admin.tickets.event', 'Etkinlik')}</th>
            <th>{t('admin.tickets.user', 'Kullanıcı')}</th>
            <th>{t('admin.tickets.qty', 'Adet')}</th>
            <th>{t('admin.list.date', 'Tarih')}</th>
            <th className="text-end">{t('admin.list.actions', 'İşlem')}</th>
          </tr>
        </thead>
        <tbody>
          {tickets.map(tkt => (
            <tr key={tkt.id}>
              <td>#{tkt.id}</td>
              <td className="text-info">{tkt.event ? tkt.event.title : t('modals.deletedEvent')}</td>
              <td>{tkt.username}</td>
              <td>{tkt.quantity}</td>
              <td>{tkt.createdAt ? new Date(tkt.createdAt).toLocaleDateString() : "-"}</td>
              <td className="text-end">
                <button 
                  onClick={() => downloadPdfTicket(tkt.id)} 
                  className="btn btn-sm btn-outline-info me-2 shadow-sm"
                  title="PDF"
                >
                  📄 PDF
                </button>                            
                <button onClick={() => handleDelete(tkt.id)} className="btn btn-sm btn-outline-danger">
                  {t('admin.tickets.cancelBtn', 'İptal Et')}
                </button>
              </td>
            </tr>
          ))}
          {tickets.length === 0 && (
            <tr><td colSpan="6" className="text-center text-muted">{t('modals.noData')}</td></tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

export default TicketManager;
import React, { useEffect, useState } from 'react';
import apiClient from '../../apiClient';
import { useTranslation } from 'react-i18next';

function UserHistoryModal({ username, onClose }) {
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const { t } = useTranslation();

  useEffect(() => {
    const fetchHistory = async () => {
      try {
        
        const response = await apiClient.get(`/api/tickets/${username}`);
        setTickets(response.data);
      } catch (error) {
        console.error("Geçmiş yüklenemedi", error);
      } finally {
        setLoading(false);
      }
    };

    if (username) fetchHistory();
  }, [username]);

  return (
    <div className="modal show d-block" style={{ backgroundColor: "rgba(0,0,0,0.8)" }}>
      <div className="modal-dialog modal-lg modal-dialog-centered">
        <div className="modal-content bg-dark text-white border-secondary">
          
          <div className="modal-header border-secondary">
            <h5 className="modal-title text-info">📜 {t('modals.userHistoryTitle', { username })}</h5>
            <button type="button" className="btn-close btn-close-white" onClick={onClose}></button>
          </div>

          <div className="modal-body" style={{ maxHeight: '60vh', overflowY: 'auto' }}>
            {loading ? (
              <div className="text-center py-4">{t('app.loading')}</div>
            ) : tickets.length === 0 ? (
              <div className="text-center text-muted py-4">{t('modals.userNoTickets')}</div>
            ) : (
              <table className="table table-dark table-striped table-sm">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>{t('admin.tickets.event')}</th>
                    <th>{t('admin.list.city')}</th>
                    <th>{t('admin.tickets.qty')}</th>
                    <th>{t('admin.users.processDate')}</th>
                  </tr>
                </thead>
                <tbody>
                  {tickets.map(t => (
                    <tr key={t.id}>
                      <td>#{t.id}</td>
                      <td className="text-warning">{t.event ? t.event.title : t('modals.deletedEvent')}</td>
                      <td>{t.event ? t.event.city : "-"}</td>
                      <td>{t.quantity}</td>
                      <td>{t.createdAt ? new Date(t.createdAt).toLocaleDateString() : "-"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
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

export default UserHistoryModal;
import React from 'react';
import { useTranslation } from 'react-i18next';

function EventTable({ events, onEdit, onDelete, onAnalyze }) {
  const { t } = useTranslation();
  return (
    <div className="table-responsive">
      <table className="table table-dark table-hover border-secondary">
        <thead>
          <tr>
            <th>{t('admin.list.id', 'ID')}</th>
            <th>{t('admin.list.title', 'Başlık')}</th>
            <th>{t('admin.list.city', 'Şehir')}</th>
            <th>{t('admin.list.date', 'Tarih')}</th>
            <th>{t('admin.list.price', 'Fiyat')}</th>
            <th className="text-end">{t('admin.list.actions', 'İşlemler')}</th>
          </tr>
        </thead>
        <tbody>
          {events.map(event => (
            <tr key={event.id}>
              <td>#{event.id}</td>
              <td className="fw-bold text-info">{event.title}</td>
              <td>{event.city}</td>
              <td>{new Date(event.dateTime).toLocaleDateString()}</td>
              <td>{event.price} ₺</td>
              <td className="text-end">
                <button 
                    className="btn btn-sm btn-outline-info me-2"
                    title="Analiz Et"
                    onClick={() => onAnalyze(event.id)}
                >
                    📈
                </button>
                <button 
                    className="btn btn-sm btn-outline-warning me-2"
                    onClick={() => onEdit(event)}
                >
                    ✏️
                </button>
                <button 
                    className="btn btn-sm btn-outline-danger"
                    onClick={() => onDelete(event.id)}
                >
                    🗑️
                </button>
              </td>
            </tr>
          ))}
          {events.length === 0 && (
            <tr><td colSpan="6" className="text-center py-4 text-muted">{t('eventList.notFound', 'Kayıtlı etkinlik yok.')}</td></tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

export default EventTable;

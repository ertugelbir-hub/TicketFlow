import React, { useState, useEffect } from 'react';
import apiClient from "../../apiClient";
import EventTable from './EventTable'; 
import EventForm from './EventForm';   
import SalesReports from './SalesReports';
import TicketManager from './TicketManager';
import UserManager from './UserManager';
import { toast } from 'react-toastify';
import EventAnalysisModal from './EventAnalysisModal';
import { getErrorMessage } from "../../utils/errorHelper";
import { useTranslation } from 'react-i18next';
function AdminPanel({ onBack }) {
  const [activeTab, setActiveTab] = useState("list");
  const [events, setEvents] = useState([]);  
  const [editingEvent, setEditingEvent] = useState(null);
  const [analysisId, setAnalysisId] = useState(null);
    const { t } = useTranslation();
    const [saving, setSaving] = useState(false);

    const handleSmartBack = () => {   
    if (editingEvent || activeTab === "form") {
      setEditingEvent(null);
      setActiveTab("list");
    } else {   
      onBack();
    }
  };

  // --- API İŞLEMLERİ ---
  const fetchEvents = async () => {
    try {
      const response = await apiClient.get("/api/events");
      setEvents(response.data.sort((a, b) => new Date(b.dateTime) - new Date(a.dateTime)));
    } catch (error) {
      console.error("Liste hatası:", error);
    }
  };

  useEffect(() => {
    if (activeTab === "list") fetchEvents();
  }, [activeTab]);

  const handleSave = async (formData) => {
    if (saving) return;
    setSaving(true);
    try {
      if (editingEvent) {
        await apiClient.put(`/api/events/${editingEvent.id}`, formData);
        toast.success(t('toasts.eventUpdated'));
      } else {
        await apiClient.post("/api/events", formData);
       toast.success(t('toasts.eventCreated'));
      }
           
      setEditingEvent(null);
      setTimeout(() => setActiveTab("list"), 1000);
    } catch (error) {
      const msg = getErrorMessage(error);
      toast.error(`❌ ${msg}`);
    }finally {setSaving(false);}

  };

  const handleDelete = async (id) => {
    if (!window.confirm(t('toasts.confirmAction'))) return;
    try {
      await apiClient.delete(`/api/events/${id}`);
      toast.success(t('toasts.eventDeleted'));
      fetchEvents();
    } catch (error) {
      toast.error(t('toasts.deleteError'));
    }
  };

  // --- GÖRÜNÜM ---
  return (
    <div className="container text-white pb-5">   
      {/* Header */}
      <div className="d-flex align-items-center mb-4">
        <button onClick={handleSmartBack} className="btn btn-outline-secondary me-3">
          {editingEvent || activeTab === "form" 
            ? ` ${t('eventDetail.backToList', 'Listeye Dön')}` 
            : t('admin.exit')
          }
        </button>
        <h2 className="mb-0">{t('admin.title')}</h2>
      </div>   

      {/* Sekmeler */}
      <ul className="nav nav-tabs mb-4 border-secondary">
        <li className="nav-item">
          <button 
            className={`nav-link ${activeTab === "list" ? "active bg-dark text-warning border-secondary border-bottom-0" : "text-secondary"}`} 
            onClick={() => { setActiveTab("list"); setEditingEvent(null); }}
          >
            {t('admin.tabs.list')}
          </button>       
        </li>
        <li className="nav-item">
          <button 
            className={`nav-link ${activeTab === "form" ? "active bg-dark text-warning border-secondary border-bottom-0" : "text-secondary"}`} 
            onClick={() => { setEditingEvent(null); setActiveTab("form"); }}
          >
           {/* Düzenleme modundaysa kalem ikonu ve düzenle başlığı göster */}
            {editingEvent ? `✏️ ${t('admin.add.editTitle', 'Etkinliği Düzenle')}` : `➕ ${t('admin.tabs.addNew')}`}
          </button>
        </li>
        <li className="nav-item">
          <button 
            className={`nav-link ${activeTab === "reports" ? "active bg-dark text-warning border-secondary border-bottom-0" : "text-secondary"}`} 
            onClick={() => setActiveTab("reports")}
          >
            📊 {t('admin.tabs.reports')}
          </button>
        </li>
        <li className="nav-item">
      <button 
        className={`nav-link ${activeTab === "tickets" ? "active bg-dark text-warning border-secondary border-bottom-0" : "text-secondary"}`} 
        onClick={() => setActiveTab("tickets")}
      >
        🎫 {t('admin.tabs.tickets')}
      </button>
    </li>
    <li className="nav-item">
      <button 
        className={`nav-link ${activeTab === "users" ? "active bg-dark text-warning border-secondary border-bottom-0" : "text-secondary"}`} 
        onClick={() => setActiveTab("users")}
      >
        👥 {t('admin.tabs.users')}
      </button>
    </li>
      </ul>

      {}
      {activeTab === "list" ? (
        <EventTable 
          events={events} 
          onDelete={handleDelete} 
          onEdit={(event) => {
             setEditingEvent(event);
             setActiveTab("form");
          }} 
          onAnalyze={(id) => setAnalysisId(id)}
        />        
      ) : activeTab === "form" ? (
        <EventForm 
          initialData={editingEvent} 
          onSubmit={handleSave} 
          loading={saving}
          onCancel={() => {
            setEditingEvent(null);
            setActiveTab("list");
          }}
        />
      ) :activeTab === "reports" ? (
        // Raporlar Sekmesi
        <SalesReports />
      ):activeTab === "tickets" ?(
     <TicketManager /> 
  ) : (
     <UserManager />   
  )
      }
      {}
      {analysisId && (
        <EventAnalysisModal 
          eventId={analysisId} 
          onClose={() => setAnalysisId(null)} 
        />   
      )}
        </div>
  );
}

export default AdminPanel;
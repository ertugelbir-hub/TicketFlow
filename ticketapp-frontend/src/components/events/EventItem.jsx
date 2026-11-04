import React, { useState, useEffect } from 'react';
import apiClient from '../../apiClient';
import { toast } from 'react-toastify';
import { useTranslation } from 'react-i18next';
function EventItem({ event, onSelect, onFavoriteToggle, isFavorite = false }) {
  const [isLiked, setIsLiked] = useState(isFavorite);
  const [loading, setLoading] = useState(false);
  const { t } = useTranslation();
  const DEFAULT_EVENT_IMAGE = "/default1.jpg";

  useEffect(() => {
  if (isFavorite === true) {
        setIsLiked(true);
    } else {
        setIsLiked(false);
    }
  }, [isFavorite]);

  const handleFavoriteClick = async (e) => {
    e.stopPropagation(); 
    if (loading) return;

    setLoading(true);
    try {      
      const res = await apiClient.post(`/api/favorites/${event.id}`);      
   
      const newState = !isLiked;
      setIsLiked(newState);
      
      // Mesaj göster
      if (res.data.includes("eklendi")) {
          toast.success(t('toasts.favAdded'));
      } else {
          toast.info(t('toasts.favRemoved'));   
          if (onFavoriteToggle) onFavoriteToggle(); 
      }

    } catch (error) {
      console.error(error);
      toast.error(t('toasts.favError'));
      setIsLiked(!isLiked);
    } finally {
      setLoading(false);
    }
  };  
  const formatEventDate = (dateStr) => {
    if (!dateStr) return "Tarih belirtilmedi";
    try {
      const d = new Date(dateStr);
      return d.toLocaleDateString("tr-TR", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
      }).replace(',', ' •');
    } catch {
      return dateStr.slice(0, 16);
    }
  };

  return (
    <div className="col-12 col-md-6 col-lg-4">
      <div className="card h-100 shadow-sm border-0 position-relative overflow-hidden">
        
        {/* MODERN CAM EFEKTLİ FAVORİ BUTONU */}
        <button 
          onClick={handleFavoriteClick}
          className="btn position-absolute top-0 end-0 m-2 rounded-circle d-flex align-items-center justify-content-center"
          style={{ 
            width: '38px', 
            height: '38px', 
            zIndex: 10, 
            backgroundColor: 'rgba(15, 23, 42, 0.75)',
            backdropFilter: 'blur(8px)',
            border: '1px solid rgba(255, 255, 255, 0.15)'
          }}
          title={isLiked ? "Favorilerden Çıkar" : "Favoriye Ekle"}
        >
          <i 
            className={`bi ${isLiked ? 'bi-heart-fill text-danger' : 'bi-heart text-white'}`} 
            style={{ fontSize: '1.1rem' }}
          ></i>
        </button> 

        {/* ETKİNLİK GÖRSELİ*/}
        <div style={{ height: "200px", overflow: "hidden", backgroundColor: "#0f172a" }}
        onClick={() => onSelect(event)}>          
          <img 
            src={event.imageUrl || DEFAULT_EVENT_IMAGE} 
            alt={event.title} 
            className="w-100 h-100"
            style={{ objectFit: "cover" }}
            onError={(e) => {
              e.target.onerror = null;
              e.target.src = DEFAULT_EVENT_IMAGE;
            }}
          />
        </div>

        {/* KART İÇERİĞİ */}
        <div className="card-body d-flex flex-column">
          <h5 className="card-title fw-bold mb-2 text-white">{event.title}</h5>
          
          <p className="mb-1 text-secondary small">
            📍 {event.city} • {event.venue}
          </p>
          
          <p className="mb-3 small text-muted">
            🗓️ {formatEventDate(event.dateTime)}
          </p>

          <div className="mt-auto">
            <h6 className="fw-bold mb-3" style={{ color: "#38bdf8" }}>
              {t('eventDetail.price')}: {event.price} ₺
            </h6>
            
            <button 
              className="btn btn-primary w-100 fw-bold shadow-sm"
              onClick={() => onSelect(event)} 
            >
              {t('eventList.inspectBtn')}
            </button>
          </div>
        </div>

      </div>
    </div>
  );
}

export default EventItem;
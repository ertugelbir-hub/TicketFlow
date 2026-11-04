import React, { useEffect, useState } from 'react'; 
import apiClient from '../../apiClient';
import EventItem from './EventItem';
import { toast } from "react-toastify";
import { Link } from "react-router-dom";
import { useTranslation } from 'react-i18next';


function EventList({ events, onEventSelect, sortType, onSortChange }) {
  const [favoriteIds, setFavoriteIds] = useState([]);
  const { t } = useTranslation();
  const [pinnedEvents, setPinnedEvents] = useState(() => {
  try {
    const saved = sessionStorage.getItem('pinnedAds');
    return saved ? JSON.parse(saved) : [];
  } catch {
    return [];
  }
});
  useEffect(() => {
  if (pinnedEvents.length === 0 && events && events.length > 0) {
    // events dizisinden tamamen bağımsız yeni bir klon alıyoruz
    const initialClone = JSON.parse(JSON.stringify(events));
    setPinnedEvents(initialClone);
    sessionStorage.setItem('pinnedAds', JSON.stringify(initialClone));
  }
}, [events, pinnedEvents]);
 const [showPromo, setShowPromo] = useState(() => {
  
  
    return sessionStorage.getItem('promoSeen') !== 'true';
  });  
  const handleClosePromo = () => {
    sessionStorage.setItem('promoSeen', 'true');
    setShowPromo(false);
  };
  
  useEffect(() => {    
    const fetchFavoriteIds = async () => {     
      const token = localStorage.getItem('token') || sessionStorage.getItem('token');
      if (!token) {
          /*"console.log("Giriş yapılmamış, favoriler çekilmiyor.");*/
          return;
      }

      try {       
        const res = await apiClient.get('/api/favorites');       
        const ids = res.data.map(event => event.id);
        /*console.log("FAVORİLER GELDİ:", ids);*/
        setFavoriteIds(ids);
      } catch (error) {
        console.error("Favori durumu kontrol edilemedi", error);
      }
    };
  fetchFavoriteIds();
  }, []); 

  if (!events || events.length === 0) {
    return <div className="text-center text-white mt-5">{t('eventList.notFound')}</div>;
  }
  const adsSource = pinnedEvents.length > 0 ? pinnedEvents : (events || []);
  const leftAds = adsSource.slice(0, 2);
  const rightAds = [...adsSource].reverse().slice(0, 2);

  const MiniCard = ({ evt }) => (
    <div className="card bg-dark border-secondary mb-3 shadow-sm">
      {evt.imageUrl && (
        <img 
          src={evt.imageUrl} 
          className="card-img-top" 
          alt={evt.title} 
          style={{ height: '100px', objectFit: 'cover', opacity: 0.8 }} 
          onClick={() => onEventSelect(evt)}
        />
      )}
      <div className="card-body p-2 text-white">
        <h6 className="fw-bold text-truncate" style={{fontSize: '0.85rem'}}>{evt.title}</h6>
        <div className="d-flex justify-content-between align-items-center mt-1">
            <span className="badge bg-secondary">{evt.price} ₺</span>          
            <button 
                className="btn btn-outline-light btn-sm py-0" 
                style={{fontSize: '0.65rem'}}
                onClick={() => onEventSelect(evt)}
            >
              {t('eventList.goBtn')}
            </button>
        </div>
      </div>
    </div>
  );

return (
  <> 
      {/* POPUP YILBAŞI REKLAMI*/}
      {showPromo && (
        <div style={{
            position: 'fixed', top: 0, left: 0, width: '100%', height: '100%',
            backgroundColor: 'rgba(0,0,0,0.9)', zIndex: 9999,
            display: 'flex', alignItems: 'center', justifyContent: 'center'
        }}>
            <div className="card text-center p-5 bg-dark text-white border-warning shadow-lg" 
                 style={{maxWidth: '500px', border: '2px solid'}}>
                
                <h1 className="display-1">🎄</h1>
                <h2 className="fw-bold text-warning mb-3">{t('promo.title')}</h2>
                <p className="lead">
                 {t('promo.desc1')}
                 <strong>{t('promo.desc2')}</strong>
                 {t('promo.desc3')}
                  </p>
                
                <div className="bg-white text-dark p-2 rounded fw-bold fs-3 mb-4" style={{letterSpacing: '5px'}}>
                    YILBASI
                </div>
                
                <p className="small text-muted mb-4">{t('promo.hint')}</p>
                
                <button className="btn btn-warning btn-lg fw-bold w-100" onClick={handleClosePromo}>
                    {t('promo.btn')}
                </button>
            </div>
        </div>
      )}

    <div className="container-fluid mt-3">
      <div className="row">
        
        {/* SOL SÜTUN */}
        <div className="col-lg-2 d-none d-lg-block">
            <div className="sticky-top" style={{ top: '20px' }}>
                <h6 className="text-warning border-bottom border-secondary pb-2 mb-3 small">{t('eventList.stars')}</h6>
                {leftAds.map(evt => <MiniCard key={`L-${evt.id}`} evt={evt} />)}
            </div>
        </div>

        {/* ORTA SÜTUN (Ana Liste) */}
        <div className="col-lg-8 col-12">
          <div className="d-flex justify-content-between align-items-center mb-3 bg-dark p-2 rounded border border-secondary shadow-sm">
                <h5 className="text-white mb-0 ms-2">{t('eventList.eventsTitle')}</h5>
                <div className="d-flex align-items-center gap-2">
                    <span className="text-warning fw-bold small">{t('eventList.sortLabel')}</span>
                    <select 
                        className="form-select form-select-sm bg-dark text-white border-secondary" 
                        style={{ width: 'auto', cursor: 'pointer' }}
                        value={sortType}
                        onChange={(e) => onSortChange(e.target.value)}
                    >
                        <option value="upcoming">{t('eventList.sortUpcoming')}</option>
                        <option value="newest-added">{t('eventList.sortNewest')}</option>
                        <option value="best-selling">{t('eventList.sortPopular')}</option>
                    </select>
                </div>
            </div>
            <div className="row g-4 justify-content-center">
              {events.map((event) => (              
                <EventItem 
                    key={event.id} 
                    event={event} 
                    onSelect={onEventSelect} 
                    isFavorite={favoriteIds.some(favId => String(favId) === String(event.id))}
                />
              ))}
            </div>
        </div>

        {/* SAĞ SÜTUN */}
        <div className="col-lg-2 d-none d-lg-block">
             <div className="sticky-top" style={{ top: '20px' }}>
                <h6 className="text-info border-bottom border-secondary pb-2 mb-3 small">{t('eventList.forYou')}</h6>
                {rightAds.map(evt => <MiniCard key={`R-${evt.id}`} evt={evt} />)}
            </div>
        </div>

      </div>
    </div>
    </>
  );
}

export default EventList;
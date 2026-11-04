import React, { useEffect, useState } from 'react';
import apiClient from '../../apiClient'; 
import EventItem from './EventItem'; 
import { toast } from 'react-toastify';
import { useTranslation } from 'react-i18next';

function Favorites({ onEventSelect }) {
  const [favorites, setFavorites] = useState([]);
  const [loading, setLoading] = useState(true);
  const { t } = useTranslation();


  const fetchFavorites = async () => {
    try {
      const res = await apiClient.get('/api/favorites');
      setFavorites(res.data);
    } catch (error) {
      console.error("Favoriler alınamadı", error);     
      setLoading(false);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFavorites();
  }, []);
  
  const handleRemoveFromList = (eventId) => {
    setFavorites(prev => prev.filter(e => e.id !== eventId));
  };

  if (loading) return (
    <div className="d-flex justify-content-center mt-5">
      <div className="spinner-border text-light" role="status"></div>
    </div>
  );

  return (
    <div className="container mt-4">
      <h2 className="text-white mb-4 border-bottom border-secondary pb-2">{t('favoritesPage.title')}</h2>
      
      {favorites.length === 0 ? (
        <div className="alert-secondary text-center py-5 card bg-dark text-white">
          <h4>{t('favoritesPage.emptyTitle')}</h4>
          <p>{t('favoritesPage.emptyDesc')}</p>
        </div>
      ) : (
        <div className="row g-4">
          {favorites.map(event => (
            <EventItem 
                key={event.id} 
                event={event}                 
                onSelect={(e) => onEventSelect(e)}                 
                onFavoriteToggle={() => handleRemoveFromList(event.id)}
                isFavorite={true}
            />
          ))}
        </div>
      )}
    </div>
  );
}

export default Favorites;
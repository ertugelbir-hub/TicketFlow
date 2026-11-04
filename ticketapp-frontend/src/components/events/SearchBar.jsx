import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import apiClient from '../../apiClient';
function SearchBar({ onSearch }) {
  const { t } = useTranslation();
  const [filters, setFilters] = useState({
    
    q: "",      // Arama kelimesi backend q
    city: "",   
    type: ""    
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFilters({ ...filters, [name]: value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();  
    onSearch(filters);
  };

  const handleClear = () => {
    const emptyFilters = { q: "", city: "", type: "" };
    setFilters(emptyFilters);
    onSearch(emptyFilters); 
  };
  const [cities, setCities] = useState([]);

useEffect(() => {
  // Sistemde kayıtlı etkinlikleri çekip içindeki benzersiz şehirleri listeliyoruz
  apiClient.get('/api/events')
    .then(res => {
      if (res.data && Array.isArray(res.data)) {
        // null olmayan ve kendini tekrar etmeyen şehirleri alıp alfabetik sırala
        const uniqueCities = [...new Set(res.data.map(e => e.city).filter(Boolean))].sort();
        setCities(uniqueCities);
      }
    })
    .catch(err => console.error("Şehirler yüklenemedi:", err));
}, []);

  return (
    <div className="card bg-dark border-secondary mb-4 p-3 shadow-sm">
      <form onSubmit={handleSubmit}>
        <div className="row g-2 align-items-center">
          
          {/* Kelime Arama */}
          <div className="col-md-4">
           <input 
              type="text" 
              name="q" 
              className="form-control bg-dark text-white border-secondary" 
              placeholder={t('search.allEvents', 'Etkinlik ara...')}
              value={filters.q}
               onChange={handleChange}
              />
              </div>
          {/* Şehir Seçimi */}
          <div className="col-md-3">
          <select 
          name="city" 
          className="form-select bg-dark text-white border-secondary" 
          value={filters.city}
          onChange={handleChange}
           >
         <option value="">{t('search.allCities')}</option>
         {cities.map((cityName) => (
         <option key={cityName} value={cityName}>{cityName}</option>
         ))}
        </select>
        </div>
          {/* Tür Seçimi */}
          <div className="col-md-3">
            <select 
              name="type" 
              className="form-select bg-dark text-white border-secondary" 
              value={filters.type}
              onChange={handleChange}
            >
              <option value="">{t('search.allEvents')}</option>
              <option value="Concert">{t('admin.add.types.concert', 'Konser')}</option>
              <option value="Theatre">{t('admin.add.types.theatre', 'Tiyatro')}</option>
              <option value="Cinema">{t('admin.add.types.cinema', 'Sinema')}</option>
              <option value="Sport">{t('admin.add.types.sport', 'Spor')}</option>
            </select>
          </div>

          {/* Butonlar */}
          <div className="col-md-2 d-flex gap-2">
            <button type="submit" className="btn btn-primary w-100">{t('search.button')}</button>
            {(filters.q || filters.city || filters.type) && (
                <button type="button" onClick={handleClear} className="btn btn-outline-secondary">X</button>
            )}
          </div>

        </div>
      </form>
    </div>
  );
}

export default SearchBar;
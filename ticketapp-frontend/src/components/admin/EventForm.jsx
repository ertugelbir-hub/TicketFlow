import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';

function EventForm({ initialData, onSubmit, loading, onCancel }) {
    const { t } = useTranslation();
  const [formData, setFormData] = useState({
    title: "", 
    city: "", 
    description: "",
    venue: "", 
    type: "Concert",
    dateTime: "",
    totalSeats: 100, 
    price: 0,
    imageUrl: ""
  });

 useEffect(() => {
    if (initialData) {
      // Backend'den gelen "2025-12-10T20:00:00" formatını
      // inputun istediği "2025-12-10T20:00" haline (ilk 16 karakter) getiriyoruz.
      const formattedDate = initialData.dateTime 
        ? initialData.dateTime.substring(0, 16) 
        : "";

      setFormData({
        ...initialData,
        dateTime: formattedDate
      });
    }
  }, [initialData]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(formData);
  };

  return (
    <div className="card bg-dark border-secondary shadow-lg">
      <div className="card-body p-4">
        <h4 className="mb-4 text-warning">
          {initialData ? `✏️ ${t('admin.add.editTitle')}` : `➕ ${t('admin.add.title')}`}
        </h4>
        <form onSubmit={handleSubmit}>
          <div className="row g-3">
            <div className="col-md-12">
              <label className="form-label">{t('admin.add.name')}</label>
              <input 
                type="text" name="title" 
                className="form-control bg-secondary text-white border-0" 
                placeholder={t('admin.add.phTitle')} 
                value={formData.title} onChange={handleChange} required 
              />
            </div>
            
            <div className="col-md-12">
              <label className="form-label">{t('admin.add.desc')}</label>
              <textarea 
                name="description"
                className="form-control bg-secondary text-white border-0"
                placeholder={t('admin.add.phDesc')}
                rows="3"
                value={formData.description || ""} 
                onChange={handleChange} 
                required
              />
            </div>
            
            <div className="col-md-6">
              <label className="form-label">{t('admin.add.city')}</label>
              <input 
                type="text" name="city" 
                className="form-control bg-secondary text-white border-0" 
                placeholder={t('admin.add.phCity')} 
                value={formData.city} onChange={handleChange} required 
              />
            </div>
            
            <div className="col-md-6">
              <label className="form-label">{t('admin.add.venue')}</label>
              <input 
                type="text" name="venue" 
                className="form-control bg-secondary text-white border-0" 
                placeholder={t('admin.add.phVenue')} 
                value={formData.venue} onChange={handleChange} required 
              />
            </div>
            
            <div className="col-md-6">
              <label className="form-label">{t('admin.add.date')}</label>
              <input 
                type="datetime-local" name="dateTime" 
                className="form-control bg-secondary text-white border-0" 
                value={formData.dateTime} onChange={handleChange} required 
              />
            </div>
            
            <div className="col-md-6">
              <label className="form-label">{t('admin.add.type')}</label>
              <select name="type" className="form-select bg-secondary text-white border-0" value={formData.type} onChange={handleChange}>
                <option value="Concert">{t('admin.add.types.concert')}</option>
                <option value="Theatre">{t('admin.add.types.theatre')}</option>
                <option value="Cinema">{t('admin.add.types.cinema')}</option>
                <option value="Sport">{t('admin.add.types.sport')}</option>
              </select>
            </div>
            
            <div className="col-md-12">
              <label className="form-label text-secondary small">{t('admin.add.posterUrl')}</label>
              <input 
                type="text" name="imageUrl" 
                className="form-control bg-dark text-white border-secondary" 
                placeholder={`https://ornek.com/resim.jpg (${t('admin.add.posterHint')})`} 
                value={formData.imageUrl || ""} 
                onChange={handleChange} 
              />
            </div>
            
            <div className="col-md-6">
              <label className="form-label text-secondary small">{t('admin.add.capacity')}</label>
              <div className="input-group">
                <span className="input-group-text bg-secondary text-warning border-0">🔢</span>
                <input 
                  type="number" name="totalSeats" 
                  className="form-control bg-dark text-white border-secondary" 
                  placeholder="100" 
                  value={formData.totalSeats} onChange={handleChange} required 
                />
                <span className="input-group-text bg-secondary text-white border-0">{t('admin.add.pcs')}</span>
              </div>
            </div>
            
            <div className="col-md-6">
              <label className="form-label text-secondary small">{t('admin.add.price')}</label>
              <div className="input-group">
                <span className="input-group-text bg-secondary text-success border-0">💲</span>
                <input 
                  type="number" name="price" step="0.01" 
                  className="form-control bg-dark text-white border-secondary" 
                  placeholder="150.00" 
                  value={formData.price} onChange={handleChange} required 
                />
                <span className="input-group-text bg-secondary text-white border-0">₺</span>
              </div>
            </div>
            
            <div className="col-12 mt-4 d-flex gap-2">
              {initialData && (
                <button 
                  type="button" 
                  className="btn btn-outline-secondary w-50 py-2 fw-semibold"
                  onClick={onCancel}
                >
                  {t('admin.tickets.cancelBtn')}
                </button>
              )}
              <button 
                type="submit" 
                className={`btn btn-warning ${initialData ? 'w-50' : 'w-100'} fw-bold py-2 shadow-sm text-dark`}
                disabled={loading}
              >                    
                {loading 
                  ? t('admin.add.saving') 
                  : (initialData ? t('admin.add.updateBtn') : t('admin.add.createBtn'))}                             
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}

export default EventForm;
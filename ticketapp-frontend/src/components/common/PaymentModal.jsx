import React, { useState } from 'react';
import Cards from 'react-credit-cards-2';
import 'react-credit-cards-2/dist/es/styles-compiled.css'; 
import { toast } from 'react-toastify';
import { useTranslation } from 'react-i18next';

const PaymentModal = ({ totalAmount, onClose, onConfirm }) => {
  const { t } = useTranslation();
  const [state, setState] = useState({
    number: '',
    name: '',
    cvc: '',
    focus: '',
    expiryMonth: '', 
    expiryYear: ''   
  });
 
  const handleNumberChange = (e) => {    
    const val = e.target.value.replace(/\D/g, '');
    if (val.length <= 16) {
      setState((prev) => ({ ...prev, number: val }));
    }
  };
  
  const handleNameChange = (e) => {
    const val = e.target.value;
    // Sadece harf ve boşluk kabul et (Türkçe karakter dahil)
    if (val === '' || /^[a-zA-ZğüşöçıİĞÜŞÖÇ\s]+$/.test(val)) {
      setState((prev) => ({ ...prev, name: val.toUpperCase() }));
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    if (name === "cvc" && value.length > 3) return; 
    setState((prev) => ({ ...prev, [name]: value }));
  };

  const handleInputFocus = (evt) => {
    setState((prev) => ({ ...prev, focus: evt.target.name }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!state.number || state.number.length < 16 || !state.name || !state.expiryMonth || !state.expiryYear || !state.cvc) {
       toast.warning(t('paymentModal.validationWarning'));
        return;
    }
    onConfirm();
  };

  const currentYear = new Date().getFullYear();
  const years = Array.from({ length: 11 }, (_, i) => currentYear + i);
  const months = Array.from({ length: 12 }, (_, i) => (i + 1).toString().padStart(2, '0'));

  return (
    <div style={{
      position: 'fixed', top: 0, left: 0, width: '100%', height: '100%',
      backgroundColor: 'rgba(0,0,0,0.8)', zIndex: 1050,
      display: 'flex', alignItems: 'center', justifyContent: 'center'
    }}>
      <div className="card bg-dark text-white border-secondary shadow-lg p-4" style={{ width: '450px' }}>
        
        {/* Modal Başlığı */}
        <div className="d-flex justify-content-between align-items-center mb-4">
          <h5 className="mb-0 text-warning">💳 {t('paymentModal.title')}</h5>
          <button className="btn-close btn-close-white" onClick={onClose}></button>
        </div>

        {/* Canlı Kredi Kartı Önizleme */}
        <div className="mb-4">
          <Cards
            number={state.number}
            name={state.name}
            expiry={`${state.expiryMonth}${state.expiryYear.slice(-2)}`} 
            cvc={state.cvc}
            focused={state.focus}
            placeholders={{ name: t('paymentModal.cardNamePlaceholder') }}
            locale={{ valid: t('paymentModal.expiryLabel') }}
          />
        </div>

        <form onSubmit={handleSubmit}>       
          {/* Kart Numarası */}
          <div className="mb-3">
            <input
              type="tel" 
              name="number"
              className="form-control bg-secondary text-white border-0"
              placeholder={t('paymentModal.cardNumber')}
              value={state.number}
              onChange={handleNumberChange} 
              onFocus={handleInputFocus}
            />
          </div>          

          {/* Kart Üzerindeki İsim */}
          <div className="mb-3">
            <input
              type="text"
              name="name"
              className="form-control bg-secondary text-white border-0"
              placeholder={t('paymentModal.nameOnCard')}
              value={state.name}
              onChange={handleNameChange} 
              onFocus={handleInputFocus}
            />
          </div>

          {/* Tarih ve CVC */}
          <div className="row mb-4">                
            <div className="col-8">
              <div className="d-flex gap-2">
                <select 
                  name="expiryMonth" 
                  className="form-select bg-secondary text-white border-0"
                  value={state.expiryMonth}
                  onChange={handleInputChange}
                  onFocus={handleInputFocus}
                >
                  <option value="" disabled>{t('paymentModal.month')}</option>
                  {months.map(m => <option key={m} value={m}>{m}</option>)}
                </select>
                
                <select 
                  name="expiryYear" 
                  className="form-select bg-secondary text-white border-0"
                  value={state.expiryYear}
                  onChange={handleInputChange}
                  onFocus={handleInputFocus}
                >
                  <option value="" disabled>{t('paymentModal.year')}</option>
                  {years.map(y => <option key={y} value={y}>{y}</option>)}
                </select>
              </div>
            </div>

            {/* CVC */}
            <div className="col-4">
              <input
                type="tel"
                name="cvc"
                className="form-control bg-secondary text-white border-0 text-center"
                placeholder={t('paymentModal.cvc')}
                maxLength={3}
                value={state.cvc}
                onChange={(e) => {                            
                  const val = e.target.value.replace(/\D/g, '');
                  if (val.length <= 3) setState(prev => ({ ...prev, cvc: val }));
                }}
                onFocus={handleInputFocus}
              />
            </div>
          </div>

          {/* Onay Butonu */}
          <button type="submit" className="btn btn-success w-100 fw-bold py-2">
            {t('paymentModal.payAndFinish', { amount: totalAmount })} ✅
          </button>
        </form>
      </div>
    </div>
  );
};

export default PaymentModal;
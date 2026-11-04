import React, { useState } from 'react';
import { useCart } from '../../context/CartContext';
import PaymentModal from '../common/PaymentModal';
import { toast } from 'react-toastify';
import apiClient from "../../apiClient"; 
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import PurchaseSuccessModal from '../common/PurchaseSuccessModal';

const CartPage = ({ onBack, onSelectEvent }) => {
  const { cartItems, removeFromCart, clearCart, totalPrice } = useCart();
  const [showPayment, setShowPayment] = useState(false);
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [purchasedTickets, setPurchasedTickets] = useState(null);
const [showSuccessModal, setShowSuccessModal] = useState(false);
  const handleCheckout = async () => {
    try {      
      const ticketsCreated = []
      for (const item of cartItems) {
        const res = await apiClient.post("/api/tickets/purchase", {
          eventId: item.eventId,
          seatIds: item.seatIds,
          quantity: item.quantity,
          couponCode: item.couponCode || ""
        });
        if (res.data) {
          ticketsCreated.push(res.data);
        }
      }
      
      toast.success(t('toasts.paymentSuccess'));    
      setPurchasedTickets(ticketsCreated.flat());
      clearCart(); 
      setShowSuccessModal(true);
      

    } catch (error) {
      console.error(error);
      const errMsg = error.response?.data?.message || t('toasts.unknownError');
      toast.error(t('toasts.paymentError', { msg: errMsg }));
    }
  };

 if (cartItems.length === 0 && !showSuccessModal) {
    return (
      <div className="text-center text-white mt-5">
        <h2>{t('cartPage.empty')}</h2>       
        <button onClick={onBack} className="btn btn-primary mt-3">
          {t('cartPage.backToEvents')}
        </button>
      </div>
    );
  }

  return (
    <div className="container mt-4 text-white">
      {/* Üst Bar: İç İçe Geçmeyi Önleyen Şık Başlık Düzeni */}
      <div className="d-flex align-items-center justify-content-between mb-4 border-bottom border-secondary pb-3">
        <div className="d-flex align-items-center gap-3">
          <button onClick={onBack} className="btn btn-outline-secondary">
             {t('common.back', 'Geri')}
          </button>
          <h2 className="mb-0">🛒 {t('app.cart', 'Alışveriş Sepetim')}</h2>
        </div>
        <button onClick={onBack} className="btn btn-outline-info btn-sm">
          {t('cartPage.backToEvents', 'Alışverişe Devam Et')} ↗
        </button>
      </div>
      
      <div className="row"> 
        <div className="col-md-8">
          {cartItems.map((item) => (
            <div key={item.id} className="card bg-dark border-secondary mb-3 shadow-sm">
              <div className="card-body d-flex justify-content-between align-items-center">
                <div>
                  <h5 className="card-title text-info"
                    style={{ cursor: 'pointer' }}
                    onClick={() => onSelectEvent && onSelectEvent(item.eventId)}
                    >
                    {item.eventTitle}
                  </h5>
                  <p className="mb-0 text-secondary small">
                  {t('cartPage.seats')}{' '}
                  {(item.seatLabels && item.seatLabels.length > 0 ? item.seatLabels : item.seatIds).join(", ")} |{' '}
                  {t('cartPage.qty')} {item.quantity}
                  {item.couponCode && <span className="badge bg-warning text-dark ms-2">{t('cartPage.coupon')} {item.couponCode}</span>}
                 </p>
                </div>
                <div className="d-flex align-items-center gap-3">
                  <span className="fs-5 fw-bold">{item.price.toFixed(2)} ₺</span>
                  <button onClick={() => removeFromCart(item.id)} className="btn btn-outline-danger btn-sm">{t('cartPage.delete')}</button>
                </div>
              </div>
            </div>
          ))}
        </div>
    
        <div className="col-md-4">
          <div className="card bg-secondary bg-opacity-25 text-white p-3">
            <h4>{t('cartPage.summaryTitle')}</h4>
            <hr />
            <div className="d-flex justify-content-between fs-4 fw-bold mb-4">
              <span>{t('cartPage.total')}</span>
              <span className="text-warning">{totalPrice.toFixed(2)} ₺</span>
            </div>
            
            <button 
              onClick={() => setShowPayment(true)} 
              className="btn btn-success w-100 py-2 fs-5"
            >
             {t('cartPage.checkoutBtn')}
            </button>
          </div>
        </div>
      </div>
    
      {showPayment && (
        <PaymentModal 
          totalAmount={totalPrice.toFixed(2)}
          onClose={() => setShowPayment(false)}
          onConfirm={() => {
            setShowPayment(false);
            handleCheckout();
          }}
        />
      )}
      {showSuccessModal && (
  <PurchaseSuccessModal 
    ticketData={purchasedTickets}
    onClose={() => {
      setShowSuccessModal(false);
      onBack();
    }}
  />
)}
    </div>
    
  );
};

export default CartPage;
import { useState, useEffect } from "react";
import apiClient from "../../apiClient";
import { toast } from 'react-toastify';
import { getErrorMessage } from "../../utils/errorHelper";
import SeatMap from "./SeatMap";
import { useCart } from '../../context/CartContext';
import PaymentModal from '../common/PaymentModal';
import { useTranslation } from 'react-i18next';
import PurchaseSuccessModal from '../common/PurchaseSuccessModal';
function EventDetail({ event, onBack,liveData }) {
 // const [showModal, setShowModal] = useState(false); 
 const { addToCart } = useCart();
  const { t } = useTranslation();

  const [selectedSeatIds, setSelectedSeatIds] = useState([]);
  const [currentStock, setCurrentStock] = useState(event.totalSeats);
  const [animate, setAnimate] = useState(false);
  const [sold24h, setSold24h] = useState(0);
  const [lastSoldAmount, setLastSoldAmount] = useState(null);
  const [animate24h, setAnimate24h] = useState(false);
  const [loading, setLoading] = useState(false);
  const [couponCode, setCouponCode] = useState(""); 
  const [discountApplied, setDiscountApplied] = useState(false); 
  const [showPaymentModal, setShowPaymentModal] = useState(false); 
  const [purchasedTicket, setPurchasedTicket] = useState(null);
const [showSuccessModal, setShowSuccessModal] = useState(false);
const [selectedSeatLabels, setSelectedSeatLabels] = useState([]);

  useEffect(() => {
    const fetchStock = async () => {
      try {
        const res = await apiClient.get("/api/events/reports/sales", { params: { eventId: event.id } });
        if(res.data) {
          if (res.data.remaining !== undefined) setCurrentStock(res.data.remaining);
          if (res.data.soldLast24Hours !== undefined) setSold24h(res.data.soldLast24Hours);
        }
      } catch (error) {          
          setCurrentStock(event.totalSeats);
      }
    };
    fetchStock();
  }, [event.id]);

  useEffect(() => {
  window.scrollTo(0, 0);
}, [event.id]);

  useEffect(() => {
    if (liveData && liveData.eventId === event.id) {   
        setCurrentStock(liveData.remainingSeats);
        setAnimate(true);
        setTimeout(() => setAnimate(false), 500);         
        setSold24h(liveData.soldLast24Hours);
        setAnimate24h(true);
        setTimeout(() => setAnimate24h(false), 500);       
        if (liveData.quantity > 0) {
            setLastSoldAmount(liveData.quantity);           
            setTimeout(() => setLastSoldAmount(null), 10000);
        }
    }
  }, [liveData, event.id]);
  if (!event) return null;
const formatDateTime = (dateStr) => {
    if (!dateStr) return "-";
    try {
      const d = new Date(dateStr);
      if (!isNaN(d.getTime())) {
        const date = d.toLocaleDateString("tr-TR", { day: "2-digit", month: "2-digit", year: "numeric" });
        const time = d.toLocaleTimeString("tr-TR", { hour: "2-digit", minute: "2-digit" });
        return `${date} • ${time}`;
      }
    } catch (e) {
      console.error("Tarih formatlama hatası", e);
    }
    return String(dateStr).replace('T', ' • ').slice(0, 18);
  };
  const applyCoupon = () => {
    if (couponCode.toUpperCase() === "YILBASI") {
        setDiscountApplied(true);
        toast.success(t('toasts.couponSuccess'));
    } else {
        setDiscountApplied(false);
        toast.error(t('toasts.couponError'));
    }
  };
  const handleAddToCart = () => {
    if (!selectedSeatIds || selectedSeatIds.length === 0) {
      toast.warning(t('toasts.selectSeatWarning'));
      return;
    }

    if (selectedSeatIds.length > 6) {
        toast.warning(t('toasts.maxSeatWarning'));
        return;
    }
        if (discountApplied && selectedSeatIds.length > 2) {
    toast.warning(t('toasts.maxCouponTickets'));
    return;
}
    
  
    const cartItem = {
      id: Date.now(),
      eventId: event.id,
      eventTitle: event.title,
      seatIds: selectedSeatIds,
      seatLabels: selectedSeatLabels,
      quantity: selectedSeatIds.length,
      price: discountApplied 
             ? (selectedSeatIds.length * event.price * 0.80) 
             : (selectedSeatIds.length * event.price),
      couponCode: discountApplied ? couponCode : null
    };

    addToCart(cartItem);
    toast.success(t('toasts.cartSuccess'));
    
    setSelectedSeatIds([]);
    setDiscountApplied(false);
    setCouponCode("");
    setSelectedSeatLabels([]);
  };
  
  
  const handlePurchase = async () => {
    if (selectedSeatIds.length === 0) {
        toast.warning(t('eventDetail.selectSeatWarning'));
        return;
    }
     if (discountApplied && selectedSeatIds.length > 2) {
    toast.warning(t('toasts.maxCouponTickets'));
    return;
     }

    setLoading(true);
    try {
      const requestBody = {
        eventId: event.id,
        seatIds: selectedSeatIds,
        quantity: selectedSeatIds.length, 
        couponCode: discountApplied ? "YILBASI" : ""        
      };

      const res = await apiClient.post("/api/tickets/purchase", requestBody);

      toast.success(t('eventDetail.purchaseSuccess', { count: selectedSeatIds.length }));
      setPurchasedTicket(res.data);
      setShowSuccessModal(true);
          
      setTimeout(() => {
          onBack();
      }, 10000);

    } catch (error) {
      console.error("Satın alma hatası:", error);
      const errorMsg = error.response?.data?.message || t('eventDetail.purchaseError');
      const msg = getErrorMessage(error);
      toast.error(`❌ ${msg}`);
    
  }  
  finally {   
      setLoading(false);
    }   

  };
  return (
    <div className="text-light mt-4">    
    
     
      <button onClick={onBack} className="btn btn-outline-secondary mb-4">
       {t('eventDetail.backToList')}
      </button>
      
     {event.imageUrl && (
        <div className="mb-4">
            <img 
                src={event.imageUrl} 
                alt={event.title} 
                className="event-detail-banner"
            />
        </div>
      )}

      {/* Etkinlik Detay Kartı */}
      <div className="card bg-dark border-secondary text-light shadow-lg">
        <div className="card-body p-5">
          <div className="row">
            {/* Sol Taraf: Bilgiler */}
            <div className="col-md-8">              
              <div className="d-flex flex-column align-items-start gap-2 mb-4">
                <div className="d-flex align-items-center gap-3"></div>
                <h2 className="display-5 fw-bold mb-0">{event.title}</h2>
    
             {/* 1. KALAN STOK */}
                    <div className={`border border-danger text-danger px-3 py-1 rounded d-flex align-items-center gap-2 ${animate ? 'stock-flash' : ''}`} style={{ transition: 'all 0.3s' }}>
                        <span className="fw-bold fs-5">{currentStock}</span>
                        <small className="text-uppercase fw-bold" style={{fontSize: '0.7rem'}}>{t('eventDetail.remaining')}</small>
                    </div>
                  </div>

                  <div className="d-flex align-items-center gap-3">
                    {/* 2. SON 24 SAAT  */}
                    <div className={`text-warning d-flex align-items-center gap-2 px-1 ${animate24h ? 'stock-flash' : ''}`} style={{ transition: 'all 0.3s' }}>
    <span className="fs-5 fw-bold">🔥 {t('eventDetail.sold24h', { count: sold24h })}</span>
</div>

                    {/* 3. GİZLİ BİLDİRİM (Biri alınca çıkar) */}
                    {lastSoldAmount && (
                        <div className="bg-danger text-white px-2 py-1 rounded shadow blink-infinite">
                            <small className="fw-bold">{t('eventDetail.justSold', { count: lastSoldAmount })}</small>
                        </div>
                    )}
                  </div>              
              <p className="lead text-secondary">{event.description || t('eventDetail.noDesc')}</p>
              
              <div className="mt-4">
                <p className="mb-2"><strong className="text-info">📍{t('eventDetail.venue')}</strong> {event.venue}, {event.city}</p>
                <p className="mb-2">
  <strong className="text-warning">📅 {t('eventDetail.date')}:</strong>{' '}
  <span className="text-white fw-semibold">{formatDateTime(event.dateTime)}</span>
</p>
              </div>            
            {/* KOLTUK HARİTASI*/}
              <hr className="border-secondary" />
              <SeatMap 
                  eventId={event.id} 
                  onSelectionChange={(ids, labels) => {setSelectedSeatIds(ids);setSelectedSeatLabels(labels || []);} }
              />
              </div>

            {/* Sağ Taraf: Fiyat ve Buton */}
            <div 
            className="col-md-4 border-start border-secondary ps-md-4 mt-4 mt-md-0" 
             style={{ position: 'sticky', top: '24px', alignSelf: 'flex-start' }}
                  >
              <div className="mb-4">
                <span className="d-block text-secondary small">{t('eventDetail.ticketPrice')}</span>
                {discountApplied ? (
                    <div>
                        <span className="text-decoration-line-through text-muted fs-5 me-2">{event.price} ₺</span>
                        <span className="display-6 fw-bold text-warning">
                            {discountApplied ? (event.price * 0.8).toFixed(2) : event.price} ₺
                        </span>
                    </div>
                ) : (
                    <span className="display-6 fw-bold text-success">{event.price} ₺</span>
                )}
              </div>
              {/* Kupon kısmı*/}
              <div className="input-group mb-3">
                  <input 
                    type="text" 
                    className="form-control bg-dark text-white border-secondary" 
                    placeholder={t('eventDetail.couponPlaceholder')}
                    value={couponCode}
                    onChange={(e) => setCouponCode(e.target.value)}
                    disabled={discountApplied} // İndirim varsa kutuyu kilitle
                  />
                  <button 
                    className="btn btn-outline-warning" 
                    type="button" 
                    onClick={applyCoupon}
                    disabled={discountApplied}
                  >
                   {t('eventDetail.apply')}
                  </button>
              </div>
              {/* Seçim Özeti */}
              <div className="bg-secondary bg-opacity-25 p-3 rounded mb-3 text-start">
                  <div className="d-flex justify-content-between mb-2">
                      <span>{t('eventDetail.selectedCount')}</span>
                      <span className="fw-bold">{selectedSeatIds.length} {t('admin.add.pcs')}</span>
                  </div>
                  <div className="d-flex justify-content-between">
                      <span>{t('eventDetail.total')}</span>
                      <span className="fw-bold text-info fs-5">
                          {discountApplied 
                           ? (selectedSeatIds.length * event.price * 0.80).toFixed(2) 
                           : (selectedSeatIds.length * event.price).toFixed(2)
                         } ₺
                      </span>
                  </div>
              </div>
              <button 
                className="btn btn-warning w-100 py-3 mb-2 fw-bold fs-5 shadow-sm text-dark"
                onClick={handleAddToCart}
                disabled={selectedSeatIds.length === 0}
              >
                🛒 {t('eventDetail.addToCart')}
              </button>
              <button 
                className="btn btn-success w-100 py-3 mt-3 fw-bold fs-5 shadow-sm"               
                onClick={() => {                   
                    if (selectedSeatIds.length === 0) {
                        toast.warning(t('eventDetail.selectSeatWarning'));
                        return;
                    }                   
                    setShowPaymentModal(true);
                }}        
                
                disabled={loading || selectedSeatIds.length === 0}
            >
               {loading ? t('app.loading') : "⚡ " + t('eventDetail.buyNow')}
            </button>
            </div>
          </div>
        </div>
      </div>   
      {showPaymentModal && (
        <PaymentModal 
            // İndirimli tutarı hesaplayıp gösterelim
            totalAmount={
                discountApplied 
                ? (selectedSeatIds.length * event.price * 0.80).toFixed(2) + " ₺"
                : (selectedSeatIds.length * event.price).toFixed(2) + " ₺"
            }
            onClose={() => setShowPaymentModal(false)}
            onConfirm={() => {
                setShowPaymentModal(false); 
                handlePurchase(); 
            }}
            
        />
      )}
      {showSuccessModal && (
  <PurchaseSuccessModal 
    ticketData={purchasedTicket}
    onClose={() => {
      setShowSuccessModal(false);
      onBack(); 
    }}
  />
)}
    </div>
  );
}

export default EventDetail;
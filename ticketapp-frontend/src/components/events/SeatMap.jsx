import React, { useEffect, useState } from 'react';
import apiClient from '../../apiClient';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import { useTranslation } from 'react-i18next';
import './SeatMap.css';
import { toast } from 'react-toastify';
function SeatMap({ eventId, onSelectionChange }) {
  const [seats, setSeats] = useState([]);
  const [selectedIds, setSelectedIds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [timeLeft, setTimeLeft] = useState(null);
   const { t } = useTranslation();
  

  const parseHoldDate = (d) => {
    if (!d) return null;
    
    // Yabancı saati türk saatine çevirme
    if (typeof d === 'string') {
        return new Date(d.endsWith('Z') ? d : d + 'Z');
    }
    
    return new Date(d);
  };
  useEffect(() => {
    const fetchSeats = async () => {
      try {
        const res = await apiClient.get(`/api/events/${eventId}/seats`);
        setSeats(res.data);
      } catch (error) {
        console.error("Koltuklar yüklenemedi:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchSeats();
  }, [eventId]);

  // Websocket
  useEffect(() => {    
    const socket = new SockJS('http://localhost:8080/ws'); 
    const stompClient = Stomp.over(socket);
    stompClient.debug = () => {};

      stompClient.connect({}, () => {
       /* console.log("Canlı Koltuk Takibi Başladı"); */
                
        stompClient.subscribe(`/topic/events/${eventId}/seats`, (message) => {     
            try {
                const incomingData = JSON.parse(message.body);               
            /*    console.log("BACKEND'DEN CANLI MESAJ GELDİ:", incomingData); */
                

                // Backend bazen liste gönderiyor, bazen tek obje. Biz hepsini listeye çevirelim ki garanti olsun:
                const updatedSeatsList = Array.isArray(incomingData) ? incomingData : [incomingData];

                setSeats(prevSeats => {                   
                    return prevSeats.map(seat => {                      
                        const matchingUpdate = updatedSeatsList.find(u => u.id === seat.id);
                        
                      
                        if (matchingUpdate) {                           
                             return { ...seat, ...matchingUpdate };
                        }
                        return seat;
                    });
                });

            } catch (e) {
                console.error("❌ Mesaj işleme hatası:", e);
            }
       });
    }, (err) => {
        console.error("🔴 WebSocket Bağlantı Hatası:", err);
    });

    return () => {
        if (stompClient && stompClient.connected) stompClient.disconnect();
    };
}, [eventId]);
// Kronometre sistemi
  useEffect(() => {    
    if (selectedIds.length === 0) {
        setTimeLeft(null);
        return;
    }   
    const interval = setInterval(() => {
        let activeSelections = []; 
        let earliestExpiry = null; 

        // Koltuk kontrolü
        selectedIds.forEach(id => {
            const seat = seats.find(s => s.id === id);
            if (seat) {
                const holdDate = parseHoldDate(seat.holdExpiresAt);
                
                // Eğer koltuğun süresi hala bitmemişse
                if (holdDate && holdDate > new Date()) {
                    activeSelections.push(id); 
                    
                    // En erken bitecek olan koltuğun süresini bul
                    if (!earliestExpiry || holdDate < earliestExpiry) {
                        earliestExpiry = holdDate;
                    }
                }
            }
        });

        // Koltuğun süresi bittiyse temizleme 
        if (activeSelections.length !== selectedIds.length) {
            setSelectedIds(activeSelections); 
            onSelectionChange(activeSelections, getSeatLabels(activeSelections));
        }

        // Ekrana kalan süreyi veren
        if (earliestExpiry) {
            const now = new Date();
            const diff = earliestExpiry - now; 
            
            if (diff > 0) {
                const m = Math.floor(diff / 60000); 
                const s = Math.floor((diff % 60000) / 1000); 
                // 9:5 yerine 09:05 yazsın diye padStart kullanıyoruz
                setTimeLeft(`${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`);
            } else {
                setTimeLeft("00:00");
            }
        } else {
            setTimeLeft(null);
        }

    }, 1000);
  
    return () => clearInterval(interval);
  }, [selectedIds, seats, onSelectionChange]);

  const getSeatLabels = (ids) => {
    return ids.map(id => {
      const s = seats.find(item => item.id === id);
      return s ? `${s.rowName}-${s.seatNumber}` : id;
    });
  };
  
 const handleSeatClick = async (seat) => {
    if (seat.isSold || seat.sold) return; 
    
    if (selectedIds.includes(seat.id)) {
        try {
            await apiClient.post(`/api/tickets/events/${eventId}/seats/${seat.id}/unhold`);
            const newSelection = selectedIds.filter(id => id !== seat.id);
            setSelectedIds(newSelection);
            onSelectionChange(newSelection, getSeatLabels(newSelection));
        } catch (error) {
    console.error("Rezerve hatası:", error);
    const errorMsg = error.response?.data?.message || t('admin.seatMap.reserveError');
    toast.error(errorMsg);
}
        return;       
    } 
    
  
    try {      
        await apiClient.post(`/api/tickets/events/${eventId}/seats/${seat.id}/hold`);
        
        const newSelection = [...selectedIds, seat.id];
        setSelectedIds(newSelection);
        onSelectionChange(newSelection, getSeatLabels(newSelection));
        
    } catch (error) {
        console.error("Rezerve hatası:", error);       
        toast.error(typeof error.response?.data === 'string' ? error.response.data : t('admin.seatMap.reserveError'));
    }
  };

if (loading) return <div className="text-white text-center py-4">{t('admin.seatMap.loading')}</div>;

  const rows = {};
 const rowNames = [...new Set(seats.map(s => s.rowName))];
seats.forEach(seat => {
  if (!rows[seat.rowName]) rows[seat.rowName] = [];
  rows[seat.rowName].push(seat);
});
  const renderSeatButton = (seat, rowName) => {
    const isSelected = selectedIds.includes(seat.id);
    const isSold = seat.isSold || seat.sold;
    const holdDate = parseHoldDate(seat.holdExpiresAt);
    const isHeld = holdDate && holdDate > new Date();

    let btnClass = "btn-outline-secondary text-white";
    if (isSold) {
      btnClass = "btn-danger disabled opacity-50";
    } else if (isSelected) {
      btnClass = "btn-success shadow";
    } else if (isHeld) {
      btnClass = "btn-warning text-dark shadow";
    }

    return (
      <button
        key={seat.id}
        className={`btn btn-sm ${btnClass}`}
        style={{
          width: '38px',
          height: '38px',
          fontWeight: 'bold',
          fontSize: '13px',
          borderRadius: '8px',
          transition: 'all 0.15s ease'
        }}
        onClick={() => handleSeatClick(seat)}
        disabled={isSold || (isHeld && !isSelected)}
        title={isSold ? t('eventDetail.full') : isHeld ? t('eventDetail.reserved') : `${rowName}-${seat.seatNumber}`}
      >
        {seat.seatNumber}
      </button>
    );
  };
return (
    <div className="card bg-dark border-secondary mb-4 shadow-lg">
      <div className="card-header bg-black text-white border-secondary py-3 d-flex justify-content-between align-items-center">
        <h5 className="mb-0 fw-bold">{t('eventDetail.stageSelection')}</h5>
        {timeLeft && (
          <div className="badge bg-warning text-dark px-3 py-2 fs-6">
            ⏱️ {t('admin.seatMap.timeLimit')}: <span className="fw-bolder text-danger">{timeLeft}</span>
          </div>
        )}
      </div>

      <div className="card-body p-4">
        {/* Sahne Çizgisi */}
        <div className="text-center mb-5">
          <div
            className="mx-auto py-2 text-white fw-bold shadow-sm"
            style={{
              maxWidth: '520px',
              letterSpacing: '8px',
              backgroundColor: '#1e293b',
              borderTop: '4px solid #6366f1',
              borderRadius: '8px 8px 30px 30px',
              color: '#94a3b8'
            }}
          >
            {t('eventDetail.stage')}
          </div>
        </div>

        {/* Tribün / Blok Başlıkları */}
<div className="d-flex justify-content-center align-items-center text-muted small fw-bold mb-3" style={{ maxWidth: '640px', margin: '0 auto' }}>
  <div className="text-center" style={{ flex: '3' }}>{t('seatMap.leftTribune')}</div>
  <div style={{ width: '32px' }}></div>
  <div className="text-center text-primary" style={{ flex: '4' }}>{t('seatMap.centerBlock')}</div>
  <div style={{ width: '32px' }}></div>
  <div className="text-center" style={{ flex: '3' }}>{t('seatMap.rightTribune')}</div>
</div>

       {/* KOLTUK SIRALARI VE AMFİ DÜZENİ */}
<div className="d-flex flex-column align-items-center">
  {rowNames.map((rowName, index) => {
    const rowSeats = rows[rowName].sort((a, b) => a.seatNumber - b.seatNumber);
    const leftBlock = rowSeats.filter(s => s.seatNumber <= 3);
    const centerBlock = rowSeats.filter(s => s.seatNumber >= 4 && s.seatNumber <= 7);
    const rightBlock = rowSeats.filter(s => s.seatNumber >= 8);

    // Her 6 sırada bir koridor oluştur (6. sıradan sonra, 12. sıradan sonra vb.)
    const showCorridor = index > 0 && index % 6 === 0;
    const corridorNumber = index / 6;

    return (
      <React.Fragment key={rowName}>
        {/* HER 6 SIRADA BİR DİNAMİK YATAY KORİDOR */}
        {showCorridor && (
          <div className="w-100 my-4 d-flex align-items-center justify-content-center">
            <div className="horizontal-walkway d-flex align-items-center justify-content-between px-4 py-2 rounded text-muted small fw-bold">
  <span className="opacity-75">◄ {t('seatMap.exitFoyer')}</span>
  <span className="text-warning tracking-wider">
  {corridorNumber}. {t('seatMap.corridor')}
</span>
  <span className="opacity-75">{t('seatMap.entranceEmergency')} ►</span>
</div>
          </div>
        )}

        {/* KOLTUK SIRASI (V-ŞEKLİNDE KANATLAR) */}
        <div className="d-flex align-items-center justify-content-center mb-3 seat-row-line">
          {/* Sol Sıra Harfi */}
          <span className="text-secondary fw-bold text-center me-3" style={{ width: '24px' }}>
            {rowName}
          </span>

          {/* 1. SOL KANAT */}
          <div className="d-flex gap-2 p-1 rounded v-wing-left">
            {leftBlock.map(seat => renderSeatButton(seat, rowName))}
          </div>

          {/* Dikey Koridor Boşluğu */}
          <div className="vertical-aisle"></div>

          {/* 2. ORTA SAHNE ÖNÜ BLOK */}
          <div className="d-flex gap-2 p-1 rounded v-center-block">
            {centerBlock.map(seat => renderSeatButton(seat, rowName))}
          </div>

          {/* Dikey Koridor Boşluğu */}
          <div className="vertical-aisle"></div>

          {/* 3. SAĞ KANAT */}
          <div className="d-flex gap-2 p-1 rounded v-wing-right">
            {rightBlock.map(seat => renderSeatButton(seat, rowName))}
          </div>

          {/* Sağ Sıra Harfi */}
          <span className="text-secondary fw-bold text-center ms-3" style={{ width: '24px' }}>
            {rowName}
          </span>
        </div>
      </React.Fragment>
    );
  })}
</div>

        {/* Legend / Bilgilendirme */}
        <div className="d-flex justify-content-center gap-4 mt-4 small text-white">
            <div className="d-flex align-items-center gap-1"><div className="bg-light border rounded" style={{width: 15, height: 15}}></div> {t('eventDetail.empty')}</div>
            <div className="d-flex align-items-center gap-1"><div className="bg-success rounded" style={{width: 15, height: 15}}></div> {t('eventDetail.selected')}</div>
            <div className="d-flex align-items-center gap-1"><div className="bg-warning rounded" style={{width: 15, height: 15}}></div> {t('eventDetail.reserved')}</div>
            <div className="d-flex align-items-center gap-1"><div className="bg-danger rounded" style={{width: 15, height: 15}}></div> {t('eventDetail.full')}</div>
        </div>
      </div>
    </div>
  );
}

export default SeatMap;
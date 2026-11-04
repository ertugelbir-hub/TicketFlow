import { useState, useEffect } from "react";

function TicketModal({ event, onClose, onConfirm }) {
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [isProcessing, setIsProcessing] = useState(false);
  
  // Simülasyon
  const [occupiedSeats, setOccupiedSeats] = useState([]);

  useEffect(() => {
    const randomOccupied = [];
    const totalSeats = 40; 
    for(let i=0; i<totalSeats; i++) {
      if(Math.random() < 0.3) { // %30 ihtimalle dolu olsun
        randomOccupied.push(i);
      }
    }
    setOccupiedSeats(randomOccupied);
  }, []);

  const toggleSeat = (index) => {
    if (occupiedSeats.includes(index)) return; 

    if (selectedSeats.includes(index)) {
      setSelectedSeats(selectedSeats.filter(id => id !== index));
    } else {   
      if (selectedSeats.length < 10) {
        setSelectedSeats([...selectedSeats, index]);
      }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (selectedSeats.length === 0) return;

    setIsProcessing(true);
    await new Promise(resolve => setTimeout(resolve, 1500)); // Yapay gecikme    

    await onConfirm(selectedSeats.length);
    
    setIsProcessing(false);
  };

  const totalPrice = event.price * selectedSeats.length;

  return (
    <div className="modal show d-block" style={{ backgroundColor: "rgba(0,0,0,0.8)" }}>
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content bg-dark text-white border-secondary">
          
          <div className="modal-header border-secondary">
            <div>
                <h5 className="modal-title">Koltuk Seçimi</h5>
                <small className="text-secondary">{event.title}</small>
            </div>
            <button type="button" className="btn-close btn-close-white" onClick={onClose}></button>
          </div>

          <div className="modal-body text-center">
            {/* SAHNE GÖRSELİ */}
            <div className="mb-4 w-100 bg-secondary rounded-pill py-1 text-dark fw-bold" style={{ opacity: 0.5 }}>
                SAHNE
            </div>

            {/* KOLTUK HARİTASI (GRID) */}
            <div className="d-flex flex-wrap justify-content-center gap-2 mb-4" style={{ maxWidth: '300px', margin: '0 auto' }}>
                {Array.from({ length: 40 }).map((_, index) => {
                    const isOccupied = occupiedSeats.includes(index);
                    const isSelected = selectedSeats.includes(index);
                    
                    let bgColor = "bg-outline-secondary"; 
                    let cursor = "pointer";
                    
                    if (isOccupied) {
                        bgColor = "bg-danger opacity-50"; 
                        cursor = "not-allowed";
                    } else if (isSelected) {
                        bgColor = "bg-success"; 
                    } else {
                        bgColor = "bg-secondary"; 
                    }

                    return (
                        <div 
                            key={index}
                            onClick={() => toggleSeat(index)}
                            className={`rounded-3 ${bgColor}`}
                            style={{ 
                                width: '30px', 
                                height: '30px', 
                                cursor: cursor,
                                transition: 'all 0.2s',
                                border: isSelected ? '2px solid white' : 'none'
                            }}
                            title={isOccupied ? "Dolu" : `Koltuk ${index + 1}`}
                        />
                    );
                })}
            </div>

            {/* BİLGİLER */}
            <div className="d-flex justify-content-between px-4 mb-3 text-small">
                <div className="d-flex align-items-center gap-2">
                    <div className="bg-secondary rounded" style={{width: 15, height: 15}}></div> Boş
                </div>
                <div className="d-flex align-items-center gap-2">
                    <div className="bg-success rounded" style={{width: 15, height: 15}}></div> Seçili
                </div>
                <div className="d-flex align-items-center gap-2">
                    <div className="bg-danger opacity-50 rounded" style={{width: 15, height: 15}}></div> Dolu
                </div>
            </div>

            <hr className="border-secondary"/>

            <div className="d-flex justify-content-between align-items-center px-2">
                <div className="text-start">
                    <div className="text-secondary small">Seçilen: {selectedSeats.length} Adet</div>
                    <div className="fs-4 fw-bold text-warning">{totalPrice} ₺</div>
                </div>
                
                <div>
                    <button type="button" className="btn btn-outline-secondary me-2" onClick={onClose} disabled={isProcessing}>
                        İptal
                    </button>
                    <button 
                        onClick={handleSubmit} 
                        className="btn btn-success fw-bold"
                        disabled={isProcessing || selectedSeats.length === 0}
                    >
                        {isProcessing ? (
                            <>
                                <span className="spinner-border spinner-border-sm me-2"></span>
                                İşleniyor...
                            </>
                        ) : "Satın Al"}
                    </button>
                </div>
            </div>

          </div>
        </div>
      </div>
    </div>
  );
}

export default TicketModal;
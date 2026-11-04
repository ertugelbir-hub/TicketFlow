import React from 'react';

function NotFound({ onGoHome }) {
  return (
    <div className="d-flex flex-column justify-content-center align-items-center vh-100 text-white text-center" 
         style={{ backgroundColor: "#1a1a1a" }}>
      
      {/* Büyük 404 Yazısı */}
      <h1 className="display-1 fw-bold text-warning" style={{ fontSize: "8rem" }}>404</h1>
      
      {/* Mesaj */}
      <h2 className="mb-3">Üzgünüz, Aradığınız Sayfa Bulunamadı! 🚀</h2>
      <p className="text-secondary mb-5 lead" style={{ maxWidth: "600px" }}>
        Gitmek istediğiniz sayfa silinmiş, taşınmış veya hiç var olmamış olabilir. 
        Bilet gişesi ana sayfada sizi bekliyor.
      </p>

      {/* Dönüş Butonu */}
      <button 
        onClick={onGoHome} 
        className="btn btn-outline-warning btn-lg px-5 py-2 rounded-pill fw-bold"
      >
        🏠 Ana Sayfaya Dön
      </button>

      {/* Süsleme */}
      <div className="mt-5 opacity-25">
        <span style={{ fontSize: "5rem" }}>🎫 👻</span>
      </div>
    </div>
  );
}

export default NotFound;
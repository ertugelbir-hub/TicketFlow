import React from 'react';
import { useTranslation } from 'react-i18next';

// ⚡ TicketFlow Logosu
function TicketFlowLogo() {
  return (
    <div className="d-flex align-items-center gap-2" style={{ cursor: "pointer" }}>
      <img 
        src="/logo.png" 
        alt="TicketFlow Logo" 
        width="62" 
        height="62"
        style={{ 
          borderRadius: "8px",
          filter: "drop-shadow(0 0 8px rgba(56, 189, 248, 0.75))" 
        }}
      />
      <span className="fw-extrabold fs-2 tracking-tight mb-0 text-white" style={{ letterSpacing: '-0.8px', fontWeight: 800 }}>
        Ticket<span style={{background: 'linear-gradient(135deg, #38bdf8 0%, #a855f7 100%)',
          WebkitBackgroundClip: 'text',
          WebkitTextFillColor: 'transparent',
          display: 'inline-block'
        }}>Flow</span>
      </span>
    </div>
  );
}

function Navbar({ 
  isAuthenticated, 
  userRole, 
  cartCount, 
  currentTab, 
  onNavigate, 
  onLogout 
}) {
  const { t, i18n } = useTranslation();
  const username = localStorage.getItem("username") || sessionStorage.getItem("username");

  return (
    <div className="d-flex flex-wrap justify-content-between align-items-center mb-4 pb-3 gap-3 border-bottom border-secondary border-opacity-25">
      
      {/* Sol: Logo */}
      <div role="button" onClick={() => onNavigate('home')}>
        <TicketFlowLogo />
      </div>

      {/* Sağ: Menü Butonları */}
      {isAuthenticated && (
        <div className="d-flex gap-2 align-items-center flex-wrap justify-content-end">
          
          {/* Dil Seçimi */}
          <div className="btn-group me-2" role="group">
            {['tr', 'en', 'de'].map(lang => (
              <button 
                key={lang}
                type="button" 
                className={`btn btn-sm px-2 ${i18n.language === lang ? 'btn-primary fw-bold' : 'btn-dark text-muted border-secondary'}`} 
                onClick={() => {
                  i18n.changeLanguage(lang);
                  localStorage.setItem('user-language', lang);
                }}
              >
                {lang.toUpperCase()}
              </button>
            ))}
          </div>

          {/* Sepetim */}
          <button 
            className={`btn btn-sm ${currentTab === 'cart' ? 'btn-primary' : 'btn-dark text-white border-secondary'} position-relative px-3`}
            onClick={() => onNavigate('cart')}
          >
            🛒 {t('app.cart')}
            {cartCount > 0 && (
              <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger" style={{fontSize: '0.65rem'}}>
                {cartCount}
              </span>
            )}
          </button>

          {/* Yönetici Paneli */}
          {userRole === 'ADMIN' && (
            <button 
              className={`btn btn-sm ${currentTab === 'admin' ? 'btn-primary' : 'btn-dark text-white border-secondary'}`} 
              onClick={() => onNavigate('admin')}
            >
              ⚡ {t('app.adminPanel')}
            </button>
          )}

          {/* Biletlerim */}
          <button 
            className={`btn btn-sm ${currentTab === 'myTickets' ? 'btn-primary' : 'btn-dark text-white border-secondary'}`} 
            onClick={() => onNavigate('myTickets')}
          >
            🎟️ {t('app.myTickets')}
          </button>

          {/* Favorilerim */}
          <button 
            className={`btn btn-sm ${currentTab === 'favorites' ? 'btn-primary' : 'btn-dark text-white border-secondary'}`} 
            onClick={() => onNavigate('favorites')}
          >
            ❤️ {t('app.favorites')}
          </button>

          {/* Profil */}
          <button 
            className={`btn btn-sm ${currentTab === 'profile' ? 'btn-primary' : 'btn-dark text-white border-secondary'}`} 
            onClick={() => onNavigate('profile')}
          >
            👤 {username}
          </button>

          {/* Çıkış */}
          <button onClick={onLogout} className="btn btn-sm btn-outline-danger ms-1">
            {t('app.logout')}
          </button>
        </div>
      )}
    </div>
  );
}

export default Navbar;
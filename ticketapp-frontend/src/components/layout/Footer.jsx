import React from 'react';
import { useTranslation } from 'react-i18next';
function Footer() {
    const { t } = useTranslation();
  return (
    <footer className="mt-5 pt-4 pb-4 border-top border-secondary border-opacity-25 text-muted small">
      <div className="d-flex flex-wrap justify-content-between align-items-center gap-3">
        
        {/* Sol: Logo ve Sistem Durum Rozeti */}
        <div className="d-flex align-items-center gap-2 flex-wrap">
          <span className="fw-bold text-white fs-6">
            Ticket<span style={{
              background: 'linear-gradient(135deg, #38bdf8 0%, #a855f7 100%)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
              display: 'inline-block'
            }}>Flow</span>
          </span>

          <span 
  className="badge rounded-pill bg-black bg-opacity-50 border border-secondary border-opacity-50 text-light px-2 py-1 ms-2 d-inline-flex align-items-center gap-2"
  style={{ fontSize: '0.72rem', cursor: 'default' }}
  title={t('footer.statusTooltip', 'Tüm servisler ayakta ve çalışıyor')}
>
  <span 
    className="rounded-circle bg-success d-inline-block"
    style={{ 
      width: '7px', 
      height: '7px', 
      boxShadow: '0 0 8px #22c55e' 
    }} 
  />
  {t('footer.status')}
</span>
        </div>

        {/* Sağ: Mimari Etiketleri, Swagger & GitHub Butonları */}
        <div className="d-flex align-items-center gap-2 flex-wrap" style={{ fontSize: '0.75rem' }}>
          
          <span className="badge bg-secondary bg-opacity-25 text-white-50 border border-secondary border-opacity-25 px-2 py-1">
            v1.0.0
          </span>

          <span className="badge bg-black bg-opacity-40 border border-secondary border-opacity-25 text-secondary fw-normal px-2 py-1">
            Spring Boot 3
          </span>
          <span className="badge bg-black bg-opacity-40 border border-secondary border-opacity-25 text-secondary fw-normal px-2 py-1">
            React 18
          </span>
          <span className="badge bg-black bg-opacity-40 border border-secondary border-opacity-25 text-secondary fw-normal px-2 py-1">
            Kafka & Redis
          </span>

          {/* Swagger API Dokümantasyon Butonu */}
          <a 
            href="http://localhost:8080/swagger-ui.html" 
            target="_blank" 
            rel="noreferrer"
            className="btn btn-sm btn-outline-info py-0 px-2 text-decoration-none"
            style={{ fontSize: '0.72rem', lineHeight: '1.6' }}
          >
            Swagger API ↗
          </a>

          {/* GitHub Depo/Profil Butonu */}
          <a 
            href="https://github.com/ertugelbir-hub" 
            target="_blank" 
            rel="noreferrer"
            className="btn btn-sm btn-outline-light py-0 px-2 text-decoration-none"
            style={{ fontSize: '0.72rem', lineHeight: '1.6' }}
          >
            GitHub ↗
          </a>

          <span className="text-secondary opacity-50 ms-1">•</span>
          <span className="text-secondary ms-1">© 2026</span>
        </div>

      </div>
    </footer>
  );
}

export default Footer;
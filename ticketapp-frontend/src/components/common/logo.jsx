import React from 'react';

export default function Logo({ size = 32 }) {
  return (
    <div className="d-flex align-items-center gap-2 text-decoration-none">
      <svg
        width={size}
        height={size}
        viewBox="0 0 36 36"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        style={{ filter: 'drop-shadow(0px 2px 8px rgba(37, 99, 235, 0.45))' }}
      >
        <defs>
          <linearGradient id="ticketFlowGrad" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stopColor="#38bdf8" />
            <stop offset="50%" stopColor="#2563eb" />
            <stop offset="100%" stopColor="#1d4ed8" />
          </linearGradient>
        </defs>

        {/* Dış Bilet Kartı Gövdesi */}
        <rect x="4" y="6" width="28" height="24" rx="6" fill="url(#ticketFlowGrad)" />

        {/* Sol ve Sağ Bilet Kupon Kesikleri */}
        <circle cx="4" cy="18" r="3.5" fill="#080d1a" />
        <circle cx="32" cy="18" r="3.5" fill="#080d1a" />

        {/* İç Akış / Dalga (Flow) Vektörü */}
        <path
          d="M12 18C14 14 16 14 18 18C20 22 22 22 24 18"
          stroke="#ffffff"
          strokeWidth="2.5"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
        <circle cx="12" cy="18" r="1.5" fill="#ffffff" />
        <circle cx="24" cy="18" r="1.5" fill="#ffffff" />
      </svg>

      <span className="fw-extrabold fs-4 tracking-tight text-white m-0 d-flex align-items-center" style={{ letterSpacing: '-0.7px' }}>
        Ticket<span style={{ color: '#38bdf8' }}>Flow</span>
      </span>
    </div>
  );
}
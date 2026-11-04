import "./App.css";
import { useEffect, useState } from "react";
import apiClient from "./apiClient";
import EventList from "./components/events/EventList";
import EventDetail from "./components/events/EventDetail";
import Auth from "./components/auth/Auth";
import MyTickets from "./components/tickets/MyTickets";
import AdminPanel from "./components/admin/AdminPanel"; 
import "bootstrap/dist/css/bootstrap.min.css";
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css'; 
import SearchBar from "./components/events/SearchBar";
import Profile from "./components/auth/Profile";
import { RotateLoader } from "react-spinners";
import NotFound from "./components/NotFound";
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import Favorites from "./components/events/Favorites";
import { useCart } from './context/CartContext';
import CartPage from './components/cart/CartPage';
import { useTranslation } from 'react-i18next';
import Navbar from "./components/layout/Navbar";
import Footer from "./components/layout/Footer";



function App() {
  const [isNotFound, setIsNotFound] = useState(window.location.pathname !== "/");
  const [isAuthenticated, setIsAuthenticated] = useState(!!(localStorage.getItem("token") || sessionStorage.getItem("token")));
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [liveData, setLiveData] = useState(null);
  const { cartItems, clearCart } = useCart();
  const [showCart, setShowCart] = useState(false);

  const [page, setPage] = useState(0); 
  const [totalPages, setTotalPages] = useState(0); 
  const [currentFilters, setCurrentFilters] = useState({}); 
  const [sortType, setSortType] = useState("upcoming"); 

  const [selectedEvent, setSelectedEvent] = useState(null);
  const [events, setEvents] = useState([]);
  const [showMyTickets, setShowMyTickets] = useState(false);
  const [showAdmin, setShowAdmin] = useState(false); 
  const [showProfile, setShowProfile] = useState(false);
  const [showFavorites, setShowFavorites] = useState(false);

  const userRole = localStorage.getItem("role") || sessionStorage.getItem("role");
  const { t, i18n } = useTranslation();

  useEffect(() => {
    if (isAuthenticated) {
      loadEvents();
    } else {
      setLoading(false);
    }
  }, [isAuthenticated]);

  // WebSocket
  useEffect(() => {    
    if (!isAuthenticated) return;
    let stompClient = null;

    const connectWebSocket = () => {
      const socket = new SockJS('http://localhost:8080/ws');
      stompClient = Stomp.over(() => socket);       
      stompClient.debug = () => {}; 

      stompClient.connect({}, () => {
        stompClient.subscribe('/topic/sales', (message) => {
          try {
            const eventData = JSON.parse(message.body);                 
            setLiveData(eventData);
          } catch (err) {
            console.error("Mesaj okuma hatası:", err);
          }
        });
      }, (err) => {
        console.error("WebSocket Bağlantı Hatası:", err);
      });
    };

    connectWebSocket();
    return () => {
      if (stompClient) stompClient.disconnect();
    };
  }, [isAuthenticated]);
  
  useEffect(() => {   
    if (!isAuthenticated) return;
    loadEvents(currentFilters, 0);
  }, [sortType]);

  async function loadEvents(filters = {}, pageNum = 0) {
    try {
      setLoading(true);      
      if (sortType === "best-selling") {
        const response = await apiClient.get('/api/events?sort=best-selling');
        let list = response.data || [];
        
        if (filters.q) list = list.filter(e => e.title?.toLowerCase().includes(filters.q.toLowerCase()));
        if (filters.city) list = list.filter(e => e.city?.toLowerCase() === filters.city.toLowerCase());
        if (filters.type) list = list.filter(e => e.type?.toLowerCase() === filters.type.toLowerCase());

        const pageSize = 6;
        setTotalPages(Math.ceil(list.length / pageSize) || 1);
        setPage(pageNum);
        setEvents(list.slice(pageNum * pageSize, (pageNum + 1) * pageSize));
        setCurrentFilters(filters);
        setError("");
        return;
      }
      const currentSortField = sortType === "newest-added" ? "id" : "dateTime";
      const currentSortDir = sortType === "newest-added" ? "desc" : "asc";
      const params = {
        page: pageNum,
        size: 6, 
        sort: currentSortField, 
        dir: currentSortDir,    
        ...filters        
      };  
      if (!params.q) delete params.q;
      if (!params.city) delete params.city;
      if (!params.type) delete params.type;

      const response = await apiClient.get("/api/events/search", { params });
      const data = response.data; 

      setEvents(data.content || (Array.isArray(data) ? data : [])); 
      setTotalPages(data.totalPages || 1); 
      setPage(data.number || 0); 
      setCurrentFilters(filters); 
      setError("");
    } catch (e) {
      console.error("Liste Hatası:", e);
      if (e.response && e.response.status === 400) {
        setError(e.response.data.message || "İstek hatalı (400). Backend loglarına bak.");
      } else if (e.response && e.response.status === 401) {
        handleLogout();
      } else {
        setError("Etkinlikler yüklenirken hata oluştu.");
      }
    } finally {
      setLoading(false);
    }
  }

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      loadEvents(currentFilters, newPage);
      window.scrollTo(0, 0);
    }
  };

  const handleLogout = () => {
    localStorage.clear();
    sessionStorage.clear();
    setIsAuthenticated(false);
    setSelectedEvent(null);
    setShowMyTickets(false);
    setShowAdmin(false);
    setEvents([]);
    setShowProfile(false);
    clearCart();
  };
  const handleNavigate = (tab) => {
    setSelectedEvent(null);
    setShowCart(tab === 'cart');
    setShowAdmin(tab === 'admin');
    setShowMyTickets(tab === 'myTickets');
    setShowFavorites(tab === 'favorites');
    setShowProfile(tab === 'profile');
    
    if (tab === 'home') {
      loadEvents({}, 0);
    }
  };
  const handleSelectEventById = async (eventId) => {
    try {
      setLoading(true);
      let targetEvent = events.find((e) => e.id === eventId);
      if (!targetEvent) {
        const res = await apiClient.get("/api/events");
        if (Array.isArray(res.data)) {
          targetEvent = res.data.find((e) => e.id === eventId);
        }
      }
      if (targetEvent) {
        setSelectedEvent(targetEvent);
        setShowCart(false); 
      } else {
        toast.error(t('toasts.eventNotFound'));
      }
    } catch (err) {
      console.error("Etkinlik detayına gidilemedi:", err);
      toast.error(t('toasts.eventDetailError'));
    } finally {
      setLoading(false);
    }
  };
  const getCurrentTab = () => {
    if (showCart) return 'cart';
    if (showAdmin) return 'admin';
    if (showMyTickets) return 'myTickets';
    if (showFavorites) return 'favorites';
    if (showProfile) return 'profile';
    return 'home';
  };

  if (isNotFound) {
    return (
      <>
        <NotFound onGoHome={() => {       
          window.history.pushState(null, "", "/");
          setIsNotFound(false);
        }} />
        <ToastContainer position="top-right" autoClose={3000} theme="dark" />
      </>
    );
  }

  let content;
  if (!isAuthenticated) {
    content = <Auth onLoginSuccess={() => setIsAuthenticated(true)} />;
  } else if (showAdmin) {
    content = <AdminPanel onBack={() => setShowAdmin(false)} />;
  } else if (showProfile) {  
    content = <Profile onBack={() => setShowProfile(false)} />;
  } else if (showMyTickets) {
    content = <MyTickets onBack={() => setShowMyTickets(false)} />;
  } else if (showCart) {
    content = <CartPage onBack={() => setShowCart(false)} onSelectEvent={handleSelectEventById} />
  } else if (showFavorites) {
    content = <Favorites onEventSelect={(event) => {
      setSelectedEvent(event);
      setShowFavorites(false); 
    }} />;
  } else if (loading) {
    content = (
      <div className="d-flex flex-column justify-content-center align-items-center mt-5">        
        <div className="mb-4">
          <RotateLoader color="#38bdf8" size={15} margin={2} />
        </div>
        <p className="text-light fs-5 mt-3">{t('app.loading')}</p>
      </div>
    );
  } else if (error) {
    content = <div className="alert alert-danger mt-5">{error}</div>;
  } else if (selectedEvent) {
    content = <EventDetail event={selectedEvent} liveData={liveData} onBack={() => {setSelectedEvent(null); setLiveData(null);}} />;
  } else {
    content = (
      <>
        <EventList 
          events={events} 
          onEventSelect={(event) => setSelectedEvent(event)} 
          sortType={sortType} 
          onSortChange={(newSort) => setSortType(newSort)} 
        />
        {totalPages > 1 && (
          <div className="d-flex justify-content-center align-items-center mt-5 gap-3">
            <button 
              className="btn btn-sm btn-outline-secondary text-white" 
              disabled={page === 0}
              onClick={() => handlePageChange(page - 1)}
            >
              {t('pagination.prev')}
            </button>
            <span className="text-white fw-bold">
              {t('pagination.page', { current: page + 1, total: totalPages })}
            </span>
            <button 
              className="btn btn-sm btn-outline-secondary text-white" 
              disabled={page === totalPages - 1}
              onClick={() => handlePageChange(page + 1)}
            >
              {t('pagination.next')}
            </button>
          </div>
        )}
      </>
    );
  }

 return (
    <div 
      style={{ 
        width: "100%", 
        minHeight: "100vh", 
        backgroundColor: "#080d1a",
        backgroundImage: "radial-gradient(ellipse 80% 50% at 50% -20%, rgba(37, 99, 235, 0.25), transparent 70%)",
        color: "#f3f4f6",
        margin: 0, 
        padding: 0, 
        overflowX: "hidden" 
      }}
    >
      <div className="container py-4" style={{ maxWidth: "1240px", margin: "0 auto" }}>
        {/* NAVBAR */}
        <Navbar 
          isAuthenticated={isAuthenticated}
          userRole={userRole}
          cartCount={cartItems.length}
          currentTab={getCurrentTab()}
          onNavigate={handleNavigate}
          onLogout={handleLogout}
        />

        {/* SEARCH BAR (Yalnızca ana sayfadayken görünür) */}
        {isAuthenticated && !selectedEvent && getCurrentTab() === 'home' && (
          <SearchBar onSearch={(filters) => loadEvents(filters, 0)} />
        )}

        {/* DİNAMİK SAYFA İÇERİĞİ */}
        {content}

        {/* FOOTER */}
        <Footer />

      </div>
      <ToastContainer position="top-right" autoClose={3000} theme="dark" />
    </div>
  );
}

export default App;
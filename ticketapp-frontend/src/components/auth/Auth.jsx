import { useState } from "react";
import apiClient from "../../apiClient";
import { jwtDecode } from "jwt-decode";
import { toast } from 'react-toastify';
import { getErrorMessage } from "../../utils/errorHelper";
import { useTranslation } from 'react-i18next';
function Auth({ onLoginSuccess }) {
  const [isLogin, setIsLogin] = useState(true); 
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [email, setEmail] = useState(""); 
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);
  const { t, i18n } = useTranslation();
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    await new Promise(r => setTimeout(r, 2000));

   const endpoint = isLogin ? "/api/auth/login" : "/api/auth/register";
    
    const payload = isLogin 
      ? { username, password } 
      : { username, password, email, role: "USER" }; 

    try {
      const response = await apiClient.post(endpoint, payload);
      /* console.log("Backend Cevabı:", response); */
      if (isLogin) {        
        const token = response.data.token || response.data; 
       /* console.log("Gelen Token:", token);    */  
        if (!token) {
            throw new Error("Backend'den Token gelmedi!");
        }
        let userRole = "USER";
       try {
            const decoded = jwtDecode(token);
          /*  console.log("Çözülen Token:", decoded); */
            
            if (decoded.role) {
                userRole = decoded.role || "USER";
            }
            
            } catch (decodeError) {
            console.error("Token çözülemedi:", decodeError);
        }
        
        if (rememberMe) {           
            localStorage.setItem("token", token);
            localStorage.setItem("username", username);
            localStorage.setItem("role", userRole);
        } else {            
            sessionStorage.setItem("token", token);
            sessionStorage.setItem("username", username);
            sessionStorage.setItem("role", userRole);
        }
        toast.success(t('toasts.welcome', { username }));
        onLoginSuccess();
      } else {        
        toast.success(t('toasts.registerSuccess'));
        setIsLogin(true);   
        setLoading(false);     
      }
      if (!isLogin) setLoading(false);
    } catch (err) {
      
      console.error("LOGIN HATASI DETAYI:", err); 
      if (!err.response) {
          setError("Uygulama hatası: " + err.message);
      }else{
      const msg = getErrorMessage(err); 
      if (msg) {
        toast.error(`❌ ${msg}`);
      }
    }
      setLoading(false);
    }
  };

return (
    <div className="d-flex justify-content-center align-items-center" style={{ minHeight: "80vh" }}>
      <div className="card bg-dark text-white border-secondary p-4 shadow-lg" style={{ width: "400px" }}>        
     
        <div className="d-flex justify-content-end mb-3">
          <div className="btn-group shadow-sm">
            <button type="button" className={`btn btn-sm ${i18n.language === 'tr' ? 'btn-light fw-bold' : 'btn-outline-light'}`} onClick={() => {i18n.changeLanguage('tr'),localStorage.setItem('user-language', 'tr');}}>TR</button>
            <button type="button" className={`btn btn-sm ${i18n.language === 'en' ? 'btn-light fw-bold' : 'btn-outline-light'}`} onClick={() => {i18n.changeLanguage('en'),localStorage.setItem('user-language', 'en');}}>EN</button>
            <button type="button" className={`btn btn-sm ${i18n.language === 'de' ? 'btn-light fw-bold' : 'btn-outline-light'}`} onClick={() => {i18n.changeLanguage('de'),localStorage.setItem('user-language', 'de');}}>DE</button>
          </div>
        </div>

        <h2 className="text-center mb-4">{isLogin ? t('auth.loginTitle') : t('auth.registerTitle')}</h2>
        
        {error && <div className="alert alert-danger">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label>{t('auth.username')}</label>
            <input type="text" className="form-control bg-secondary text-white border-0" value={username} onChange={(e) => setUsername(e.target.value)} required />
          </div>

          {!isLogin && (
            <div className="mb-3">
              <label>Email</label>
              <input type="email" className="form-control bg-secondary text-white border-0" value={email} onChange={(e) => setEmail(e.target.value)} required />
            </div>
          )}

          <div className="mb-4">
            <label>{t('auth.password')}</label>
            <input type="password" className="form-control bg-secondary text-white border-0" value={password} onChange={(e) => setPassword(e.target.value)} required />
          </div>
          
          {isLogin && (
            <div className="mb-3 form-check">
                <input type="checkbox" className="form-check-input" id="rememberMe" checked={rememberMe} onChange={(e) => setRememberMe(e.target.checked)} />
                <label className="form-check-label text-secondary" htmlFor="rememberMe">
                    {t('auth.rememberMe')}
                </label>
            </div>
          )}

          <button type="submit" className="btn btn-primary w-100 py-2 fw-bold" disabled={loading} >
            {loading ? (
              <>
                <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                {t('app.loading')}
              </>
            ) : (
              isLogin ? t('auth.loginBtn') : t('auth.registerBtn')
            )}
          </button>
        </form>

        <div className="text-center mt-3">
          <small className="text-secondary">
            {isLogin ? t('auth.noAccount') + " " : t('auth.haveAccount') + " "}
            <span className="text-primary text-decoration-underline cursor-pointer" style={{ cursor: "pointer" }} onClick={() => { setIsLogin(!isLogin); setError(""); }}>
              {isLogin ? t('auth.registerBtn') : t('auth.loginBtn')}
            </span>
          </small>
        </div>

      </div>
    </div>
  );
}
export default Auth;
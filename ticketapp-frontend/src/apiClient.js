import axios from "axios";
import { toast } from 'react-toastify';
import i18n from './i18n';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
  headers: {
    "Content-Type": "application/json",
  },
});
let isRateLimitToastActive = false;
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token") || sessionStorage.getItem("token");
 
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);
apiClient.interceptors.response.use(
  (response) => {
    return response; 
  },
  (error) => {
if (error.response && error.response.status === 429) {
    if (isRateLimitToastActive) {
        return Promise.reject(error);
    }

    isRateLimitToastActive = true;
    toast.dismiss(); 

    let secondsLeft = error.response.data.retryAfter || 5;
    
    const toastId = toast.error(i18n.t('toasts.rateLimitWait', { seconds: secondsLeft }), {
        duration: 5000, 
        onClose: () => {  
            isRateLimitToastActive = false;
        }
    });

    const interval = setInterval(() => {
        secondsLeft -= 1;
        if (secondsLeft <= 0) {
            clearInterval(interval);
            toast.dismiss(toastId);
            isRateLimitToastActive = false; 
        } else {
            toast.update(toastId, {
                render: i18n.t('toasts.rateLimitSlowDown', { seconds: secondsLeft }),
                type: "error"
            });
        }
    }, 1000);
}
  
    return Promise.reject(error); 
  }
);

export default apiClient;
// Backendden gelen hatayı analiz edip okunabilir metin döndürür
import i18n from '../i18n';
export const getErrorMessage = (error) => {
  const resData = error.response?.data;

  if (!resData) {
    return i18n.t('toasts.serverUnreachable');
  }
    if (error.response.status === 429) {
    return null;
  }
  // Yetkilendirme / Giriş hatası
  if (error.response.status === 401) {
    return i18n.t('toasts.invalidCredentials');
  }

  // Validiation
  if (resData.details && Array.isArray(resData.details)) {
    return resData.details.map(err => err.message).join(" | ");
  }

  // Normal Hata Mesajı
  if (resData.message) {
    return resData.message;
  }


  // Hiçbiri değilse
  return i18n.t('toasts.unexpectedError');
};
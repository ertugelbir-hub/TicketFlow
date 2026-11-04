import apiClient from '../apiClient';
import { toast } from 'react-toastify';
import i18n from '../i18n';

export const downloadPdfTicket = async (ticketId) => {
  try {
    toast.info(i18n.t('toasts.pdfPreparing'));

    // PDF dosyasını binary (blob) olarak alıyoruz
    const response = await apiClient.get(`/api/tickets/${ticketId}/pdf`, {
      responseType: 'blob'
    });

    // Tarayıcı indirmesini tetikle
    const blob = new Blob([response.data], { type: 'application/pdf' });
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.setAttribute('download', `TicketFlow-Bilet-${ticketId}.pdf`);
    document.body.appendChild(link);
    link.click();
    link.parentNode.removeChild(link);
    window.URL.revokeObjectURL(downloadUrl);

    toast.success(i18n.t('toasts.downloadSuccess', { type: 'PDF' }));
  } catch (error) {
    console.error("PDF indirme hatası:", error);
    toast.error(i18n.t('toasts.downloadError'));
  }
};
import React, { useEffect, useState } from 'react';
import apiClient from '../../apiClient';
import UserHistoryModal from './UserHistoryModal';
import { useTranslation } from 'react-i18next';

function UserManager() {
  const [users, setUsers] = useState([]);
  const [selectedUser, setSelectedUser] = useState(null);
  const { t } = useTranslation();
  useEffect(() => {
    apiClient.get("/api/users")
      .then(res => setUsers(res.data))
      .catch(err => console.error(err));
  }, []);

 return (
    <div className="table-responsive">
      <h4 className="text-warning mb-3">👥 {t('admin.users.title', 'Kayıtlı Üyeler')}</h4>
      <table className="table table-dark table-hover border-secondary align-middle">
        <thead>
          <tr>
            <th>ID</th>
            <th>{t('auth.username', 'Kullanıcı Adı')}</th>
            <th>{t('admin.users.email', 'Email')}</th>
            <th>{t('admin.users.role', 'Rol')}</th>
            <th className="text-end">{t('admin.list.actions', 'İşlemler')}</th>
          </tr>
        </thead>
        <tbody>
          {users.map(u => (
            <tr key={u.id}>
              <td>#{u.id}</td>
              <td className="fw-bold">{u.username}</td>
              <td>{u.email}</td>
              <td>
                <span className={`badge ${u.role === 'ADMIN' ? 'bg-danger' : 'bg-primary'}`}>
                  {u.role}
                </span>
              </td>
              <td className="text-end">
                <button 
                  className="btn btn-sm btn-outline-info"
                  onClick={() => setSelectedUser(u.username)}>
                  📜 {t('admin.users.historyBtn', 'Geçmiş')}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>      
      {selectedUser && (
        <UserHistoryModal 
          username={selectedUser} 
          onClose={() => setSelectedUser(null)} 
        />
      )}
    </div>
  );
}

export default UserManager;
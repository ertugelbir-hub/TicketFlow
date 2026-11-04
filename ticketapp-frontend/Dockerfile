# 1. Node.js yüklü hafif bir Linux kullan
FROM node:20-alpine

# 2. Çalışma klasörünü ayarla
WORKDIR /app

# 3. Paket listesini kopyala ve yükle
COPY package.json .
RUN npm install --legacy-peer-deps

# 4. Kodların geri kalanını kopyala
COPY . .

# 5. Uygulama 5173 portundan çalışacak
EXPOSE 5173

# 6. Başlat (ÖNEMLİ: --host parametresi dışarıdan erişim için şart)
CMD ["npm", "run", "dev", "--", "--host"]
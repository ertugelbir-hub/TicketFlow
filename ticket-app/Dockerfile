# --- AŞAMA 1: DERLEME (BUILD) ---
# Maven yüklü bir Linux imajı kullanıyoruz
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Sadece pom.xml'i kopyalayıp bağımlılıkları indir (Cache avantajı)
COPY pom.xml .
# Kaynak kodları kopyala
COPY src ./src

# Projeyi Docker'ın içinde paketle (Testleri atla)
RUN mvn clean package -Dmaven.test.skip=true

# --- AŞAMA 2: ÇALIŞTIRMA (RUN) ---
# Sadece Java yüklü hafif bir Linux
FROM eclipse-temurin:21-jdk
WORKDIR /app

# İlk aşamada üretilen JAR dosyasını al
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
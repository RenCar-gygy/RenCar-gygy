# RenCarApp - Ekran Envanteri

> Tasarım checklist'indeki ekranların tek doğruluk kaynağı.
> API karşılığı, navigasyon grafiği ve ekip bölüşümü önerisi burada tutulur.

---

## Auth ve Onboarding Akışı

| Ekran | Paket (planlanan) | API karşılığı | Not |
|-------|-------------------|---------------|-----|
| Splash | `ui/splash/` | — | Oturum/token kontrolü; yönlendirme |
| Onboarding | `ui/onboarding/` | — | Yerel; ilk açılış |
| Giriş / Kayıt | `ui/auth/login/`, `ui/auth/register/` | `POST /auth/login`, `POST /auth/register` | JWT döner |
| OTP Doğrulama | `ui/auth/otp/` | `POST /auth/login`, `POST /auth/verify-otp` | Login akışında; `expiresAt` geri sayımı |
| Ehliyet Doğrulama | `ui/license/` | `POST /license/upload`, `GET /license/status` | Ön/arka yüz + selfie kamera; PENDING → CUSTOMER |

---

## Ana Akış (Main + Bottom Bar)

| Ekran | Paket (planlanan) | API karşılığı | Not |
|-------|-------------------|---------------|-----|
| Ana Harita | `ui/map/` | `GET /vehicles` (konum alanları) | MapLibre + OSM |
| Kiralama Geçmişi | `ui/rental/history/` | `GET /rentals`, `GET /rentals/stats` | `COMPLETED` kiralamalar; ödeme durumu rozeti; öğe tıklanınca özet |
| Cüzdan / Ödeme Yöntemleri | `ui/payment/wallet/` | `GET /wallet`, `POST /wallet/topup`, `GET/POST /cards`, `DELETE /cards/{id}`, `PATCH /cards/{id}/default` | Bakiye, yükleme, kayıtlı kartlar |
| Profil | `ui/profile/` | `GET /auth/me`, `POST /auth/logout`, `GET /rentals/stats` | Aylık istatistik; `ON_RESUME` ile yenileme |

---

## Kiralama Alt Grafiği (Nested — bottom bar gizli)

| Ekran | Paket (planlanan) | API karşılığı | Not |
|-------|-------------------|---------------|-----|
| Araç Detay | `ui/vehicle/detail/` | `GET /vehicles/{id}`, `GET /quote`, `POST /reservations` | Plan sekmeleri; müsait olmayan araçta rezervasyon engeli |
| Rezervasyon Onayı | `ui/rental/confirmation/` | `POST /rentals`, `DELETE /reservations` | Günlük planda `DatePicker` ile `endDate` |
| Başlangıç Fotoğrafları | `ui/rental/start_photos/` | `POST /rentals/{id}/photos`, `POST /start` | FileProvider JPEG kamera; 4 yön (Dk/Sa) |
| Aktif Kiralama | `ui/rental/active/` | `GET /rentals/active`, `POST /finish`, `DELETE` | Sayaç, yerel kilit simülasyonu, Socket.IO konum |
| Kiralama Özeti | `ui/rental/summary/` | `GET /rentals/{id}`, `POST /rentals/{id}/pay` | Fatura özeti; cüzdan/kart ödeme; opsiyonel indirim kodu |
| Araç Teslim Fotoğrafı | `ui/rental/delivery_photos/` | **Yok** | Ürün stub; yerel kamera önizleme; ana akış dışı |

---

## Navigasyon Grafiği (Özet)

```
Splash
  └─ auth/
       ├─ onboarding
       ├─ login ↔ register
       ├─ otp
       └─ license
  └─ main/  [BottomBar: Harita | Geçmiş | Cüzdan | Profil]
       ├─ map
       ├─ history
       ├─ wallet
       ├─ profile
       └─ rental/  [nested, bottom bar gizli]
            ├─ vehicle_detail/{vehicleId}
            ├─ confirmation/{vehicleId}
            ├─ start_photos/{rentalId}
            ├─ active/{rentalId}
            ├─ summary/{rentalId}
            └─ delivery_photos/{rentalId}
```

---

## Kullanıcı Rolü ve Akış

API'daki `UserResponseDto.role` değerlerine göre yönlendirme:

| Rol | Anlam | Mobil akış |
|-----|-------|------------|
| `PENDING` | Kayıt/giriş yapılmış; ehliyet onayı bekleniyor | Auth → License → (onay sonrası refresh token ile CUSTOMER) |
| `CUSTOMER` | Ehliyet onaylı müşteri | Main (Harita, kiralama vb.) |
| `ADMIN` | Yönetici | Mobil kapsam dışı |

---

## Ekip Bölüşümü

Detaylı gerekçe bu dosyanın sonundaki tabloda ve sprint planında tartışılır.
Checklist'teki ön atama korunmuştur; dengeleme önerileri için bkz. ekip mesajı / README.

| Kişi | Ekranlar | Repository / API |
|------|----------|------------------|
| **Furkan** | Splash, Onboarding, Login/Register, OTP, License, Ana Harita | `AuthRepository`, `LicenseRepository`, `VehicleRepository` (liste/konum) |
| **Nazlı** | Araç Detay, Rezervasyon Onayı, Başlangıç Fotoğrafları, Aktif Kiralama | `VehicleRepository` (detay), `RentalRepository` |
| **Çağla** | Özet, Cüzdan, Kiralama Geçmişi, Profil | `WalletRepository`, `RentalRepository` (liste/detay/pay/stats), `AuthRepository` (me/logout) |

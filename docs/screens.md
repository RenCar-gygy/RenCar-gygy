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
| OTP Doğrulama | `ui/auth/otp/` | **Yok** | Tasarım ekranı; stub veya onboarding adımı |
| Ehliyet Doğrulama | `ui/license/` | `POST /license/upload`, `GET /license/status` | PENDING → CUSTOMER |

---

## Ana Akış (Main + Bottom Bar)

| Ekran | Paket (planlanan) | API karşılığı | Not |
|-------|-------------------|---------------|-----|
| Ana Harita | `ui/map/` | `GET /vehicles` (konum alanları) | MapLibre + OSM |
| Kiralama Geçmişi | `ui/rental/history/` | `GET /rentals` | Alt çubuk sekmesi |
| Cüzdan / Ödeme Yöntemleri | `ui/payment/wallet/` | **Yok** | Stub |
| Profil | `ui/profile/` | `GET /auth/me`, `POST /auth/logout` | Alt çubuk sekmesi |

---

## Kiralama Alt Grafiği (Nested — bottom bar gizli)

| Ekran | Paket (planlanan) | API karşılığı | Not |
|-------|-------------------|---------------|-----|
| Araç Detay | `ui/vehicle/detail/` | `GET /vehicles/{id}` | Haritadan veya listeden |
| Rezervasyon Onayı | `ui/rental/confirmation/` | `POST /rentals` hazırlığı | `vehicleId`, `endDate` |
| Ödeme / Kiralama Özeti | `ui/rental/summary/` | `RentalResponseDto.totalPrice` | Ödeme UI stub; fiyat API'dan |
| Araç Teslim Fotoğrafı | `ui/rental/delivery_photos/` | **Yok** | 4 yön; stub |
| Aktif Kiralama | `ui/rental/active/` | `GET /rentals/{id}`, `POST /rentals/{id}/return` | Aktif kiralama yönetimi |

---

## Navigasyon Grafiği (Özet)

```
Splash
  └─ auth/
       ├─ onboarding
       ├─ login ↔ register
       ├─ otp (opsiyonel / stub)
       └─ license
  └─ main/  [BottomBar: Harita | Geçmiş | Cüzdan | Profil]
       ├─ map
       ├─ history
       ├─ wallet
       ├─ profile
       └─ rental/  [nested, bottom bar gizli]
            ├─ vehicle_detail/{vehicleId}
            ├─ confirmation/{vehicleId}
            ├─ summary/{vehicleId}
            ├─ delivery_photos/{vehicleId}
            └─ active/{rentalId}
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
| **Nazlı** | Araç Detay, Rezervasyon Onayı, Teslim Fotoğrafı, Aktif Kiralama | `VehicleRepository` (detay), `RentalRepository` |
| **Çağla** | Ödeme/Özet, Cüzdan, Kiralama Geçmişi, Profil | `RentalRepository` (liste), `AuthRepository` (me/logout); ödeme stub |

# RenCarApp — Nested Navigation ve Ekip Ekran Bölüşümü

**Proje:** RenCarApp (Araç Kiralama)  
**Doküman sürümü:** 1.0  
**Tarih:** 2 Temmuz 2026  
**Kapsam:** Sprint 0 navigasyon iskeleti, grafik yapısı, ekip bölüşümü eşlemesi, Sprint 1 çalışma kuralları

---

## 1. Amaç

Bu doküman, RenCarApp projesinde kullanılan **iç içe (nested) Compose Navigation** yapısını ve **3 kişilik ekip ekran bölüşümünü** nasıl eşleştirdiğimizi açıklar. Sprint 0'da kodlanan iskelet; Sprint 1'de her ekip üyesinin hangi paket ve NavHost satırına dokunacağını netleştirir.

**Teknik referans dosyalar:**

| Dosya | Rol |
|-------|-----|
| `ui/navigation/RenCarNavHost.kt` | NavHost + nested graph tanımları |
| `ui/navigation/RenCarDestination.kt` | Route sabitleri (tek doğruluk kaynağı) |
| `ui/navigation/RenCarBottomBar.kt` | Alt çubuk sekmeleri |
| `docs/screens.md` | Ekran envanteri ve ekip tablosu |

---

## 2. Genel Yapı: 3 İç İçe Grafik + Splash

```
Kök NavHost (RenCarNavHost)
│
├── splash                          → Furkan
│
├── auth/  (nested graph 1)         → Furkan
│   ├── onboarding
│   ├── login ↔ register
│   ├── otp
│   └── license
│
└── main/  (nested graph 2)
    ├── map                           → Furkan
    ├── history                       → Çağla
    ├── wallet                        → Çağla
    ├── profile                       → Çağla
    │
    └── rental/  (nested graph 3)     → Kiralama hattı
        ├── vehicle_detail/{id}       → Nazlı
        ├── confirmation/{id}         → Nazlı
        ├── summary/{id}              → Çağla
        ├── delivery_photos/{id}      → Nazlı
        └── active/{rentalId}         → Nazlı
```

**Özet:** NavHost, ekip bölüşümüne göre üç ana grafiğe ayrılmıştır — Auth (Furkan), Main sekmeler (Furkan + Çağla), Rental akışı (Nazlı + Çağla summary).

---

## 3. Ekip Bölüşümü ile Navigasyon Eşlemesi

| Grafik | Route prefix | Sorumlu | Ekranlar |
|--------|--------------|---------|----------|
| Kök | `splash` | **Furkan** | Splash |
| `auth/` | `auth/*` | **Furkan** | Onboarding, Login, Register, OTP, License |
| `main/` | `main/map` | **Furkan** | Ana Harita |
| `main/` | `main/history`, `main/wallet`, `main/profile` | **Çağla** | Kiralama Geçmişi, Cüzdan, Profil |
| `rental/` | `rental/vehicle`, `confirmation`, `delivery_photos`, `active` | **Nazlı** | Araç Detay, Rezervasyon Onayı, Teslim Fotoğrafı, Aktif Kiralama |
| `rental/` | `rental/summary` | **Çağla** | Ödeme / Kiralama Özeti |

### 3.1 Önemli not: Summary ekranı

`summary` ekranı **Çağla'ya** atanmıştır (`ui/rental/summary/`) ancak navigasyon grafiğinde **rental alt grafiğinde** yer alır. Bunun nedeni kullanıcı akışıdır: Onay → Özet → Teslim fotoğrafı → Aktif kiralama. Sahiplik **paket** bazında; navigasyon **akış** bazındadır.

### 3.2 Repository eşlemesi (Sprint 1 veri kaynağı)

| Kişi | Repository / API |
|------|------------------|
| **Furkan** | `AuthRepository`, `LicenseRepository`, `VehicleRepository` (liste/konum) |
| **Nazlı** | `VehicleRepository` (detay), `RentalRepository` |
| **Çağla** | `RentalRepository` (liste), `AuthRepository` (me/logout); ödeme stub |

Sprint 0–1 aşamasında tüm repository'ler **Fake*Repository** stub implementasyonları kullanır.

---

## 4. Nested Navigation Teknik Detay

Compose Navigation'da `navigation { }` bloğu bir **alt grafik** tanımlar. Projede üç kez kullanılmıştır.

### 4.1 Auth grafiği (`auth/`)

- **Başlangıç hedefi:** `auth/onboarding`
- **Amaç:** Kayıt, giriş, ehliyet akışını tek grafikte toplamak
- **Avantaj:** Auth tamamlanınca `popUpTo(auth) { inclusive = true }` ile tüm auth stack temizlenip `main`'e geçilebilir; geri tuşu Login'e dönmez

### 4.2 Main grafiği (`main/`)

- **Başlangıç hedefi:** `main/map`
- **Amaç:** Alt çubuklu ana uygulama sekmeleri
- **Sekmeler:** Harita, Geçmiş, Cüzdan, Profil
- **Sekme geçişi:** `popUpTo(Map) { saveState = true }` + `launchSingleTop` + `restoreState`

### 4.3 Rental alt grafiği (`rental/` — main içinde nested)

- **Başlangıç hedefi:** `rental/vehicle/{vehicleId}`
- **Amaç:** Haritadan başlayan full-screen kiralama hattı
- **Avantaj:** Bottom bar gizlenir; geri tuşu rental adımları arasında çalışır, çıkınca haritaya döner

---

## 5. Bottom Bar (Alt Gezinme Çubuğu) Mantığı

Alt çubuk yalnızca **ana sekme rotalarında** görünür:

| Route | Bottom bar |
|-------|------------|
| `main/map` | Görünür |
| `main/history` | Görünür |
| `main/wallet` | Görünür |
| `main/profile` | Görünür |
| `auth/*` | Gizli |
| `rental/*` | Gizli |
| `splash` | Gizli |

**Teknik uygulama:** `RenCarDestination.bottomBarRoutes` kümesi ile `currentRoute` karşılaştırılır; eşleşirse `RenCarBottomBar` render edilir.

---

## 6. Route Sabitleri ve Parametreler

Tüm route string'leri `RenCarDestination.kt` içinde tanımlıdır. Sprint 1'de bu dosyaya **yeni route eklenmemeli**; mevcut sözleşmeye uyulmalıdır.

### 6.1 Auth rotaları

| Sabit | Route |
|-------|-------|
| `Onboarding` | `auth/onboarding` |
| `Login` | `auth/login` |
| `Register` | `auth/register` |
| `Otp` | `auth/otp` |
| `License` | `auth/license` |

### 6.2 Main rotaları

| Sabit | Route |
|-------|-------|
| `Map` | `main/map` |
| `RentalHistory` | `main/history` |
| `Wallet` | `main/wallet` |
| `Profile` | `main/profile` |

### 6.3 Rental rotaları (parametreli)

| Sabit | Route | Parametre |
|-------|-------|-----------|
| `VehicleDetail` | `rental/vehicle/{vehicleId}` | `vehicleId` |
| `RentalConfirmation` | `rental/confirmation/{vehicleId}` | `vehicleId` |
| `RentalSummary` | `rental/summary/{vehicleId}` | `vehicleId` |
| `DeliveryPhotos` | `rental/delivery_photos/{vehicleId}` | `vehicleId` |
| `ActiveRental` | `rental/active/{rentalId}` | `rentalId` |

### 6.4 Route helper fonksiyonları

Navigasyon çağrılarında ham string yazmak yerine helper kullanılır:

- `RenCarDestination.vehicleDetailRoute(vehicleId)`
- `RenCarDestination.rentalConfirmationRoute(vehicleId)`
- `RenCarDestination.rentalSummaryRoute(vehicleId)`
- `RenCarDestination.deliveryPhotosRoute(vehicleId)`
- `RenCarDestination.activeRentalRoute(rentalId)`

---

## 7. MVI ile Navigasyon Entegrasyonu

ViewModel **navigasyon API'si bilmez**. Akış:

```
Kullanıcı etkileşimi
        → Intent
        → ViewModel
        → Effect (örn. NavigateToMap)
        → Route (LaunchedEffect ile Effect tüketimi)
        → NavHost'tan gelen lambda (örn. onNavigateToMap)
        → navController.navigate(...)
```

**Yasak:** ViewModel içinde `NavController`, `Context` veya Compose navigasyon API'si.

**Örnek (Login → Main):**

1. `LoginViewModel` → `_effect.send(LoginEffect.NavigateToMap)`
2. `LoginRoute` → `onNavigateToMap()` çağrısı
3. `RenCarNavHost` → `navController.navigate(MainGraph) { popUpTo(AuthGraph) { inclusive = true } }`

---

## 8. Sprint 1 Çalışma Kuralları

### 8.1 Kim hangi dosyayı yazar?

| Kişi | Paket | NavHost'ta değiştireceği placeholder |
|------|-------|--------------------------------------|
| **Furkan** | `ui/splash/`, `ui/onboarding/`, `ui/auth/*`, `ui/license/`, `ui/map/` | Splash, auth/*, Map |
| **Nazlı** | `ui/vehicle/detail/`, `ui/rental/confirmation/`, `delivery_photos/`, `active/` | rental/ altı 4 ekran (summary hariç) |
| **Çağla** | `ui/rental/history/`, `ui/payment/wallet/`, `ui/profile/`, `ui/rental/summary/` | history, wallet, profile, rental/summary |

### 8.2 Her ekran paketi (MVI zorunlu yapı)

```
ui/<feature>/<screen>/
├── <Screen>Contract.kt   // UiState + Intent + Effect
├── <Screen>ViewModel.kt  // @HiltViewModel, Fake repository
└── <Screen>Screen.kt     // Route (stateful) + Screen (stateless)
```

**Referans ekran:** İlk tamamlanan Login ekranı (`ui/auth/login/`) ekip standardı olacaktır.

### 8.3 NavHost merge kuralları

1. Herkes **yalnızca kendi `composable { }` bloğunu** değiştirir
2. `RenCarDestination.kt` route string'lerine **dokunulmaz** (ortak sözleşme)
3. Yeni route gerekirse ekip onayı + `docs/screens.md` güncellemesi zorunlu
4. NavHost'a navigasyon lambda'ları eklerken ilgili `*Route` parametreleri kullanılır

### 8.4 Kiralama akışı sırası (hedef)

```
Harita (Furkan)
  → Araç Detay (Nazlı)
  → Rezervasyon Onayı (Nazlı)
  → Ödeme / Özet (Çağla)
  → Teslim Fotoğrafı (Nazlı)
  → Aktif Kiralama (Nazlı)
```

Bu zincir Sprint 2 entegrasyonunda Effect → lambda ile bağlanacaktır.

---

## 9. Kullanıcı Rolü ve Yönlendirme (API)

| Rol | Anlam | Mobil akış |
|-----|-------|------------|
| `PENDING` | Ehliyet onayı bekleniyor | Auth → License |
| `CUSTOMER` | Ehliyet onaylı müşteri | Main (Harita, kiralama vb.) |
| `ADMIN` | Yönetici | Mobil kapsam dışı |

Login sonrası rol kontrolü ViewModel'de yapılır; yönlendirme Effect ile Route'a iletilir.

---

## 10. Sprint Durumu

| Sprint | Durum | İçerik |
|--------|-------|--------|
| **Sprint 0** | Tamamlandı | Hilt, nested NavHost, Fake repo, NetworkModule iskeleti, placeholder ekranlar |
| **Sprint 1** | Planlanıyor | MVI ekran implementasyonu (Fake repository ile) |
| **Sprint 2** | Planlanıyor | Akış entegrasyonu (Splash yönlendirme, rental zinciri) |
| **Sprint 3** | Planlanıyor | Gerçek API entegrasyonu (`Default*Repository`) |

---

## 11. Sık Sorulan Sorular

**S: Nested navigation zorunlu mu?**  
C: Evet; `docs/decisions.md` kararı gereği Auth, Main ve Rental ayrı grafiklerde yönetilir.

**S: OTP ekranı API'da var mı?**  
C: Hayır. Stub veya onboarding adımı olarak geliştirilir; davranış uydurulmaz.

**S: Ödeme / Cüzdan API'da var mı?**  
C: Hayır. UI stub; kiralama fiyatı `RentalResponseDto.totalPrice` ile API'dan gelir.

**S: Bottom bar rental'da neden gizli?**  
C: Kiralama hattı full-screen akış; alt çubuk kullanıcı deneyimini bozar.

---

## 12. İlgili Bağlantılar

- GitHub org: https://github.com/RenCar-gygy
- Repository: https://github.com/RenCar-gygy/RenCar-gygy
- API dokümantasyonu: https://rencar.halitkalayci.com/api/docs
- MapLibre: https://maplibre.org/

---

*Bu doküman RenCarApp ekip içi kullanım içindir. Navigasyon veya ekip bölüşümünde değişiklik yapılırsa bu dosya ve `docs/screens.md` birlikte güncellenmelidir.*

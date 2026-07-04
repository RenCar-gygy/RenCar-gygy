# decisions.md

> Projede verilen bütün mimarisel-teknik kararları ve karar geçmişini içeren dökümantasyondur.

**Sprint 0 (02.07.2026):** Hilt/KSP, nested NavHost iskeleti, NetworkModule, repository interface + Fake stub'lar tamamlandı.

---

### Dependency Injection Kütüphanesi

- Seçim: **Hilt**
- Son Güncelleme Tarihi: 02.07.2026
- Alternatifler: **Koin**
- Sebep: Android ekosisteminde resmi destek, LyraApp ile tutarlılık, test edilebilirlik.

---

### Navigasyon

- Seçim: **Compose Navigation** — **iç içe (nested) grafikler**
- Son Güncelleme Tarihi: 02.07.2026
- Bağımlılık: `androidx.navigation:navigation-compose` **2.9.5**
- Uygulama: `ui/navigation/RenCarNavHost.kt`, `RenCarDestination.kt`, `RenCarBottomBar.kt`
- Grafik yapısı:
  1. **Kök grafik**: Splash → Auth veya Main
  2. **Auth grafiği** (`auth`): Onboarding, Login, Register, OTP, License
  3. **Main grafiği** (`main`): Alt çubuklu sekmeler (Harita, Geçmiş, Cüzdan, Profil)
  4. **Rental alt grafiği** (`rental`, Main içinde nested): Araç detay → Onay → Özet → Teslim foto → Aktif kiralama
- MVI uyumu: ViewModel'de navigasyon API'si yoktur; navigasyon `Intent → Effect → Route lambda` ile akar.

---

### Sunum Katmanı Mimarisi

- Seçim: **MVI (Model-View-Intent)**
- Son Güncelleme Tarihi: 02.07.2026
- Kapsam: Her ekran State + Intent + Effect sözleşmesiyle yazılır.
- Sebep: Tek yönlü veri akışı, durumsuz UI, test edilebilirlik.

---

### Hilt Annotation Processing

- Seçim: **KSP** (kapt değil)
- Son Güncelleme Tarihi: 02.07.2026
- Planlanan sürümler: Hilt **2.59.2**, KSP **2.3.2**, Kotlin **2.2.10** — **uygulandı (Sprint 0)**
- Sebep: KSP, kapt'a göre hızlıdır ve Kotlin 2.2 ile uyumludur.

---

### AGP 9 Built-in Kotlin + KSP Uyumu

- Karar: `gradle.properties` içinde **`android.disallowKotlinSourceSets=false`** zorunlu olacak.
- Son Güncelleme Tarihi: 02.07.2026
- Sebep: AGP 9 built-in Kotlin + KSP birlikte çalışması için gerekli.

---

### Alt Gezinme Çubuğu (Bottom Navigation Bar)

- Seçim: **Material 3 `NavigationBar`** — Main grafiğinde tek dış `Scaffold`
- Son Güncelleme Tarihi: 02.07.2026
- Sekmeler: **Harita**, **Geçmiş**, **Cüzdan**, **Profil**
- Görünürlük: Yalnızca Main grafiğinin üst düzey sekme rotalarında; Auth ve Rental alt grafiğinde gizli.
- MVI kapsamı: BNB navigasyon iskeletidir; State/Intent/Effect sözleşmesi yoktur.

---

### Harita

- Seçim: **MapLibre Android SDK** + **OpenStreetMap** tile kaynağı
- Son Güncelleme Tarihi: 02.07.2026
- Dokümantasyon: https://maplibre.org/
- Veri kaynağı: `VehicleResponseDto.latitude/longitude` (`GET /vehicles`)
- Not: Bağımlılık harita ekranı implementasyonu başladığında eklenecek.

---

### Backend API

- Base URL: **`https://rencar.halitkalayci.com/`**
- OpenAPI: **`/api/docs`** (JSON: `/api/docs-json`)
- Son Güncelleme Tarihi: 02.07.2026
- Kimlik doğrulama: JWT Bearer (`accessToken` + `refreshToken` rotation)
- Müşteri uçları: Auth, License, Vehicles (AVAILABLE), Rentals
- Admin uçları mobil kapsam dışıdır

---

### Backend Henüz Entegre Değilken Veri Katmanı

- Karar: **Stub repository** deseni — Repository interface + `Fake<X>Repository`
- Son Güncelleme Tarihi: 02.07.2026
- Sebep: Ekranlar paralel gelişirken API entegrasyonu aşamalı yapılır.

---

### Ağ Katmanı (Networking)

- Seçim: **Retrofit + OkHttp + kotlinx.serialization**
- Son Güncelleme Tarihi: 02.07.2026
- Planlanan sürümler: Retrofit **2.11.0**, OkHttp **4.12.0**, kotlinx-serialization-json **1.8.1**
- Uygulama: `data/network/NetworkModule.kt`, base URL yukarıdaki adrestir. API arayüzleri `data/network/api/` altında.

---

### Tasarımda Olup API'da Olmayan Ekranlar

- Karar: Aşağıdaki ekranlar **UI-only / stub** olarak geliştirilir; davranış uydurulmaz.
- Son Güncelleme Tarihi: 02.07.2026
- Ekranlar:
  - **OTP Doğrulama** — API e-posta/parola + JWT kullanır
  - **Ödeme / Cüzdan** — API'da ödeme uçları yok; fiyat `RentalResponseDto.totalPrice` ile sunucuda hesaplanır
  - **Araç Teslim Fotoğrafı (4 yön)** — API'da karşılık yok

---

### Referans Ekran

- Karar: İlk tamamlanan **Login** ekranı (`ui/auth/login/`) referans implementasyon olacaktır.
- Son Güncelleme Tarihi: 02.07.2026

---

### Sprint 2 — Oturum ve Auth Repository (Batch 1)

- Karar: **`SessionStore`** (DataStore Preferences) ile JWT ve onboarding tamamlandı bayrağı kalıcı saklanır.
- Son Güncelleme Tarihi: 03.07.2026
- Uygulama: `data/session/SessionStore.kt`, `di/SessionModule.kt`
- Bağımlılık: `androidx.datastore:datastore-preferences` **1.1.7**

---

### Auth Repository — Telefon + OTP Sözleşmesi

- Karar: `AuthRepository` OpenAPI ile hizalanır: `requestOtp`, `verifyOtp`, `register`, `logout`, `getCurrentUser`.
- Son Güncelleme Tarihi: 03.07.2026
- Fake OTP kodu: **`123456`** (backend simülasyonu ile uyumlu)
- Telefon normalizasyonu: UI'daki 10 haneli `5xxxxxxxxx` → API formatı `+905xxxxxxxxx`
- Register UI geçici uyumu (Sprint 2): Telefon-only + sentetik alanlar — **Sprint 3 Batch 5 ile kaldırıldı** (bkz. aşağı).
- Eski `login(email, password)` imzası kaldırıldı; ViewModel entegrasyonu Batch 3'te yapıldı.

---

### Sprint 2 — Splash Otomatik Yönlendirme (Batch 2)

- Karar: Splash açılışında `AuthRepository.getCurrentUser()` ve `SessionStore.isOnboardingCompleted()` ile otomatik yönlendirme yapılır.
- Son Güncelleme Tarihi: 03.07.2026
- Yönlendirme kuralları:
  - Oturum + `CUSTOMER` → Main (Harita)
  - Oturum + `PENDING` → License
  - Oturum yok + onboarding tamamlandı → Login
  - Oturum yok + onboarding görülmedi → Splash UI (Hemen Başla / Giriş yap)
- `Giriş yap` tıklanınca onboarding tamamlandı bayrağı set edilir; onboarding bitişi Batch 3'te `OnboardingViewModel`'e eklenecektir.

---

### Sprint 2 — Auth ViewModel Entegrasyonu (Batch 3)

- Karar: Login, Register ve OTP ViewModel'leri `AuthRepository` ile bağlanır; rol bazlı yönlendirme OTP doğrulaması sonrası yapılır.
- Son Güncelleme Tarihi: 03.07.2026
- Login: `requestOtp` → OTP ekranı
- Register: sentetik alanlarla `register` → OTP ekranı
- OTP: `verifyOtp` → `PENDING` ise License, `CUSTOMER` ise Main
- Fake OTP kodu: **123456**
- Onboarding tamamlandı bayrağı `OnboardingViewModel.FinishClicked` ile set edilir
- Fake kayıtlı kullanıcılar `SessionStore` içinde kalıcı tutulur; logout oturumu temizler, telefon kaydını silmez

---

### Sprint 2 — License ve Harita Entegrasyonu (Batch 4)

- Karar: `LicenseViewModel` ve `MapViewModel` fake repository'ler ile bağlanır.
- Son Güncelleme Tarihi: 03.07.2026
- License: stub fotoğraflarla `upload`; fake repo anında `APPROVED` döner; Devam Et kullanıcıyı `CUSTOMER` yapar
- Harita: `VehicleRepository.listAvailable()` ile pin listesi; fiyat etiketi `pricePerDay` üzerinden türetilir
- Sprint 3'te gerçek fotoğraf seçimi ve MapLibre koordinat eşlemesi eklenecektir

---

### Onboarding 2–3. Sayfa Tasarımı

- Karar: Splash (sayfa 1) ile görsel dil uyumlu, uygulama akışını anlatan 2 onboarding sayfası eklendi.
- Son Güncelleme Tarihi: 03.07.2026
- Sayfa 2: Harita keşfi — mini harita illüstrasyonu, fiyat pinleri, arama çubuğu
- Sayfa 3: Hızlı kiralama — 3 adımlı kart (Kayıt ol → Doğrula → Yola çık)
- Metinler API/stub davranışı uydurmaz; yalnızca kullanıcı yönlendirmesi amaçlıdır

---

### Sprint 3 — Gerçek API Auth (Batch 1)

- Karar: `DefaultAuthRepository` prod binding olarak `AuthRepository` yerine geçer; `FakeAuthRepository` test/geliştirme için korunur.
- Son Güncelleme Tarihi: 04.07.2026
- Uçlar: `POST /auth/login` (OTP gönder), `POST /auth/verify-otp`, `POST /auth/register`, `POST /auth/logout`, `GET /auth/me`
- DTO'lar OpenAPI ile hizalandı (`LoginDto.phone`, `VerifyOtpDto`, `OtpRequiredResponseDto`)
- Oturum `SessionStore` üzerinden kalıcı; logout/me Bearer token ile çağrılır

---

### Sprint 3 — Gerçek API License (Batch 2)

- Karar: `DefaultLicenseRepository` prod binding olarak `LicenseRepository` yerine geçer; `FakeLicenseRepository` test/geliştirme için korunur.
- Son Güncelleme Tarihi: 04.07.2026
- Uçlar: `GET /license/status`, `POST /license/upload` (multipart: `front`, `back`; Bearer token)
- Stub yükleme: UI henüz gerçek fotoğraf seçmiyor; repository tek bayt stub'ı geçerli 1x1 PNG'ye çevirir
- `UNDER_REVIEW` sonrası ana akışa geçiş yok; `APPROVED` + `/auth/me` ile `CUSTOMER` doğrulandıktan sonra Devam Et çalışır
- Swagger test ADMIN hesabı (OpenAPI örnekleri): telefon `+905550000000`, OTP `123456`, e-posta `admin@rencar.com`

---

### Sprint 3 — Gerçek API Vehicles (Batch 3)

- Karar: `DefaultVehicleRepository` prod binding olarak `VehicleRepository` yerine geçer; `FakeVehicleRepository` test/geliştirme için korunur.
- Son Güncelleme Tarihi: 04.07.2026
- Uçlar: `GET /vehicles` (type/page/limit), `GET /vehicles/{id}` — Bearer token, **CUSTOMER** rolü gerekir
- Harita: `MapViewModel` mevcut `listAvailable()` akışını kullanır; koordinatlar API'dan gelir (MapLibre eşlemesi Batch 4)

---

### Sprint 3 — MapLibre + OSM (Batch 4)

- Karar: Ana harita stub illüstrasyonu yerine MapLibre Android SDK (`11.8.0`) + OpenStreetMap raster tile kullanılır.
- Son Güncelleme Tarihi: 04.07.2026
- `MapVehiclePin` artık `latitude/longitude` taşır; API `VehicleResponseDto` koordinatları haritada GeoJSON pin olarak gösterilir
- OSM tile istekleri `User-Agent: RenCarApp/1.0` ile yapılır
- Konum izni / kullanıcı konumu odaklama sonraki iterasyonda ele alınacaktır

---

### Sprint 3 — Kayıt Formu (Batch 5)

- Karar: Stub kayıt (`{phone}@rencar.local`, sabit parola/ad) kaldırıldı; `RegisterScreen` OpenAPI `RegisterDto` alanlarını toplar.
- Son Güncelleme Tarihi: 04.07.2026
- Alanlar: `fullName`, `email`, `password` (min 6), `phone` (10 hane, `5` ile başlar)
- Akış: Kayıt Ol → `POST /auth/register` → OTP ekranı (telefon doğrulama)
- UI: Login/Register ile aynı görsel dil; kaydırılabilir form

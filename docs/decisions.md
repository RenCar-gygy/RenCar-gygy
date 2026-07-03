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
- Register UI geçici uyumu: Telefon-only kayıt ekranı Sprint 2'de fake register için sentetik alanlar kullanır (`{phone}@rencar.local`, parola `123456`, ad `Kullanıcı`); tam form tasarım gelene kadar geçerlidir.
- Eski `login(email, password)` imzası kaldırıldı; ViewModel entegrasyonu Batch 3'te yapılacaktır.

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

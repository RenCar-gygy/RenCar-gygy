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

- Base URL: **`https://rencarv2.halitkalayci.com/`** (v2 — eğitmen yayını, 12.07.2026)
- OpenAPI UI: **`/api/docs`**
- OpenAPI JSON: **`/api/openapi.json`** (yerel kopya: `docs/api/openapi-v2.json`)
- Eski v1 (`https://rencar.halitkalayci.com/`) kullanımdan kalkıyor; yeni geliştirme v2 üzerinden yapılır.
- Son Güncelleme Tarihi: 13.07.2026
- Kimlik doğrulama: JWT Bearer (`accessToken` + `refreshToken` rotation)
- Müşteri uçları (v2): Auth, License, Vehicles, Reservations, Rentals, Cards, Wallet
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
- Harita: `VehicleRepository.listAvailable()` ile pin listesi; fiyat etiketi `VehiclePriceFormatter` ile API alanlarından (`pricePerHour` pin, plan kartları `pricePerMinute` / `pricePerHour` / `pricePerDay`)
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

### Sprint 4 — Harita Konum Entegrasyonu (auth-map-api Batch 2)

- Karar: Eğitmen referansı (`HomeScreen`, `RencarMap`, `MapStyle`) mevcut `MapScreen`/`MapLibreMapView` yapısına entegre edildi; ayrı `HomeScreen` oluşturulmadı.
- Son Güncelleme Tarihi: 08.07.2026
- `play-services-location` ile FusedLocationProvider; izin isteği ve 5 sn aralıklı konum güncellemesi
- Kullanıcı konumu MapLibre `GeoJsonSource` + `CircleLayer` (mavi nokta); araç pinleri Compose overlay olarak korundu
- OSM tile URL'leri `a/b/c.tile.openstreetmap.org` olarak güncellendi
- Konum FAB: izin yoksa istek; varsa kamerayı kullanıcı konumuna odaklar
- Debug emülatör: Google varsayılan konumu (Mountain View) algılanırsa Üsküdar test koordinatına normalize edilir; FAB tıklanınca `getCurrentLocation` ile taze okuma yapılır

---

### Sprint 4 — Harita Konum Davranışı (auth-map-api Batch 4)

- Karar: Eğitmen `RencarMap`/`HomeScreen` notları `MapLibreMapView` ve `MapLocationTracker` üzerine uyarlandı.
- Son Güncelleme Tarihi: 09.07.2026
- İlk açılışta yalnızca bir kez kullanıcı konumuna zoom (`LaunchedEffect` + `moveCamera`); logcat'e koordinat/zoom yazılır
- Konum güncellemelerinde yalnızca mavi nokta (marker) güncellenir; otomatik kamera zoom'u yapılmaz
- Konum FAB (`shouldFocusMyLocation`) ile manuel zoom korunur
- Konum izni reddedilirse harita kullanılamaz; `MapPermissionDeniedScreen` gösterilir
- `Priority.PRIORITY_HIGH_ACCURACY`; güncelleme aralığı 1–2 sn (`setWaitForAccurateLocation(false)`)

---

### Sprint 4 — Harita Bölge Etiketi (auth-map-api Batch 3)

- Karar: Alt paneldeki `areaLabel` sabit metin yerine kullanıcı konumuna göre dinamik üretilir.
- Son Güncelleme Tarihi: 08.07.2026
- Android `Geocoder` (tr-TR) ile ilçe adı çözülür: öncelik `subAdminArea` (ilçe), ardından şehirden farklı `locality`; mahalle (`subLocality`) ve sokak alanları kullanılmaz
- En yakın müsait araç pinine haversine mesafesi yürüme hızı (~5 km/s) ile dakikaya çevrilir; format: `{bölge} çevresinde · {dk} dk uzaklıkta`
- `MapAreaLabelResolver` Hilt ile enjekte edilir; konum veya araç listesi güncellenince `MapViewModel` etiketi yeniler

---

### Sprint 3 — Kayıt Formu (Batch 5)

- Karar: Stub kayıt (`{phone}@rencar.local`, sabit parola/ad) kaldırıldı; `RegisterScreen` OpenAPI `RegisterDto` alanlarını toplar.
- Son Güncelleme Tarihi: 04.07.2026
- Alanlar: `fullName`, `email`, `password` (min 6), `phone` (10 hane, `5` ile başlar)
- Akış: Kayıt Ol → `POST /auth/register` → OTP ekranı (telefon doğrulama)
- UI: Login/Register ile aynı görsel dil; kaydırılabilir form

---

### Sprint 4 — Oturum Yenileme (auth-map-api Batch 1)

- Karar: Access token süresi dolduğunda `POST /auth/refresh` ile otomatik yenileme yapılır; refresh başarısızsa oturum temizlenir.
- Son Güncelleme Tarihi: 08.07.2026
- `AuthorizedRequestExecutor` tüm bearer gerektiren repository çağrılarında 401 sonrası tek retry uygular
- `getCurrentUser()` artık `/auth/me` hata verdiğinde bayat cache kullanmaz; refresh dener
- Refresh başarısızsa kullanıcı login ekranına yönlendirilir (Splash `onFailure` akışı)

---

### Sprint 4 — Harita Araç Tipi Filtresi

- Karar: Filtre paneline OpenAPI `VehicleType` alt kırılımı eklendi (SEDAN, HATCHBACK, SUV, STATION, MINIVAN); çoklu seçim istemci tarafında uygulanır.
- Son Güncelleme Tarihi: 09.07.2026
- Mevcut kategori çipleri (Tümü / Ekonomik / Konfor / SUV) ve “yalnızca müsait” switch'i korunur; tip filtresi bunların üzerine ek istemci filtresi olarak çalışır.
- API `GET /vehicles` tek `type` query parametresi desteklediği için çoklu tip seçiminde önce kategori kaynağından liste alınır, ardından `selectedVehicleTypes` ile süzülür.

---

### API v2 — Furkan ekranları (auth-map-api)

- Karar: Eğitmen v2 API'sine geçiş; Furkan sorumluluğundaki Auth/License/Harita katmanları hizalanır.
- Son Güncelleme Tarihi: 13.07.2026
- **Ağ:** `NetworkModule` base URL `rencarv2`; OpenAPI JSON `/api/openapi.json`
- **Vehicle DTO/domain:** `pricePerMinute`, `pricePerHour`, `segment`, `RESERVED` status eklendi
- **Harita:** Kategori sekmeleri `?segment=` query ile API'den filtrelenir; `includeBusy=true` ile RENTED/RESERVED pinler gri gösterilir; pin fiyatı `pricePerHour` kullanır
- **Harita zoom kontrolleri:** Ana haritada sağ altta `+`/`-` FAB'ları; `MapCameraActions` ile MapLibre `zoomBy(±1)` animasyonu
- **Harita clustering:** Yakın pinler ekran piksel mesafesine göre tek "N" küme balonunda birleştirilir; yarıçap zoom arttıkça küçülür; zoom ≥ 13'te clustering kapalı; zoom ≥ 11.5'te 2'li kümeler ayrılır; küme tıklanınca kümedeki pinler zorunlu tekil moda alınır, bounds + min zoom 13 ile açılır; üst üste binen pinler dairesel spread ile ayrılır
- **Canlı araç konumu (Socket.IO):** `RideLocationClient` (`data/rental/`) aktif kiralama ekranında `my-vehicle` event'ini dinler; JWT handshake + `AuthorizedRequestExecutor.refreshCurrentSession()` ile connect_error sonrası tek seferlik oturum tazeleme; konum `VehiclePoint` olarak `ActiveRentalViewModel` state'ine akar
- **License:** `POST /license/upload` artık `selfie` multipart alanı zorunlu; ön/arka yüz ve selfie gerçek kamera ile çekilir
- **Auth DTO:** `RegisterDto.phone` zorunlu; `referralCode` opsiyonel (kayıt formunda desteklenir)
- **Kayıt akışı:** `POST /auth/register` token döner; kayıt sonrası OTP yerine doğrudan License ekranına yönlendirilir

---

### API v2 — Kiralama akışı zamanlaması (rental-flow-pages)

- Karar: Kiralama akışı OpenAPI v2 ile hizalandı; plan bazlı fotoğraf ve `start`/`finish` zamanlaması API sözleşmesine uygun.
- Son Güncelleme Tarihi: 15.07.2026
- **Dakikalık / Saatlik:** `POST /rentals` → `PREPARING` → kilidi aç → **başlangıç foto ekranı** (`POST /rentals/{id}/photos` ×4) → `POST /rentals/{id}/start` → `ACTIVE` (sayaç/ücret burada başlar)
- **Günlük:** `POST /rentals` + `endDate` → doğrudan `ACTIVE`; başlangıç fotoğrafı ve `start` çağrısı yok; kilidi aç yalnızca yerel kilit durumunu günceller
- **Bitir:** `POST /rentals/{id}/finish` → doğrudan özet ekranı; API bitiş fotoğrafı zorunlu tutmaz
- **Onay ekranı:** `create` sonrası `start` çağrılmaz; dk/sa için aktif ekran `PREPARING` modunda açılır
- **Başlangıç fotoğrafları:** `ui/rental/start_photos/` — FileProvider + `TakePicture` ile tam çözünürlüklü JPEG; `POST /rentals/{id}/photos` + `POST /start`
- **Teslim fotoğrafları:** `ui/rental/delivery_photos/` — ürün stub (API yok); gerçek kamera ile yerel önizleme; ana akıştan çıkarıldı (`finish` → özet)
- **Günlük plan:** Rezervasyon onayında `DatePicker` ile `endDate` seçimi; quote süresi seçilen tarihe göre hesaplanır
- **Araç detay:** Plan sekmeleri + `GET /quote`; `RENTED`/`RESERVED` için rezervasyon butonu devre dışı; seçilen plan onay ekranına query ile aktarılır
- **Rezervasyon iptali:** Onay ekranından geri dönülünce `DELETE /reservations/{id}`
- **Hata mesajları:** `ApiException` + `ApiErrorContext` ile 409/403/404 bağlama özel mesajlar

---

### API v2 — Kiralama domain ve plan mapping (rental-flow-pages Faz 2)

- Karar: `RentalPlan` eşlemesi repository katmanına taşındı; UI `data.rental.RentalPlan` kullanır.
- Son Güncelleme Tarihi: 15.07.2026
- `RentalPlanMapping.kt`: `toNetwork()`, `defaultQuoteMinutes()`, `durationLabel()`, `requiresScheduledEndDate()`
- `VehicleRepository.getQuote` artık domain `RentalPlan` alır; network DTO sızıntısı ViewModel'den kaldırıldı
- `Rental` domain modeli genişletildi: `plan`, `serviceFee`, `distanceKm`, `durationMinutes`, `paymentStatus`, araç özet alanları (`vehicleBrand` vb.)
- `RentalSummaryViewModel` / `RentalHistoryViewModel` sabit mesafe ve tahmini servis ücreti yerine domain alanlarını kullanır
- Kullanılmayan `RentalRepository.returnRental()` kaldırıldı; akış `finish` üzerinden devam eder
- Aktif kiralama haritası: `followLocationWithPan` ile ilk zoom sonrası yumuşak pan; konum beklenirken placeholder; rezervasyon modunda `mm:ss` geri sayım

---

### API v2 — Cüzdan, ödeme ve profil (feature/cagla-wallet-screen)

- Karar: Çağla ekranları OpenAPI v2 ile hizalandı; `WalletRepository` + `DefaultWalletRepository` proje standardına taşındı.
- Son Güncelleme Tarihi: 16.07.2026
- **Cüzdan:** `GET /wallet`, `POST /wallet/topup` — `AuthorizedRequestExecutor` ile Bearer token; işlem geçmişi API `transactions` dizisinden
- **Kartlar:** `GET/POST /cards`, `DELETE /cards/{id}`, `PATCH /cards/{id}/default` — DTO: `last4`, `expMonth`, `expYear`; fake fallback kaldırıldı
- **Ödeme:** `POST /rentals/{id}/pay` — özet ekranında cüzdan veya kayıtlı kart seçimi; `PAID` durumunda ödeme butonu gizlenir
- **İstatistik:** `GET /rentals/stats` — geçmiş ve profil ekranlarında aylık özet
- **Geçmiş:** Liste öğesine tıklanınca `rental/summary/{rentalId}` ekranına yönlendirme
- **Profil:** `GET /auth/me` + aylık istatistik kartı; profil düzenleme API karşılığı olmadığı için stub mesaj korunur
- **Profil yenileme:** `ON_RESUME` lifecycle ile `LoadProfile` tetiklenir (cüzdan/geçmiş ile aynı desen)
- **İndirim kodu:** Özet ekranında ödenmemiş kiralamalarda opsiyonel `OutlinedTextField`; `POST /rentals/{id}/pay` body `discountCode` alanına aktarılır; başarılı ödemede API `discountAmount` fatura satırında gösterilir
- **Geçmiş ödeme etiketi:** `RentalHistoryCard` üzerinde `paymentStatus` → "Ödendi" / "Ödenmedi" rozeti
- **Rental ödeme alanları:** `Rental.discountAmount` + `paymentMethod` domain'e eklendi; özet ve geçmiş ekranlarında ödenmiş kiralamalarda gösterilir
- **Profil davet kodu:** `GET /auth/me` → `referralCode` profil kartında + panoya kopyalama
- **Profil istatistik:** `GET /rentals/stats` → `totalMinutes` aylık özet kartında
- **Harita araç listesi:** `GET /vehicles?type=` tek tip seçiliyken sunucu tarafı filtre; `page` ile tüm sayfalar birleştirilir (limit 100, max 20 sayfa)

---

### Ehliyet onay — Talebi İptal Et (UNDER_REVIEW)

- Karar: OpenAPI v2'de ehliyet talebi iptal ucu yok (`GET /license/status`, `POST /license/upload` dışında müşteri ucu yok).
- Son Güncelleme Tarihi: 16.07.2026
- **UI:** `UNDER_REVIEW` durumunda «Onay Bekleniyor» butonu (pasif) + «Talebi İptal Et» metin butonu gösterilir.
- **Davranış:** «Talebi İptal Et» → `POST /auth/logout` + giriş ekranına dönüş (sunucu tarafında talep silinmez; kullanıcı oturumu sonlandırır).
- **Gelecek:** Backend `DELETE /license` veya benzeri uç eklendiğinde repository + ViewModel güncellenmeli.

---

### Fiyat gösterimi standardizasyonu (harita pini + araç detay)

- Karar: Tüm kullanıcıya dönük birim fiyatlar API alanlarından (`pricePerMinute`, `pricePerHour`, `pricePerDay`) ve tek formatter'dan (`VehiclePriceFormatter`) üretilir.
- Son Güncelleme Tarihi: 16.07.2026
- **Harita pini:** `Vehicle.pricePerHour` → tam sayı `₺{saatlik}` (ana harita ve detay sabit haritası aynı overload)
- **Detay plan kartları + Kiralama Ücreti paneli:** Seçili planın birim fiyatı; quote `estimatedTotal` detay ekranında gösterilmez (onay ekranında kalır)
- **Kaldırılan:** `mapPinLabel(pricePerDay)` türetimi — `pricePerHour ≠ pricePerDay/24` olduğunda pin tutarsızlığına yol açıyordu

---

### UX düzeltmeleri — snackbar, geri yığını, harita CTA, fatura (16.07.2026)

- **Snackbar:** `RenCarNavHost` `SnackbarHostState` ile Cüzdan, Profil, Geçmiş ve Özet ekranlarındaki mesajları gösterir (`onShowSnackbar = { _ -> }` kaldırıldı).
- **Aktif kiralama geri yığını:** Onay/detay → aktif geçişinde `popUpTo(Map)`; `ActiveRentalRoute` `BackHandler` ile haritaya döner (onay ekranına geri dönüp tekrar `POST /rentals` engellenir).
- **Harita CTA:** `MapViewModel` `getActive` / `listMine(PREPARING)` / `getActive` rezervasyon kontrolü; alt panelde «Devam» / «Onayla» banner'ı; `ON_RESUME` ile yenileme.
- **Özet fatura:** `usageFee = totalPrice - serviceFee - startFee`; `startFee > 0` ise «Açılış Ücreti» satırı ayrı gösterilir.

---

### Cüzdan ödemesi — bakiye düşümü (16.07.2026)

- **Sorun:** Varsayılan ödeme yöntemi state'te `CARD` idi; cüzdan seçili görünse bile kart ödemesi simüle edilebiliyordu. Cüzdan sekmesi ödeme sonrası eski bakiyeyi gösterebiliyordu.
- **Düzeltme:** Varsayılan `WALLET`; `POST /rentals/{id}/pay` body'de `cardId` yalnızca `CARD` iken gönderilir; cüzdan öncesi bakiye yeterlilik kontrolü; başarılı cüzdan ödemesinde `WalletRefreshNotifier` ile cüzdan ekranı yenilenir.
- **API:** `method: WALLET` → tutar cüzdandan düşülür (`walletBalance` yanıtta döner); yetersiz bakiye → 409.

---

### Eksik kapanış — status mapping, onay fiyatları, geçmiş ödeme, API temizliği (16.07.2026)

- **Araç status fallback:** Bilinmeyen API status → `MAINTENANCE` (rezervasyon kapalı; yanlışlıkla kiralanabilir `AVAILABLE` riski kaldırıldı).
- **Kiralama status fallback:** Bilinmeyen API status → `COMPLETED` (aktif kiralama yanlış pozitifi engellenir).
- **Araç detay:** `MAINTENANCE` için «Bu araç bakımda.» mesajı.
- **Onay ekranı fiyatları:** `VehiclePriceFormatter.planPriceLabel()` ve `formatMoney()`; contract varsayılanları boş string.
- **Geçmiş ödeme CTA:** Ödenmemiş satırlarda «Öde» butonu → `rental/summary/{rentalId}`.
- **API temizliği:** Kullanılmayan `RentalApi.returnRental` kaldırıldı (akış `finish` üzerinden).
- **Dokümantasyon:** v2 API URL güncellemesi; OTP artık gerçek API; Sprint 2 fiyat notu güncellendi.
- **Dokunulmadı (bilinçli):** Teslim fotoğrafı stub; profil menü stub'ları (Ayarlar, Yardım, Davet et, profil düzenleme).

---

### Günlük plan quote — 2 günlük fiyat şişmesi (16.07.2026)

- **Sorun:** Quote süresi `Instant.now()` → seçilen iade günü 23:59 arasındaki ham dakika farkıyla hesaplanıyordu. API kuralı: DAILY planda «başlanmış gün tam sayılır»; bugün + yarın iade = 2 takvim günü = 2× günlük ücret.
- **Düzeltme:** `dailyQuoteMinutes()` takvim günü sayısı × 1440 kullanır (`RentalPlanMapping.kt`). Varsayılan iade tarihi `LocalDate.now()` (1 gün). Süre etiketi: `1 gün · 16 Tem 2026` formatı.

---

### Harita gri pin — detay 404 önleme (16.07.2026)

- **Sorun:** `includeBusy=true` ile gösterilen gri pinlere tıklanınca `GET /vehicles/{id}` 404 dönüyordu (API: müsait olmayan araç yalnızca aktif kiracıya görünür); kullanıcıya «İstenen kayıt bulunamadı» çıkıyordu.
- **Davranış:** Gri pin (`isInUse`) tıklanınca detaya gitme; «Bu araç şu anda müsait değil.» snackbar. Kendi aktif kiralama/rezervasyonundaki araç (`activeSession.vehicleId`) istisna — detay açılır.

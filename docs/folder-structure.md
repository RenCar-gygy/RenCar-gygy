# RenCarApp - Klasör Yapısı

> Planlanan paket ve dosya yerleşimi. Henüz oluşturulmamış dizinler implementasyon
> başladığında eklenecektir. Mevcut proje yalnızca varsayılan Android şablonunu içerir.

---

## Kök Dizin

```
RenCarApp/
├── agents.md                          # Genel kurallar (AI + ekip)
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/turkcell/rencarapp/
│           ├── RenCarApplication.kt   # @HiltAndroidApp (eklenecek)
│           ├── MainActivity.kt        # @AndroidEntryPoint (güncellenecek)
│           ├── ui/
│           ├── data/
│           └── di/
├── docs/
│   ├── architecture/
│   │   ├── mvi-overview.md
│   │   ├── mvi-contracts.md
│   │   └── mvi-viewmodel-rules.md
│   ├── decisions.md
│   ├── screens.md
│   └── folder-structure.md            # bu dosya
└── gradle/
    └── libs.versions.toml
```

---

## UI Katmanı (`ui/`)

```
ui/
├── theme/
│   ├── Color.kt
│   ├── Theme.kt
│   └── Type.kt
├── navigation/
│   ├── RenCarDestination.kt           # route sabitleri
│   ├── RenCarNavHost.kt               # kök NavHost + nested graph'lar
│   └── RenCarBottomBar.kt             # alt çubuk sekmeleri
├── splash/
│   ├── SplashContract.kt
│   ├── SplashViewModel.kt
│   └── SplashScreen.kt
├── onboarding/
│   ├── OnboardingContract.kt
│   ├── OnboardingViewModel.kt
│   └── OnboardingScreen.kt
├── auth/
│   ├── login/
│   │   ├── LoginContract.kt
│   │   ├── LoginViewModel.kt
│   │   └── LoginScreen.kt
│   ├── register/
│   │   ├── RegisterContract.kt
│   │   ├── RegisterViewModel.kt
│   │   └── RegisterScreen.kt
│   └── otp/
│       ├── OtpContract.kt
│       ├── OtpViewModel.kt
│       └── OtpScreen.kt
├── license/
│   ├── LicenseContract.kt
│   ├── LicenseViewModel.kt
│   └── LicenseScreen.kt
├── map/
│   ├── MapContract.kt
│   ├── MapViewModel.kt
│   └── MapScreen.kt
├── vehicle/
│   └── detail/
│       ├── VehicleDetailContract.kt
│       ├── VehicleDetailViewModel.kt
│       └── VehicleDetailScreen.kt
├── rental/
│   ├── confirmation/
│   │   ├── RentalConfirmationContract.kt
│   │   ├── RentalConfirmationViewModel.kt
│   │   └── RentalConfirmationScreen.kt
│   ├── summary/
│   │   ├── RentalSummaryContract.kt
│   │   ├── RentalSummaryViewModel.kt
│   │   └── RentalSummaryScreen.kt
│   ├── delivery_photos/
│   │   ├── DeliveryPhotosContract.kt
│   │   ├── DeliveryPhotosViewModel.kt
│   │   └── DeliveryPhotosScreen.kt
│   ├── active/
│   │   ├── ActiveRentalContract.kt
│   │   ├── ActiveRentalViewModel.kt
│   │   └── ActiveRentalScreen.kt
│   └── history/
│       ├── RentalHistoryContract.kt
│       ├── RentalHistoryViewModel.kt
│       └── RentalHistoryScreen.kt
├── payment/
│   └── wallet/
│       ├── WalletContract.kt
│       ├── WalletViewModel.kt
│       └── WalletScreen.kt
└── profile/
    ├── ProfileContract.kt
    ├── ProfileViewModel.kt
    └── ProfileScreen.kt
```

Her ekran paketi aynı üç dosyayı içerir: **Contract**, **ViewModel**, **Screen** (Route + stateless Screen).

---

## Veri Katmanı (`data/`)

```
data/
├── network/
│   ├── NetworkModule.kt               # Json, OkHttp, Retrofit @Provides
│   ├── AuthApi.kt
│   ├── LicenseApi.kt
│   ├── VehicleApi.kt
│   └── RentalApi.kt
├── auth/
│   ├── AuthRepository.kt              # interface
│   ├── FakeAuthRepository.kt          # stub (geçici)
│   ├── DefaultAuthRepository.kt       # gerçek API (sonra)
│   └── dto/                           # AuthResponseDto, LoginDto, ...
├── license/
│   ├── LicenseRepository.kt
│   ├── FakeLicenseRepository.kt
│   └── dto/
├── vehicle/
│   ├── VehicleRepository.kt
│   ├── FakeVehicleRepository.kt
│   └── dto/
├── rental/
│   ├── RentalRepository.kt
│   ├── FakeRentalRepository.kt
│   └── dto/
└── session/
    ├── TokenStore.kt                  # access/refresh token saklama (planlanan)
    └── SessionModule.kt
```

---

## DI Katmanı (`di/`)

```
di/
├── AuthModule.kt
├── LicenseModule.kt
├── VehicleModule.kt
└── RentalModule.kt
```

Her modül `@Binds` ile interface → implementasyon eşlemesi yapar. Stub aşamasında hedef `Fake*Repository`, API entegrasyonunda `Default*Repository` olur.

---

## Test (ileride)

```
app/src/test/java/com/turkcell/rencarapp/
└── ui/auth/login/
    └── LoginViewModelTest.kt

app/src/androidTest/java/com/turkcell/rencarapp/
└── (instrumentation testler)
```

---

## Ortak İşler (ekip birlikte — Sprint 0)

Implementasyon başlamadan önce tek kişi veya pair ile yapılması önerilen ortak işler:

| İş | Dosyalar | Önerilen sorumlu |
|----|----------|------------------|
| Hilt + KSP kurulumu | `build.gradle.kts`, `gradle.properties`, `RenCarApplication.kt` | Furkan |
| Navigasyon iskeleti | `ui/navigation/*` | Furkan (harita sahibi) |
| NetworkModule iskeleti | `data/network/NetworkModule.kt` | Furkan veya rotasyon |
| Theme / design token | `ui/theme/*` | Çağla (profil/ödeme UI yoğun) |
| Repository interface'leri | `data/*/XRepository.kt` | Üçü birlikte tanımlar; stub'lar paralel |

Bu tablo zorunlu değildir; ekip içinde yeniden dağıtılabilir.

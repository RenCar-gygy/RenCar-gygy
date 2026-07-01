# RenCarApp - ViewModel ve UI Bağlama Kuralları

> ViewModel, UI bağlama (Route/Screen) ve DI için **tek doğruluk kaynağıdır**.
> Sözleşme için bkz. [mvi-contracts.md](mvi-contracts.md), genel akış için [mvi-overview.md](mvi-overview.md).
>
> Referans: `ui/auth/login/LoginViewModel.kt`, `LoginScreen.kt`, `di/AuthModule.kt` (ilk tamamlandığında).

---

## 1. Temel Kural

> ViewModel'in UI ile tek temas noktası `fun onIntent(intent: <Screen>Intent)`'tir.
> State `StateFlow`, Effect `Channel` ile dışarı açılır; her ikisinin de mutable hali
> **private** tutulur. ViewModel içinde **hiçbir Android/Compose/Context bağımlılığı bulunamaz.**

---

## 2. ViewModel İskeleti

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LoginEffect>(Channel.BUFFERED)
    val effect: Flow<LoginEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.EmailChanged -> updateForm { it.copy(email = intent.value) }
            is LoginIntent.PasswordChanged -> updateForm { it.copy(password = intent.value) }
            is LoginIntent.TogglePasswordVisibility ->
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            is LoginIntent.Submit -> submit()
        }
    }
}
```

---

## 3. Zorunlu Kurallar

1. `@HiltViewModel` + `@Inject constructor`. Bağımlılıklar `private val` olarak alınır.
2. `MutableStateFlow` private; dışarı `asStateFlow()` ile `StateFlow` olarak açılır.
3. Tek seferlik olaylar `Channel(Channel.BUFFERED)` private; dışarı `receiveAsFlow()` ile açılır.
4. Tek giriş noktası `onIntent(...)`; `when` **tüm** Intent dallarını kapsar (exhaustive).
5. Asenkron iş `viewModelScope.launch { ... }` içinde, repository üzerinden yapılır.
6. **Yasak:** ViewModel içinde `Context`, `View`, `@Composable`, `Activity`, navigasyon API'si.
7. State güncellemeleri yalnızca `_uiState.update { it.copy(...) }` ile yapılır (atomik).

---

## 4. Türetilen Alanlar

Buton aktifliği gibi türetilen alanlar, ilgili alan her değiştiğinde yeniden hesaplanır.
Türetme tek bir yardımcıda toplanır:

```kotlin
private fun updateForm(transform: (LoginUiState) -> LoginUiState) {
    _uiState.update { current ->
        val updated = transform(current)
        updated.copy(isLoginEnabled = updated.isFormValid())
    }
}

private fun LoginUiState.isFormValid(): Boolean =
    email.isNotBlank() && password.isNotBlank()
```

---

## 5. Asenkron Akış ve Effect

- İşlem başlarken/biterken `isLoading` güncellenir; çift gönderim için erken `return` ile korunur.
- Sonuç `Result<…>` üzerinden ele alınır; başarı/hata Effect'e dönüştürülür.

```kotlin
private fun submit() {
    val state = _uiState.value
    if (!state.isLoginEnabled || state.isLoading) return
    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        val result = authRepository.login(state.email, state.password)
        _uiState.update { it.copy(isLoading = false) }
        result
            .onSuccess { user ->
                when (user.role) {
                    UserRole.PENDING -> _effect.send(LoginEffect.NavigateToLicense)
                    UserRole.CUSTOMER -> _effect.send(LoginEffect.NavigateToMap)
                    else -> _effect.send(LoginEffect.ShowError("Bu hesap türü desteklenmiyor."))
                }
            }
            .onFailure { _effect.send(LoginEffect.ShowError(it.message ?: "Giriş başarısız.")) }
    }
}
```

---

## 6. UI Bağlama: Route + Screen

İki composable zorunludur:

- **`<Screen>Route`** (durumlu): ViewModel'i `hiltViewModel()` ile alır, state'i
  `collectAsStateWithLifecycle()` ile toplar, Effect'leri `LaunchedEffect` içinde tüketir.
- **`<Screen>Screen`** (durumsuz): `state` ve `onIntent` parametrelerini alır; preview edilebilir olmalıdır.

Navigasyon Effect'ten `Route` içinde tüketilir; ViewModel navigasyon API'si bilmez.

**Yasak:** `<Screen>Screen` içinde ViewModel referansı, repository çağrısı veya iş mantığı.

---

## 7. DI Modülü (Repository Bağlama)

Interface → implementasyon bağlaması `di/<X>Module.kt` içinde `@Binds` ile yapılır.
Gerçek API geldiğinde yalnızca `@Binds` hedefi değiştirilir.

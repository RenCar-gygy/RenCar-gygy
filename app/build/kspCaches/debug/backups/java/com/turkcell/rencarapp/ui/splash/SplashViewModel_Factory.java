package com.turkcell.rencarapp.ui.splash;

import com.turkcell.rencarapp.data.auth.AuthRepository;
import com.turkcell.rencarapp.data.session.SessionStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class SplashViewModel_Factory implements Factory<SplashViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<SessionStore> sessionStoreProvider;

  private SplashViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<SessionStore> sessionStoreProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.sessionStoreProvider = sessionStoreProvider;
  }

  @Override
  public SplashViewModel get() {
    return newInstance(authRepositoryProvider.get(), sessionStoreProvider.get());
  }

  public static SplashViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<SessionStore> sessionStoreProvider) {
    return new SplashViewModel_Factory(authRepositoryProvider, sessionStoreProvider);
  }

  public static SplashViewModel newInstance(AuthRepository authRepository,
      SessionStore sessionStore) {
    return new SplashViewModel(authRepository, sessionStore);
  }
}

package com.turkcell.rencarapp.ui.license;

import com.turkcell.rencarapp.data.auth.AuthRepository;
import com.turkcell.rencarapp.data.license.LicenseRepository;
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
public final class LicenseViewModel_Factory implements Factory<LicenseViewModel> {
  private final Provider<LicenseRepository> licenseRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<SessionStore> sessionStoreProvider;

  private LicenseViewModel_Factory(Provider<LicenseRepository> licenseRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<SessionStore> sessionStoreProvider) {
    this.licenseRepositoryProvider = licenseRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.sessionStoreProvider = sessionStoreProvider;
  }

  @Override
  public LicenseViewModel get() {
    return newInstance(licenseRepositoryProvider.get(), authRepositoryProvider.get(), sessionStoreProvider.get());
  }

  public static LicenseViewModel_Factory create(
      Provider<LicenseRepository> licenseRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<SessionStore> sessionStoreProvider) {
    return new LicenseViewModel_Factory(licenseRepositoryProvider, authRepositoryProvider, sessionStoreProvider);
  }

  public static LicenseViewModel newInstance(LicenseRepository licenseRepository,
      AuthRepository authRepository, SessionStore sessionStore) {
    return new LicenseViewModel(licenseRepository, authRepository, sessionStore);
  }
}

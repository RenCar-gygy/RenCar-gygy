package com.turkcell.rencarapp.data.license;

import com.turkcell.rencarapp.data.auth.AuthorizedRequestExecutor;
import com.turkcell.rencarapp.data.network.api.LicenseApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class DefaultLicenseRepository_Factory implements Factory<DefaultLicenseRepository> {
  private final Provider<LicenseApi> licenseApiProvider;

  private final Provider<AuthorizedRequestExecutor> authorizedRequestExecutorProvider;

  private DefaultLicenseRepository_Factory(Provider<LicenseApi> licenseApiProvider,
      Provider<AuthorizedRequestExecutor> authorizedRequestExecutorProvider) {
    this.licenseApiProvider = licenseApiProvider;
    this.authorizedRequestExecutorProvider = authorizedRequestExecutorProvider;
  }

  @Override
  public DefaultLicenseRepository get() {
    return newInstance(licenseApiProvider.get(), authorizedRequestExecutorProvider.get());
  }

  public static DefaultLicenseRepository_Factory create(Provider<LicenseApi> licenseApiProvider,
      Provider<AuthorizedRequestExecutor> authorizedRequestExecutorProvider) {
    return new DefaultLicenseRepository_Factory(licenseApiProvider, authorizedRequestExecutorProvider);
  }

  public static DefaultLicenseRepository newInstance(LicenseApi licenseApi,
      AuthorizedRequestExecutor authorizedRequestExecutor) {
    return new DefaultLicenseRepository(licenseApi, authorizedRequestExecutor);
  }
}

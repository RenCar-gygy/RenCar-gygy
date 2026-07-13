package com.turkcell.rencarapp.data.rental;

import com.turkcell.rencarapp.data.auth.AuthorizedRequestExecutor;
import com.turkcell.rencarapp.data.network.api.RentalApi;
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
public final class DefaultRentalRepository_Factory implements Factory<DefaultRentalRepository> {
  private final Provider<RentalApi> rentalApiProvider;

  private final Provider<AuthorizedRequestExecutor> authorizedRequestExecutorProvider;

  private DefaultRentalRepository_Factory(Provider<RentalApi> rentalApiProvider,
      Provider<AuthorizedRequestExecutor> authorizedRequestExecutorProvider) {
    this.rentalApiProvider = rentalApiProvider;
    this.authorizedRequestExecutorProvider = authorizedRequestExecutorProvider;
  }

  @Override
  public DefaultRentalRepository get() {
    return newInstance(rentalApiProvider.get(), authorizedRequestExecutorProvider.get());
  }

  public static DefaultRentalRepository_Factory create(Provider<RentalApi> rentalApiProvider,
      Provider<AuthorizedRequestExecutor> authorizedRequestExecutorProvider) {
    return new DefaultRentalRepository_Factory(rentalApiProvider, authorizedRequestExecutorProvider);
  }

  public static DefaultRentalRepository newInstance(RentalApi rentalApi,
      AuthorizedRequestExecutor authorizedRequestExecutor) {
    return new DefaultRentalRepository(rentalApi, authorizedRequestExecutor);
  }
}

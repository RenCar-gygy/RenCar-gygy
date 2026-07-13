package com.turkcell.rencarapp.data.rental;

import com.turkcell.rencarapp.data.network.api.RentalApi;
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
public final class RentalRepositoryImpl_Factory implements Factory<RentalRepositoryImpl> {
  private final Provider<RentalApi> rentalApiProvider;

  private RentalRepositoryImpl_Factory(Provider<RentalApi> rentalApiProvider) {
    this.rentalApiProvider = rentalApiProvider;
  }

  @Override
  public RentalRepositoryImpl get() {
    return newInstance(rentalApiProvider.get());
  }

  public static RentalRepositoryImpl_Factory create(Provider<RentalApi> rentalApiProvider) {
    return new RentalRepositoryImpl_Factory(rentalApiProvider);
  }

  public static RentalRepositoryImpl newInstance(RentalApi rentalApi) {
    return new RentalRepositoryImpl(rentalApi);
  }
}

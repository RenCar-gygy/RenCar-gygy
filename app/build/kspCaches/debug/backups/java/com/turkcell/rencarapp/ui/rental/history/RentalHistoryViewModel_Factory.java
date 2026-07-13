package com.turkcell.rencarapp.ui.rental.history;

import com.turkcell.rencarapp.data.rental.RentalRepository;
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
public final class RentalHistoryViewModel_Factory implements Factory<RentalHistoryViewModel> {
  private final Provider<RentalRepository> rentalRepositoryProvider;

  private RentalHistoryViewModel_Factory(Provider<RentalRepository> rentalRepositoryProvider) {
    this.rentalRepositoryProvider = rentalRepositoryProvider;
  }

  @Override
  public RentalHistoryViewModel get() {
    return newInstance(rentalRepositoryProvider.get());
  }

  public static RentalHistoryViewModel_Factory create(
      Provider<RentalRepository> rentalRepositoryProvider) {
    return new RentalHistoryViewModel_Factory(rentalRepositoryProvider);
  }

  public static RentalHistoryViewModel newInstance(RentalRepository rentalRepository) {
    return new RentalHistoryViewModel(rentalRepository);
  }
}

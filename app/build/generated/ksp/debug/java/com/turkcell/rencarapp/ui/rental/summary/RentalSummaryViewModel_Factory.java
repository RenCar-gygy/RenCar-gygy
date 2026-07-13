package com.turkcell.rencarapp.ui.rental.summary;

import androidx.lifecycle.SavedStateHandle;
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
public final class RentalSummaryViewModel_Factory implements Factory<RentalSummaryViewModel> {
  private final Provider<RentalRepository> rentalRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private RentalSummaryViewModel_Factory(Provider<RentalRepository> rentalRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.rentalRepositoryProvider = rentalRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public RentalSummaryViewModel get() {
    return newInstance(rentalRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static RentalSummaryViewModel_Factory create(
      Provider<RentalRepository> rentalRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new RentalSummaryViewModel_Factory(rentalRepositoryProvider, savedStateHandleProvider);
  }

  public static RentalSummaryViewModel newInstance(RentalRepository rentalRepository,
      SavedStateHandle savedStateHandle) {
    return new RentalSummaryViewModel(rentalRepository, savedStateHandle);
  }
}

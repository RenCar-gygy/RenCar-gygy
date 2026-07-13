package com.turkcell.rencarapp.ui.rental.confirmation;

import androidx.lifecycle.SavedStateHandle;
import com.turkcell.rencarapp.data.rental.RentalRepository;
import com.turkcell.rencarapp.data.vehicle.VehicleRepository;
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
public final class RentalConfirmationViewModel_Factory implements Factory<RentalConfirmationViewModel> {
  private final Provider<VehicleRepository> vehicleRepositoryProvider;

  private final Provider<RentalRepository> rentalRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private RentalConfirmationViewModel_Factory(Provider<VehicleRepository> vehicleRepositoryProvider,
      Provider<RentalRepository> rentalRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.vehicleRepositoryProvider = vehicleRepositoryProvider;
    this.rentalRepositoryProvider = rentalRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public RentalConfirmationViewModel get() {
    return newInstance(vehicleRepositoryProvider.get(), rentalRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static RentalConfirmationViewModel_Factory create(
      Provider<VehicleRepository> vehicleRepositoryProvider,
      Provider<RentalRepository> rentalRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new RentalConfirmationViewModel_Factory(vehicleRepositoryProvider, rentalRepositoryProvider, savedStateHandleProvider);
  }

  public static RentalConfirmationViewModel newInstance(VehicleRepository vehicleRepository,
      RentalRepository rentalRepository, SavedStateHandle savedStateHandle) {
    return new RentalConfirmationViewModel(vehicleRepository, rentalRepository, savedStateHandle);
  }
}

package com.turkcell.rencarapp.ui.rental.active;

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
public final class ActiveRentalViewModel_Factory implements Factory<ActiveRentalViewModel> {
  private final Provider<RentalRepository> rentalRepositoryProvider;

  private final Provider<VehicleRepository> vehicleRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private ActiveRentalViewModel_Factory(Provider<RentalRepository> rentalRepositoryProvider,
      Provider<VehicleRepository> vehicleRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.rentalRepositoryProvider = rentalRepositoryProvider;
    this.vehicleRepositoryProvider = vehicleRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public ActiveRentalViewModel get() {
    return newInstance(rentalRepositoryProvider.get(), vehicleRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static ActiveRentalViewModel_Factory create(
      Provider<RentalRepository> rentalRepositoryProvider,
      Provider<VehicleRepository> vehicleRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new ActiveRentalViewModel_Factory(rentalRepositoryProvider, vehicleRepositoryProvider, savedStateHandleProvider);
  }

  public static ActiveRentalViewModel newInstance(RentalRepository rentalRepository,
      VehicleRepository vehicleRepository, SavedStateHandle savedStateHandle) {
    return new ActiveRentalViewModel(rentalRepository, vehicleRepository, savedStateHandle);
  }
}

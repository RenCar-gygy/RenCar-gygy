package com.turkcell.rencarapp.ui.rental.delivery_photos;

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
public final class DeliveryPhotosViewModel_Factory implements Factory<DeliveryPhotosViewModel> {
  private final Provider<RentalRepository> rentalRepositoryProvider;

  private final Provider<VehicleRepository> vehicleRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private DeliveryPhotosViewModel_Factory(Provider<RentalRepository> rentalRepositoryProvider,
      Provider<VehicleRepository> vehicleRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.rentalRepositoryProvider = rentalRepositoryProvider;
    this.vehicleRepositoryProvider = vehicleRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public DeliveryPhotosViewModel get() {
    return newInstance(rentalRepositoryProvider.get(), vehicleRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static DeliveryPhotosViewModel_Factory create(
      Provider<RentalRepository> rentalRepositoryProvider,
      Provider<VehicleRepository> vehicleRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new DeliveryPhotosViewModel_Factory(rentalRepositoryProvider, vehicleRepositoryProvider, savedStateHandleProvider);
  }

  public static DeliveryPhotosViewModel newInstance(RentalRepository rentalRepository,
      VehicleRepository vehicleRepository, SavedStateHandle savedStateHandle) {
    return new DeliveryPhotosViewModel(rentalRepository, vehicleRepository, savedStateHandle);
  }
}
